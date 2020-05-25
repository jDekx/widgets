package test.widget.server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import test.widget.server.ServerConfigurationProperties;
import test.widget.server.controller.params.FilteringParams;
import test.widget.server.controller.params.PaginationParams;
import test.widget.server.controller.params.WidgetParams;
import test.widget.server.domain.Area;
import test.widget.server.domain.Widget;
import test.widget.server.exception.WidgetNotFoundException;
import test.widget.server.repository.WidgetRepository;
import test.widget.server.service.WidgetFilteringService;
import test.widget.server.service.WidgetService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * REST-controller for operations with widgets.
 *
 * @author Mikhail Kondratev
 * @see Widget
 */
@Slf4j
@RestController
public class WidgetController {

    /**
     * Widget repository.
     */
    private final WidgetRepository widgetRepository;

    /**
     * General server configuration properties.
     */
    private final ServerConfigurationProperties serverConfigurationProperties;

    /**
     * Lock for concurrent access to widgets.
     */
    private final ReadWriteLock readWriteLock;

    /**
     * Widget service.
     */
    private final WidgetService widgetService;

    /**
     * Widget filtering service.
     */
    private final WidgetFilteringService widgetFilterService;

    /**
     * Constructor.
     *
     * @param widgetRepository              widget repository.
     * @param serverConfigurationProperties server configuration properties.
     * @param widgetService                 widget service.
     * @param widgetFilterService           widget filtering service.
     */
    public WidgetController(final WidgetRepository widgetRepository,
                            final ServerConfigurationProperties serverConfigurationProperties,
                            final WidgetService widgetService,
                            final WidgetFilteringService widgetFilterService) {
        this.widgetRepository = widgetRepository;
        this.serverConfigurationProperties = serverConfigurationProperties;
        this.widgetService = widgetService;
        this.widgetFilterService = widgetFilterService;

        this.readWriteLock = new ReentrantReadWriteLock();
    }

    /**
     * Method for getting all widgets, sorted by z index, ascending.
     *
     * @param paginationParams pagination params.
     * @return sorted list of all widgets.
     * @throws InterruptedException                    if thread was interrupted.
     * @throws MissingServletRequestParameterException if object {@link FilteringParams} is invalid.
     */
    @GetMapping(WidgetControllerApiPath.WIDGETS_PATH)
    @Transactional
    public HttpEntity<Collection<Widget>> getAll(final FilteringParams filteringParams,
                                                 final PaginationParams paginationParams) throws InterruptedException,
            MissingServletRequestParameterException {

        log.debug("Requested all widgets with filter: {}, paging: {}", filteringParams, paginationParams);

        if (paginationParams.getPageSize() == null) {
            paginationParams.setPageSize(serverConfigurationProperties.getPageDefaultSize());
        }

        paginationParams.setPageSize(Math.min(Math.max(paginationParams.getPageSize(), 0), serverConfigurationProperties.getPageMaxSize()));

        final Lock readLock = readWriteLock.readLock();

        boolean isLocked = false;
        try {
            isLocked = readLock.tryLock() || readLock.tryLock(serverConfigurationProperties.getLockTimeout(), TimeUnit.SECONDS);

            Collection<Widget> widgets = widgetRepository.findAll();

            if (filteringParamsAreValid(filteringParams)) {
                final Area area = new Area();
                area.setX(filteringParams.getX());
                area.setY(filteringParams.getY());
                area.setWidth(filteringParams.getWidth());
                area.setHeight(filteringParams.getHeight());

                widgets = widgetFilterService.filterWidgetsInsideArea(widgets, area);
            }

            widgets = widgets
                    .stream()
                    .sorted(Comparator.comparingInt(Widget::getZ))
                    .skip(paginationParams.getOffset() == null ? 0 : Math.max(paginationParams.getOffset(), 0))
                    .limit(paginationParams.getPageSize())
                    .collect(Collectors.toList());

            return new ResponseEntity<>(widgets, HttpStatus.OK);
        } finally {
            if (isLocked) {
                readLock.unlock();
            }
        }
    }

    /**
     * Validates {@link FilteringParams}.
     * Params should all be set or all be null.
     *
     * @param filteringParams params to be validated.
     * @return <code>true</code> if all params are present, <code>false</code> - if all params are null.
     * @throws MissingServletRequestParameterException if at least one of the params is null and at least one of the params is set.
     */
    private boolean filteringParamsAreValid(final FilteringParams filteringParams) throws MissingServletRequestParameterException {
        if (filteringParams.getX() == null
                && filteringParams.getY() == null
                && filteringParams.getWidth() == null
                && filteringParams.getHeight() == null) {
            return false;
        }

        if (filteringParams.getX() == null) {
            throw new MissingServletRequestParameterException("x", "Integer");
        }

        if (filteringParams.getY() == null) {
            throw new MissingServletRequestParameterException("y", "Integer");
        }

        if (filteringParams.getWidth() == null) {
            throw new MissingServletRequestParameterException("width", "Integer");
        }

        if (filteringParams.getHeight() == null) {
            throw new MissingServletRequestParameterException("height", "Integer");
        }

        return filteringParams.getX() != null
                && filteringParams.getY() != null
                && filteringParams.getWidth() != null
                && filteringParams.getHeight() != null;

    }

    /**
     * Creates new widget with requested parameters.
     *
     * @param widgetParams widget parameters.
     * @return newly created widget.
     * @throws InterruptedException if thread was interrupted.
     */
    @PostMapping(WidgetControllerApiPath.WIDGETS_PATH)
    @Transactional
    public HttpEntity<Widget> createNew(@RequestBody final WidgetParams widgetParams) throws InterruptedException,
            MissingServletRequestParameterException {

        log.debug("Requested to create widget with params: {}", widgetParams);

        validateWidgetParams(widgetParams);

        final Widget newWidget = widgetService.createNew();

        //noinspection ConstantConditions
        newWidget.setX(widgetParams.getX());
        //noinspection ConstantConditions
        newWidget.setY(widgetParams.getY());
        //noinspection ConstantConditions
        newWidget.setWidth(widgetParams.getWidth());
        //noinspection ConstantConditions
        newWidget.setHeight(widgetParams.getHeight());
        newWidget.setLastModified(LocalDateTime.now());

        final Lock writeLock = readWriteLock.writeLock();

        boolean isLocked = false;
        try {
            isLocked = writeLock.tryLock() || writeLock.tryLock(serverConfigurationProperties.getLockTimeout(), TimeUnit.SECONDS);

            widgetService.updateWidgetZIndex(newWidget, widgetParams.getZ());
            widgetRepository.save(newWidget);


            return new ResponseEntity<>(newWidget, HttpStatus.CREATED);
        } finally {
            if (isLocked) {
                writeLock.unlock();
            }
        }
    }

    /**
     * Validates required params in {@link WidgetParams} object.
     *
     * @param widgetParams params object to be validated.
     * @throws MissingServletRequestParameterException if required field is missing.
     */
    protected void validateWidgetParams(final WidgetParams widgetParams) throws MissingServletRequestParameterException {
        if (widgetParams.getX() == null) {
            throw new MissingServletRequestParameterException("x", "Integer");
        }

        if (widgetParams.getY() == null) {
            throw new MissingServletRequestParameterException("y", "Integer");
        }

        if (widgetParams.getWidth() == null) {
            throw new MissingServletRequestParameterException("width", "Integer");
        }

        if (widgetParams.getHeight() == null) {
            throw new MissingServletRequestParameterException("height", "Integer");
        }
    }

    /**
     * Getting widget by it's identifier.
     *
     * @param id widget identifier.
     * @return found widget with specified identifier.
     * @throws InterruptedException    if thread was interrupted.
     * @throws WidgetNotFoundException if widget with specified id was not found.
     */
    @GetMapping(WidgetControllerApiPath.WIDGETS_PATH + "/{id}")
    @Transactional
    public HttpEntity<Widget> getById(@PathVariable final String id) throws InterruptedException {

        log.debug("Requested widget by id: {}", id);

        final Lock readLock = readWriteLock.readLock();

        boolean isLocked = false;
        try {
            isLocked = readLock.tryLock() || readLock.tryLock(serverConfigurationProperties.getLockTimeout(), TimeUnit.SECONDS);
            final Widget foundWidget = widgetRepository.findById(id)
                    .orElseThrow(() -> new WidgetNotFoundException(id));

            return new ResponseEntity<>(foundWidget, HttpStatus.OK);
        } finally {
            if (isLocked) {
                readLock.unlock();
            }
        }
    }

    /**
     * Updates existing widget with specified params or creates a new widget with this values,
     * if a widget with specified id was not found.
     *
     * @param widgetParams widget params new values.
     * @param id           widget identifier.
     * @return {@link HttpEntity} that holds updated or newly created widget.
     */
    @PutMapping(WidgetControllerApiPath.WIDGETS_PATH + "/{id}")
    @Transactional
    public HttpEntity<Widget> update(@RequestBody final WidgetParams widgetParams, @PathVariable final String id)
            throws InterruptedException, MissingServletRequestParameterException {

        log.debug("Requested to update widget with id: {} with values: {}", id, widgetParams);

        final Lock writeLock = readWriteLock.writeLock();

        boolean isLocked = false;
        try {
            isLocked = writeLock.tryLock() || writeLock.tryLock(serverConfigurationProperties.getLockTimeout(), TimeUnit.SECONDS);

            final Widget widget = widgetRepository.findById(id)
                    .orElseGet(widgetService::createNew);

            if (widget.isNew()) {
                validateWidgetParams(widgetParams);
            }

            widget.setLastModified(LocalDateTime.now());

            if (widgetParams.getX() != null) {
                widget.setX(widgetParams.getX());
            }

            if (widgetParams.getY() != null) {
                widget.setY(widgetParams.getY());
            }

            if (widgetParams.getWidth() != null) {
                widget.setWidth(widgetParams.getWidth());
            }

            if (widgetParams.getHeight() != null) {
                widget.setHeight(widgetParams.getHeight());
            }

            widgetService.updateWidgetZIndex(widget, widgetParams.getZ());
            widgetRepository.save(widget);

            return new ResponseEntity<>(widget, widget.isNew() ? HttpStatus.CREATED : HttpStatus.OK);
        } finally {
            if (isLocked) {
                writeLock.unlock();
            }
        }
    }

    /**
     * Deletes widget with specified id.
     *
     * @param id id of a widget to be removed.
     */
    @DeleteMapping(WidgetControllerApiPath.WIDGETS_PATH + "/{id}")
    @Transactional
    public void delete(@PathVariable final String id) throws InterruptedException {

        log.debug("Requested to delete widget with id: {}", id);

        final Lock lock = readWriteLock.writeLock();

        boolean isLocked = false;
        try {
            isLocked = lock.tryLock() || lock.tryLock(serverConfigurationProperties.getLockTimeout(), TimeUnit.SECONDS);
            widgetRepository.deleteById(id);

        } finally {
            if (isLocked) {
                lock.unlock();
            }
        }
    }
}

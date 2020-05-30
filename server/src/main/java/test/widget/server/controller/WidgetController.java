package test.widget.server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import test.widget.server.ServerConfigurationProperties;
import test.widget.server.controller.params.FilteringParams;
import test.widget.server.controller.params.PaginationParams;
import test.widget.server.controller.params.WidgetParams;
import test.widget.server.domain.Area;
import test.widget.server.domain.Widget;
import test.widget.server.exception.WidgetNotFoundException;
import test.widget.server.service.WidgetService;

import java.util.Collection;
import java.util.Comparator;
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
     * General server configuration properties.
     */
    private final ServerConfigurationProperties serverConfigurationProperties;

    /**
     * Widget service.
     */
    private final WidgetService widgetService;

    /**
     * Constructor.
     *
     * @param serverConfigurationProperties server configuration properties.
     * @param widgetService                 widget service.
     */
    public WidgetController(final ServerConfigurationProperties serverConfigurationProperties,
                            final WidgetService widgetService) {
        this.serverConfigurationProperties = serverConfigurationProperties;
        this.widgetService = widgetService;
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
    public HttpEntity<Collection<Widget>> getAll(final FilteringParams filteringParams,
                                                 final PaginationParams paginationParams) throws InterruptedException,
            MissingServletRequestParameterException {

        log.debug("Requested all widgets with filter: {}, paging: {}", filteringParams, paginationParams);

        if (paginationParams.getPageSize() == null) {
            paginationParams.setPageSize(serverConfigurationProperties.getPageDefaultSize());
        }

        paginationParams.setPageSize(Math.min(Math.max(paginationParams.getPageSize(), 0), serverConfigurationProperties.getPageMaxSize()));

        Collection<Widget> widgets;

        if (filteringParamsAreValid(filteringParams)) {
            final Area area = new Area();
            area.setX(filteringParams.getX());
            area.setY(filteringParams.getY());
            area.setWidth(filteringParams.getWidth());
            area.setHeight(filteringParams.getHeight());

            widgets = widgetService.getWidgetsInsideArea(area);
        } else {
            widgets = widgetService.getWidgetsInsideArea(Area.EMPTY_AREA);
        }

        widgets = widgets
                .stream()
                .sorted(Comparator.comparingInt(Widget::getZ))
                .skip(paginationParams.getOffset() == null ? 0 : Math.max(paginationParams.getOffset(), 0))
                .limit(paginationParams.getPageSize())
                .collect(Collectors.toList());

        return new ResponseEntity<>(widgets, HttpStatus.OK);
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
    public HttpEntity<Widget> createNew(@RequestBody final WidgetParams widgetParams) throws InterruptedException,
            MissingServletRequestParameterException {

        log.debug("Requested to create widget with params: {}", widgetParams);

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

        final Widget newWidget = widgetService.createNew(widgetParams.getX(), widgetParams.getY(), widgetParams.getWidth(), widgetParams.getHeight(), widgetParams.getZ());

        return new ResponseEntity<>(newWidget, HttpStatus.CREATED);

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
    public HttpEntity<Widget> getById(@PathVariable final String id) throws InterruptedException {

        log.debug("Requested widget by id: {}", id);

        return new ResponseEntity<>(widgetService.findById(id), HttpStatus.OK);
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
    public HttpEntity<Widget> update(@RequestBody final WidgetParams widgetParams, @PathVariable final String id)
            throws InterruptedException {

        log.debug("Requested to update widget with id: {} with values: {}", id, widgetParams);

        final Widget widget = widgetService.updateOrCreate(id, widgetParams.getX(), widgetParams.getY(),
                widgetParams.getWidth(), widgetParams.getHeight(), widgetParams.getZ());

        return new ResponseEntity<>(widget, HttpStatus.OK);
    }

    /**
     * Deletes widget with specified id.
     *
     * @param id id of a widget to be removed.
     */
    @DeleteMapping(WidgetControllerApiPath.WIDGETS_PATH + "/{id}")
    public void delete(@PathVariable final String id) throws InterruptedException {

        log.debug("Requested to delete widget with id: {}", id);

        widgetService.deleteById(id);
    }
}

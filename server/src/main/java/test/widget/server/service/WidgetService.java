package test.widget.server.service;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import test.widget.server.ServerConfigurationProperties;
import test.widget.server.domain.Area;
import test.widget.server.domain.Widget;
import test.widget.server.exception.WidgetNotFoundException;
import test.widget.server.repository.WidgetRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.Objects.requireNonNull;

/**
 * Service with basic operations with widgets.
 * <p>
 * Uses {@link UUID#randomUUID()} as an id when creating new widget.
 *
 * @author Mikhail Kondratev
 */
@Component
public class WidgetService {

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
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    /**
     * Service for filtering widgets inside an area.
     */
    private final WidgetFilteringService widgetFilterService;

    /**
     * Constructor.
     *
     * @param widgetRepository              widget repository.
     * @param serverConfigurationProperties general server configuration properties.
     * @param widgetFilterService           widgets filtering service.
     */
    public WidgetService(final WidgetRepository widgetRepository,
                         final ServerConfigurationProperties serverConfigurationProperties,
                         final WidgetFilteringService widgetFilterService) {
        this.widgetRepository = widgetRepository;
        this.serverConfigurationProperties = serverConfigurationProperties;
        this.widgetFilterService = widgetFilterService;
    }

    /**
     * Calculates the highest z-index.
     *
     * @return the highest z-index value that one widget has,
     * or {@link ServerConfigurationProperties#getInitialZIndex()} if no widgets exist.
     */
    protected int getHighestZIndex() {
        return widgetRepository.findAll()
                .stream()
                .map(Widget::getZ)
                .max(Integer::compareTo)
                .orElse(serverConfigurationProperties.getInitialZIndex());
    }

    /**
     * Sets widget's z coordinate.
     * If new z-index value is specified sets it's value to widget's index and shifts all existing widget's z-index by 1.
     * If new z-index value is <code>null</code> - sets first free index among all existing widgets.
     *
     * @param widget    widget, whose z-index should be set.
     * @param newZIndex new z-index value.
     */
    protected void updateWidgetZIndex(final Widget widget, @Nullable final Integer newZIndex) {
        if (newZIndex == null) {
            final int highestZIndex = getHighestZIndex();

            if (widget.isNew()) {
                widget.setZ(highestZIndex + 1);
                return;
            }

            if (highestZIndex > widget.getZ()) {
                widget.setZ(highestZIndex + 1);
            }

            return;
        }

        widget.setZ(newZIndex);

        widgetRepository.findAll()
                .stream()
                .parallel()
                .filter(existingWidget -> existingWidget.getZ() >= newZIndex)
                .filter(existingWidget -> !requireNonNull(existingWidget.getId()).equals(widget.getId()))
                .peek(existingWidget -> existingWidget.setZ(existingWidget.getZ() + 1))
                .forEach(widgetRepository::save);

    }

    /**
     * Finds widget by specified id.
     *
     * @param id id of a widget to be found.
     * @return found widget with id is equal to specified one.
     * @throws InterruptedException    if the thread was interrupted.
     * @throws WidgetNotFoundException if widget with specified does not exist.
     */
    @Transactional
    public Widget findById(final String id) throws InterruptedException, WidgetNotFoundException {
        final Lock readLock = readWriteLock.readLock();

        boolean isLocked = false;
        try {
            isLocked = readLock.tryLock() || readLock.tryLock(serverConfigurationProperties.getLockTimeout(), TimeUnit.SECONDS);

            return widgetRepository.findById(id)
                    .orElseThrow(() -> new WidgetNotFoundException(id));
        } finally {
            if (isLocked) {
                readLock.unlock();
            }
        }
    }

    /**
     * Saves specified widget.
     * <p/>
     * Updated {@link Widget#getLastModified()} with current time.
     * Also, moves other widgets with greater or equal z-index up.
     *
     * @param widget widget to be saved.
     * @param z      z-index of a widget, if specified.
     * @return saved widget.
     */
    protected Widget save(final Widget widget, @Nullable final Integer z) {
        widget.setLastModified(LocalDateTime.now());
        updateWidgetZIndex(widget, z);
        widgetRepository.save(widget);

        return widget;
    }

    /**
     * Gets widgets inside specified area.
     *
     * @param area filtering criteria, or {@link Area#EMPTY_AREA} if no filtering is required.
     * @return collection of widgets fits the area.
     * @throws InterruptedException if the thread was interrupted.
     */
    @Transactional
    public Collection<Widget> getWidgetsInsideArea(final Area area) throws InterruptedException {
        final Lock readLock = readWriteLock.readLock();

        List<Widget> widgets;
        boolean isLocked = false;
        try {
            isLocked = readLock.tryLock() || readLock.tryLock(serverConfigurationProperties.getLockTimeout(), TimeUnit.SECONDS);
            widgets = widgetRepository.findAll();
        } finally {
            if (isLocked) {
                readLock.unlock();
            }
        }

        if (Area.EMPTY_AREA.equals(area)) {
            return widgets;
        }

        return widgetFilterService.filterWidgetsInsideArea(widgets, area);
    }

    /**
     * Updated the widget with specified id, if this widget is present.
     * Otherwise creates new widgets with given params.
     *
     * @param id id of a widget to be found and updated.
     * @return updated or newly created widget.
     * @throws InterruptedException if the thread was interrupted.
     */
    @Transactional
    public Widget updateOrCreate(final String id,
                                 @Nullable final Integer x,
                                 @Nullable final Integer y,
                                 @Nullable final Integer width,
                                 @Nullable final Integer height,
                                 @Nullable final Integer z) throws InterruptedException {
        final Lock readLock = readWriteLock.readLock();

        boolean isLocked = false;
        try {
            isLocked = readLock.tryLock() || readLock.tryLock(serverConfigurationProperties.getLockTimeout(), TimeUnit.SECONDS);


            final Optional<Widget> foundWidgetOptional = widgetRepository.findById(id);

            if (foundWidgetOptional.isEmpty()) {
                return createNew(requireNonNull(x), requireNonNull(y), requireNonNull(width), requireNonNull(height), z);
            }

            final Widget widget = foundWidgetOptional.get();

            if (x != null) {
                widget.setX(x);
            }

            if (y != null) {
                widget.setY(y);
            }

            if (width != null) {
                widget.setWidth(width);
            }

            if (height != null) {
                widget.setHeight(height);
            }

            return save(widget, z);
        } finally {
            if (isLocked) {
                readLock.unlock();
            }
        }
    }

    /**
     * Deletes widget with specified id.
     *
     * @param id id of a widget to be removed.
     * @throws InterruptedException if the thread was interrupted.
     */
    @Transactional
    public void deleteById(final String id) throws InterruptedException {
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

    /**
     * Creates new widget with specified params.
     *
     * @param x      x coordinate.
     * @param y      y coordinate.
     * @param width  a width of a widget.
     * @param height a height of a widget.
     * @param z      z-index, if specified.
     * @return created widget.
     * @throws InterruptedException if the thread was interrupted.
     */
    @Transactional
    public Widget createNew(final int x, final int y, final int width, final int height, @Nullable final Integer z) throws InterruptedException {
        final Lock readLock = readWriteLock.readLock();

        boolean isLocked = false;
        try {
            isLocked = readLock.tryLock() || readLock.tryLock(serverConfigurationProperties.getLockTimeout(), TimeUnit.SECONDS);

            final Widget widget = new Widget();
            widget.setId(UUID.randomUUID().toString());
            widget.setNew(true);
            widget.setX(x);
            widget.setY(y);
            widget.setWidth(width);
            widget.setHeight(height);

            return save(widget, z);
        } finally {
            if (isLocked) {
                readLock.unlock();
            }
        }
    }
}

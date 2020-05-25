package test.widget.server.service;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import test.widget.server.ServerConfigurationProperties;
import test.widget.server.domain.Widget;
import test.widget.server.repository.WidgetRepository;

import java.util.UUID;

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
     * Constructor.
     *
     * @param widgetRepository              widget repository.
     * @param serverConfigurationProperties general server configuration properties.
     */
    public WidgetService(final WidgetRepository widgetRepository,
                         final ServerConfigurationProperties serverConfigurationProperties) {
        this.widgetRepository = widgetRepository;
        this.serverConfigurationProperties = serverConfigurationProperties;
    }

    /**
     * Creates new widget object.
     * Initializes {@link Widget#getId()} with random {@link UUID} and sets {@link Widget#isNew()} true.
     *
     * @return newly created widget object.
     */
    public Widget createNew() {
        final Widget widget = new Widget();
        widget.setId(UUID.randomUUID().toString());
        widget.setNew(true);
        return widget;
    }

    /**
     * Calculates the highest z-index.
     *
     * @return the highest z-index value that one widget has,
     * or {@link ServerConfigurationProperties#getInitialZIndex()} if no widgets exist.
     */
    public int getHighestZIndex() {
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
    public void updateWidgetZIndex(final Widget widget, @Nullable final Integer newZIndex) {
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
}

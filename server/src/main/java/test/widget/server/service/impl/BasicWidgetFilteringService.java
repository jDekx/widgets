package test.widget.server.service.impl;

import test.widget.server.domain.Area;
import test.widget.server.domain.Widget;
import test.widget.server.service.WidgetFilteringService;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Basic filtering implementation.
 * Complexity is O(n).
 *
 * @author Mikhail Kondratev
 */
public class BasicWidgetFilteringService implements WidgetFilteringService {

    @Override
    public Collection<Widget> filterWidgetsInsideArea(final Collection<Widget> widgets, final Area area) {
        return widgets
                .stream()
                .filter(widget -> widget.getX() >= area.getX()
                        && widget.getY() >= area.getY()
                        && widget.getX() + widget.getWidth() <= area.getX() + area.getWidth()
                        && widget.getY() + widget.getHeight() <= area.getY() + area.getHeight())
                .collect(Collectors.toList());
    }
}

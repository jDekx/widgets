package test.widget.server.service;

import test.widget.server.domain.Area;
import test.widget.server.domain.Widget;

import java.util.Collection;

/**
 * Widgets filtering service interface.
 *
 * @author Mikhail Kondratev
 */
public interface WidgetFilteringService {

    /**
     * Return widgets filtered to fit an area.
     * Widget should fall entirely inside an area.
     *
     * @param widgets widgets to be filtered.
     * @param area    filtering widgets area.
     * @return Set of widgets that fully fit inside the area.
     */
    Collection<Widget> filterWidgetsInsideArea(Collection<Widget> widgets, final Area area);
}

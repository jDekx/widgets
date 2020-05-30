package test.widget.server.service.impl;


import com.github.davidmoten.rtree2.Entry;
import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Rectangle;
import com.github.davidmoten.rtree2.geometry.internal.RectangleFloat;
import com.github.davidmoten.rtree2.internal.EntryDefault;
import test.widget.server.domain.Area;
import test.widget.server.domain.Widget;
import test.widget.server.service.WidgetFilteringService;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service for finding widgets inside an area based on R-Tree.
 * <p/>
 * Service is subscribed to Widget events to build trees for effectively searching widgets.
 *
 * @author Mikhail Kondratev
 */
public class RTreeWidgetFilteringService implements WidgetFilteringService {

    @Override
    public Collection<Widget> filterWidgetsInsideArea(final Collection<Widget> widgets, final Area area) {
        final Set<Entry<Widget, Rectangle>> entries = widgets.stream()
                .map(widget -> EntryDefault.entry(widget, RectangleFloat.create(widget.getX(), widget.getY(), widget.getX() + widget.getWidth(), widget.getY() + widget.getHeight())))
                .collect(Collectors.toSet());

        final RTree<Widget, Rectangle> tree = RTree.minChildren(8).maxChildren(64).<Widget, Rectangle>create().add(entries);

        final Rectangle areaRectangle = Geometries.rectangle(area.getX(), area.getY(), area.getX() + area.getWidth(), area.getY() + area.getHeight());

        return StreamSupport.stream(
                tree.search(areaRectangle, (g1, g2) ->
                        g1.x1() >= g2.x1()
                                && g1.y1() >= g2.y1()
                                && g1.x2() <= g2.x2()
                                && g1.y2() <= g2.y2()).spliterator(), false)
                .map(Entry::value)
                .collect(Collectors.toSet());
    }
}

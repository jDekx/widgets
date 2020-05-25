package test.widget.server.service;

import test.widget.server.domain.Area;
import test.widget.server.domain.Widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility method for {@link WidgetFilteringService} tests and benchmarks.
 *
 * @author Mikhail Kondratev
 */
public class WidgetFilteringServiceTestUtils {

    /**
     * Creates list of widgets with random coordinates.
     * Coordinates and dimensions are capped at 100.
     *
     * @param widgetsCount number of widgets to be generated.
     * @return list with generated widgets. Each widget has unique sequence id.
     */
    public static List<Widget> generateWidgets(final int widgetsCount) {
        final Random random = new Random();

        final List<Widget> result = new ArrayList<>(widgetsCount);

        for (int i = 1; i <= widgetsCount; i++) {
            final Widget widget = new Widget();
            widget.setId(Integer.toString(i));
            widget.setX(random.nextInt(100));
            widget.setY(random.nextInt(100));
            widget.setWidth(random.nextInt(100));
            widget.setHeight(random.nextInt(100));

            result.add(widget);
        }

        return result;
    }

    /**
     * Creates area with lower left corner at (0, 0), and height and width is equal to 100.
     *
     * @return created widget.
     */
    public static Area createArea() {
        final Random random = new Random();
        final Area area = new Area();
        area.setX(random.nextInt(100));
        area.setY(random.nextInt(100));
        area.setWidth(random.nextInt(100));
        area.setHeight(random.nextInt(100));

        return area;
    }
}

package test.widget.server.service;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import test.widget.server.domain.Area;
import test.widget.server.domain.Widget;
import test.widget.server.service.impl.BasicWidgetFilteringService;
import test.widget.server.service.impl.RTreeWidgetFilteringService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testing {@link WidgetFilteringService} implementations.
 *
 * @author Mikhail Kondratev
 */
public class WidgetFilteringComparisonTest {

    /**
     * TreeMap service implementation to be compared.
     */
    private test.widget.server.service.impl.RTreeWidgetFilteringService RTreeWidgetFilteringService;

    /**
     * Service basic implementation to be compared.
     */
    private BasicWidgetFilteringService basicWidgetFilteringService;

    @Before
    public void setUp() {
        RTreeWidgetFilteringService = new RTreeWidgetFilteringService();
        basicWidgetFilteringService = new BasicWidgetFilteringService();
    }

    /**
     * Asserts that conditions given in test task's text are met.
     */
    @Test
    public void testFromTask() {
        //given
        final Widget widget1 = new Widget();
        widget1.setId("1");
        widget1.setX(0);
        widget1.setY(0);
        widget1.setWidth(100);
        widget1.setHeight(100);

        final Widget widget2 = new Widget();
        widget2.setId("2");
        widget2.setX(0);
        widget2.setY(50);
        widget2.setWidth(100);
        widget2.setHeight(100);

        final Widget widget3 = new Widget();
        widget3.setId("3");
        widget3.setX(100);
        widget3.setY(50);
        widget3.setWidth(100);
        widget3.setHeight(100);

        final List<Widget> widgets = List.of(widget1, widget2, widget3);

        final Area area = new Area();
        area.setX(0);
        area.setY(0);
        area.setWidth(100);
        area.setHeight(150);

        //when
        final Collection<Widget> inefficient = basicWidgetFilteringService.filterWidgetsInsideArea(widgets, area);
        final Collection<Widget> efficient = RTreeWidgetFilteringService.filterWidgetsInsideArea(widgets, area);

        //then
        assertThat(efficient)
                .hasSameElementsAs(inefficient)
                .extracting(Widget::getId)
                .containsOnlyOnce("1", "2");
    }

    /**
     * Asserts that an efficient method produces same elements as an inefficient on random widgets.
     */
    @Test
    public void testRandomWidgets() {
        for (int i = 0; i < 10000; i++) {

            final List<Widget> widgets = WidgetFilteringServiceTestUtils.generateWidgets(10);
            final Area area = WidgetFilteringServiceTestUtils.createArea();

            final Collection<Widget> inefficient = basicWidgetFilteringService.filterWidgetsInsideArea(widgets, area);
            final Collection<Widget> efficient = RTreeWidgetFilteringService.filterWidgetsInsideArea(widgets, area);

            assertThat(efficient).hasSameElementsAs(inefficient);
        }
    }

    /**
     * Asserts that filter would return empty set when no widgets is present.
     */
    @Test
    public void testEmptyFilter() {
        //given
        //when

        final Area area = new Area();
        area.setX(0);
        area.setY(0);
        area.setWidth(10);
        area.setHeight(10);

        //then
        Assertions.assertThat(RTreeWidgetFilteringService.filterWidgetsInsideArea(Collections.emptySet(), area)).isEmpty();
        assertThat(basicWidgetFilteringService.filterWidgetsInsideArea(Collections.emptySet(), area)).isEmpty();

    }
}
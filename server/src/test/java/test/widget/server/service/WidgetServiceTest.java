package test.widget.server.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import test.widget.server.ServerConfigurationProperties;
import test.widget.server.domain.Widget;
import test.widget.server.repository.WidgetRepository;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link WidgetService}.
 *
 * @author Mikhail Kondratev
 */
public class WidgetServiceTest {

    /**
     * Service to be tested.
     */
    private WidgetService widgetService;

    /**
     * Widget repository.
     */
    private WidgetRepository widgetRepository;

    /**
     * Widget filtering service.
     */
    private WidgetFilteringService widgetFilteringService;

    /**
     * Server configuration properties.
     */
    private ServerConfigurationProperties properties;

    @Before
    public void setUp() {
        widgetRepository = Mockito.mock(WidgetRepository.class);
        widgetFilteringService = Mockito.mock(WidgetFilteringService.class);
        properties = new ServerConfigurationProperties();
        properties.setInitialZIndex(0);

        widgetService = new WidgetService(widgetRepository, properties, widgetFilteringService);
    }

    /**
     * Widgets with value, greater that specified should have incremented z-index.
     */
    @Test
    public void testIncrementingWidgetsZIndex() {
        //given
        final Widget widget1 = new Widget();
        widget1.setId(UUID.randomUUID().toString());
        widget1.setZ(1);

        final Widget widget2 = new Widget();
        widget2.setId(UUID.randomUUID().toString());
        widget2.setZ(2);

        final Widget widget3 = new Widget();
        widget3.setId(UUID.randomUUID().toString());
        widget3.setZ(3);

        ArgumentCaptor<Widget> argumentCaptor = ArgumentCaptor.forClass(Widget.class);
        when(widgetRepository.findAll()).thenReturn(List.of(widget1, widget2, widget3));

        //when
        widgetService.updateWidgetZIndex(widget1, 2);

        //then
        verify(widgetRepository, times(2)).save(argumentCaptor.capture());

        assertThat(argumentCaptor.getAllValues()).hasSize(2)
                .extracting(Widget::getZ)
                .containsExactly(3, 4);
    }

    /**
     * Service should not increment widget's z-index if {@link WidgetService#updateWidgetZIndex(Widget, Integer)}
     * is called with highest z;
     */
    @Test
    public void testNotIncrementingZIndexWhenAllWidgetsAreInBackground() {
        //given
        final Widget widget1 = new Widget();
        widget1.setId(UUID.randomUUID().toString());
        widget1.setZ(1);

        final Widget widget2 = new Widget();
        widget2.setId(UUID.randomUUID().toString());
        widget2.setZ(2);

        final Widget widget3 = new Widget();
        widget3.setId(UUID.randomUUID().toString());
        widget3.setZ(3);

        when(widgetRepository.findAll()).thenReturn(List.of(widget1, widget2, widget3));

        //when
        widgetService.updateWidgetZIndex(widget3, 100);

        //then
        verify(widgetRepository, never()).save(any());
    }

    /**
     * Asserts that z-index will be equal to the highest value.
     */
    @Test
    public void testAutoZCalculationOnNewWidget() {
        //given
        final Widget widget1 = new Widget();
        widget1.setId(UUID.randomUUID().toString());
        widget1.setZ(1);

        final Widget widget2 = new Widget();
        widget2.setId(UUID.randomUUID().toString());
        widget2.setZ(2);

        final Widget widget3 = new Widget();
        widget3.setId(UUID.randomUUID().toString());
        widget3.setZ(3);

        when(widgetRepository.findAll()).thenReturn(List.of(widget1, widget2, widget3));

        final Widget newWidget = new Widget();
        newWidget.setId(UUID.randomUUID().toString());
        newWidget.setNew(true);

        //when
        widgetService.updateWidgetZIndex(newWidget, null);

        //then
        assertThat(newWidget.getZ()).isEqualTo(4);
    }

    /**
     * Asserts that non-new widget will have the highest z-index value when z-index is calculated automatically.
     */
    @Test
    public void testAutoZCalculationOnExistingWidget() {
        //given
        final Widget widget1 = new Widget();
        widget1.setId(UUID.randomUUID().toString());
        widget1.setZ(1);

        final Widget widget2 = new Widget();
        widget2.setId(UUID.randomUUID().toString());
        widget2.setZ(2);

        final Widget widget3 = new Widget();
        widget3.setId(UUID.randomUUID().toString());
        widget3.setZ(3);

        when(widgetRepository.findAll()).thenReturn(List.of(widget1, widget2, widget3));

        //when
        widgetService.updateWidgetZIndex(widget2, null);

        //then
        assertThat(widget2.getZ()).isEqualTo(4);
    }

    /**
     * Asserts that z-index will not be changed on non-new widget if this widget has the highest z-index.
     */
    @Test
    public void testZIndexIsNotChanged() {
        //given
        final Widget widget1 = new Widget();
        widget1.setId(UUID.randomUUID().toString());
        widget1.setZ(1);

        final Widget widget2 = new Widget();
        widget2.setId(UUID.randomUUID().toString());
        widget2.setZ(2);

        final Widget widget3 = new Widget();
        widget3.setId(UUID.randomUUID().toString());
        widget3.setZ(3);

        when(widgetRepository.findAll()).thenReturn(List.of(widget1, widget2, widget3));

        //when
        widgetService.updateWidgetZIndex(widget3, null);

        //then
        assertThat(widget3.getZ()).isEqualTo(3);
    }
}
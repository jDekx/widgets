package test.widget.server.repository.impl;

import org.junit.Before;
import org.junit.Test;
import test.widget.server.domain.Widget;
import test.widget.server.repository.WidgetRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link WidgetHeapRepository}.
 *
 * @author Mikhail Kondratev
 */
public class WidgetHeapRepositoryTest {

    /**
     * Repository to be tested.
     */
    private WidgetHeapRepository repository;

    @Before
    public void setUp() {
        repository = new WidgetHeapRepository();
    }

    /**
     * Saving widget and fetching it by id should return copy of that widget
     * with {@link Widget#isNew()} set to <code>false</code>.
     */
    @Test
    public void testSavingAndLoading() {
        //given
        final Widget widget = new Widget();
        widget.setId("1");
        widget.setNew(true);

        //when
        repository.save(widget);

        //then
        @SuppressWarnings("OptionalGetWithoutIsPresent") final Widget foundWidget = repository.findById("1").get();

        assertThat(foundWidget).isNotSameAs(widget);
        assertThat(foundWidget.isNew()).isTrue();
    }

    /**
     * Finding all of saved widgets should return exact size and exact element ids.
     */
    @Test
    public void testFindingAllWidgets() {
        //given
        final Widget widget1 = new Widget();
        widget1.setId("1");

        final Widget widget2 = new Widget();
        widget2.setId("2");

        final Widget widget3 = new Widget();
        widget3.setId("3");

        //when
        repository.save(widget1);
        repository.save(widget2);
        repository.save(widget3);

        //then
        final List<Widget> allWidgets = repository.findAll();

        assertThat(allWidgets)
                .hasSize(3)
                .extracting(Widget::getId)
                .contains("1", "2", "3");
    }

    /**
     * Finding missing widget by id should return {@link Optional#empty()}.
     */
    @Test
    public void testFindingByIdMissingWidget() {
        //given

        //when
        final Optional<Widget> nonExistingWidgetOptional = repository.findById("123");

        //then
        assertThat(nonExistingWidgetOptional).isEmpty();
    }

    /**
     * Asserts that no exception is thrown when deleting non existing widget by id.
     */
    @Test
    public void testDeletingMissingWidget() {
        //given

        //when
        repository.deleteById("1");

        //then
    }

    /**
     * After deleting widget from repository it should not be fetched with {@link WidgetRepository#findAll()} call.
     */
    @Test
    public void testDeletingWidgetFromRepository() {
        //given
        Widget widget = new Widget();
        widget.setId("1");
        repository.save(widget);

        widget = new Widget();
        widget.setId("2");
        repository.save(widget);

        widget = new Widget();
        widget.setId("3");
        repository.save(widget);

        //when
        repository.deleteById("1");

        //then
        final List<Widget> allWidgets = repository.findAll();

        assertThat(allWidgets)
                .hasSize(2)
                .extracting(Widget::getId)
                .contains("2", "3");
    }
}
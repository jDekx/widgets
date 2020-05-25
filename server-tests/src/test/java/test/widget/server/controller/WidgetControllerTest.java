package test.widget.server.controller;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import test.widget.server.ServerConfigurationProperties;
import test.widget.server.domain.Widget;
import test.widget.server.repository.WidgetRepository;

import java.util.*;

import static javax.json.Json.createObjectBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link WidgetController}.
 *
 * @author Mikhail Kondratev
 */
@SpringBootTest
@WebAppConfiguration
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class WidgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WidgetRepository widgetRepository;

    @Autowired
    private ServerConfigurationProperties configurationProperties;

    /**
     * Clear widget repository after each test.
     */
    @AfterEach
    void tearDown() {
        widgetRepository.findAll().stream()
                .map(Widget::getId)
                .forEach(widgetRepository::deleteById);
    }

    /**
     * Test widget creation through endpoint and verifying it's values through get request.
     *
     * @throws Exception on test error.
     */
    @Test
    void testCreatingWidget() throws Exception {
        //given
        //when
        final MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(WidgetControllerApiPath.WIDGETS_PATH)
                .content(createObjectBuilder()
                        .add("x", "30")
                        .add("y", "40")
                        .add("z", "1")
                        .add("width", "100")
                        .add("height", "200")
                        .build()
                        .toString()
                )
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        final String id = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.id");
        assertThat(id).isNotEmpty();

        //then
        mockMvc.perform(get(WidgetControllerApiPath.WIDGETS_PATH + "/" + id)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.x").value("30"))
                .andExpect(jsonPath("$.y").value("40"))
                .andExpect(jsonPath("$.z").value("1"))
                .andExpect(jsonPath("$.width").value("100"))
                .andExpect(jsonPath("$.height").value("200"));
    }

    /**
     * Tests changing widget's z index after creating new widget with existing z index.
     *
     * @throws Exception on test error.
     */
    @Test
    void testMovingWidgetWithGreaterIndex() throws Exception {

        //given
        final Map<Integer, String> indexToIdMap = new HashMap<>();

        final int widgetsCount = 3;

        for (int zIndex = 1; zIndex <= widgetsCount; zIndex++) {
            final MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(WidgetControllerApiPath.WIDGETS_PATH)
                    .content(createObjectBuilder()
                            .add("x", "10")
                            .add("y", "10")
                            .add("z", zIndex)
                            .add("width", "100")
                            .add("height", "100")
                            .build()
                            .toString()
                    )
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andReturn();

            final String id = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.id");

            indexToIdMap.put(zIndex, id);
        }

        //when
        final MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(WidgetControllerApiPath.WIDGETS_PATH)
                .content(createObjectBuilder()
                        .add("x", "10")
                        .add("y", "10")
                        .add("z", 2)
                        .add("width", "100")
                        .add("height", "100")
                        .build()
                        .toString()
                )
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        final String newWidgetId = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.id");
        assertThat(newWidgetId).isNotEmpty();

        //then
        Map.of(
                1, indexToIdMap.get(1),
                2, newWidgetId,
                3, indexToIdMap.get(2),
                4, indexToIdMap.get(3)
        ).forEach((expectedZIndex, widgetId) -> {
            try {
                mockMvc.perform(get(WidgetControllerApiPath.WIDGETS_PATH + "/" + widgetId)
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.z").value(expectedZIndex));
            } catch (Exception e) {
                fail("Exception during mockMvc request", e);
            }
        });
    }

    /**
     * Test ensures that after creating several widgets with same z index they will have sequence of z indices.
     *
     * @throws Exception on test error.
     */
    @Test
    void testCreatingSeveralWidgetsWithSameZIndex() throws Exception {
        //given
        //when

        final Deque<String> widgetIds = new LinkedList<>();
        final Random random = new Random();

        for (int widgetCounter = 1; widgetCounter <= 10; widgetCounter++) {
            final MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(WidgetControllerApiPath.WIDGETS_PATH)
                    .content(createObjectBuilder()
                            .add("x", random.nextInt())
                            .add("y", random.nextInt())
                            .add("z", 1)
                            .add("width", random.nextInt())
                            .add("height", random.nextInt())
                            .build()
                            .toString()
                    )
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andReturn();

            final String id = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.id");

            widgetIds.addFirst(id);
        }

        //then
        final MvcResult allWidgetsMvcResult = mockMvc.perform(MockMvcRequestBuilders.get(WidgetControllerApiPath.WIDGETS_PATH)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        final JSONArray arrayOfIds = JsonPath.read(allWidgetsMvcResult.getResponse().getContentAsString(), "$[*]['id']");

        assertThat(arrayOfIds)
                .containsExactlyElementsOf(widgetIds);
    }

    /**
     * Asserts that server will return {@link HttpStatus#BAD_REQUEST} when creating widget's required arguments are missing.
     *
     * @throws Exception on test error.
     */
    @Test
    void testMissingRequiredArguments() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(WidgetControllerApiPath.WIDGETS_PATH)
                .content(createObjectBuilder()
                        .build()
                        .toString()
                )
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.post(WidgetControllerApiPath.WIDGETS_PATH)
                .content(createObjectBuilder()
                        .add("x", "10")
                        .build()
                        .toString()
                )
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.post(WidgetControllerApiPath.WIDGETS_PATH)
                .content(createObjectBuilder()
                        .add("x", "10")
                        .add("y", "10")
                        .build()
                        .toString()
                )
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.post(WidgetControllerApiPath.WIDGETS_PATH)
                .content(createObjectBuilder()
                        .add("x", "10")
                        .add("y", "10")
                        .add("width", "10")
                        .build()
                        .toString()
                )
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    /**
     * Asserts that widget, created without specifying z-index will have highest index.
     *
     * @throws Exception on test error.
     */
    @Test
    void testCreatingWidgetWithoutZIndex() throws Exception {
        //given
        Widget widget = new Widget();
        widget.setId("1");
        widget.setZ(1);
        widgetRepository.save(widget);

        widget = new Widget();
        widget.setId("2");
        widget.setZ(2);
        widgetRepository.save(widget);

        //when
        //then
        mockMvc.perform(MockMvcRequestBuilders.post(WidgetControllerApiPath.WIDGETS_PATH)
                .content(createObjectBuilder()
                        .add("x", "10")
                        .add("y", "10")
                        .add("width", "10")
                        .add("height", "10")
                        .build()
                        .toString()
                )
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.z").value(3));
    }

    /**
     * Asserts that after editing existence widget's z index, other widgets have z-index shifted.
     *
     * @throws Exception on test error.
     */
    @Test
    void testEditWidget() throws Exception {
        //given
        Widget widget = new Widget();
        widget.setId("1");
        widget.setZ(1);
        widgetRepository.save(widget);

        widget = new Widget();
        widget.setId("2");
        widget.setZ(2);
        widgetRepository.save(widget);

        widget = new Widget();
        widget.setId("3");
        widget.setZ(3);
        widgetRepository.save(widget);

        //when
        mockMvc.perform(put(WidgetControllerApiPath.WIDGETS_PATH + "/3")
                .content(createObjectBuilder()
                        .add("z", "1")
                        .build()
                        .toString()
                )
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        //then
        assertThat(widgetRepository.findById("1")).isPresent().map(Widget::getZ).get().isEqualTo(2);
        assertThat(widgetRepository.findById("2")).isPresent().map(Widget::getZ).get().isEqualTo(3);
        assertThat(widgetRepository.findById("3")).isPresent().map(Widget::getZ).get().isEqualTo(1);
    }

    /**
     * Asserts that after editing a widget, if it has the highest z index, its index hasn't been changed.
     *
     * @throws Exception on test error.
     */
    @Test
    void testEditSingleWidget() throws Exception {
        //given
        Widget widget = new Widget();
        widget.setId("1");
        widget.setZ(1);
        widgetRepository.save(widget);

        //when
        mockMvc.perform(put(WidgetControllerApiPath.WIDGETS_PATH + "/1")
                .content(createObjectBuilder()
                        .add("z", "5")
                        .build()
                        .toString()
                )
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        //then
        assertThat(widgetRepository.findById("1")).isPresent().map(Widget::getZ).get().isEqualTo(5);
    }

    /**
     * Asserts that passing no parameters into change widget endpoint doesn't remove any attributes.
     *
     * @throws Exception on test error.
     */
    @Test
    void testEditingWithoutParams() throws Exception {
        //given
        Widget widget = new Widget();
        widget.setId("1");
        widget.setX(100);
        widget.setY(200);
        widget.setWidth(300);
        widget.setHeight(400);
        widget.setZ(555);
        widgetRepository.save(widget);

        //when
        mockMvc.perform(put(WidgetControllerApiPath.WIDGETS_PATH + "/1")
                .content(createObjectBuilder()
                        .build()
                        .toString()
                )
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        //then
        @SuppressWarnings("OptionalGetWithoutIsPresent") final Widget editedWidget = widgetRepository.findById("1").get();
        assertThat(editedWidget.getX()).isEqualTo(100);
        assertThat(editedWidget.getY()).isEqualTo(200);
        assertThat(editedWidget.getWidth()).isEqualTo(300);
        assertThat(editedWidget.getHeight()).isEqualTo(400);
        assertThat(editedWidget.getZ()).isEqualTo(555);
    }

    /**
     * Asserts sequence of delete calls deletes widget and will not have any errors.
     *
     * @throws Exception on test error.
     */
    @Test
    void testDeleteWidget() throws Exception {
        //given
        Widget widget = new Widget();
        widget.setId("1");
        widget.setX(100);
        widget.setY(200);
        widget.setWidth(300);
        widget.setHeight(400);
        widget.setZ(555);

        widgetRepository.save(widget);

        //when
        mockMvc.perform(delete(WidgetControllerApiPath.WIDGETS_PATH + "/1")
                .content(createObjectBuilder()
                        .build()
                        .toString()
                )
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(delete(WidgetControllerApiPath.WIDGETS_PATH + "/1")
                .content(createObjectBuilder()
                        .build()
                        .toString()
                )
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        //then
        assertThat(widgetRepository.findById("1")).isEmpty();
    }

    /**
     * Asserts that passing pageSize into controller will return number of elements equals to passed value.
     *
     * @throws Exception on test error.
     */
    @Test
    void testPaging() throws Exception {
        //given
        fillRepositoryWithWidgets(10);

        //when
        final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(WidgetControllerApiPath.WIDGETS_PATH)
                .param("pageSize", "2")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        //then
        final JSONArray arrayOfIds = JsonPath.read(result.getResponse().getContentAsString(), "$[*]['id']");
        assertThat(arrayOfIds)
                .hasSize(2)
                .containsOnlyOnce("1", "2");
    }

    /**
     * Asserts that passing huge page number will result in max default page size.
     *
     * @throws Exception on test error.
     */
    @Test
    void testMaxPagingSize() throws Exception {
        //given
        configurationProperties.setPageMaxSize(20);
        fillRepositoryWithWidgets(100);

        //when
        final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(WidgetControllerApiPath.WIDGETS_PATH)
                .param("pageSize", "50")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        //then
        final JSONArray arrayOfIds = JsonPath.read(result.getResponse().getContentAsString(), "$[*]['id']");
        assertThat(arrayOfIds)
                .hasSize(20);

    }

    /**
     * Test paging with offset.
     *
     * @throws Exception on test error.
     */
    @Test
    void testPagingWithOffset() throws Exception {
        //given
        fillRepositoryWithWidgets(20);

        //when
        final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(WidgetControllerApiPath.WIDGETS_PATH)
                .param("pageSize", "5")
                .param("offset", "5")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        //then
        final JSONArray arrayOfIds = JsonPath.read(result.getResponse().getContentAsString(), "$[*]['id']");
        assertThat(arrayOfIds)
                .hasSize(5)
                .containsOnlyOnce("6", "7", "8", "9", "10");

    }

    /**
     * Test filtering widgets by area.
     *
     * @throws Exception on test error.
     */
    @Test
    void testFiltering() throws Exception {
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

        widgetRepository.save(widget1);
        widgetRepository.save(widget2);
        widgetRepository.save(widget3);

        //when
        final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(WidgetControllerApiPath.WIDGETS_PATH)
                .param("x", "0")
                .param("y", "0")
                .param("width", "100")
                .param("height", "150")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        //then
        final JSONArray arrayOfIds = JsonPath.read(result.getResponse().getContentAsString(), "$[*]['id']");
        assertThat(arrayOfIds)
                .hasSize(2)
                .containsOnlyOnce("1", "2");

    }

    /**
     * Test missing filter params.
     *
     * @throws Exception on test error.
     */
    @Test
    void testMissingFilteringParam() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(MockMvcRequestBuilders.get(WidgetControllerApiPath.WIDGETS_PATH)
                .param("x", "0")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    /**
     * Creates widgets and saves them in repository.
     * Widgets will have from 1 to widgetNumber id and z-index.
     *
     * @param widgetNumber number of widgets to be created.
     */
    protected void fillRepositoryWithWidgets(final int widgetNumber) {
        final Random random = new Random();

        for (int i = 1; i <= widgetNumber; i++) {
            final Widget widget = new Widget();
            widget.setId(Integer.toString(i));
            widget.setX(random.nextInt());
            widget.setY(random.nextInt());
            widget.setWidth(random.nextInt());
            widget.setHeight(random.nextInt());
            widget.setZ(i);
            widgetRepository.save(widget);
        }
    }
}

package test.widget.server.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static javax.json.Json.createObjectBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Asserts that controller works properly when database is used for storing widgets.
 *
 * @author Mikhail Kondratev
 */
@SpringBootTest
@WebAppConfiguration
@ActiveProfiles("test-h2")
@AutoConfigureMockMvc
public class WidgetControllerTestWithDatabase {

    @Autowired
    private MockMvc mockMvc;

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

}

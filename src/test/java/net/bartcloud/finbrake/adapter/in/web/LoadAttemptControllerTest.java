package net.bartcloud.finbrake.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import net.bartcloud.finbrake.application.port.in.ProcessLoadAttempt;
import net.bartcloud.finbrake.domain.LoadDecision;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LoadAttemptController.class)
class LoadAttemptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProcessLoadAttempt useCase;

    @Test
    void respondsWithDecisionWhenProcessed() throws Exception {
        when(useCase.process(any())).thenReturn(Optional.of(new LoadDecision("1", "c", true)));

        mockMvc.perform(
                        post("/load-attempts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"id":"1","customer_id":"c","load_amount":"$10.00","time":"2018-01-01T00:00:00Z"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.customer_id").value("c"))
                .andExpect(jsonPath("$.accepted").value(true));
    }

    @Test
    void respondsWithNoContentWhenDuplicate() throws Exception {
        when(useCase.process(any())).thenReturn(Optional.empty());

        mockMvc.perform(
                        post("/load-attempts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"id":"1","customer_id":"c","load_amount":"$10.00","time":"2018-01-01T00:00:00Z"}
                                """))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void respondsWithBadRequestWhenFieldMissing() throws Exception {
        mockMvc.perform(
                        post("/load-attempts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"id":"1","customer_id":"c","time":"2018-01-01T00:00:00Z"}
                                """))
                .andExpect(status().isBadRequest());
    }
}

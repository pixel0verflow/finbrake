package net.bartcloud.finbrake.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.bartcloud.finbrake.adapter.in.web.LoadAttemptRequest;
import net.bartcloud.finbrake.adapter.in.web.LoadDecisionResponse;
import net.bartcloud.finbrake.application.port.in.ProcessLoadAttempt;
import net.bartcloud.finbrake.domain.LoadDecision;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
class SampleFixtureParityTest {

    @Autowired
    private ProcessLoadAttempt useCase;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void replayingSampleInputProducesSampleOutput() throws Exception {
        List<String> expected = readLines("sample-output.txt");
        List<String> actual = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(
                new ClassPathResource("sample-input.txt").getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                LoadAttemptRequest req = mapper.readValue(line, LoadAttemptRequest.class);
                Optional<LoadDecision> decision = useCase.process(req.toDomain());
                decision.ifPresent(
                        loadDecision -> actual.add(mapper.writeValueAsString(LoadDecisionResponse.from(loadDecision))));
            }
        }
        assertThat(actual).containsExactlyElementsOf(expected);
    }

    private static List<String> readLines(String resource) throws Exception {
        try (BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(
                new ClassPathResource(resource).getInputStream(), StandardCharsets.UTF_8))) {
            List<String> out = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    out.add(line);
                }
            }
            return out;
        }
    }
}

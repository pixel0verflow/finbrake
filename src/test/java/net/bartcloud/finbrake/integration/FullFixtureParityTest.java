package net.bartcloud.finbrake.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
class FullFixtureParityTest {

    @Autowired
    private ProcessLoadAttempt useCase;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void replayingFullInputProducesFullOutput() throws Exception {
        ClassPathResource input = new ClassPathResource("input.txt");
        ClassPathResource output = new ClassPathResource("output.txt");
        assumeTrue(
                input.exists() && output.exists(),
                "input.txt/output.txt not present in test resources; skipping full parity test");

        List<String> expected = readLines(output);
        List<String> actual = new ArrayList<>();
        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(input.getInputStream(), StandardCharsets.UTF_8))) {
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

    private static List<String> readLines(ClassPathResource resource) throws Exception {
        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
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

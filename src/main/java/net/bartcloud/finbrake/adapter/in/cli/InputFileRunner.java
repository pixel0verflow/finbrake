package net.bartcloud.finbrake.adapter.in.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import net.bartcloud.finbrake.adapter.in.web.LoadAttemptRequest;
import net.bartcloud.finbrake.adapter.in.web.LoadDecisionResponse;
import net.bartcloud.finbrake.application.port.in.ProcessLoadAttempt;
import net.bartcloud.finbrake.domain.LoadDecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(name = "finbrake.input")
public class InputFileRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(InputFileRunner.class);

    private final ProcessLoadAttempt useCase;
    private final ObjectMapper mapper;
    private final Path inputPath;
    private final Optional<Path> outputPath;

    public InputFileRunner(
            ProcessLoadAttempt useCase,
            ObjectMapper mapper,
            @Value("${finbrake.input}") Path inputPath,
            @Value("${finbrake.output:}") String outputPath) {
        this.useCase = useCase;
        this.mapper = mapper;
        this.inputPath = inputPath;
        this.outputPath =
                (outputPath == null || outputPath.isBlank()) ? Optional.empty() : Optional.of(Path.of(outputPath));
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Processing input file {}", inputPath);
        long total = 0;
        long emitted = 0;
        try (BufferedReader reader = Files.newBufferedReader(inputPath);
                BufferedWriter writer = outputPath.isPresent() ? Files.newBufferedWriter(outputPath.get()) : null) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                total++;
                LoadAttemptRequest request = mapper.readValue(line, LoadAttemptRequest.class);
                Optional<LoadDecision> decision = useCase.process(request.toDomain());
                if (decision.isEmpty()) {
                    continue;
                }
                String json = mapper.writeValueAsString(LoadDecisionResponse.from(decision.get()));
                if (writer != null) {
                    writer.write(json);
                    writer.newLine();
                } else {
                    System.out.println(json);
                }
                emitted++;
            }
            if (writer != null) {
                writer.flush();
            }
        }
        log.info("Processed {} lines, emitted {} responses", total, emitted);
    }
}

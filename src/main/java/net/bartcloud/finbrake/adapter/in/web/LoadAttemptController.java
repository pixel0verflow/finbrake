package net.bartcloud.finbrake.adapter.in.web;

import jakarta.validation.Valid;
import net.bartcloud.finbrake.application.port.in.ProcessLoadAttempt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/load-attempts")
public class LoadAttemptController {

    private final ProcessLoadAttempt useCase;

    public LoadAttemptController(ProcessLoadAttempt useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<LoadDecisionResponse> submit(@Valid @RequestBody LoadAttemptRequest request) {
        return useCase.process(request.toDomain())
                .map(LoadDecisionResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}

package net.bartcloud.finbrake.application.port.in;

import java.util.Optional;
import net.bartcloud.finbrake.domain.LoadAttempt;
import net.bartcloud.finbrake.domain.LoadDecision;

public interface ProcessLoadAttempt {
    Optional<LoadDecision> process(LoadAttempt attempt);
}

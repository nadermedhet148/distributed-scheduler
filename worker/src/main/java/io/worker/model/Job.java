package io.worker.model;

import java.time.Instant;
import java.util.UUID;

public record Job(
        UUID id,
        String frequency,
        String metadata,
        int userId,
        int segment,
        Instant createdAt,
        Instant nextExec,
        Instant lastExec,
        int retry
) {}
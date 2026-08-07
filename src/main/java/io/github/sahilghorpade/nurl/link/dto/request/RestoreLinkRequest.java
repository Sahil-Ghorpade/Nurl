package io.github.sahilghorpade.nurl.link.dto.request;

import java.time.Instant;

public record RestoreLinkRequest(
		Instant expiresAt
) {
}

package io.github.sahilghorpade.nurl.link.dto.response;

import java.time.Instant;

public record LinkResponse(
		Long id,
		String originalUrl,
		String shortCode,
		String shortUrl,
		Instant createdAt,
		Instant expiresAt
) {
}

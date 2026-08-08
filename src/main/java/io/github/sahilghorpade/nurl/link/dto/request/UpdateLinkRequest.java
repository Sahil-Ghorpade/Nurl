package io.github.sahilghorpade.nurl.link.dto.request;

import org.hibernate.validator.constraints.URL;

import java.time.Instant;

public record UpdateLinkRequest(

		@URL(message = "Invalid URL.")
		String originalUrl,

		Instant expiresAt
) {
}

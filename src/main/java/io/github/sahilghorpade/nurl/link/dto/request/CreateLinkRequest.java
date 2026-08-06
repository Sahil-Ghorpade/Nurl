package io.github.sahilghorpade.nurl.link.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record CreateLinkRequest(

		@NotBlank(message = "URL is required")
		@URL(message = "Invalid URL")
		String originalLink
) {
}

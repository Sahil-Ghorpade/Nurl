package io.github.sahilghorpade.nurl.link.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record CreateLinkRequest(

		@NotBlank(message = "URL is required")
		@URL(message = "Invalid URL")
		String originalUrl,

		@Size(
				min = 3,
				max = 30,
				message = "Alias must be between 3 and 30 characters"
		)
		@Pattern(
				regexp = "^[A-Za-z0-9_-]+$",
				message = "Alias can only contain letters, numbers, hyphens (-), and underscores (_)"
		)
		String alias
) {
}

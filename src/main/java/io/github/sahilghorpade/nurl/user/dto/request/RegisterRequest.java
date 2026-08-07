package io.github.sahilghorpade.nurl.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest (

		@NotBlank(message = "Name is required")
		@Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
		String name,

		@NotBlank(message = "Email is requird")
		@Email(message = "Invalid email format")
		@Size(max = 255, message = "Email cannot exceed 255 characters")
		String email,

		@NotBlank(message = "message is required")
		@Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters")
		String password
) {
}

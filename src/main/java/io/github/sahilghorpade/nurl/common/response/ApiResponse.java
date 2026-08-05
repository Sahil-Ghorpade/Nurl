package io.github.sahilghorpade.nurl.common.response;

import java.time.Instant;

public record ApiResponse<T>(
		boolean success,
		String message,
		T data,
		Instant timestamp
) {
}

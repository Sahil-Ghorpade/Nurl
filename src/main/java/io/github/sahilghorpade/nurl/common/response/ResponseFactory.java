package io.github.sahilghorpade.nurl.common.response;

import java.time.Instant;

public final class ResponseFactory {

	private ResponseFactory() {}

	public static <T> ApiResponse<T> success(
			String message,
			T data
	) {
		return new ApiResponse<>(
				true,
				message,
				data,
				Instant.now()
		);
	}
}

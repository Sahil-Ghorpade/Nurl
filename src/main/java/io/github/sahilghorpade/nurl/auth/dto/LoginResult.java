package io.github.sahilghorpade.nurl.auth.dto;

public record LoginResult(
		String accessToken,
		String refreshToken
) {
}
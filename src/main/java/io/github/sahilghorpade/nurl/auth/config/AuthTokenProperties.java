package io.github.sahilghorpade.nurl.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record AuthTokenProperties(
		long accessTokenExpiration,
		long refreshTokenExpiration
) {
}
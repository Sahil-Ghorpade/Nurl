package io.github.sahilghorpade.nurl.auth.service;

import io.github.sahilghorpade.nurl.auth.config.AuthCookieProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class AuthCookieService {

	private final AuthCookieProperties properties;

	public AuthCookieService(
			AuthCookieProperties properties
	) {
		this.properties = properties;
	}

	public ResponseCookie createAccessCookie(
			String accessToken,
			long maxAgeSeconds
	) {
		return ResponseCookie
				.from(properties.name(), accessToken)
				.httpOnly(true)
				.secure(properties.secure())
				.sameSite(properties.sameSite())
				.path("/")
				.maxAge(maxAgeSeconds)
				.build();
	}

	public ResponseCookie createRefreshCookie(
			String refreshToken,
			long maxAgeSeconds
	) {
		return ResponseCookie
				.from(properties.refreshName(), refreshToken)
				.httpOnly(true)
				.secure(properties.secure())
				.sameSite(properties.sameSite())
				.path("/auth")
				.maxAge(maxAgeSeconds)
				.build();
	}

	public ResponseCookie clearAccessCookie() {
		return ResponseCookie
				.from(properties.name(), "")
				.httpOnly(true)
				.secure(properties.secure())
				.sameSite(properties.sameSite())
				.path("/")
				.maxAge(0)
				.build();
	}

	public ResponseCookie clearRefreshCookie() {
		return ResponseCookie
				.from(properties.refreshName(), "")
				.httpOnly(true)
				.secure(properties.secure())
				.sameSite(properties.sameSite())
				.path("/auth")
				.maxAge(0)
				.build();
	}
}
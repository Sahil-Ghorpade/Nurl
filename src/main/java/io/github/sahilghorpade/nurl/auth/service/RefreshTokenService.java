package io.github.sahilghorpade.nurl.auth.service;

import io.github.sahilghorpade.nurl.auth.entity.RefreshToken;
import io.github.sahilghorpade.nurl.auth.repository.RefreshTokenRepository;
import io.github.sahilghorpade.nurl.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
public class RefreshTokenService {

	private final RefreshTokenRepository refreshTokenRepository;
	private final SecureRandom secureRandom;

	@Value("${jwt.refresh-token-expiration}")
	private long refreshTokenExpiration;

	public RefreshTokenService(
			RefreshTokenRepository refreshTokenRepository
	) {
		this.refreshTokenRepository = refreshTokenRepository;
		this.secureRandom = new SecureRandom();
	}

	@Transactional
	public String createRefreshToken(User user) {

		byte[] randomBytes = new byte[64];
		secureRandom.nextBytes(randomBytes);

		String rawToken =
				Base64.getUrlEncoder()
						.withoutPadding()
						.encodeToString(randomBytes);

		String tokenHash = hashToken(rawToken);

		Instant expiresAt =
				Instant.now()
						.plus(
								refreshTokenExpiration,
								ChronoUnit.MILLIS
						);

		RefreshToken refreshToken =
				RefreshToken.create(
						tokenHash,
						user,
						expiresAt
				);

		refreshTokenRepository.save(refreshToken);

		return rawToken;
	}

	@Transactional(readOnly = true)
	public RefreshToken findValidToken(String rawToken) {

		String tokenHash = hashToken(rawToken);

		return refreshTokenRepository
				.findByTokenHash(tokenHash)
				.filter(RefreshToken::isActive)
				.orElse(null);
	}

	@Transactional
	public void revoke(RefreshToken refreshToken) {
		refreshToken.revoke();
		refreshTokenRepository.save(refreshToken);
	}

	private String hashToken(String token) {

		try {

			MessageDigest digest =
					MessageDigest.getInstance("SHA-256");

			byte[] hash =
					digest.digest(
							token.getBytes(StandardCharsets.UTF_8)
					);

			StringBuilder hexString =
					new StringBuilder(hash.length * 2);

			for (byte b : hash) {
				hexString.append(
						String.format("%02x", b)
				);
			}

			return hexString.toString();

		} catch (NoSuchAlgorithmException exception) {

			throw new IllegalStateException(
					"SHA-256 algorithm is not available",
					exception
			);
		}
	}
}
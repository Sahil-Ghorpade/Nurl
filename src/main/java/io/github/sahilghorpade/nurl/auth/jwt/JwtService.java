package io.github.sahilghorpade.nurl.auth.jwt;

import io.github.sahilghorpade.nurl.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.access-token-expiration}")
	private long accessTokenExpiration;

	public String generateAccessToken(User user) {
		return buildToken(
				user,
				accessTokenExpiration
		);
	}

	private String buildToken(
			User user,
			long expiration
	) {
		Date issuedAt = new Date();

		Date expiresAt = new Date(issuedAt.getTime() + expiration);

		return Jwts
				.builder()
				.subject(
						user.getEmail()
				)
				.claim(
						"userId",
						user.getId()
				)
				.issuedAt(
						issuedAt
				)
				.expiration(
						expiresAt
				)
				.signWith(
						getSigningKey()
				)
				.compact();
	}

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(
				secret.getBytes(StandardCharsets.UTF_8)
		);
	}

	private Claims extractAllClaims(String token) {

		return Jwts
				.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	public String extractSubject(String token) {

		return extractAllClaims(token)
				.getSubject();
	}

	public boolean isTokenValid(String token) {

		try {

			extractAllClaims(token);

			return true;

		} catch (JwtException | IllegalArgumentException exception) {

			return false;

		}
	}
}

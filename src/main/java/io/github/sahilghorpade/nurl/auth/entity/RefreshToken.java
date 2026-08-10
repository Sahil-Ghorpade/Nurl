package io.github.sahilghorpade.nurl.auth.entity;

import io.github.sahilghorpade.nurl.user.entity.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
		name = "refresh_tokens",
		indexes = {
				@Index(
						name = "idx_refresh_tokens_user_id",
						columnList = "user_id"
				),
				@Index(
						name = "idx_refresh_tokens_expires_at",
						columnList = "expires_at"
				)
		}
)
public class RefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(
			nullable = false,
			unique = true,
			length = 64
	)
	private String tokenHash;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "user_id",
			nullable = false
	)
	private User user;

	@Column(
			nullable = false
	)
	private Instant expiresAt;

	@Column(
			nullable = false,
			updatable = false
	)
	private Instant createdAt;

	@Column
	private Instant revokedAt;

	protected RefreshToken() {
	}

	private RefreshToken(
			String tokenHash,
			User user,
			Instant expiresAt
	) {
		this.tokenHash = tokenHash;
		this.user = user;
		this.expiresAt = expiresAt;
		this.createdAt = Instant.now();
	}

	public static RefreshToken create(
			String tokenHash,
			User user,
			Instant expiresAt
	) {
		return new RefreshToken(
				tokenHash,
				user,
				expiresAt
		);
	}

	public Long getId() {
		return id;
	}

	public String getTokenHash() {
		return tokenHash;
	}

	public User getUser() {
		return user;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getRevokedAt() {
		return revokedAt;
	}

	public boolean isExpired() {
		return Instant.now().isAfter(expiresAt);
	}

	public boolean isRevoked() {
		return revokedAt != null;
	}

	public boolean isActive() {
		return !isExpired() && !isRevoked();
	}

	public void revoke() {
		this.revokedAt = Instant.now();
	}
}
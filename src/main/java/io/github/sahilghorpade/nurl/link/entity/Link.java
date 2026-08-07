package io.github.sahilghorpade.nurl.link.entity;

import io.github.sahilghorpade.nurl.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "links")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Link {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(
			nullable = false,
			columnDefinition = "TEXT"
	)
	private String originalUrl;

	@Column(
			nullable = false,
			unique = true,
			length = 10
	)
	private String shortCode;

	@Column(
			nullable = false
	)
	private long clickCount;

	@Column
	private Instant expiresAt;

	@Column(nullable = false)
	private boolean deleted = false;

	@Column
	private Instant deletedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "user_id",
			nullable = false
	)
	private User user;

	@Column(
			nullable = false,
			updatable = false
	)
	private Instant createdAt;

	@Column(
			nullable = false
	)
	private Instant updatedAt;

	@PrePersist
	protected void onCreate() {
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	protected void onUpdate() {
		Instant now = Instant.now();
		this.updatedAt = now;
	}

	public void increaseClickCount() {
		this.clickCount++;
	}

	public boolean isExpired() {
		return expiresAt != null &&
				Instant.now().isAfter(expiresAt);
	}

	public void delete() {
		this.deleted = true;
		this.deletedAt = Instant.now();

		if (this.expiresAt == null ||
				this.expiresAt.isAfter(deletedAt.plus(30, ChronoUnit.DAYS))) {
			this.expiresAt = deletedAt.plus(30, ChronoUnit.DAYS);
		}
	}

	public void restore(Instant expiresAt) {
		this.deleted = false;
		this.deletedAt = null;
		this.expiresAt = expiresAt;
	}

	public void assignUser(User user) {
		this.user = user;
	}

	public void changeShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public Link(String originalUrl) {
		this.originalUrl = originalUrl;
	}
}

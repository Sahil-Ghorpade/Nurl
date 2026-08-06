package io.github.sahilghorpade.nurl.link.entity;

import io.github.sahilghorpade.nurl.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "links")
@Getter
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
}

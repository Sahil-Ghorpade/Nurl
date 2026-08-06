package io.github.sahilghorpade.nurl.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User {

	@Id
	@GeneratedValue
	private UUID id;

	@Column(
			nullable = false,
			length = 100
	)
	private String name;

	@Column(
			nullable = false,
			length = 255,
			unique = true
	)
	private String email;

	@Column(
			nullable = false,
			length = 255
	)
	private String password;

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
	public void prePersist() {

		Instant now = Instant.now();

		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	public void preUpdate() {

		updatedAt = Instant.now();

	}

	public static User create(
			String name,
			String email,
			String password
	) {
		User user = new User();
		user.name = name;
		user.email = email;
		user.password = password;

		return user;
	}

	public void changePassword(String encodedPassword) {
		this.password = encodedPassword;
	}

}

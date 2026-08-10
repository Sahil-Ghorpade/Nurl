package io.github.sahilghorpade.nurl.auth.repository;

import io.github.sahilghorpade.nurl.auth.entity.RefreshToken;
import io.github.sahilghorpade.nurl.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository
		extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByTokenHash(String tokenHash);

	void deleteAllByUser(User user);
}
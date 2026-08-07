package io.github.sahilghorpade.nurl.link.repository;

import io.github.sahilghorpade.nurl.link.entity.Link;
import io.github.sahilghorpade.nurl.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {

	boolean existsByShortCode(String shortCode);

	Optional<Link> findByShortCode(String shortCode);

	Optional<Link> findByIdAndDeletedFalse(long id);

	Optional<Link> findByIdAndDeletedTrue(long id);

	Optional<Link> findByShortCodeAndDeletedFalse(
			String shortCode
	);

	Page<Link> findByUserAndDeletedFalse(
			User user,
			Pageable pageable
	);

	Page<Link> findByUserAndDeletedTrue(
			User user,
			Pageable pageable
	);
}

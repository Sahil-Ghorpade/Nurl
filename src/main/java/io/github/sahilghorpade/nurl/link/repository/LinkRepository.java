package io.github.sahilghorpade.nurl.link.repository;

import io.github.sahilghorpade.nurl.link.entity.Link;
import io.github.sahilghorpade.nurl.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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

	long countByUser(User user);

	long countByUserAndDeletedFalse(User user);

	long countByUserAndDeletedTrue(User user);

	long countByUserAndDeletedFalseAndExpiresAtBefore(
			User user,
			Instant now
	);

	@Query("""
	    SELECT COALESCE(SUM(l.clickCount), 0)
	    FROM Link l
	    WHERE l.user = :user
	      AND l.deleted = false
	""")
	Long getTotalClicks(
			@Param("user") User user
	);


	@Modifying
	@Query("""
    	DELETE FROM Link l
    	WHERE l.expiresAt IS NOT NULL
      	AND l.expiresAt <= :now
	""")
	int deleteExpiredLinks(
			@Param("now") Instant now
	);
}

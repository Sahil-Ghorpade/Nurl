package io.github.sahilghorpade.nurl.dashboard.service;

import io.github.sahilghorpade.nurl.auth.security.UserPrincipal;
import io.github.sahilghorpade.nurl.dashboard.dto.response.DashboardResponse;
import io.github.sahilghorpade.nurl.link.repository.LinkRepository;
import io.github.sahilghorpade.nurl.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class DashboardService {

	private final LinkRepository linkRepository;

	public DashboardService(LinkRepository linkRepository) {
		this.linkRepository = linkRepository;
	}

	@Transactional(readOnly = true)
	public DashboardResponse getDashboard(
			User user
	) {

		long totalLinks =
				linkRepository.countByUser(user);

		long activeLinks =
				linkRepository.countByUserAndDeletedFalse(user);

		long deletedLinks =
				linkRepository.countByUserAndDeletedTrue(user);

		long expiredLinks =
				linkRepository
						.countByUserAndDeletedFalseAndExpiresAtBefore(
								user,
								Instant.now()
						);

		long totalClicks =
				java.util.Optional
						.ofNullable(
								linkRepository.getTotalClicks(user)
						)
						.orElse(0L);

		double averageClicksPerActiveLink =
				activeLinks == 0
						? 0
						: (double) totalClicks / activeLinks;

		return new DashboardResponse(
				totalLinks,
				activeLinks,
				deletedLinks,
				expiredLinks,
				totalClicks,
				averageClicksPerActiveLink
		);
	}
}
package io.github.sahilghorpade.nurl.dashboard.service;

import io.github.sahilghorpade.nurl.dashboard.dto.response.DashboardResponse;
import io.github.sahilghorpade.nurl.link.repository.LinkRepository;
import io.github.sahilghorpade.nurl.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

	@Mock
	private LinkRepository linkRepository;

	private DashboardService dashboardService;

	@BeforeEach
	void setUp() {
		dashboardService = new DashboardService(linkRepository);
	}

	@Test
	void shouldCalculateDashboardMetricsForUserWithActiveLinks() {
		User user = mock(User.class);

		when(linkRepository.countByUser(user)).thenReturn(10L);
		when(linkRepository.countByUserAndDeletedFalse(user)).thenReturn(8L);
		when(linkRepository.countByUserAndDeletedTrue(user)).thenReturn(2L);
		when(linkRepository.countByUserAndDeletedFalseAndExpiresAtBefore(eq(user), any(Instant.class)))
				.thenReturn(1L);
		when(linkRepository.getTotalClicks(user)).thenReturn(40L);

		DashboardResponse response = dashboardService.getDashboard(user);

		assertNotNull(response);
		assertEquals(10L, response.totalLinks());
		assertEquals(8L, response.activeLinks());
		assertEquals(2L, response.expiredLinks());
		assertEquals(1L, response.deletedLinks());
		assertEquals(40L, response.totalClicks());
		assertEquals(5.0, response.averageClicksPerLink(), 0.001);

		verify(linkRepository).countByUser(user);
		verify(linkRepository).countByUserAndDeletedFalse(user);
		verify(linkRepository).countByUserAndDeletedTrue(user);
		verify(linkRepository).countByUserAndDeletedFalseAndExpiresAtBefore(eq(user), any(Instant.class));
		verify(linkRepository).getTotalClicks(user);
	}

	@Test
	void shouldHandleZeroActiveLinksWithoutDivisionByZero() {
		User user = mock(User.class);

		when(linkRepository.countByUser(user)).thenReturn(0L);
		when(linkRepository.countByUserAndDeletedFalse(user)).thenReturn(0L);
		when(linkRepository.countByUserAndDeletedTrue(user)).thenReturn(0L);
		when(linkRepository.countByUserAndDeletedFalseAndExpiresAtBefore(eq(user), any(Instant.class)))
				.thenReturn(0L);
		when(linkRepository.getTotalClicks(user)).thenReturn(null);

		DashboardResponse response = dashboardService.getDashboard(user);

		assertNotNull(response);
		assertEquals(0L, response.totalLinks());
		assertEquals(0L, response.activeLinks());
		assertEquals(0L, response.expiredLinks());
		assertEquals(0L, response.deletedLinks());
		assertEquals(0L, response.totalClicks());
		assertEquals(0.0, response.averageClicksPerLink(), 0.001);
	}
}

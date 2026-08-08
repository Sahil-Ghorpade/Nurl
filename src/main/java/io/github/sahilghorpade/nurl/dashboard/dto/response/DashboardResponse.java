package io.github.sahilghorpade.nurl.dashboard.dto.response;

public record DashboardResponse (

		long totalLinks,

		long activeLinks,

		long expiredLinks,

		long deletedLinks,

		long totalClicks,

		double averageClicksPerLink
) {
}

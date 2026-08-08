package io.github.sahilghorpade.nurl.link.scheduler;


import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;



@Component
public class LinkCleanupScheduler {

	private final LinkCleanupService  linkCleanupService;

	private static final Logger logger =
			LoggerFactory.getLogger(LinkCleanupScheduler.class);

	public LinkCleanupScheduler(
			LinkCleanupService linkCleanupService
	) {
		this.linkCleanupService = linkCleanupService;
	}

	@Scheduled(cron = "${app.cleanup.cron}")
	public void scheduleLinkCleanup() {
		logger.info("Scheduling link cleanup...");

		int deleted =
				linkCleanupService.cleanupExpiredLinks();

		logger.info("Link cleanup finished. Deleted {} links", deleted);
	}
}

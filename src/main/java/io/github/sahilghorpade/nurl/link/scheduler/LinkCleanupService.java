package io.github.sahilghorpade.nurl.link.scheduler;

import io.github.sahilghorpade.nurl.link.repository.LinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class LinkCleanupService {

	private LinkRepository linkRepository;

	public LinkCleanupService(
			LinkRepository linkRepository
	) {
		this.linkRepository = linkRepository;
	}

	public int cleanupExpiredLinks() {

		return linkRepository.deleteExpiredLinks(
				Instant.now()
		);
	}
}

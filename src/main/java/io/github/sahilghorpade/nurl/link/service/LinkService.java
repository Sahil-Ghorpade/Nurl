package io.github.sahilghorpade.nurl.link.service;

import io.github.sahilghorpade.nurl.auth.security.UserPrincipal;
import io.github.sahilghorpade.nurl.common.exception.ForbiddenException;
import io.github.sahilghorpade.nurl.common.exception.ResourceNotFoundException;
import io.github.sahilghorpade.nurl.link.dto.request.CreateLinkRequest;
import io.github.sahilghorpade.nurl.link.dto.response.LinkResponse;
import io.github.sahilghorpade.nurl.link.entity.Link;
import io.github.sahilghorpade.nurl.link.mapper.LinkMapper;
import io.github.sahilghorpade.nurl.link.repository.LinkRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LinkService {

	private final LinkRepository linkRepository;
	private final LinkMapper linkMapper;

	public LinkService(
			LinkRepository linkRepository,
			LinkMapper linkMapper
	) {
		this.linkRepository = linkRepository;
		this.linkMapper = linkMapper;
	}

	public LinkResponse createLink(CreateLinkRequest request) {
		return null;
	}

	@Transactional
	public String redirctLink(String shortCode) {

		Link link = linkRepository
				.findByShortCode(shortCode)
				.orElseThrow(() ->
						new ResourceNotFoundException(
								"No such link"
						));

		link.increaseClickCount();

		linkRepository.save(link);

		return link.getOriginalUrl();
	}

	@Transactional(readOnly = true)
	public LinkResponse getAnalytics(
			Long id,
			UserPrincipal principal
	) {

		Link link = linkRepository
				.findById(id)
				.orElseThrow(() ->
						new ResourceNotFoundException(
								"No such link"
						)
				);

		if (!link.getUser().getId().equals(principal.getUser().getId())) {
			throw new ForbiddenException(
					"You are not allowed to access this resource"
			);
		}

		return linkMapper.toResponse(link);
	}

	@Transactional(readOnly = true)
	public Page<LinkResponse> getLinks(
			Pageable pageable,
			UserPrincipal principal
	) {
		Page<Link> links = linkRepository.findByUser(
				principal.getUser(),
				pageable
		);

		return links
				.map(linkMapper::toResponse);
	}
}

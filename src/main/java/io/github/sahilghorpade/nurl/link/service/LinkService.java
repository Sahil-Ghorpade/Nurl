package io.github.sahilghorpade.nurl.link.service;

import io.github.sahilghorpade.nurl.auth.security.UserPrincipal;
import io.github.sahilghorpade.nurl.common.config.AppProperties;
import io.github.sahilghorpade.nurl.common.exception.*;
import io.github.sahilghorpade.nurl.link.dto.request.CreateLinkRequest;
import io.github.sahilghorpade.nurl.link.dto.request.RestoreLinkRequest;
import io.github.sahilghorpade.nurl.link.dto.request.UpdateLinkRequest;
import io.github.sahilghorpade.nurl.link.dto.response.LinkResponse;
import io.github.sahilghorpade.nurl.link.entity.Link;
import io.github.sahilghorpade.nurl.link.mapper.LinkMapper;
import io.github.sahilghorpade.nurl.link.repository.LinkRepository;
import io.github.sahilghorpade.nurl.link.util.ShortCodeGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

@Service
public class LinkService {

	private final LinkRepository linkRepository;
	private final LinkMapper linkMapper;
	private final ShortCodeGenerator shortCodeGenerator;

	private final String baseUrl;
	private final Set<String> reservedAliases;

	private static final int MAX_ATTEMPTS = 5;

	public LinkService(
			LinkRepository linkRepository,
			LinkMapper linkMapper,
			ShortCodeGenerator shortCodeGenerator,
			AppProperties appProperties
	) {
		this.linkRepository = linkRepository;
		this.linkMapper = linkMapper;
		this.shortCodeGenerator = shortCodeGenerator;

		this.baseUrl = appProperties.getBaseUrl();
		this.reservedAliases = appProperties.getReservedAliases();

	}

	private String resolveShortCode(CreateLinkRequest request) {

		if (hasCustomAlias(request)) {
			return validateAndUseAlias(request.alias());
		}

		return generateUniqueShortCode();
	}

	private String validateAndUseAlias(String alias) {

		if (reservedAliases.contains(alias.toLowerCase())) {
			throw new BadRequestException("Alias is reserved.");
		}

		if (linkRepository.existsByShortCode(alias)) {
			throw new ConflictException("Alias already exists.");
		}

		return alias;
	}

	private String generateUniqueShortCode() {

		for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {

			String code = shortCodeGenerator.generate();

			if (!linkRepository.existsByShortCode(code)) {
				return code;
			}
		}

		throw new ShortCodeGenerationException(
				"Unable to generate unique short code."
		);
	}

	private boolean hasCustomAlias(CreateLinkRequest request) {
		return request.alias() != null && !request.alias().isBlank();
	}

	@Transactional
	public LinkResponse createLink(
			CreateLinkRequest request,
			UserPrincipal principal
	) {

		if (request.expiresAt() != null &&
				!request.expiresAt().isAfter(Instant.now())) {
			throw new BadRequestException("Expiration time must be in the future.");
		}

		String shortCode = resolveShortCode(request);

		Link link = linkMapper.toEntity(request);

		link.assignUser(principal.getUser());
		link.changeShortCode(shortCode);

		Link savedLink = linkRepository.save(link);

		return linkMapper.toResponse(savedLink, baseUrl);
	}

	@Transactional
	public String redirectLink(String shortCode) {

		Link link = linkRepository
				.findByShortCodeAndDeletedFalse(shortCode)
				.orElseThrow(() ->
						new ResourceNotFoundException(
								"No such link"
						));

		if (link.isExpired()) {
			throw new LinkExpiredException(
					"This link is expired."
			);
		}

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
				.findByIdAndDeletedFalse(id)
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

		return linkMapper.toResponse(link, baseUrl);
	}

	@Transactional(readOnly = true)
	public Page<LinkResponse> getLinks(
			Pageable pageable,
			UserPrincipal principal
	) {
		Page<Link> links = linkRepository.findByUserAndDeletedFalse(
				principal.getUser(),
				pageable
		);

		return links
				.map(link -> linkMapper.toResponse(link, baseUrl));
	}

	@Transactional(readOnly = true)
	public Page<LinkResponse> getDeletedLinks(
			Pageable pageable,
			UserPrincipal principal
	) {
		Page<Link> links =
				linkRepository
						.findByUserAndDeletedTrue(principal.getUser(), pageable);

		return links
				.map(link -> linkMapper.toResponse(link, baseUrl));

	}

	@Transactional
	public LinkResponse updateLink(
			Long id,
			UpdateLinkRequest request,
			UserPrincipal principal
	) {
		Link link = linkRepository
				.findByIdAndDeletedFalse(id)
				.orElseThrow(() ->
						new ResourceNotFoundException(
								"No such link"
						)
				);

		if(!link.getUser().getId().equals(principal.getUser().getId())) {
			throw new ForbiddenException(
					"You are not allowed to access this resource"
			);
		}

		if (request.expiresAt() != null) {
			link.changeExpiresAt(request.expiresAt());
		}

		if (request.originalUrl() != null) {
			link.changeOriginalUrl(request.originalUrl());
		}

		return linkMapper.toResponse(link, baseUrl);
	}

	@Transactional
	public void deleteLink(
			Long id,
			UserPrincipal principal
	) {
		Link link = linkRepository
				.findByIdAndDeletedFalse(id)
				.orElseThrow(() ->
					new ResourceNotFoundException("No such link"
					)
				);

		if (!link.getUser().getId().equals(principal.getUser().getId())) {
			throw new ForbiddenException(
					"You are not allowed to access this resource"
			);
		}

		link.delete();
	}

	@Transactional
	public void restoreLink(
			Long id,
			RestoreLinkRequest request,
			UserPrincipal principal
	) {
		Link link = linkRepository
				.findByIdAndDeletedTrue(id)
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

		if (request.expiresAt() != null &&
		!request.expiresAt().isAfter(Instant.now())) {
			throw new BadRequestException(
					"Expiration time must be in the future."
			);
		}

		link.restore(request.expiresAt());
	}

	@Transactional
	public void deleteLinkPermanently(
			Long id,
			UserPrincipal principal
	) {
		Link link = linkRepository
							.findByIdAndDeletedTrue(id)
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

		linkRepository.delete(link);
	}
}

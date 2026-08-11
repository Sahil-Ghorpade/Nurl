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
import io.github.sahilghorpade.nurl.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LinkServiceTest {

	@Mock
	private LinkRepository linkRepository;

	@Mock
	private LinkMapper linkMapper;

	@Mock
	private ShortCodeGenerator shortCodeGenerator;

	@Mock
	private AppProperties appProperties;

	@Mock
	private UserPrincipal principal;

	private LinkService linkService;

	@BeforeEach
	void setUp() {

		when(appProperties.getBaseUrl())
				.thenReturn("http://localhost:8080");

		when(appProperties.getReservedAliases())
				.thenReturn(Set.of(
						"login",
						"register",
						"links",
						"health"
				));

		linkService = new LinkService(
				linkRepository,
				linkMapper,
				shortCodeGenerator,
				appProperties
		);
	}

	@Test
	void shouldCreateLinkWithGeneratedShortCode() {

		CreateLinkRequest request =
				new CreateLinkRequest(
						"https://example.com",
						null,
						null
				);

		Link link =
				new Link("https://example.com");

		LinkResponse expectedResponse =
				new LinkResponse(
						1L,
						"https://example.com",
						"abc123",
						"http://localhost:8080/abc123",
						Instant.now(),
						null
				);

		when(shortCodeGenerator.generate())
				.thenReturn("abc123");

		when(linkRepository.existsByShortCode("abc123"))
				.thenReturn(false);

		when(linkMapper.toEntity(request))
				.thenReturn(link);

		when(linkRepository.save(link))
				.thenReturn(link);

		when(linkMapper.toResponse(
				link,
				"http://localhost:8080"
		)).thenReturn(expectedResponse);

		LinkResponse response =
				linkService.createLink(
						request,
						principal
				);

		assertSame(
				expectedResponse,
				response
		);

		verify(shortCodeGenerator)
				.generate();

		verify(linkRepository)
				.existsByShortCode("abc123");

		verify(linkMapper)
				.toEntity(request);

		verify(linkRepository)
				.save(link);

		verify(linkMapper)
				.toResponse(
						link,
						"http://localhost:8080"
				);
	}

	@Test
	void shouldCreateLinkWithCustomAlias() {

		CreateLinkRequest request =
				new CreateLinkRequest(
						"https://example.com",
						"github",
						null
				);

		Link link =
				new Link("https://example.com");

		LinkResponse expectedResponse =
				new LinkResponse(
						1L,
						"https://example.com",
						"github",
						"http://localhost:8080/github",
						Instant.now(),
						null
				);

		when(linkRepository.existsByShortCode("github"))
				.thenReturn(false);

		when(linkMapper.toEntity(request))
				.thenReturn(link);

		when(linkRepository.save(link))
				.thenReturn(link);

		when(linkMapper.toResponse(
				link,
				"http://localhost:8080"
		)).thenReturn(expectedResponse);

		LinkResponse response =
				linkService.createLink(
						request,
						principal
				);

		assertSame(
				expectedResponse,
				response
		);

		verify(linkRepository)
				.existsByShortCode("github");

		verify(linkRepository)
				.save(link);

		verify(shortCodeGenerator, never())
				.generate();

		verify(linkMapper)
				.toEntity(request);

		verify(linkMapper)
				.toResponse(
						link,
						"http://localhost:8080"
				);
	}

	@Test
	void shouldRejectReservedAlias() {

		CreateLinkRequest request =
				new CreateLinkRequest(
						"https://example.com",
						"LOGIN",
						null
				);

		BadRequestException exception =
				assertThrows(
						BadRequestException.class,
						() -> linkService.createLink(
								request,
								principal
						)
				);

		assertEquals(
				"Alias is reserved.",
				exception.getMessage()
		);

		verify(linkRepository, never())
				.existsByShortCode(anyString());

		verify(linkRepository, never())
				.save(any());

		verify(shortCodeGenerator, never())
				.generate();
	}

	@Test
	void shouldRejectDuplicateAlias() {

		CreateLinkRequest request =
				new CreateLinkRequest(
						"https://example.com",
						"github",
						null
				);

		when(linkRepository.existsByShortCode("github"))
				.thenReturn(true);

		ConflictException exception =
				assertThrows(
						ConflictException.class,
						() -> linkService.createLink(
								request,
								principal
						)
				);

		assertEquals(
				"Alias already exists.",
				exception.getMessage()
		);

		verify(linkRepository)
				.existsByShortCode("github");

		verify(linkRepository, never())
				.save(any());

		verify(linkMapper, never())
				.toEntity(any());

		verify(shortCodeGenerator, never())
				.generate();
	}

	@Test
	void shouldRetryWhenGeneratedShortCodeAlreadyExists() {

		CreateLinkRequest request =
				new CreateLinkRequest(
						"https://example.com",
						null,
						null
				);

		Link link =
				new Link("https://example.com");

		LinkResponse expectedResponse =
				new LinkResponse(
						1L,
						"https://example.com",
						"xyz789",
						"http://localhost:8080/xyz789",
						Instant.now(),
						null
				);

		when(shortCodeGenerator.generate())
				.thenReturn("abc123", "xyz789");

		when(linkRepository.existsByShortCode("abc123"))
				.thenReturn(true);

		when(linkRepository.existsByShortCode("xyz789"))
				.thenReturn(false);

		when(linkMapper.toEntity(request))
				.thenReturn(link);

		when(linkRepository.save(link))
				.thenReturn(link);

		when(linkMapper.toResponse(
				link,
				"http://localhost:8080"
		)).thenReturn(expectedResponse);

		LinkResponse response =
				linkService.createLink(
						request,
						principal
				);

		assertSame(
				expectedResponse,
				response
		);

		verify(shortCodeGenerator, times(2))
				.generate();

		verify(linkRepository)
				.existsByShortCode("abc123");

		verify(linkRepository)
				.existsByShortCode("xyz789");

		verify(linkRepository)
				.save(link);
	}

	@Test
	void shouldThrowExceptionWhenUnableToGenerateUniqueShortCode() {

		CreateLinkRequest request =
				new CreateLinkRequest(
						"https://example.com",
						null,
						null
				);

		when(shortCodeGenerator.generate())
				.thenReturn(
						"code01",
						"code02",
						"code03",
						"code04",
						"code05"
				);

		when(linkRepository.existsByShortCode(anyString()))
				.thenReturn(true);

		ShortCodeGenerationException exception =
				assertThrows(
						ShortCodeGenerationException.class,
						() -> linkService.createLink(
								request,
								principal
						)
				);

		assertEquals(
				"Unable to generate unique short code.",
				exception.getMessage()
		);

		verify(
				shortCodeGenerator,
				times(5)
		).generate();

		verify(
				linkRepository,
				times(5)
		).existsByShortCode(anyString());

		verify(linkRepository, never())
				.save(any());

		verify(linkMapper, never())
				.toEntity(any());
	}

	@Test
	void shouldRejectExpiredDateDuringLinkCreation() {

		CreateLinkRequest request =
				new CreateLinkRequest(
						"https://example.com",
						null,
						Instant.now().minusSeconds(60)
				);

		BadRequestException exception =
				assertThrows(
						BadRequestException.class,
						() -> linkService.createLink(
								request,
								principal
						)
				);

		assertEquals(
				"Expiration time must be in the future.",
				exception.getMessage()
		);

		verifyNoInteractions(
				shortCodeGenerator,
				linkRepository,
				linkMapper
		);
	}

	@Test
	void shouldCreateLinkWithFutureExpiration() {

		Instant expiresAt =
				Instant.now().plusSeconds(3600);

		CreateLinkRequest request =
				new CreateLinkRequest(
						"https://example.com",
						null,
						expiresAt
				);

		Link link =
				new Link("https://example.com");

		LinkResponse expectedResponse =
				new LinkResponse(
						1L,
						"https://example.com",
						"abc123",
						"http://localhost:8080/abc123",
						Instant.now(),
						expiresAt
				);

		when(shortCodeGenerator.generate())
				.thenReturn("abc123");

		when(linkRepository.existsByShortCode("abc123"))
				.thenReturn(false);

		when(linkMapper.toEntity(request))
				.thenReturn(link);

		when(linkRepository.save(link))
				.thenReturn(link);

		when(linkMapper.toResponse(
				link,
				"http://localhost:8080"
		)).thenReturn(expectedResponse);

		LinkResponse response =
				linkService.createLink(
						request,
						principal
				);

		assertSame(
				expectedResponse,
				response
		);

		verify(shortCodeGenerator)
				.generate();

		verify(linkRepository)
				.existsByShortCode("abc123");

		verify(linkRepository)
				.save(link);
	}

	@Test
	void shouldThrowExceptionWhenLinkDoesNotExist() {

		when(linkRepository.findByShortCodeAndDeletedFalse("abc123"))
				.thenReturn(java.util.Optional.empty());

		ResourceNotFoundException exception =
				assertThrows(
						ResourceNotFoundException.class,
						() -> linkService.redirectLink("abc123")
				);

		assertEquals(
				"No such link",
				exception.getMessage()
		);

		verify(linkRepository)
				.findByShortCodeAndDeletedFalse("abc123");

		verify(linkRepository, never())
				.save(any());
	}

	@Test
	void shouldNotRedirectDeletedLink() {

		when(linkRepository.findByShortCodeAndDeletedFalse("abc123"))
				.thenReturn(java.util.Optional.empty());

		assertThrows(
				ResourceNotFoundException.class,
				() -> linkService.redirectLink("abc123")
		);

		verify(linkRepository)
				.findByShortCodeAndDeletedFalse("abc123");

		verify(linkRepository, never())
				.save(any());
	}

	@Test
	void shouldRejectExpiredLink() {

		Link link =
				new Link("https://example.com");

		link.changeExpiresAt(
				Instant.now().minusSeconds(60)
		);

		when(linkRepository.findByShortCodeAndDeletedFalse("abc123"))
				.thenReturn(java.util.Optional.of(link));

		LinkExpiredException exception =
				assertThrows(
						LinkExpiredException.class,
						() -> linkService.redirectLink("abc123")
				);

		assertEquals(
				"This link has expired",
				exception.getMessage()
		);

		verify(linkRepository)
				.findByShortCodeAndDeletedFalse("abc123");

		verify(linkRepository, never())
				.save(any());
	}

	@Test
	void shouldRedirectAndIncreaseClickCount() {

		Link link =
				new Link("https://example.com");

		when(linkRepository.findByShortCodeAndDeletedFalse("abc123"))
				.thenReturn(java.util.Optional.of(link));

		String originalUrl =
				linkService.redirectLink("abc123");

		assertEquals(
				"https://example.com",
				originalUrl
		);

		assertEquals(
				1,
				link.getClickCount()
		);

		verify(linkRepository)
				.findByShortCodeAndDeletedFalse("abc123");

		verify(linkRepository)
				.save(link);
	}

	@Test
	void shouldThrowExceptionWhenAnalyticsLinkDoesNotExist() {

		Long linkId = 1L;

		when(linkRepository.findByIdAndDeletedFalse(linkId))
				.thenReturn(Optional.empty());

		ResourceNotFoundException exception =
				assertThrows(
						ResourceNotFoundException.class,
						() -> linkService.getAnalytics(
								linkId,
								principal
						)
				);

		assertEquals(
				"No such link",
				exception.getMessage()
		);

		verify(linkRepository)
				.findByIdAndDeletedFalse(linkId);

		verify(linkMapper, never())
				.toResponse(any(), anyString());
	}

	@Test
	void shouldRejectAnalyticsAccessForNonOwner() {

		Long linkId = 1L;

		User owner = mock(User.class);
		User currentUser = mock(User.class);

		UUID ownerId = UUID.randomUUID();
		UUID currentUserId = UUID.randomUUID();

		when(owner.getId())
				.thenReturn(ownerId);

		when(currentUser.getId())
				.thenReturn(currentUserId);

		when(principal.getUser())
				.thenReturn(currentUser);

		Link link =
				new Link("https://example.com");

		link.assignUser(owner);

		when(linkRepository.findByIdAndDeletedFalse(linkId))
				.thenReturn(Optional.of(link));

		ForbiddenException exception =
				assertThrows(
						ForbiddenException.class,
						() -> linkService.getAnalytics(
								linkId,
								principal
						)
				);

		assertEquals(
				"You are not allowed to access this resource",
				exception.getMessage()
		);

		verify(linkRepository)
				.findByIdAndDeletedFalse(linkId);

		verify(linkMapper, never())
				.toResponse(any(), anyString());
	}

	@Test
	void shouldReturnAnalyticsForOwner() {

		Long linkId = 1L;

		User owner = mock(User.class);

		UUID ownerId = UUID.randomUUID();

		when(owner.getId())
				.thenReturn(ownerId);

		when(principal.getUser())
				.thenReturn(owner);

		Link link =
				new Link("https://example.com");

		link.assignUser(owner);

		LinkResponse expectedResponse =
				new LinkResponse(
						linkId,
						"https://example.com",
						"abc123",
						"http://localhost:8080/abc123",
						Instant.now(),
						null
				);

		when(linkRepository.findByIdAndDeletedFalse(linkId))
				.thenReturn(Optional.of(link));

		when(linkMapper.toResponse(
				link,
				"http://localhost:8080"
		)).thenReturn(expectedResponse);

		LinkResponse response =
				linkService.getAnalytics(
						linkId,
						principal
				);

		assertSame(
				expectedResponse,
				response
		);

		verify(linkRepository)
				.findByIdAndDeletedFalse(linkId);

		verify(linkMapper)
				.toResponse(
						link,
						"http://localhost:8080"
				);
	}

	@Test
	void shouldReturnActiveLinksForUser() {

		Pageable pageable =
				PageRequest.of(0, 10);

		User user =
				mock(User.class);

		Link link =
				new Link("https://example.com");

		LinkResponse expectedResponse =
				new LinkResponse(
						1L,
						"https://example.com",
						"abc123",
						"http://localhost:8080/abc123",
						Instant.now(),
						null
				);

		when(principal.getUser())
				.thenReturn(user);

		Page<Link> linkPage =
				new PageImpl<>(
						List.of(link),
						pageable,
						1
				);

		when(linkRepository.findByUserAndDeletedFalse(
				user,
				pageable
		)).thenReturn(linkPage);

		when(linkMapper.toResponse(
				link,
				"http://localhost:8080"
		)).thenReturn(expectedResponse);

		Page<LinkResponse> response =
				linkService.getLinks(
						pageable,
						principal
				);

		assertEquals(1, response.getTotalElements());

		assertSame(
				expectedResponse,
				response.getContent().get(0)
		);

		verify(principal)
				.getUser();

		verify(linkRepository)
				.findByUserAndDeletedFalse(
						user,
						pageable
				);

		verify(linkMapper)
				.toResponse(
						link,
						"http://localhost:8080"
				);
	}

	@Test
	void shouldReturnDeletedLinksForUser() {

		Pageable pageable =
				PageRequest.of(0, 10);

		User user =
				mock(User.class);

		Link link =
				new Link("https://example.com");

		link.delete();

		LinkResponse expectedResponse =
				new LinkResponse(
						1L,
						"https://example.com",
						"abc123",
						"http://localhost:8080/abc123",
						Instant.now(),
						null
				);

		when(principal.getUser())
				.thenReturn(user);

		Page<Link> linkPage =
				new PageImpl<>(
						List.of(link),
						pageable,
						1
				);

		when(linkRepository.findByUserAndDeletedTrue(
				user,
				pageable
		)).thenReturn(linkPage);

		when(linkMapper.toResponse(
				link,
				"http://localhost:8080"
		)).thenReturn(expectedResponse);

		Page<LinkResponse> response =
				linkService.getDeletedLinks(
						pageable,
						principal
				);

		assertEquals(
				1,
				response.getTotalElements()
		);

		assertSame(
				expectedResponse,
				response.getContent().get(0)
		);

		verify(principal)
				.getUser();

		verify(linkRepository)
				.findByUserAndDeletedTrue(
						user,
						pageable
				);

		verify(linkMapper)
				.toResponse(
						link,
						"http://localhost:8080"
				);
	}

	@Test
	void shouldThrowExceptionWhenUpdatingNonExistingLink() {

		Long linkId = 1L;

		UpdateLinkRequest request =
				new UpdateLinkRequest(
						"https://new-example.com",
						null
				);

		when(linkRepository.findByIdAndDeletedFalse(linkId))
				.thenReturn(Optional.empty());

		ResourceNotFoundException exception =
				assertThrows(
						ResourceNotFoundException.class,
						() -> linkService.updateLink(
								linkId,
								request,
								principal
						)
				);

		assertEquals(
				"No such link",
				exception.getMessage()
		);

		verify(linkRepository)
				.findByIdAndDeletedFalse(linkId);

		verify(linkMapper, never())
				.toResponse(any(), anyString());
	}

	@Test
	void shouldRejectUpdateForNonOwner() {

		Long linkId = 1L;

		User owner = mock(User.class);
		User currentUser = mock(User.class);

		UUID ownerId = UUID.randomUUID();
		UUID currentUserId = UUID.randomUUID();

		when(owner.getId())
				.thenReturn(ownerId);

		when(currentUser.getId())
				.thenReturn(currentUserId);

		when(principal.getUser())
				.thenReturn(currentUser);

		Link link =
				new Link("https://example.com");

		link.assignUser(owner);

		UpdateLinkRequest request =
				new UpdateLinkRequest(
						"https://new-example.com",
						null
				);

		when(linkRepository.findByIdAndDeletedFalse(linkId))
				.thenReturn(Optional.of(link));

		ForbiddenException exception =
				assertThrows(
						ForbiddenException.class,
						() -> linkService.updateLink(
								linkId,
								request,
								principal
						)
				);

		assertEquals(
				"You are not allowed to access this resource",
				exception.getMessage()
		);

		verify(linkRepository)
				.findByIdAndDeletedFalse(linkId);

		verify(linkMapper, never())
				.toResponse(any(), anyString());
	}

	@Test
	void shouldUpdateLinkForOwner() {

		Long linkId = 1L;

		User owner = mock(User.class);

		UUID ownerId = UUID.randomUUID();

		when(owner.getId())
				.thenReturn(ownerId);

		when(principal.getUser())
				.thenReturn(owner);

		Link link =
				new Link("https://example.com");

		link.assignUser(owner);

		Instant newExpiration =
				Instant.now().plusSeconds(3600);

		UpdateLinkRequest request =
				new UpdateLinkRequest(
						"https://new-example.com",
						newExpiration
				);

		LinkResponse expectedResponse =
				new LinkResponse(
						linkId,
						"https://new-example.com",
						"abc123",
						"http://localhost:8080/abc123",
						Instant.now(),
						newExpiration
				);

		when(linkRepository.findByIdAndDeletedFalse(linkId))
				.thenReturn(Optional.of(link));

		when(linkMapper.toResponse(
				link,
				"http://localhost:8080"
		)).thenReturn(expectedResponse);

		LinkResponse response =
				linkService.updateLink(
						linkId,
						request,
						principal
				);

		assertSame(
				expectedResponse,
				response
		);

		assertEquals(
				"https://new-example.com",
				link.getOriginalUrl()
		);

		assertEquals(
				newExpiration,
				link.getExpiresAt()
		);

		verify(linkRepository)
				.findByIdAndDeletedFalse(linkId);

		verify(linkMapper)
				.toResponse(
						link,
						"http://localhost:8080"
				);
	}

	@Test
	void shouldThrowExceptionWhenDeletingNonExistingLink() {

		Long linkId = 1L;

		when(linkRepository.findByIdAndDeletedFalse(linkId))
				.thenReturn(Optional.empty());

		ResourceNotFoundException exception =
				assertThrows(
						ResourceNotFoundException.class,
						() -> linkService.deleteLink(
								linkId,
								principal
						)
				);

		assertEquals(
				"No such link",
				exception.getMessage()
		);

		verify(linkRepository)
				.findByIdAndDeletedFalse(linkId);
	}

	@Test
	void shouldRejectDeleteForNonOwner() {

		Long linkId = 1L;

		User owner = mock(User.class);
		User currentUser = mock(User.class);

		UUID ownerId = UUID.randomUUID();
		UUID currentUserId = UUID.randomUUID();

		when(owner.getId())
				.thenReturn(ownerId);

		when(currentUser.getId())
				.thenReturn(currentUserId);

		when(principal.getUser())
				.thenReturn(currentUser);

		Link link =
				new Link("https://example.com");

		link.assignUser(owner);

		when(linkRepository.findByIdAndDeletedFalse(linkId))
				.thenReturn(Optional.of(link));

		ForbiddenException exception =
				assertThrows(
						ForbiddenException.class,
						() -> linkService.deleteLink(
								linkId,
								principal
						)
				);

		assertEquals(
				"You are not allowed to access this resource",
				exception.getMessage()
		);

		assertFalse(link.isDeleted());

		verify(linkRepository)
				.findByIdAndDeletedFalse(linkId);
	}

	@Test
	void shouldSoftDeleteLinkForOwner() {

		Long linkId = 1L;

		User owner = mock(User.class);

		UUID ownerId = UUID.randomUUID();

		when(owner.getId())
				.thenReturn(ownerId);

		when(principal.getUser())
				.thenReturn(owner);

		Link link =
				new Link("https://example.com");

		link.assignUser(owner);

		when(linkRepository.findByIdAndDeletedFalse(linkId))
				.thenReturn(Optional.of(link));

		linkService.deleteLink(
				linkId,
				principal
		);

		assertTrue(link.isDeleted());

		assertNotNull(link.getDeletedAt());

		verify(linkRepository)
				.findByIdAndDeletedFalse(linkId);

		verify(linkRepository, never())
				.delete(any());
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

	@Test
	void shouldThrowExceptionWhenRestoringNonExistingDeletedLink() {

		Long linkId = 1L;

		RestoreLinkRequest request =
				new RestoreLinkRequest(null);

		when(linkRepository.findByIdAndDeletedTrue(linkId))
				.thenReturn(Optional.empty());

		ResourceNotFoundException exception =
				assertThrows(
						ResourceNotFoundException.class,
						() -> linkService.restoreLink(
								linkId,
								request,
								principal
						)
				);

		assertEquals(
				"No such link",
				exception.getMessage()
		);

		verify(linkRepository)
				.findByIdAndDeletedTrue(linkId);
	}

	@Test
	void shouldRejectRestoreForNonOwner() {

		Long linkId = 1L;

		User owner = mock(User.class);
		User currentUser = mock(User.class);

		UUID ownerId = UUID.randomUUID();
		UUID currentUserId = UUID.randomUUID();

		when(owner.getId()).thenReturn(ownerId);
		when(currentUser.getId()).thenReturn(currentUserId);
		when(principal.getUser()).thenReturn(currentUser);

		Link link = new Link("https://example.com");
		link.assignUser(owner);
		link.delete();

		RestoreLinkRequest request =
				new RestoreLinkRequest(null);

		when(linkRepository.findByIdAndDeletedTrue(linkId))
				.thenReturn(Optional.of(link));

		ForbiddenException exception =
				assertThrows(
						ForbiddenException.class,
						() -> linkService.restoreLink(
								linkId,
								request,
								principal
						)
				);

		assertEquals(
				"You are not allowed to access this resource",
				exception.getMessage()
		);

		assertTrue(link.isDeleted());
	}

	@Test
	void shouldRejectRestoreWithPastExpiration() {

		Long linkId = 1L;

		User owner = mock(User.class);

		UUID ownerId = UUID.randomUUID();

		when(owner.getId()).thenReturn(ownerId);
		when(principal.getUser()).thenReturn(owner);

		Link link = new Link("https://example.com");
		link.assignUser(owner);
		link.delete();

		Instant expiredAt =
				Instant.now().minusSeconds(60);

		RestoreLinkRequest request =
				new RestoreLinkRequest(expiredAt);

		when(linkRepository.findByIdAndDeletedTrue(linkId))
				.thenReturn(Optional.of(link));

		BadRequestException exception =
				assertThrows(
						BadRequestException.class,
						() -> linkService.restoreLink(
								linkId,
								request,
								principal
						)
				);

		assertEquals(
				"Expiration time must be in the future.",
				exception.getMessage()
		);

		assertTrue(link.isDeleted());
	}

	@Test
	void shouldRestoreLinkWithoutExpiration() {

		Long linkId = 1L;

		User owner = mock(User.class);

		UUID ownerId = UUID.randomUUID();

		when(owner.getId()).thenReturn(ownerId);
		when(principal.getUser()).thenReturn(owner);

		Link link = new Link("https://example.com");
		link.assignUser(owner);
		link.delete();

		RestoreLinkRequest request =
				new RestoreLinkRequest(null);

		when(linkRepository.findByIdAndDeletedTrue(linkId))
				.thenReturn(Optional.of(link));

		linkService.restoreLink(
				linkId,
				request,
				principal
		);

		assertFalse(link.isDeleted());
		assertNull(link.getExpiresAt());
		assertNull(link.getDeletedAt());

		verify(linkRepository)
				.findByIdAndDeletedTrue(linkId);
	}

	@Test
	void shouldRestoreLinkWithNewExpiration() {

		Long linkId = 1L;

		User owner = mock(User.class);

		UUID ownerId = UUID.randomUUID();

		when(owner.getId()).thenReturn(ownerId);
		when(principal.getUser()).thenReturn(owner);

		Link link = new Link("https://example.com");
		link.assignUser(owner);
		link.delete();

		Instant newExpiration =
				Instant.now().plusSeconds(3600);

		RestoreLinkRequest request =
				new RestoreLinkRequest(newExpiration);

		when(linkRepository.findByIdAndDeletedTrue(linkId))
				.thenReturn(Optional.of(link));

		linkService.restoreLink(
				linkId,
				request,
				principal
		);

		assertFalse(link.isDeleted());
		assertNull(link.getDeletedAt());

		assertEquals(
				newExpiration,
				link.getExpiresAt()
		);

		verify(linkRepository)
				.findByIdAndDeletedTrue(linkId);
	}

	@Test
	void shouldRejectPermanentDeleteForNonOwner() {

		Long linkId = 1L;

		User owner = mock(User.class);
		User currentUser = mock(User.class);

		UUID ownerId = UUID.randomUUID();
		UUID currentUserId = UUID.randomUUID();

		when(owner.getId()).thenReturn(ownerId);
		when(currentUser.getId()).thenReturn(currentUserId);
		when(principal.getUser()).thenReturn(currentUser);

		Link link = new Link("https://example.com");
		link.assignUser(owner);
		link.delete();

		when(linkRepository.findByIdAndDeletedTrue(linkId))
				.thenReturn(Optional.of(link));

		ForbiddenException exception =
				assertThrows(
						ForbiddenException.class,
						() -> linkService.deleteLinkPermanently(
								linkId,
								principal
						)
				);

		assertEquals(
				"You are not allowed to access this resource",
				exception.getMessage()
		);

		verify(linkRepository, never())
				.delete(any());
	}

	@Test
	void shouldPermanentlyDeleteLinkForOwner() {

		Long linkId = 1L;

		User owner = mock(User.class);

		UUID ownerId = UUID.randomUUID();

		when(owner.getId()).thenReturn(ownerId);
		when(principal.getUser()).thenReturn(owner);

		Link link = new Link("https://example.com");
		link.assignUser(owner);
		link.delete();

		when(linkRepository.findByIdAndDeletedTrue(linkId))
				.thenReturn(Optional.of(link));

		linkService.deleteLinkPermanently(
				linkId,
				principal
		);

		verify(linkRepository)
				.findByIdAndDeletedTrue(linkId);

		verify(linkRepository)
				.delete(link);
	}

	@Test
	void shouldCreatePublicLink() {

		CreateLinkRequest request =
				new CreateLinkRequest(
						"https://example.com",
						null,
						null
				);

		Link link =
				new Link("https://example.com");

		LinkResponse expectedResponse =
				new LinkResponse(
						1L,
						"https://example.com",
						"pub123",
						"http://localhost:8080/pub123",
						Instant.now(),
						Instant.now().plusSeconds(86400)
				);

		when(shortCodeGenerator.generate())
				.thenReturn("pub123");

		when(linkRepository.existsByShortCode("pub123"))
				.thenReturn(false);

		when(linkRepository.save(any(Link.class)))
				.thenReturn(link);

		when(linkMapper.toResponse(
				any(Link.class),
				eq("http://localhost:8080")
		)).thenReturn(expectedResponse);

		LinkResponse response =
				linkService.createPublicLink(request);

		assertSame(expectedResponse, response);

		verify(shortCodeGenerator).generate();
		verify(linkRepository).existsByShortCode("pub123");
		verify(linkRepository).save(any(Link.class));
		verify(linkMapper).toResponse(any(Link.class), eq("http://localhost:8080"));
	}

	@Test
	void shouldRestoreLinkForOwner() {

		Long linkId = 1L;
		User owner = mock(User.class);
		UUID ownerId = UUID.randomUUID();

		when(owner.getId()).thenReturn(ownerId);
		when(principal.getUser()).thenReturn(owner);

		Link link = new Link("https://example.com");
		link.assignUser(owner);

		when(linkRepository.findByIdAndDeletedTrue(linkId))
				.thenReturn(Optional.of(link));

		RestoreLinkRequest request = new RestoreLinkRequest(null);

		linkService.restoreLink(linkId, request, principal);

		assertFalse(link.isDeleted());
		verify(linkRepository).findByIdAndDeletedTrue(linkId);
	}

	@Test
	void shouldRejectRestoringExpiredLink() {

		Long linkId = 1L;
		User owner = mock(User.class);
		UUID ownerId = UUID.randomUUID();

		when(owner.getId()).thenReturn(ownerId);
		when(principal.getUser()).thenReturn(owner);

		Link link = new Link("https://example.com");
		link.assignUser(owner);
		link.changeExpiresAt(Instant.now().minusSeconds(3600));

		when(linkRepository.findByIdAndDeletedTrue(linkId))
				.thenReturn(Optional.of(link));

		RestoreLinkRequest request = new RestoreLinkRequest(null);

		BadRequestException exception = assertThrows(
				BadRequestException.class,
				() -> linkService.restoreLink(linkId, request, principal)
		);

		assertEquals("Expired links cannot be restored.", exception.getMessage());
	}

}
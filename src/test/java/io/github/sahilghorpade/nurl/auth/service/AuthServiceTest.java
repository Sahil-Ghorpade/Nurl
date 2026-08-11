package io.github.sahilghorpade.nurl.auth.service;

import io.github.sahilghorpade.nurl.auth.dto.LoginRequest;
import io.github.sahilghorpade.nurl.auth.dto.LoginResult;
import io.github.sahilghorpade.nurl.auth.entity.RefreshToken;
import io.github.sahilghorpade.nurl.auth.jwt.JwtService;
import io.github.sahilghorpade.nurl.common.exception.UnauthorizedException;
import io.github.sahilghorpade.nurl.user.entity.User;
import io.github.sahilghorpade.nurl.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtService jwtService;

	@Mock
	private RefreshTokenService refreshTokenService;

	private AuthService authService;

	@BeforeEach
	void setUp() {
		authService = new AuthService(
				userRepository,
				passwordEncoder,
				jwtService,
				refreshTokenService
		);
	}

	@Test
	void shouldLoginSuccessfullyWithValidCredentials() {
		LoginRequest request = new LoginRequest("user@example.com", "password123");

		User user = mock(User.class);
		when(user.getPassword()).thenReturn("hashedPassword");

		when(userRepository.findByEmail("user@example.com"))
				.thenReturn(Optional.of(user));

		when(passwordEncoder.matches("password123", "hashedPassword"))
				.thenReturn(true);

		when(jwtService.generateAccessToken(user))
				.thenReturn("access-token-123");

		when(refreshTokenService.createRefreshToken(user))
				.thenReturn("refresh-token-456");

		LoginResult result = authService.login(request);

		assertNotNull(result);
		assertEquals("access-token-123", result.accessToken());
		assertEquals("refresh-token-456", result.refreshToken());

		verify(userRepository).findByEmail("user@example.com");
		verify(passwordEncoder).matches("password123", "hashedPassword");
		verify(jwtService).generateAccessToken(user);
		verify(refreshTokenService).createRefreshToken(user);
	}

	@Test
	void shouldThrowUnauthorizedExceptionWhenUserNotFound() {
		LoginRequest request = new LoginRequest("nonexistent@example.com", "password123");

		when(userRepository.findByEmail("nonexistent@example.com"))
				.thenReturn(Optional.empty());

		UnauthorizedException exception = assertThrows(
				UnauthorizedException.class,
				() -> authService.login(request)
		);

		assertEquals("Invalid credentials provided", exception.getMessage());
		verify(passwordEncoder, never()).matches(anyString(), anyString());
	}

	@Test
	void shouldThrowUnauthorizedExceptionWhenPasswordMismatch() {
		LoginRequest request = new LoginRequest("user@example.com", "wrongpassword");

		User user = mock(User.class);
		when(user.getPassword()).thenReturn("hashedPassword");

		when(userRepository.findByEmail("user@example.com"))
				.thenReturn(Optional.of(user));

		when(passwordEncoder.matches("wrongpassword", "hashedPassword"))
				.thenReturn(false);

		UnauthorizedException exception = assertThrows(
				UnauthorizedException.class,
				() -> authService.login(request)
		);

		assertEquals("Invalid credentials provided", exception.getMessage());
		verify(jwtService, never()).generateAccessToken(any());
	}

	@Test
	void shouldRefreshTokensSuccessfully() {
		String rawRefreshToken = "valid-refresh-token";
		RefreshToken tokenEntity = mock(RefreshToken.class);
		User user = mock(User.class);

		when(tokenEntity.getUser()).thenReturn(user);
		when(refreshTokenService.findValidToken(rawRefreshToken))
				.thenReturn(tokenEntity);

		when(jwtService.generateAccessToken(user))
				.thenReturn("new-access-token");

		when(refreshTokenService.createRefreshToken(user))
				.thenReturn("new-refresh-token");

		LoginResult result = authService.refresh(rawRefreshToken);

		assertNotNull(result);
		assertEquals("new-access-token", result.accessToken());
		assertEquals("new-refresh-token", result.refreshToken());

		verify(refreshTokenService).revoke(tokenEntity);
		verify(jwtService).generateAccessToken(user);
		verify(refreshTokenService).createRefreshToken(user);
	}

	@Test
	void shouldThrowUnauthorizedExceptionWhenRefreshTokenInvalid() {
		String invalidToken = "invalid-token";

		when(refreshTokenService.findValidToken(invalidToken))
				.thenReturn(null);

		UnauthorizedException exception = assertThrows(
				UnauthorizedException.class,
				() -> authService.refresh(invalidToken)
		);

		assertEquals("Invalid refresh token", exception.getMessage());
		verify(refreshTokenService, never()).revoke(any());
	}

	@Test
	void shouldLogoutSuccessfully() {
		String rawRefreshToken = "valid-token";
		RefreshToken tokenEntity = mock(RefreshToken.class);

		when(refreshTokenService.findValidToken(rawRefreshToken))
				.thenReturn(tokenEntity);

		authService.logout(rawRefreshToken);

		verify(refreshTokenService).revoke(tokenEntity);
	}

	@Test
	void shouldDoNothingOnLogoutWithBlankToken() {
		authService.logout("");
		authService.logout(null);

		verify(refreshTokenService, never()).findValidToken(anyString());
		verify(refreshTokenService, never()).revoke(any());
	}
}

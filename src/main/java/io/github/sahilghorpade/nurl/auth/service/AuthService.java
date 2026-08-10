package io.github.sahilghorpade.nurl.auth.service;

import io.github.sahilghorpade.nurl.auth.dto.LoginRequest;
import io.github.sahilghorpade.nurl.auth.dto.LoginResponse;
import io.github.sahilghorpade.nurl.auth.dto.LoginResult;
import io.github.sahilghorpade.nurl.auth.entity.RefreshToken;
import io.github.sahilghorpade.nurl.auth.jwt.JwtService;
import io.github.sahilghorpade.nurl.common.exception.UnauthorizedException;
import io.github.sahilghorpade.nurl.user.entity.User;
import io.github.sahilghorpade.nurl.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;

	public AuthService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			RefreshTokenService refreshTokenService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.refreshTokenService = refreshTokenService;
	}

	@Transactional
	public LoginResult login(LoginRequest loginRequest) {

		User user = userRepository
				.findByEmail(loginRequest.email())
				.orElseThrow(() ->
						new UnauthorizedException(
								"Invalid credentials provided"
						));

		if(!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
			throw new UnauthorizedException(
					"Invalid credentials provided"
			);
		}

		String accessToken =
				jwtService.generateAccessToken(user);

		String refreshToken =
				refreshTokenService.createRefreshToken(user);

		return new LoginResult(
				accessToken,
				refreshToken
		);
	}

	@Transactional
	public LoginResult refresh(String rawRefreshToken) {

		RefreshToken refreshToken =
				refreshTokenService.findValidToken(rawRefreshToken);

		if (refreshToken == null) {
			throw new UnauthorizedException(
					"Invalid refresh token"
			);
		}

		User user = refreshToken.getUser();

		refreshTokenService.revoke(refreshToken);

		String accessToken =
				jwtService.generateAccessToken(user);

		String newRefreshToken =
				refreshTokenService.createRefreshToken(user);

		return new LoginResult(
				accessToken,
				newRefreshToken
		);
	}

	@Transactional
	public void logout(String rawRefreshToken) {

		if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
			return;
		}

		RefreshToken refreshToken =
				refreshTokenService.findValidToken(rawRefreshToken);

		if (refreshToken != null) {
			refreshTokenService.revoke(refreshToken);
		}
	}
}

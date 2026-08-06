package io.github.sahilghorpade.nurl.auth.service;

import io.github.sahilghorpade.nurl.auth.dto.LoginRequest;
import io.github.sahilghorpade.nurl.auth.dto.LoginResponse;
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

	public AuthService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest loginRequest) {

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

		return new LoginResponse(
				accessToken,
				"Bearer"
		);
	}
}

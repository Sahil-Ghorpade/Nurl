	package io.github.sahilghorpade.nurl.auth.controller;

	import io.github.sahilghorpade.nurl.auth.config.AuthCookieProperties;
	import io.github.sahilghorpade.nurl.auth.config.AuthTokenProperties;
	import io.github.sahilghorpade.nurl.auth.dto.LoginRequest;
	import io.github.sahilghorpade.nurl.auth.dto.LoginResponse;
	import io.github.sahilghorpade.nurl.auth.dto.LoginResult;
	import io.github.sahilghorpade.nurl.auth.service.AuthCookieService;
	import io.github.sahilghorpade.nurl.auth.service.AuthService;
	import io.github.sahilghorpade.nurl.common.response.ApiResponse;
	import io.github.sahilghorpade.nurl.common.response.ResponseFactory;
	import io.github.sahilghorpade.nurl.user.dto.request.RegisterRequest;
	import io.github.sahilghorpade.nurl.user.dto.response.UserResponse;
	import io.github.sahilghorpade.nurl.user.service.UserService;
	import jakarta.validation.Valid;
	import org.springframework.http.HttpHeaders;
	import org.springframework.http.HttpStatus;
	import org.springframework.http.ResponseCookie;
	import org.springframework.http.ResponseEntity;
	import org.springframework.web.bind.annotation.PostMapping;
	import org.springframework.web.bind.annotation.RequestBody;
	import org.springframework.web.bind.annotation.RequestMapping;
	import org.springframework.web.bind.annotation.RestController;
	import io.github.sahilghorpade.nurl.common.exception.UnauthorizedException;
	import org.springframework.web.bind.annotation.CookieValue;

	@RestController
	@RequestMapping("/auth")
	public class AuthController {

		private final UserService userService;
		private final AuthService  authService;
		private final AuthCookieService authCookieService;
		private final AuthTokenProperties authTokenProperties;

		public AuthController(
				UserService userService,
				AuthService authService,
				AuthCookieService authCookieService,
				AuthTokenProperties authTokenProperties
		) {
			this.userService = userService;
			this.authService = authService;
			this.authCookieService = authCookieService;
			this.authTokenProperties = authTokenProperties;
		}

		@PostMapping("/register")
		public ResponseEntity<ApiResponse<UserResponse>> registerUser(
				@Valid
				@RequestBody
				RegisterRequest registerRequest
		) {
			UserResponse userResponse =
					userService.register(registerRequest);

			return ResponseEntity
					.status(HttpStatus.CREATED)
					.body(ResponseFactory.success(
							"Account created successfully",
							userResponse));
		}

		@PostMapping("/login")
		public ResponseEntity<ApiResponse<LoginResponse>> loginUser(
				@Valid
				@RequestBody
				LoginRequest loginRequest
		) {
			LoginResult loginResult =
					authService.login(loginRequest);

			ResponseCookie accessCookie =
					authCookieService.createAccessCookie(
							loginResult.accessToken(),
							authTokenProperties.accessTokenExpiration() / 1000
					);

			ResponseCookie refreshCookie =
					authCookieService.createRefreshCookie(
							loginResult.refreshToken(),
							authTokenProperties.refreshTokenExpiration() / 1000
					);

			return ResponseEntity
					.ok()
					.header(
							HttpHeaders.SET_COOKIE,
							accessCookie.toString()
					)
					.header(
							HttpHeaders.SET_COOKIE,
							refreshCookie.toString()
					)
					.body(
							ResponseFactory.success(
									"Login Successful.",
									new LoginResponse(
											loginResult.accessToken(),
											"Bearer"
									)
							)
					);
		}

		@PostMapping("/refresh")
		public ResponseEntity<ApiResponse<Void>> refresh(
				@CookieValue(
						name = "nurl_refresh_token",
						required = false
				)
				String refreshToken
		) {

			if (refreshToken == null || refreshToken.isBlank()) {
				throw new UnauthorizedException(
						"Refresh token required"
				);
			}

			LoginResult loginResult =
					authService.refresh(refreshToken);

			ResponseCookie accessCookie =
					authCookieService.createAccessCookie(
							loginResult.accessToken(),
							authTokenProperties.accessTokenExpiration() / 1000
					);

			ResponseCookie refreshCookie =
					authCookieService.createRefreshCookie(
							loginResult.refreshToken(),
							authTokenProperties.refreshTokenExpiration() / 1000
					);

			return ResponseEntity
					.ok()
					.header(
							HttpHeaders.SET_COOKIE,
							accessCookie.toString()
					)
					.header(
							HttpHeaders.SET_COOKIE,
							refreshCookie.toString()
					)
					.body(
							ResponseFactory.success(
									"Token refreshed successfully",
									null
							)
					);
		}

		@PostMapping("/logout")
		public ResponseEntity<ApiResponse<Void>> logout(
				@CookieValue(
						name = "nurl_refresh_token",
						required = false
				)
				String refreshToken
		) {

			authService.logout(refreshToken);

			ResponseCookie accessCookie =
					authCookieService.clearAccessCookie();

			ResponseCookie refreshCookie =
					authCookieService.clearRefreshCookie();

			return ResponseEntity
					.ok()
					.header(
							HttpHeaders.SET_COOKIE,
							accessCookie.toString()
					)
					.header(
							HttpHeaders.SET_COOKIE,
							refreshCookie.toString()
					)
					.body(
							ResponseFactory.success(
									"Logout successful",
									null
							)
					);
		}
	}

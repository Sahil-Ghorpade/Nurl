	package io.github.sahilghorpade.nurl.auth.controller;

	import io.github.sahilghorpade.nurl.auth.dto.LoginRequest;
	import io.github.sahilghorpade.nurl.auth.dto.LoginResponse;
	import io.github.sahilghorpade.nurl.auth.service.AuthService;
	import io.github.sahilghorpade.nurl.common.response.ApiResponse;
	import io.github.sahilghorpade.nurl.common.response.ResponseFactory;
	import io.github.sahilghorpade.nurl.user.dto.request.RegisterRequest;
	import io.github.sahilghorpade.nurl.user.dto.response.UserResponse;
	import io.github.sahilghorpade.nurl.user.service.UserService;
	import jakarta.validation.Valid;
	import org.springframework.http.HttpStatus;
	import org.springframework.http.ResponseEntity;
	import org.springframework.security.crypto.password.PasswordEncoder;
	import org.springframework.web.bind.annotation.PostMapping;
	import org.springframework.web.bind.annotation.RequestBody;
	import org.springframework.web.bind.annotation.RequestMapping;
	import org.springframework.web.bind.annotation.RestController;

	@RestController
	@RequestMapping("/auth")
	public class AuthController {

		private final UserService userService;
		private final AuthService  authService;

		public AuthController(
				UserService userService,
				AuthService authService
		) {
			this.userService = userService;
			this.authService = authService;
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
			LoginResponse loginResponse = authService.login(loginRequest);

			return ResponseEntity
					.ok(
						ResponseFactory.success(
								"Login successful.",
								loginResponse
						)
					);
		}
	}

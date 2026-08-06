package io.github.sahilghorpade.nurl.user.controller;


import io.github.sahilghorpade.nurl.common.response.ApiResponse;
import io.github.sahilghorpade.nurl.common.response.ResponseFactory;
import io.github.sahilghorpade.nurl.user.dto.request.RegisterRequest;
import io.github.sahilghorpade.nurl.user.dto.response.UserResponse;
import io.github.sahilghorpade.nurl.user.entity.User;
import io.github.sahilghorpade.nurl.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

	private UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
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
}

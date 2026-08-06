package io.github.sahilghorpade.nurl.common.exception;

import io.github.sahilghorpade.nurl.common.response.ApiResponse;
import io.github.sahilghorpade.nurl.common.response.ResponseFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
			ResourceNotFoundException exception
	) {
		return ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(ResponseFactory.failure(exception.getMessage()));
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ApiResponse<Void>> handleConflictException(
			ConflictException exception
	) {
		return ResponseEntity
				.status(HttpStatus.CONFLICT)
				.body(ResponseFactory.failure(exception.getMessage()));
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ApiResponse<Void>> handleBadRequestException(
			BadRequestException exception
	) {
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ResponseFactory.failure(exception.getMessage()));
	}

	@ExceptionHandler(ForbiddenException.class)
	public ResponseEntity<ApiResponse<Void>> handleForbiddenException(
			ForbiddenException exception
	) {
		return ResponseEntity
				.status(HttpStatus.FORBIDDEN)
				.body(ResponseFactory.failure(exception.getMessage()));
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ApiResponse<Void>> handleUnauthorizedException(
			UnauthorizedException exception
	) {
		return ResponseEntity
				.status(HttpStatus.UNAUTHORIZED)
				.body(ResponseFactory.failure(exception.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
			MethodArgumentNotValidException exception
	) {

		String message = "Validation failed";

		if (!exception.getBindingResult().getFieldErrors().isEmpty()) {
			message = exception
					.getBindingResult()
					.getFieldErrors()
					.getFirst()
					.getDefaultMessage();
		}

		return ResponseEntity
				.badRequest()
				.body(ResponseFactory.failure(message));

	}
}

package io.github.sahilghorpade.nurl.common.exception;

import io.github.sahilghorpade.nurl.common.response.ApiResponse;
import io.github.sahilghorpade.nurl.common.response.ResponseFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
			ResourceNotFoundException exception
	) {
		return ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(ResponseFactory.failure(exception.getMessage()));
	}
}

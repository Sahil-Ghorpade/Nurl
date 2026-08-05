package io.github.sahilghorpade.nurl.common.health.controller;


import io.github.sahilghorpade.nurl.common.health.dto.HealthResponse;
import io.github.sahilghorpade.nurl.common.health.service.HealthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

	private final HealthService healthService;

	public HealthController(HealthService healthService) {
		this.healthService = healthService;
	}

	@GetMapping("/health")
	public ResponseEntity<HealthResponse> health() {
		HealthResponse response = healthService.getHealth();

		return ResponseEntity.ok(response);
	}
}

package io.github.sahilghorpade.nurl.common.health.service;

import io.github.sahilghorpade.nurl.common.health.dto.HealthResponse;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

	public HealthResponse getHealth() {
		return new HealthResponse(
				"UP",
				"Nurl",
				"1.0.0"
		);
	}
}

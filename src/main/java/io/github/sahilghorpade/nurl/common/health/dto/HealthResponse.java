package io.github.sahilghorpade.nurl.common.health.dto;

public record HealthResponse (
		String status,
		String application,
		String version
) {
}
package io.github.sahilghorpade.nurl.dashboard.controller;

import io.github.sahilghorpade.nurl.auth.security.UserPrincipal;
import io.github.sahilghorpade.nurl.common.response.ApiResponse;
import io.github.sahilghorpade.nurl.common.response.ResponseFactory;
import io.github.sahilghorpade.nurl.dashboard.dto.response.DashboardResponse;
import io.github.sahilghorpade.nurl.dashboard.service.DashboardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController {

	private final DashboardService dashboardService;

	public DashboardController(
			DashboardService dashboardService
	) {
		this.dashboardService = dashboardService;
	}

	@GetMapping("/dashboard")
	public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
			@AuthenticationPrincipal UserPrincipal principal
	) {

		DashboardResponse response =
				dashboardService.getDashboard(principal.getUser());

		return ResponseEntity
				.status(HttpStatus.OK)
				.body(
						ResponseFactory.success(
								"Dashboard retrieved successfully.",
								response
						)
				);
	}
}
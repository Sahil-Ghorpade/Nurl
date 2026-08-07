package io.github.sahilghorpade.nurl.link.controller;

import io.github.sahilghorpade.nurl.auth.security.UserPrincipal;
import io.github.sahilghorpade.nurl.common.response.ApiResponse;
import io.github.sahilghorpade.nurl.common.response.ResponseFactory;
import io.github.sahilghorpade.nurl.link.dto.request.CreateLinkRequest;
import io.github.sahilghorpade.nurl.link.dto.response.LinkResponse;
import io.github.sahilghorpade.nurl.link.service.LinkService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
public class LinkController {

	private final LinkService linkService;

	public LinkController(LinkService linkService) {
		this.linkService = linkService;
	}

	//Create Link
	@PostMapping("/link")
	public ResponseEntity<ApiResponse<LinkResponse>> createLink(
			@Valid
			@RequestBody
			CreateLinkRequest request,
			@AuthenticationPrincipal UserPrincipal principal
	) {
		LinkResponse response = linkService.createLink(request, principal);

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ResponseFactory.success(
						"Short URL created successfully",
						response
				));
	}

	//Redirect to original link
	@GetMapping("/{shortCode}")
	public ResponseEntity<Void> redirectLink(
			@PathVariable
			String shortCode
	) {
		String originalUrl = linkService.redirectLink(shortCode);

		return ResponseEntity
				.status(HttpStatus.FOUND)
				.location(URI.create(originalUrl))
				.build();
	}

	//Analyze a link
	@GetMapping("/link/{id}")
	public ResponseEntity<ApiResponse<LinkResponse>> getAnalysis(
			@PathVariable
			Long id,
			@AuthenticationPrincipal UserPrincipal principal
	) {
		LinkResponse response =
				linkService.getAnalytics(id, principal);

		return ResponseEntity
				.status(HttpStatus.OK)
				.body(ResponseFactory.success(
						"Link retrieved successfully",
						response
				));
	}

	//Get all link
	@GetMapping("/links")
	public ResponseEntity<ApiResponse<Page<LinkResponse>>> getLinks(
			@PageableDefault(
					size = 10,
					sort = "createdAt",
					direction = Sort.Direction.DESC
			)
			Pageable pageable,

			@AuthenticationPrincipal
			UserPrincipal principal
	) {
		Page<LinkResponse>  response =
				linkService.getLinks(pageable, principal);

		return ResponseEntity
				.status(HttpStatus.OK)
				.body(ResponseFactory.success(
						"Links retrieved successfully",
						response
				));
	}

	//Soft-delete link
	@DeleteMapping("/link/{id}")
	public ResponseEntity<Void> deleteLink(
			@PathVariable Long id,
			@AuthenticationPrincipal UserPrincipal principal
	) {
		linkService.deleteLink(id, principal);

		return ResponseEntity.
				noContent().
				build();
	}
}

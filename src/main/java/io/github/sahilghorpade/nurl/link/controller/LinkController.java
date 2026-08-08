package io.github.sahilghorpade.nurl.link.controller;

import io.github.sahilghorpade.nurl.auth.security.UserPrincipal;
import io.github.sahilghorpade.nurl.common.response.ApiResponse;
import io.github.sahilghorpade.nurl.common.response.ResponseFactory;
import io.github.sahilghorpade.nurl.link.dto.request.CreateLinkRequest;
import io.github.sahilghorpade.nurl.link.dto.request.RestoreLinkRequest;
import io.github.sahilghorpade.nurl.link.dto.request.UpdateLinkRequest;
import io.github.sahilghorpade.nurl.link.dto.response.LinkResponse;
import io.github.sahilghorpade.nurl.link.qr.QrCodeService;
import io.github.sahilghorpade.nurl.link.service.LinkService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
public class LinkController {

	private final LinkService linkService;
	private final QrCodeService qrCodeService;

	public LinkController(
			LinkService linkService,
			QrCodeService qrCodeService
	) {
		this.linkService = linkService;
		this.qrCodeService = qrCodeService;
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

	// Get all deleted link
	@GetMapping("links/deleted")
	public ResponseEntity<ApiResponse<Page<LinkResponse>>> getDeletedLink(
			@PageableDefault(
					size = 10,
					sort = "deletedAt",
					direction = Sort.Direction.DESC
			)
			Pageable pageable,

			@AuthenticationPrincipal
			UserPrincipal principal
	) {
		Page<LinkResponse>  response =
				linkService.getDeletedLinks(pageable, principal);

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

	//Hard-delete link
	@DeleteMapping("/link/delete/{id}")
	public ResponseEntity<Void> deleteLinkPermanently(
			@PathVariable
			Long id,
			@AuthenticationPrincipal UserPrincipal principal
	) {
		linkService.deleteLinkPermanently(id, principal);

		return ResponseEntity.
				noContent().
				build();
	}

	//Restore link
	@PatchMapping("/link/restore/{id}")
	public ResponseEntity<Void> restoreLink(
			@PathVariable Long id,
			@RequestBody RestoreLinkRequest request,
			@AuthenticationPrincipal UserPrincipal principal
	) {
		linkService.restoreLink(id, request, principal);

		return ResponseEntity
				.noContent()
				.build();
	}

	//Update link
	@PatchMapping("/link/{id}")
	public ResponseEntity<ApiResponse<LinkResponse>> updateLink(
			@PathVariable Long id,
			@Valid @RequestBody UpdateLinkRequest request,
			@AuthenticationPrincipal UserPrincipal principal
	) {
		LinkResponse response = linkService.updateLink(id, request, principal);

		return ResponseEntity
				.status(HttpStatus.OK)
				.body(ResponseFactory.success(
					"Link updated successfully",
						response
				));
	}

	//Generate QR
	@GetMapping("/link/qr/{id}")
	public ResponseEntity<byte[]> getQR(
			@PathVariable Long id,
			@AuthenticationPrincipal UserPrincipal principal
	) {
		byte[] qr =
				qrCodeService.generateQrCode(id, principal);

		return ResponseEntity
				.status(HttpStatus.OK)
				.header(
						"Content-Disposition",
						"inline; filename=\"qr.jpg\""
				)
				.contentType(MediaType.IMAGE_PNG)
				.body(qr);
	}
}
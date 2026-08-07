package io.github.sahilghorpade.nurl.link.mapper;

import io.github.sahilghorpade.nurl.link.dto.request.CreateLinkRequest;
import io.github.sahilghorpade.nurl.link.dto.response.LinkResponse;
import io.github.sahilghorpade.nurl.link.entity.Link;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class LinkMapper {

	@Value("${app.base-url}")
	private String baseUrl;

	public LinkResponse toResponse(Link link) {

		return new LinkResponse(
				link.getId(),
				link.getOriginalUrl(),
				link.getShortCode(),
				baseUrl + "/" + link.getShortCode(),
				link.getCreatedAt()
		);
	}

	public Link toEntity(CreateLinkRequest request) {
		return new Link(request.originalUrl());
	}

	public LinkResponse toResponse(
			Link link,
			String baseUrl
	) {
		return new LinkResponse(
				link.getId(),
				link.getOriginalUrl(),
				link.getShortCode(),
				baseUrl + "/" + link.getShortCode(),
				link.getCreatedAt()
		);
	}
}

package io.github.sahilghorpade.nurl.link.qr;

import io.github.sahilghorpade.nurl.auth.security.UserPrincipal;
import io.github.sahilghorpade.nurl.common.exception.ForbiddenException;
import io.github.sahilghorpade.nurl.common.exception.ResourceNotFoundException;
import io.github.sahilghorpade.nurl.link.entity.Link;
import io.github.sahilghorpade.nurl.link.repository.LinkRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

@Service
public class QrCodeService {

	@Value("${app.base-url}")
	private String baseUrl;

	private final LinkRepository linkRepository;
	private final QrCodeGenerator generator;

	public QrCodeService(
			LinkRepository linkRepository,
			QrCodeGenerator generator
	) {
		this.linkRepository = linkRepository;
		this.generator = generator;
	}

	@Transactional(readOnly = true)
	public byte[] generateQrCode(
			Long id,
			UserPrincipal principal
	) {

		Link link = linkRepository
						.findByIdAndDeletedFalse(id)
						.orElseThrow(() ->
									new ResourceNotFoundException(
											"No such link."
									)
								);

		if (!link.getUser().getId().equals(principal.getUser().getId())) {
			throw new ForbiddenException(
					"You are not allowed to access this resource."
			);
		}

		String shortUrl =
				baseUrl + "/" + link.getShortCode();

		return generator.generateQR(shortUrl);
	}
}

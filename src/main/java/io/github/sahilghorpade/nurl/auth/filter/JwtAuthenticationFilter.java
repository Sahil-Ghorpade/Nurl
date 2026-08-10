package io.github.sahilghorpade.nurl.auth.filter;

import io.github.sahilghorpade.nurl.auth.jwt.JwtService;
import io.github.sahilghorpade.nurl.auth.security.CustomUserDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final CustomUserDetailsService userDetailsService;

	public JwtAuthenticationFilter(
			JwtService jwtService,
			CustomUserDetailsService userDetailsService
	) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}

	private void sendUnauthorized(
			HttpServletResponse response
	) throws IOException {

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json");

		response.getWriter().write("""
            {
                "success": false,
                "message": "Authentication required.",
                "data": null
            }
            """);
	}

	private String extractTokenfromCookie(
			HttpServletRequest request
	) {
		Cookie[] cookies = request.getCookies();

		if (cookies == null) {
			return null;
		}

		for (Cookie cookie : request.getCookies()) {
			if ("nurl_access_token".equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {

		String jwt = extractTokenfromCookie(request);

		if (jwt == null || jwt.isBlank()) {
			filterChain.doFilter(request, response);
			return;
		}

		try {

			if (!jwtService.isTokenValid(jwt)) {

				sendUnauthorized(response);
				return;
			}

			String email = jwtService.extractSubject(jwt);

			UserDetails userPrincipal =
					userDetailsService.loadUserByUsername(email);

			UsernamePasswordAuthenticationToken authentication =
					new UsernamePasswordAuthenticationToken(
							userPrincipal,
							null,
							userPrincipal.getAuthorities()
					);

			SecurityContextHolder
					.getContext()
					.setAuthentication(authentication);

			filterChain.doFilter(request, response);
		}
		catch (JwtException | UsernameNotFoundException exception) {
			SecurityContextHolder.clearContext();

			sendUnauthorized(response);
		}


	}
}
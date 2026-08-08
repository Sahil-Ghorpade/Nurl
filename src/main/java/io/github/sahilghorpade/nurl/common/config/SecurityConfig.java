package io.github.sahilghorpade.nurl.common.config;

import io.github.sahilghorpade.nurl.auth.filter.JwtAuthenticationFilter;
import io.github.sahilghorpade.nurl.auth.security.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;
import java.util.List;


@Configuration
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final CustomUserDetailsService customUserDetailsService;
	@Value("${app.frontend-url}")
	private String frontendUrl;

	public SecurityConfig(
			JwtAuthenticationFilter jwtAuthenticationFilter,
			CustomUserDetailsService customUserDetailsService
	) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.customUserDetailsService = customUserDetailsService;
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {

		CorsConfiguration configuration =
				new CorsConfiguration();

		configuration.setAllowedOrigins(
				List.of(frontendUrl)
		);

		configuration.setAllowedMethods(
				List.of(
						"GET",
						"POST",
						"PUT",
						"DELETE",
						"OPTIONS"
				)
		);

		configuration.setAllowedHeaders(
				List.of(
						"Authorization",
						"Content-Type"
				)
		);

		configuration.setAllowCredentials(false);

		UrlBasedCorsConfigurationSource source =
				new UrlBasedCorsConfigurationSource();

		source.registerCorsConfiguration(
				"/**",
				configuration
		);

		return source;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http
				.csrf(csrf -> csrf.disable())

				.cors(cors -> {})

				.formLogin(form -> form.disable())

				.httpBasic(httpBasic -> httpBasic.disable())

				.sessionManagement(session ->
						session.sessionCreationPolicy(
								SessionCreationPolicy.STATELESS
						)
				)

				.authorizeHttpRequests(auth -> auth

						.requestMatchers("/auth/**")
						.permitAll()

						.requestMatchers("/api/health")
						.permitAll()

						.requestMatchers(
								"/link",
								"/links",
								"/dashboard"
						)
						.authenticated()

						.requestMatchers(
								HttpMethod.GET,
								"/{shortCode}"
						)
						.permitAll()

						.anyRequest()
						.authenticated()
				)

				.exceptionHandling(exception -> exception
						.authenticationEntryPoint(
								(request, response, authException) -> {

									response.setStatus(
											HttpServletResponse.SC_UNAUTHORIZED
									);

									response.setContentType(
											"application/json"
									);

									response.getWriter().write("""
								{
									"success": false,
									"message": "Authentication required.",
									"data": null
								}
								""");
								})
				)

				.addFilterBefore(
						jwtAuthenticationFilter,
						UsernamePasswordAuthenticationFilter.class
				);

		return http.build();
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {

		DaoAuthenticationProvider provider =
				new DaoAuthenticationProvider(customUserDetailsService);

		provider.setPasswordEncoder(
				passwordEncoder()
		);

		return provider;
	}

	@Bean
	public AuthenticationManager authenticationManager(
			AuthenticationConfiguration configuration
	) throws Exception {

		return configuration.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
package io.github.sahilghorpade.nurl.auth.security;

import io.github.sahilghorpade.nurl.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) {

		return userRepository
				.findByEmail(username)
				.map(UserPrincipal :: new)
				.orElseThrow(() ->
						new UsernameNotFoundException(
								"Invalid Credentials provided!"
						));
	}
}

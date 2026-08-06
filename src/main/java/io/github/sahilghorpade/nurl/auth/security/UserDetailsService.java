package io.github.sahilghorpade.nurl.auth.security;

import org.springframework.security.core.userdetails.UserDetails;

public interface UserDetailsService {

	UserDetails loadUserByUsername(String username);
}

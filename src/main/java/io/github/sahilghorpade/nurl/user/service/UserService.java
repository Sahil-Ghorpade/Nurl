package io.github.sahilghorpade.nurl.user.service;

import io.github.sahilghorpade.nurl.common.exception.ConflictException;
import io.github.sahilghorpade.nurl.user.dto.request.RegisterRequest;
import io.github.sahilghorpade.nurl.user.dto.response.UserResponse;
import io.github.sahilghorpade.nurl.user.entity.User;
import io.github.sahilghorpade.nurl.user.mapper.UserMapper;
import io.github.sahilghorpade.nurl.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UserService {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	public UserService(
			UserRepository userRepository,
			PasswordEncoder PasswordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = PasswordEncoder;
	}

	public UserResponse register(RegisterRequest registerRequest) {

		if (userRepository.existsByEmail(registerRequest.email())) {
			throw new ConflictException("Email already registered.");
		}

		User user = UserMapper.toEntity(registerRequest);

		user.changePassword(passwordEncoder.encode(registerRequest.password()));

		User savedUser = userRepository.save(user);

		return UserMapper.toResponse(savedUser);

	}

}

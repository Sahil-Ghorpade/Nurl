package io.github.sahilghorpade.nurl.user.mapper;

import io.github.sahilghorpade.nurl.user.dto.request.RegisterRequest;
import io.github.sahilghorpade.nurl.user.dto.response.UserResponse;
import io.github.sahilghorpade.nurl.user.entity.User;

public final class UserMapper {
	private UserMapper() {
	}

	public static User toEntity(
			RegisterRequest request
	) {
		return User.create(
				request.name(),
				request.email(),
				request.password()
		);
	}

	public static UserResponse toResponse(
			User user
	) {
		return new UserResponse(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getCreatedAt()
		);
	}
}

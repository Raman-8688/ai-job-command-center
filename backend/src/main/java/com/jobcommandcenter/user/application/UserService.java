package com.jobcommandcenter.user.application;

import com.jobcommandcenter.common.error.ResourceNotFoundException;
import com.jobcommandcenter.user.api.CurrentUserResponse;
import com.jobcommandcenter.user.domain.User;
import com.jobcommandcenter.user.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return new CurrentUserResponse(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getDisplayName(),
            user.getRole(),
            user.getAccountStatus(),
            user.getCreatedAt(),
            user.getLastLoginAt()
        );
    }
}

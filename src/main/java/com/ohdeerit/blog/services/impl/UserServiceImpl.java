package com.ohdeerit.blog.services.impl;

import com.ohdeerit.blog.repositories.UserRepository;
import com.ohdeerit.blog.domain.entities.UserEntity;
import jakarta.persistence.EntityNotFoundException;
import com.ohdeerit.blog.services.UserService;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserEntity getUser(UUID id) {

        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No user found with id: " + id));
    }
}

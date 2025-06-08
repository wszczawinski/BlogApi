package com.ohdeerit.blog.services;

import com.ohdeerit.blog.domain.entities.UserEntity;

import java.util.UUID;

public interface UserService {
    UserEntity getUser(UUID id);
}

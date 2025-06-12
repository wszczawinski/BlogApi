package com.ohdeerit.blog.services.interfaces;

import com.ohdeerit.blog.models.entities.UserEntity;

import java.util.UUID;

public interface UserService {
    UserEntity getUser(UUID id);
}

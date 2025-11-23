package com.ohdeerit.blog.security;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import com.ohdeerit.blog.repositories.UserRepository;
import com.ohdeerit.blog.models.entities.UserEntity;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("USER_NOT_FOUND | email={}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        return new BlogUserDetails(user);
    }
}

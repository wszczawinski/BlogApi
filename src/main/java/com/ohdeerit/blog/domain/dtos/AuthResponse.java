package com.ohdeerit.blog.domain.dtos;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private long expiresIn;

}

package com.ohdeerit.blog.domain.dtos;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    private String email;
    private String password;

}

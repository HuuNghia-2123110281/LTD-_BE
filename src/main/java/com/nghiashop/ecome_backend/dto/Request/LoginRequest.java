package com.nghiashop.ecome_backend.dto.Request;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}

package com.nghiashop.ecome_backend.dto.Request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String fullName;
    private String email;
    private String password;
}

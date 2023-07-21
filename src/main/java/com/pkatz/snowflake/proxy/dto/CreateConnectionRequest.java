package com.pkatz.snowflake.proxy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Optional;

public record CreateConnectionRequest(
        @NotBlank(message = "Account identifier cannot be blank")
        String account,
        @Email(message = "Username must be an email")
        String username,
        @NotBlank(message = "Password cannot be blank")
        String password,
        @NotBlank(message = "Role cannot be blank")
        String role,
        Optional<String> warehouse
) {
}

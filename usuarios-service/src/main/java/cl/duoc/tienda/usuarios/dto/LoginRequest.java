package cl.duoc.tienda.usuarios.dto;

import jakarta.validation.constraints.*;

public record LoginRequest(
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(max = 64) String password
) {}

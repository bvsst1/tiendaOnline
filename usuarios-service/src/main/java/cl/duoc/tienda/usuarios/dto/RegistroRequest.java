package cl.duoc.tienda.usuarios.dto;

import jakarta.validation.constraints.*;

public record RegistroRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre admite hasta 100 caracteres") String nombre,
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es válido")
        @Size(max = 150, message = "El correo admite hasta 150 caracteres") String email,
        @NotBlank(message = "La contraseña es obligatoria")
        @Pattern(regexp = "[\\x21-\\x7E]{8,64}", message = "Usa entre 8 y 64 caracteres ASCII sin espacios") String password
) {}

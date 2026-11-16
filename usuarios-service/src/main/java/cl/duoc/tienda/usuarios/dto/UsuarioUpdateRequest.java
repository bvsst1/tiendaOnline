package cl.duoc.tienda.usuarios.dto;

import cl.duoc.tienda.usuarios.model.Rol;
import jakarta.validation.constraints.*;

public record UsuarioUpdateRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 100) String nombre,
        @NotBlank(message = "El correo es obligatorio") @Email @Size(max = 150) String email,
        @NotNull(message = "El rol es obligatorio") Rol rol,
        @Pattern(regexp = "[\\x21-\\x7E]{8,64}", message = "Usa entre 8 y 64 caracteres ASCII sin espacios") String password
) {}

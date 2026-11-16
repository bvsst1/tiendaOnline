package cl.duoc.tienda.usuarios.dto;

import cl.duoc.tienda.usuarios.model.Rol;
import cl.duoc.tienda.usuarios.model.Usuario;

public record UsuarioResponse(Long id, String nombre, String email, Rol rol) {
    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getRol());
    }
}


package cl.duoc.tienda.usuarios.controller;

import java.net.URI;
import cl.duoc.tienda.usuarios.dto.*;
import cl.duoc.tienda.usuarios.service.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UsuarioService usuarios;
    private final TokenService tokens;

    public AuthController(UsuarioService usuarios, TokenService tokens) {
        this.usuarios = usuarios;
        this.tokens = tokens;
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest solicitud) {
        UsuarioResponse usuario = usuarios.registrar(solicitud);
        return ResponseEntity.created(URI.create("/api/usuarios/" + usuario.id())).body(usuario);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest solicitud) {
        return tokens.emitir(usuarios.autenticar(solicitud));
    }
}

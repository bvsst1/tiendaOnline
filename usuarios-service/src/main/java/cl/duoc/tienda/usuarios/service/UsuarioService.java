package cl.duoc.tienda.usuarios.service;

import java.util.List;
import java.util.Locale;
import cl.duoc.tienda.usuarios.dto.*;
import cl.duoc.tienda.usuarios.exception.*;
import cl.duoc.tienda.usuarios.model.*;
import cl.duoc.tienda.usuarios.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsuarioService {
    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);
    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    public UsuarioResponse registrar(RegistroRequest solicitud) {
        String email = normalizarEmail(solicitud.email());
        if (repository.existsByEmail(email)) {
            throw new ConflictoException("El correo ya está registrado");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(solicitud.nombre().strip());
        usuario.setEmail(email);
        usuario.setPasswordHash(encoder.encode(solicitud.password()));
        // El registro público no permite elegir privilegios de administración.
        usuario.setRol(Rol.CLIENTE);
        repository.saveAndFlush(usuario);
        log.info("Usuario creado id={}", usuario.getId());
        return UsuarioResponse.desde(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return repository.findAll(org.springframework.data.domain.Sort.by("id"))
                .stream().map(UsuarioResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.desde(buscar(id));
    }

    public UsuarioResponse actualizar(Long id, UsuarioUpdateRequest solicitud) {
        Usuario usuario = buscar(id);
        String email = normalizarEmail(solicitud.email());
        if (repository.existsByEmailAndIdNot(email, id)) {
            throw new ConflictoException("El correo ya está registrado");
        }
        usuario.setNombre(solicitud.nombre().strip());
        usuario.setEmail(email);
        usuario.setRol(solicitud.rol());
        if (solicitud.password() != null) {
            usuario.setPasswordHash(encoder.encode(solicitud.password()));
        }
        repository.saveAndFlush(usuario);
        log.info("Usuario actualizado id={}", id);
        return UsuarioResponse.desde(usuario);
    }

    public void eliminar(Long id) {
        repository.delete(buscar(id));
        log.info("Usuario eliminado id={}", id);
    }

    @Transactional(readOnly = true)
    public Usuario autenticar(LoginRequest solicitud) {
        Usuario usuario = repository.findByEmail(normalizarEmail(solicitud.email()))
                .orElseThrow(CredencialesInvalidasException::new);
        if (!encoder.matches(solicitud.password(), usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }
        return usuario;
    }

    private Usuario buscar(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new RecursoNoEncontradoException("No existe el usuario con ID " + id));
    }

    private String normalizarEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }
}

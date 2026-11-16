package cl.duoc.tienda.usuarios;

import java.util.Map;
import cl.duoc.tienda.usuarios.model.*;
import cl.duoc.tienda.usuarios.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UsuariosIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UsuarioRepository repository;
    @Autowired PasswordEncoder encoder;
    private String adminToken;
    private String clienteToken;

    @BeforeEach
    void preparar() throws Exception {
        repository.deleteAll();
        crear("Admin", "admin@tienda.cl", Rol.ADMINISTRADOR);
        crear("Cliente", "cliente@tienda.cl", Rol.CLIENTE);
        adminToken = login("admin@tienda.cl");
        clienteToken = login("cliente@tienda.cl");
    }

    private void crear(String nombre, String email, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPasswordHash(encoder.encode("Prueba123!"));
        usuario.setRol(rol);
        repository.saveAndFlush(usuario);
    }

    private String login(String email) throws Exception {
        String respuesta = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("email", email, "password", "Prueba123!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.passwordHash").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(respuesta).get("token").asText();
    }

    @Test
    void registroGuardaHashYNoPermiteElegirRol() throws Exception {
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nombre":"Ana","email":"ANA@TIENDA.CL","password":"Prueba123!"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
        var usuario = repository.findByEmail("ana@tienda.cl").orElseThrow();
        assertThat(usuario.getPasswordHash()).isNotEqualTo("Prueba123!");
        assertThat(encoder.matches("Prueba123!", usuario.getPasswordHash())).isTrue();

        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nombre":"Intruso","email":"intruso@tienda.cl","password":"Prueba123!","rol":"ADMINISTRADOR"}
                        """))
                .andExpect(status().isBadRequest());
        assertThat(repository.findByEmail("intruso@tienda.cl")).isEmpty();
    }

    @Test
    void rechazaDatosInvalidosYCorreoDuplicado() throws Exception {
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nombre":"  ","email":"incorrecto","password":"123"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists());
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nombre":"Otro","email":"ADMIN@TIENDA.CL","password":"Prueba123!"}
                        """))
                .andExpect(status().isConflict());
        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    void loginIncorrectoDevuelve401() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"admin@tienda.cl","password":"equivocada"}
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Correo o contraseña incorrectos"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"noexiste@tienda.cl","password":"equivocada"}
                        """)).andExpect(status().isUnauthorized());
    }

    @Test
    void exigeAutenticacionYPermisos() throws Exception {
        mvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("cliente@tienda.cl"));
        mvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer token-alterado"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void administradorRealizaCrudYCompruebaPersistencia() throws Exception {
        String resultado = mvc.perform(post("/api/usuarios")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nombre":"Luis","email":"luis@tienda.cl","password":"Prueba123!"}
                        """))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(resultado).get("id").asLong();
        mvc.perform(get("/api/usuarios/" + id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Luis"));
        mvc.perform(put("/api/usuarios/" + id).header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nombre":"Luis Actualizado","email":"luis@tienda.cl","rol":"CLIENTE","password":"Nueva123!"}
                        """))
                .andExpect(status().isOk());
        assertThat(repository.findById(id).orElseThrow().getNombre()).isEqualTo("Luis Actualizado");
        assertThat(encoder.matches("Nueva123!", repository.findById(id).orElseThrow().getPasswordHash())).isTrue();
        mvc.perform(delete("/api/usuarios/" + id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
        assertThat(repository.findById(id)).isEmpty();
        mvc.perform(get("/api/usuarios/" + id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizacionNoPermiteCorreoDuplicadoNiRolInvalido() throws Exception {
        long id = repository.findByEmail("cliente@tienda.cl").orElseThrow().getId();
        mvc.perform(put("/api/usuarios/" + id).header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nombre":"Cliente","email":"admin@tienda.cl","rol":"CLIENTE"}
                        """))
                .andExpect(status().isConflict());
        mvc.perform(put("/api/usuarios/" + id).header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nombre":"Cliente","email":"cliente@tienda.cl","rol":"SUPERUSUARIO"}
                        """))
                .andExpect(status().isBadRequest());
    }
}


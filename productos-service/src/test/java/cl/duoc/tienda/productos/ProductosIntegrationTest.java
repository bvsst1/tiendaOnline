package cl.duoc.tienda.productos;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import javax.crypto.spec.SecretKeySpec;
import cl.duoc.tienda.productos.repository.ProductoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductosIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired ProductoRepository repository;
    private String admin;
    private String cliente;
    private static final String PRODUCTO = """
            {"nombre":"Mouse USB","descripcion":"Mouse óptico","categoria":"Accesorios","precio":9990,"stock":10}
            """;

    @BeforeEach
    void preparar() {
        repository.deleteAll();
        admin = token("ADMINISTRADOR", Instant.now().plusSeconds(900), "tienda-usuarios", "tienda-api");
        cliente = token("CLIENTE", Instant.now().plusSeconds(900), "tienda-usuarios", "tienda-api");
    }

    private String token(String rol, Instant expira, String issuer, String audience) {
        var key = new SecretKeySpec("clave-solo-para-pruebas-automaticas-1234567890".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        var encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        var claims = JwtClaimsSet.builder().issuer(issuer).audience(List.of(audience)).subject("1")
                .issuedAt(Instant.now().minusSeconds(1000)).expiresAt(expira).claim("rol", rol).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    @Test
    void permiteCrudAlAdminYGuardaCambios() throws Exception {
        String respuesta = mvc.perform(post("/api/productos").header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_JSON).content(PRODUCTO))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(respuesta).get("id").asLong();
        assertThat(repository.findById(id)).isPresent();

        mvc.perform(get("/api/productos/" + id).header("Authorization", "Bearer " + cliente))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Mouse USB"));
        mvc.perform(get("/api/productos?nombre=mOuSe").header("Authorization", "Bearer " + cliente))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));

        mvc.perform(put("/api/productos/" + id).header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_JSON).content(PRODUCTO.replace("\"stock\":10", "\"stock\":7")))
                .andExpect(status().isOk());
        assertThat(repository.findById(id).orElseThrow().getStock()).isEqualTo(7);
        mvc.perform(delete("/api/productos/" + id).header("Authorization", "Bearer " + admin))
                .andExpect(status().isNoContent());
        assertThat(repository.findById(id)).isEmpty();
        mvc.perform(get("/api/productos/" + id).header("Authorization", "Bearer " + cliente))
                .andExpect(status().isNotFound());
    }

    @Test
    void clienteNoPuedeCrearModificarNiEliminar() throws Exception {
        mvc.perform(post("/api/productos").header("Authorization", "Bearer " + cliente)
                .contentType(MediaType.APPLICATION_JSON).content(PRODUCTO))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/productos/1").header("Authorization", "Bearer " + cliente)
                .contentType(MediaType.APPLICATION_JSON).content(PRODUCTO))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/productos/1").header("Authorization", "Bearer " + cliente))
                .andExpect(status().isForbidden());
        assertThat(repository.count()).isZero();
    }

    @Test
    void rechazaValoresInvalidosSinPersistir() throws Exception {
        mvc.perform(post("/api/productos").header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nombre":" ","categoria":"","precio":0,"stock":-1}
                        """))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errores.stock").exists());
        mvc.perform(post("/api/productos").header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_JSON).content(PRODUCTO.replace("9990", "9.999")))
                .andExpect(status().isBadRequest());
        assertThat(repository.count()).isZero();
    }

    @Test
    void exigeTokenValidoNoSoloUnRolDeclarado() throws Exception {
        mvc.perform(get("/api/productos")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/productos").header("Authorization", "Bearer falso"))
                .andExpect(status().isUnauthorized());
        String[] invalidos = {
            token("ADMINISTRADOR", Instant.now().minusSeconds(120), "tienda-usuarios", "tienda-api"),
            token("ADMINISTRADOR", Instant.now().plusSeconds(900), "otro-emisor", "tienda-api"),
            token("ADMINISTRADOR", Instant.now().plusSeconds(900), "tienda-usuarios", "otra-api")
        };
        for (String invalido : invalidos) {
            mvc.perform(get("/api/productos").header("Authorization", "Bearer " + invalido))
                    .andExpect(status().isUnauthorized());
        }
        String sinRol = token("OTRO", Instant.now().plusSeconds(900), "tienda-usuarios", "tienda-api");
        mvc.perform(get("/api/productos").header("Authorization", "Bearer " + sinRol))
                .andExpect(status().isForbidden());
    }

    @Test
    void manejaIdInexistenteYFormatoIncorrecto() throws Exception {
        mvc.perform(put("/api/productos/999999").header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_JSON).content(PRODUCTO))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/productos/999999").header("Authorization", "Bearer " + admin))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/productos/no-numero").header("Authorization", "Bearer " + admin))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/productos").header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_JSON).content("{mal formado}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/productos?nombre=sin-resultados").header("Authorization", "Bearer " + cliente))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }
}

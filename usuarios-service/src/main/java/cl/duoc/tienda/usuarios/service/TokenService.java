package cl.duoc.tienda.usuarios.service;

import java.time.Instant;
import java.util.List;
import cl.duoc.tienda.usuarios.dto.*;
import cl.duoc.tienda.usuarios.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    private final JwtEncoder encoder;
    private final String issuer;
    private final String audience;

    public TokenService(JwtEncoder encoder, @Value("${tienda.jwt.issuer}") String issuer,
                        @Value("${tienda.jwt.audience}") String audience) {
        this.encoder = encoder;
        this.issuer = issuer;
        this.audience = audience;
    }

    public LoginResponse emitir(Usuario usuario) {
        Instant ahora = Instant.now();
        long duracion = 900;
        var claims = JwtClaimsSet.builder().issuer(issuer).audience(List.of(audience))
                .subject(usuario.getId().toString()).issuedAt(ahora).expiresAt(ahora.plusSeconds(duracion))
                .claim("rol", usuario.getRol().name()).build();
        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new LoginResponse(token, "Bearer", duracion, UsuarioResponse.desde(usuario));
    }
}

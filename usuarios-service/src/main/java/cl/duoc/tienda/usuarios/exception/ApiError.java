package cl.duoc.tienda.usuarios.exception;

import java.time.Instant;
import java.util.Map;

public record ApiError(Instant fecha, int estado, String mensaje, Map<String, String> errores) {
    public static ApiError crear(int estado, String mensaje) {
        return new ApiError(Instant.now(), estado, mensaje, Map.of());
    }
}


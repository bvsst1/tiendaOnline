package cl.duoc.tienda.productos.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;

public record ProductoRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 100) String nombre,
        @Size(max = 500) String descripcion,
        @NotBlank(message = "La categoría es obligatoria") @Size(max = 60) String categoria,
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor que cero")
        @Digits(integer = 10, fraction = 2, message = "El precio admite hasta 10 enteros y 2 decimales") BigDecimal precio,
        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo") Integer stock
) {}


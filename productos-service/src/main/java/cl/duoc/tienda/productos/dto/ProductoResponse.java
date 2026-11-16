package cl.duoc.tienda.productos.dto;

import java.math.BigDecimal;
import cl.duoc.tienda.productos.model.Producto;

public record ProductoResponse(Long id, String nombre, String descripcion, String categoria,
                               BigDecimal precio, Integer stock) {
    public static ProductoResponse desde(Producto producto) {
        return new ProductoResponse(producto.getId(), producto.getNombre(), producto.getDescripcion(),
                producto.getCategoria(), producto.getPrecio(), producto.getStock());
    }
}


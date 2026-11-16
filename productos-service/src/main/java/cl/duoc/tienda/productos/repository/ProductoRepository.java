package cl.duoc.tienda.productos.repository;

import java.util.List;
import cl.duoc.tienda.productos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByNombreContainingIgnoreCaseOrderByIdAsc(String nombre);
}

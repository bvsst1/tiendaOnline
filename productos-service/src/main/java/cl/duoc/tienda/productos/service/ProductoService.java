package cl.duoc.tienda.productos.service;

import java.util.List;
import cl.duoc.tienda.productos.dto.*;
import cl.duoc.tienda.productos.exception.RecursoNoEncontradoException;
import cl.duoc.tienda.productos.model.Producto;
import cl.duoc.tienda.productos.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductoService {
    private static final Logger log = LoggerFactory.getLogger(ProductoService.class);
    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(String nombre) {
        List<Producto> productos = nombre == null || nombre.isBlank()
                ? repository.findAll(Sort.by("id"))
                : repository.findByNombreContainingIgnoreCaseOrderByIdAsc(nombre.strip());
        return productos.stream().map(ProductoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        return ProductoResponse.desde(buscar(id));
    }

    public ProductoResponse crear(ProductoRequest solicitud) {
        Producto producto = new Producto();
        copiar(solicitud, producto);
        repository.saveAndFlush(producto);
        log.info("Producto creado id={}", producto.getId());
        return ProductoResponse.desde(producto);
    }

    public ProductoResponse actualizar(Long id, ProductoRequest solicitud) {
        Producto producto = buscar(id);
        copiar(solicitud, producto);
        repository.saveAndFlush(producto);
        log.info("Producto actualizado id={}", id);
        return ProductoResponse.desde(producto);
    }

    public void eliminar(Long id) {
        repository.delete(buscar(id));
        log.info("Producto eliminado id={}", id);
    }

    private Producto buscar(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new RecursoNoEncontradoException("No existe el producto con ID " + id));
    }

    private void copiar(ProductoRequest solicitud, Producto producto) {
        producto.setNombre(solicitud.nombre().strip());
        producto.setDescripcion(solicitud.descripcion() == null ? null : solicitud.descripcion().strip());
        producto.setCategoria(solicitud.categoria().strip());
        producto.setPrecio(solicitud.precio());
        producto.setStock(solicitud.stock());
    }
}


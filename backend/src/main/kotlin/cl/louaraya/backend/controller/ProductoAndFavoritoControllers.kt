package cl.louaraya.backend.controller

import cl.louaraya.backend.dto.*
import cl.louaraya.backend.service.FavoritoService
import cl.louaraya.backend.service.ProductoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Catálogo de piezas de macramé y gestión administrativa")
class ProductoController(private val productoService: ProductoService) {

    @GetMapping
    @Operation(summary = "Listar productos activos con filtro o búsqueda")
    fun listar(@RequestParam(required = false) q: String?): ResponseEntity<List<ProductoResponse>> {
        return ResponseEntity.ok(productoService.listar(q))
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener el detalle de un producto por ID")
    fun detalle(@PathVariable id: Int): ResponseEntity<ProductoResponse> {
        return ResponseEntity.ok(productoService.obtenerPorId(id))
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear nuevo producto artesanal (Solo ADMIN)")
    fun crear(@Valid @RequestBody req: ProductoRequest): ResponseEntity<ProductoResponse> {
        return ResponseEntity.ok(productoService.crear(req))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Modificar un producto existente (Solo ADMIN)")
    fun actualizar(
        @PathVariable id: Int,
        @Valid @RequestBody req: ProductoRequest
    ): ResponseEntity<ProductoResponse> {
        return ResponseEntity.ok(productoService.actualizar(id, req))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desactivar un producto del catálogo (Solo ADMIN)")
    fun eliminar(@PathVariable id: Int): ResponseEntity<MensajeGenericoResponse> {
        productoService.eliminar(id)
        return ResponseEntity.ok(MensajeGenericoResponse(exito = true, mensaje = "Producto eliminado"))
    }
}

@RestController
@RequestMapping("/api/favoritos")
@Tag(name = "Favoritos", description = "Gestión de piezas guardadas por el cliente")
class FavoritoController(private val favoritoService: FavoritoService) {

    @GetMapping
    @Operation(summary = "Obtener los IDs de productos favoritos del cliente")
    fun listar(auth: Authentication): ResponseEntity<Set<Int>> {
        return ResponseEntity.ok(favoritoService.listarIds(auth.name))
    }

    @PostMapping("/{productoId}")
    @Operation(summary = "Alternar favorito (agrega si no existe, quita si ya existe)")
    fun alternar(@PathVariable productoId: Int, auth: Authentication): ResponseEntity<Map<String, Any>> {
        val esFavorito = favoritoService.alternar(auth.name, productoId)
        return ResponseEntity.ok(mapOf("productoId" to productoId, "esFavorito" to esFavorito))
    }
}

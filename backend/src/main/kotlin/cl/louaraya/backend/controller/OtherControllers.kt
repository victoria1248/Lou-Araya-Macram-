package cl.louaraya.backend.controller

import cl.louaraya.backend.dto.*
import cl.louaraya.backend.model.Campana
import cl.louaraya.backend.service.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/campanas")
@Tag(name = "Campañas", description = "Colecciones temáticas")
class CampanaController(private val campanaService: CampanaService) {

    @GetMapping
    @Operation(summary = "Listar campañas activas")
    fun listar(): ResponseEntity<List<Campana>> = ResponseEntity.ok(campanaService.listar())

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Guardar campaña (Solo ADMIN)")
    fun guardar(@RequestBody c: Campana): ResponseEntity<Campana> = ResponseEntity.ok(campanaService.guardar(c))
}

@RestController
@RequestMapping("/api/promociones")
@Tag(name = "Promociones", description = "Ofertas temporales con descuento")
class PromocionController(private val promocionService: PromocionService) {

    @GetMapping
    @Operation(summary = "Listar promociones vigentes")
    fun activas(): ResponseEntity<List<PromocionResponse>> = ResponseEntity.ok(promocionService.activas())

    @GetMapping("/todas")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar historial de todas las promociones (Solo ADMIN)")
    fun todas(): ResponseEntity<List<PromocionResponse>> = ResponseEntity.ok(promocionService.todas())

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear nueva promoción y notificar a los clientes (Solo ADMIN)")
    fun crear(@Valid @RequestBody req: PromocionRequest): ResponseEntity<PromocionResponse> =
        ResponseEntity.ok(promocionService.crear(req))

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar una promoción (Solo ADMIN)")
    fun eliminar(@PathVariable id: Long): ResponseEntity<MensajeGenericoResponse> {
        promocionService.eliminar(id)
        return ResponseEntity.ok(MensajeGenericoResponse(true, "Promoción eliminada"))
    }
}

@RestController
@RequestMapping("/api/ferias")
@Tag(name = "Ferias y Eventos", description = "Puntos de encuentro y stands presenciales")
class FeriaController(private val feriaService: FeriaService) {

    @GetMapping
    @Operation(summary = "Listar todas las ferias programadas")
    fun listar(auth: Authentication?): ResponseEntity<List<FeriaResponse>> {
        return ResponseEntity.ok(feriaService.listar(auth?.name))
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Publicar una nueva feria (Solo ADMIN)")
    fun crear(@Valid @RequestBody req: FeriaRequest): ResponseEntity<FeriaResponse> =
        ResponseEntity.ok(feriaService.guardar(req))

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar datos de una feria (Solo ADMIN)")
    fun actualizar(@PathVariable id: Int, @Valid @RequestBody req: FeriaRequest): ResponseEntity<FeriaResponse> =
        ResponseEntity.ok(feriaService.guardar(req, id))

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar feria (Solo ADMIN)")
    fun eliminar(@PathVariable id: Int): ResponseEntity<MensajeGenericoResponse> {
        feriaService.eliminar(id)
        return ResponseEntity.ok(MensajeGenericoResponse(true, "Feria eliminada"))
    }

    @PostMapping("/{id}/inscribir")
    @Operation(summary = "Inscribirse o confirmar asistencia a un evento artesanal")
    fun inscribir(@PathVariable id: Int, auth: Authentication): ResponseEntity<Map<String, Any>> {
        val inscrito = feriaService.inscribir(id, auth.name)
        return ResponseEntity.ok(mapOf("feriaId" to id, "inscrito" to inscrito))
    }
}

@RestController
@RequestMapping("/api/notificaciones")
@Tag(name = "Notificaciones", description = "Avisos de pedidos, mensajes y novedades")
class NotificacionController(private val notificacionService: NotificacionService) {

    @GetMapping
    @Operation(summary = "Listar notificaciones del usuario autenticado")
    fun listar(auth: Authentication): ResponseEntity<List<NotificacionResponse>> =
        ResponseEntity.ok(notificacionService.listar(auth.name))

    @PutMapping("/{id}/leida")
    @Operation(summary = "Marcar una notificación como leída")
    fun marcarLeida(@PathVariable id: String, auth: Authentication): ResponseEntity<Void> {
        notificacionService.marcarLeida(id, auth.name)
        return ResponseEntity.ok().build()
    }

    @PutMapping("/marcar-todas-leidas")
    @Operation(summary = "Marcar todas las notificaciones como leídas")
    fun marcarTodas(auth: Authentication): ResponseEntity<Void> {
        notificacionService.marcarTodasLeidas(auth.name)
        return ResponseEntity.ok().build()
    }
}

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Dashboard Administrativo", description = "Métricas y resumen operativo del negocio")
class AdminDashboardController(private val dashboardService: DashboardService) {

    @GetMapping
    @Operation(summary = "Obtener estadísticas reales de inventario y pedidos")
    fun estadisticas(): ResponseEntity<DashboardStatsResponse> =
        ResponseEntity.ok(dashboardService.obtenerEstadisticas())
}

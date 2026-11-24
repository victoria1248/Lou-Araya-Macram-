package cl.louaraya.backend.controller

import cl.louaraya.backend.dto.*
import cl.louaraya.backend.service.SolicitudService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/solicitudes")
@Tag(name = "Solicitudes y Pedidos", description = "Creación, trazabilidad y estados de confección artesanal")
class SolicitudController(private val solicitudService: SolicitudService) {

    @PostMapping
    @Operation(summary = "Crear nueva solicitud o encargo de macramé personalizado")
    fun crear(
        auth: Authentication,
        @Valid @RequestBody req: CrearSolicitudRequest
    ): ResponseEntity<SolicitudResponse> {
        return ResponseEntity.ok(solicitudService.crear(auth.name, req))
    }

    @GetMapping
    @Operation(summary = "Listar solicitudes (cliente ve las suyas, administrador ve todas)")
    fun listar(
        auth: Authentication,
        @RequestParam(required = false) estado: String?
    ): ResponseEntity<List<SolicitudResponse>> {
        val esAdmin = auth.authorities.any { it.authority == "ROLE_ADMIN" }
        return ResponseEntity.ok(solicitudService.listar(auth.name, esAdmin, estado))
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener el detalle de una solicitud")
    fun detalle(
        @PathVariable id: String,
        auth: Authentication
    ): ResponseEntity<SolicitudResponse> {
        val esAdmin = auth.authorities.any { it.authority == "ROLE_ADMIN" }
        return ResponseEntity.ok(solicitudService.obtenerPorId(id, auth.name, esAdmin))
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar el estado de elaboración y stock (Solo ADMIN)")
    fun cambiarEstado(
        @PathVariable id: String,
        auth: Authentication,
        @Valid @RequestBody req: CambiarEstadoSolicitudRequest
    ): ResponseEntity<SolicitudResponse> {
        return ResponseEntity.ok(solicitudService.cambiarEstado(id, auth.name, req))
    }

    @GetMapping("/{id}/historial")
    @Operation(summary = "Ver el historial de cambios de estado de una solicitud")
    fun historial(@PathVariable id: String): ResponseEntity<List<HistorialResponse>> {
        return ResponseEntity.ok(solicitudService.listarHistorial(id))
    }

    @PostMapping("/{id}/mensajes")
    @Operation(summary = "Enviar un mensaje respecto al pedido entre cliente y artesano")
    fun enviarMensaje(
        @PathVariable id: String,
        auth: Authentication,
        @Valid @RequestBody req: MensajeRequest
    ): ResponseEntity<MensajeResponse> {
        return ResponseEntity.ok(solicitudService.enviarMensaje(id, auth.name, req))
    }

    @GetMapping("/{id}/mensajes")
    @Operation(summary = "Obtener la conversación vinculada a la solicitud")
    fun listarMensajes(
        @PathVariable id: String,
        auth: Authentication
    ): ResponseEntity<List<MensajeResponse>> {
        val esAdmin = auth.authorities.any { it.authority == "ROLE_ADMIN" }
        return ResponseEntity.ok(solicitudService.listarMensajes(id, auth.name, esAdmin))
    }
}

package cl.louaraya.backend.controller

import cl.louaraya.backend.dto.*
import cl.louaraya.backend.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints de registro, login, perfil y recuperación de contraseña")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión con usuario/correo y contraseña")
    fun login(@Valid @RequestBody req: LoginRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.login(req))
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar un nuevo cliente")
    fun registro(@Valid @RequestBody req: RegistroRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.registro(req))
    }

    @GetMapping("/perfil")
    @Operation(summary = "Consultar el perfil del usuario autenticado")
    fun perfil(auth: Authentication): ResponseEntity<PerfilResponse> {
        return ResponseEntity.ok(authService.obtenerPerfil(auth.name))
    }

    @PutMapping("/perfil")
    @Operation(summary = "Actualizar datos personales o contraseña del usuario")
    fun actualizarPerfil(
        auth: Authentication,
        @RequestBody req: ActualizarPerfilRequest
    ): ResponseEntity<PerfilResponse> {
        return ResponseEntity.ok(authService.actualizarPerfil(auth.name, req))
    }

    @PostMapping("/recuperar-password")
    @Operation(summary = "Solicitar token de recuperación de contraseña")
    fun recuperar(@Valid @RequestBody req: RecuperarPasswordRequest): ResponseEntity<MensajeGenericoResponse> {
        val token = authService.solicitarRecuperacion(req.correo)
        return ResponseEntity.ok(
            MensajeGenericoResponse(
                exito = true,
                mensaje = "Si la cuenta existe, se ha generado el enlace de recuperación (Token de verificación: $token)"
            )
        )
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Confirmar nueva contraseña con el token de verificación")
    fun resetConfirm(@Valid @RequestBody req: ResetPasswordConfirmRequest): ResponseEntity<MensajeGenericoResponse> {
        authService.resetPasswordConfirm(req)
        return ResponseEntity.ok(
            MensajeGenericoResponse(exito = true, mensaje = "Contraseña actualizada exitosamente")
        )
    }
}

package cl.louaraya.backend.service

import cl.louaraya.backend.config.JwtUtils
import cl.louaraya.backend.dto.*
import cl.louaraya.backend.exception.BadRequestException
import cl.louaraya.backend.exception.ForbiddenException
import cl.louaraya.backend.exception.ResourceNotFoundException
import cl.louaraya.backend.exception.UnauthorizedException
import cl.louaraya.backend.model.*
import cl.louaraya.backend.repository.*
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class AuthService(
    private val usuarioRepository: UsuarioRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtils: JwtUtils,
    private val passwordResetTokenRepository: PasswordResetTokenRepository
) {
    fun login(req: LoginRequest): AuthResponse {
        val identificador = req.identificador.trim()
        val usuario = usuarioRepository.findByCorreoIgnoreCase(identificador)
            .or { usuarioRepository.findByUsuarioIgnoreCase(identificador) }
            .orElseThrow { UnauthorizedException("Correo, usuario o contraseña incorrectos") }

        if (!passwordEncoder.matches(req.contrasena, usuario.contrasenaHash)) {
            throw UnauthorizedException("Correo, usuario o contraseña incorrectos")
        }

        if (!usuario.activo) {
            throw ForbiddenException("Esta cuenta ha sido desactivada")
        }

        val token = jwtUtils.generarToken(usuario.correo, usuario.rol.name)

        return AuthResponse(
            token = token,
            id = usuario.id!!,
            correo = usuario.correo,
            usuario = usuario.usuario,
            nombre = usuario.nombre,
            apellido = usuario.apellido,
            rol = usuario.rol.name,
            fotoUrl = usuario.fotoUrl
        )
    }

    @Transactional
    fun registro(req: RegistroRequest): AuthResponse {
        val correo = req.correo.trim().lowercase()
        val usuarioHandle = req.usuario.trim()

        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw BadRequestException("Este correo ya está registrado")
        }
        if (usuarioRepository.existsByUsuarioIgnoreCase(usuarioHandle)) {
            throw BadRequestException("Este nombre de usuario ya existe")
        }

        val nuevo = Usuario(
            correo = correo,
            contrasenaHash = passwordEncoder.encode(req.contrasena),
            nombre = req.nombre.trim(),
            apellido = req.apellido.trim(),
            usuario = usuarioHandle,
            telefono = req.telefono?.trim(),
            fechaNacimiento = req.fechaNacimiento?.trim(),
            rol = Rol.CLIENTE
        )

        val guardado = usuarioRepository.save(nuevo)
        val token = jwtUtils.generarToken(guardado.correo, guardado.rol.name)

        return AuthResponse(
            token = token,
            id = guardado.id!!,
            correo = guardado.correo,
            usuario = guardado.usuario,
            nombre = guardado.nombre,
            apellido = guardado.apellido,
            rol = guardado.rol.name,
            fotoUrl = guardado.fotoUrl
        )
    }

    fun obtenerPerfil(correo: String): PerfilResponse {
        val u = usuarioRepository.findByCorreoIgnoreCase(correo)
            .orElseThrow { ResourceNotFoundException("Usuario no encontrado") }
        return PerfilResponse(
            id = u.id!!,
            correo = u.correo,
            usuario = u.usuario,
            nombre = u.nombre,
            apellido = u.apellido,
            telefono = u.telefono,
            fechaNacimiento = u.fechaNacimiento,
            rol = u.rol.name,
            fotoUrl = u.fotoUrl
        )
    }

    @Transactional
    fun actualizarPerfil(correo: String, req: ActualizarPerfilRequest): PerfilResponse {
        val u = usuarioRepository.findByCorreoIgnoreCase(correo)
            .orElseThrow { ResourceNotFoundException("Usuario no encontrado") }

        if (!req.contrasenaActual.isNullOrBlank() && !req.nuevaContrasena.isNullOrBlank()) {
            if (!passwordEncoder.matches(req.contrasenaActual, u.contrasenaHash)) {
                throw BadRequestException("La contraseña actual no es correcta")
            }
            if (req.nuevaContrasena.length < 8) {
                throw BadRequestException("La nueva contraseña debe tener al menos 8 caracteres")
            }
            u.contrasenaHash = passwordEncoder.encode(req.nuevaContrasena)
        }

        u.nombre = req.nombre.trim()
        u.apellido = req.apellido.trim()
        u.telefono = req.telefono?.trim()
        u.fotoUrl = req.fotoUrl
        u.actualizadoEn = Instant.now()

        val guardado = usuarioRepository.save(u)
        return PerfilResponse(
            id = guardado.id!!,
            correo = guardado.correo,
            usuario = guardado.usuario,
            nombre = guardado.nombre,
            apellido = guardado.apellido,
            telefono = guardado.telefono,
            fechaNacimiento = guardado.fechaNacimiento,
            rol = guardado.rol.name,
            fotoUrl = guardado.fotoUrl
        )
    }

    @Transactional
    fun solicitarRecuperacion(correo: String): String {
        val u = usuarioRepository.findByCorreoIgnoreCase(correo.trim().lowercase())
            .orElseThrow { BadRequestException("Si el correo está registrado, se enviará el enlace de recuperación.") }

        val token = UUID.randomUUID().toString()
        val resetToken = PasswordResetToken(
            usuario = u,
            token = token,
            expiraEn = Instant.now().plus(2, ChronoUnit.HOURS)
        )
        passwordResetTokenRepository.save(resetToken)
        return token
    }

    @Transactional
    fun resetPasswordConfirm(req: ResetPasswordConfirmRequest) {
        val resetToken = passwordResetTokenRepository.findByToken(req.token)
            .orElseThrow { BadRequestException("Token de recuperación no válido o vencido") }

        if (resetToken.usado || resetToken.expiraEn.isBefore(Instant.now())) {
            throw BadRequestException("El token ha expirado o ya fue utilizado")
        }

        val usuario = resetToken.usuario!!
        usuario.contrasenaHash = passwordEncoder.encode(req.nuevaContrasena)
        usuario.actualizadoEn = Instant.now()
        usuarioRepository.save(usuario)

        resetToken.usado = true
        passwordResetTokenRepository.save(resetToken)
    }
}

@Service
class ProductoService(
    private val productoRepository: ProductoRepository,
    private val objectMapper: ObjectMapper
) {
    fun listar(q: String?): List<ProductoResponse> {
        val productos = if (!q.isNullOrBlank()) {
            productoRepository.buscar(q.trim())
        } else {
            productoRepository.findByActivoTrueOrderByIdAsc()
        }
        return productos.map { toResponse(it) }
    }

    fun obtenerPorId(id: Int): ProductoResponse {
        val p = productoRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Producto no encontrado") }
        return toResponse(p)
    }

    @Transactional
    fun crear(req: ProductoRequest): ProductoResponse {
        if (productoRepository.existsByCodigoIgnoreCase(req.codigo.trim())) {
            throw BadRequestException("Ya existe un producto con el código ${req.codigo}")
        }
        val p = Producto(
            codigo = req.codigo.trim().uppercase(),
            nombre = req.nombre.trim(),
            categoria = req.categoria,
            material = req.material,
            dimensiones = req.dimensiones,
            precio = req.precio,
            stock = req.stock,
            imagenKey = req.imagenKey,
            fotos = objectMapper.writeValueAsString(req.fotos)
        )
        return toResponse(productoRepository.save(p))
    }

    @Transactional
    fun actualizar(id: Int, req: ProductoRequest): ProductoResponse {
        val p = productoRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Producto no encontrado") }

        if (!p.codigo.equals(req.codigo.trim(), ignoreCase = true) &&
            productoRepository.existsByCodigoIgnoreCase(req.codigo.trim())
        ) {
            throw BadRequestException("Ya existe un producto con el código ${req.codigo}")
        }

        p.codigo = req.codigo.trim().uppercase()
        p.nombre = req.nombre.trim()
        p.categoria = req.categoria
        p.material = req.material
        p.dimensiones = req.dimensiones
        p.precio = req.precio
        p.stock = req.stock
        p.imagenKey = req.imagenKey
        p.fotos = objectMapper.writeValueAsString(req.fotos)
        p.actualizadoEn = Instant.now()

        return toResponse(productoRepository.save(p))
    }

    @Transactional
    fun eliminar(id: Int) {
        val p = productoRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Producto no encontrado") }
        p.activo = false
        productoRepository.save(p)
    }

    private fun toResponse(p: Producto): ProductoResponse {
        val listaFotos = try {
            objectMapper.readValue(p.fotos, object : TypeReference<List<String>>() {})
        } catch (_: Exception) {
            emptyList<String>()
        }
        return ProductoResponse(
            id = p.id!!,
            codigo = p.codigo,
            nombre = p.nombre,
            categoria = p.categoria,
            material = p.material,
            dimensiones = p.dimensiones,
            precio = p.precio,
            stock = p.stock,
            imagenKey = p.imagenKey,
            fotos = listaFotos
        )
    }
}

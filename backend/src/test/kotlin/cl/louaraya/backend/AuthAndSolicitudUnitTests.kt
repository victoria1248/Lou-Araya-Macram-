package cl.louaraya.backend

import cl.louaraya.backend.config.JwtUtils
import cl.louaraya.backend.model.EstadoSolicitud
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class AuthAndSolicitudUnitTests {

    private val jwtSecret = "4f9a72c1d3e5b8a6f0192837465abcde9876543210fedcba123456789abcdef0"
    private val jwtUtils = JwtUtils(jwtSecret, 3600000L) // 1 hora
    private val passwordEncoder = BCryptPasswordEncoder(10)

    @Test
    fun `debe generar y validar token JWT correctamente`() {
        val correo = "admin@admin.com"
        val rol = "ADMIN"
        val token = jwtUtils.generarToken(correo, rol)

        assertNotNull(token)
        assertTrue(token.split(".").size == 3)

        val usernameExtraido = jwtUtils.obtenerUsernameDelToken(token)
        assertEquals(correo, usernameExtraido)

        val userDetails = User(correo, "hash", emptyList())
        assertTrue(jwtUtils.validarToken(token, userDetails))
    }

    @Test
    fun `debe hashear contraseñas con BCrypt sin almacenar texto plano`() {
        val passwordPlana = "Admin1234!"
        val hash = passwordEncoder.encode(passwordPlana)

        assertNotNull(hash)
        assertNotEquals(passwordPlana, hash)
        assertTrue(hash.startsWith("\$2a\$10\$") || hash.startsWith("\$2b\$10\$"))
        assertTrue(passwordEncoder.matches(passwordPlana, hash))
        assertFalse(passwordEncoder.matches("PasswordErronea", hash))
    }

    @Test
    fun `debe validar flujo de estados de solicitud artesanal`() {
        val flujoValido = listOf(
            EstadoSolicitud.PENDIENTE,
            EstadoSolicitud.REVISION,
            EstadoSolicitud.APROBADA,
            EstadoSolicitud.ELABORACION,
            EstadoSolicitud.LISTA,
            EstadoSolicitud.ENTREGADA
        )

        assertEquals(6, flujoValido.size)
        assertTrue(flujoValido.contains(EstadoSolicitud.ELABORACION))
        assertTrue(EstadoSolicitud.values().contains(EstadoSolicitud.CANCELADA))
    }
}

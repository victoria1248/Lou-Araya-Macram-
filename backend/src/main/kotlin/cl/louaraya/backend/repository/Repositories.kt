package cl.louaraya.backend.repository

import cl.louaraya.backend.model.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UsuarioRepository : JpaRepository<Usuario, Long> {
    fun findByCorreoIgnoreCase(correo: String): Optional<Usuario>
    fun findByUsuarioIgnoreCase(usuario: String): Optional<Usuario>
    fun existsByCorreoIgnoreCase(correo: String): Boolean
    fun existsByUsuarioIgnoreCase(usuario: String): Boolean
    fun findByRol(rol: Rol): List<Usuario>
}

@Repository
interface ProductoRepository : JpaRepository<Producto, Int> {
    fun findByActivoTrueOrderByIdAsc(): List<Producto>
    fun findByCodigoIgnoreCase(codigo: String): Optional<Producto>
    fun existsByCodigoIgnoreCase(codigo: String): Boolean
    fun findByCategoriaAndActivoTrue(categoria: String): List<Producto>
    @Query("SELECT p FROM Producto p WHERE p.activo = true AND (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(p.material) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(p.codigo) LIKE LOWER(CONCAT('%', :q, '%')))")
    fun buscar(q: String): List<Producto>
}

@Repository
interface FavoritoRepository : JpaRepository<Favorito, Long> {
    fun findByUsuarioId(usuarioId: Long): List<Favorito>
    fun findByUsuarioIdAndProductoId(usuarioId: Long, productoId: Int): Optional<Favorito>
    fun existsByUsuarioIdAndProductoId(usuarioId: Long, productoId: Int): Boolean
    fun deleteByUsuarioIdAndProductoId(usuarioId: Long, productoId: Int)
}

@Repository
interface SolicitudRepository : JpaRepository<Solicitud, String> {
    fun findByUsuarioIdOrderByCreadaEnDesc(usuarioId: Long): List<Solicitud>
    fun findAllByOrderByCreadaEnDesc(): List<Solicitud>
    fun findByEstadoOrderByCreadaEnDesc(estado: EstadoSolicitud): List<Solicitud>
    fun countByEstado(estado: EstadoSolicitud): Long
}

@Repository
interface HistorialSolicitudRepository : JpaRepository<HistorialSolicitud, Long> {
    fun findBySolicitudIdOrderByFechaAsc(solicitudId: String): List<HistorialSolicitud>
}

@Repository
interface MensajeSolicitudRepository : JpaRepository<MensajeSolicitud, Long> {
    fun findBySolicitudIdOrderByFechaAsc(solicitudId: String): List<MensajeSolicitud>
}

@Repository
interface CampanaRepository : JpaRepository<Campana, Int> {
    fun findByActivaTrueOrderByOrdenAsc(): List<Campana>
    fun findByNombreIgnoreCase(nombre: String): Optional<Campana>
}

@Repository
interface PromocionRepository : JpaRepository<Promocion, Long> {
    fun findByActivaTrueAndFinMillisGreaterThanOrderByFinMillisAsc(ahora: Long): List<Promocion>
    fun findAllByOrderByCreadaEnDesc(): List<Promocion>
}

@Repository
interface FeriaRepository : JpaRepository<Feria, Int> {
    fun findAllByOrderByIdAsc(): List<Feria>
}

@Repository
interface InscripcionFeriaRepository : JpaRepository<InscripcionFeria, Long> {
    fun findByFeriaId(feriaId: Int): List<InscripcionFeria>
    fun findByFeriaIdAndCorreoIgnoreCase(feriaId: Int, correo: String): Optional<InscripcionFeria>
    fun existsByFeriaIdAndCorreoIgnoreCase(feriaId: Int, correo: String): Boolean
    fun deleteByFeriaIdAndCorreoIgnoreCase(feriaId: Int, correo: String)
    fun countByFeriaId(feriaId: Int): Long
}

@Repository
interface NotificacionRepository : JpaRepository<Notificacion, String> {
    fun findByUsuarioIdOrderByFechaDesc(usuarioId: Long): List<Notificacion>
    fun countByUsuarioIdAndLeidaFalse(usuarioId: Long): Long
}

@Repository
interface PasswordResetTokenRepository : JpaRepository<PasswordResetToken, Long> {
    fun findByToken(token: String): Optional<PasswordResetToken>
}

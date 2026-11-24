package cl.louaraya.backend.model

import jakarta.persistence.*
import java.time.Instant

enum class EstadoSolicitud {
    PENDIENTE,
    REVISION,
    APROBADA,
    ELABORACION,
    LISTA,
    ENTREGADA,
    CANCELADA
}

@Entity
@Table(name = "solicitudes")
class Solicitud(
    @Id
    @Column(length = 64)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    var usuario: Usuario? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    var producto: Producto? = null,

    @Column(nullable = false)
    var unidades: Int = 1,

    @Column(name = "fecha_requerida", nullable = false, length = 30)
    var fechaRequerida: String = "",

    @Column(columnDefinition = "TEXT")
    var comentario: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var estado: EstadoSolicitud = EstadoSolicitud.PENDIENTE,

    @Column(nullable = false)
    var personalizada: Boolean = false,

    @Column(nullable = false, length = 60)
    var color: String = "Original",

    @Column(nullable = false, length = 60)
    var material: String = "Original",

    @Column(nullable = false, length = 60)
    var medidas: String = "Original",

    @Column(name = "precio_estimado")
    var precioEstimado: Int? = null,

    @Column(name = "creada_en")
    var creadaEn: Instant = Instant.now(),

    @Column(name = "actualizada_en")
    var actualizadaEn: Instant = Instant.now()
)

@Entity
@Table(name = "historial_solicitudes")
class HistorialSolicitud(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false)
    var solicitud: Solicitud? = null,

    @Column(name = "estado_anterior", length = 30)
    var estadoAnterior: String? = null,

    @Column(name = "estado_nuevo", nullable = false, length = 30)
    var estadoNuevo: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cambiado_por_id")
    var cambiadoPor: Usuario? = null,

    @Column(columnDefinition = "TEXT")
    var nota: String? = null,

    @Column(nullable = false)
    var fecha: Instant = Instant.now()
)

@Entity
@Table(name = "mensajes_solicitud")
class MensajeSolicitud(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false)
    var solicitud: Solicitud? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emisor_id", nullable = false)
    var emisor: Usuario? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    var contenido: String = "",

    @Column(nullable = false)
    var leido: Boolean = false,

    @Column(nullable = false)
    var fecha: Instant = Instant.now()
)

@Entity
@Table(name = "campanas")
class Campana(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(nullable = false, unique = true, length = 100)
    var nombre: String = "",

    @Column(nullable = false, columnDefinition = "TEXT")
    var descripcion: String = "",

    @Column(nullable = false)
    var activa: Boolean = true,

    @Column(nullable = false)
    var orden: Int = 0
)

@Entity
@Table(name = "promociones")
class Promocion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, length = 150)
    var titulo: String = "",

    @Column(nullable = false, columnDefinition = "TEXT")
    var mensaje: String = "",

    @Column(nullable = false, length = 60)
    var tipo: String = "",

    @Column(name = "fin_millis", nullable = false)
    var finMillis: Long = 0L,

    @Column(nullable = false)
    var activa: Boolean = true,

    @Column(name = "creada_en")
    var creadaEn: Instant = Instant.now()
)

@Entity
@Table(name = "ferias")
class Feria(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(nullable = false, length = 150)
    var nombre: String = "",

    @Column(nullable = false, length = 50)
    var tipo: String = "Feria",

    @Column(nullable = false, length = 50)
    var fecha: String = "",

    @Column(name = "fecha_fin", length = 50)
    var fechaFin: String = "",

    @Column(nullable = false, length = 255)
    var lugar: String = "",

    @Column(nullable = false)
    var latitud: Double = 0.0,

    @Column(nullable = false)
    var longitud: Double = 0.0,

    @Column(columnDefinition = "TEXT")
    var descripcion: String = ""
)

@Entity
@Table(name = "inscripciones_feria", uniqueConstraints = [UniqueConstraint(columnNames = ["feria_id", "correo"])])
class InscripcionFeria(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feria_id", nullable = false)
    var feria: Feria? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    var usuario: Usuario? = null,

    @Column(nullable = false, length = 150)
    var correo: String = "",

    @Column(nullable = false)
    var fecha: Instant = Instant.now()
)

@Entity
@Table(name = "notificaciones")
class Notificacion(
    @Id
    @Column(length = 64)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    var usuario: Usuario? = null,

    @Column(nullable = false, length = 150)
    var titulo: String = "",

    @Column(nullable = false, columnDefinition = "TEXT")
    var mensaje: String = "",

    @Column(nullable = false)
    var leida: Boolean = false,

    @Column(length = 50)
    var tipo: String = "GENERAL",

    @Column(name = "referencia_id", length = 64)
    var referenciaId: String? = null,

    @Column(nullable = false)
    var fecha: Instant = Instant.now()
)

@Entity
@Table(name = "password_reset_tokens")
class PasswordResetToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    var usuario: Usuario? = null,

    @Column(nullable = false, unique = true, length = 100)
    var token: String = "",

    @Column(name = "expira_en", nullable = false)
    var expiraEn: Instant = Instant.now(),

    @Column(nullable = false)
    var usado: Boolean = false
)

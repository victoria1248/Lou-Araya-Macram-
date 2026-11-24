package cl.louaraya.backend.model

import jakarta.persistence.*
import java.time.Instant

enum class Rol {
    ADMIN,
    CLIENTE
}

@Entity
@Table(name = "usuarios")
class Usuario(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true, length = 150)
    var correo: String = "",

    @Column(name = "contrasena_hash", nullable = false)
    var contrasenaHash: String = "",

    @Column(nullable = false, length = 100)
    var nombre: String = "",

    @Column(nullable = false, length = 100)
    var apellido: String = "",

    @Column(nullable = false, unique = true, length = 60)
    var usuario: String = "",

    @Column(length = 30)
    var telefono: String? = null,

    @Column(name = "fecha_nacimiento", length = 20)
    var fechaNacimiento: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var rol: Rol = Rol.CLIENTE,

    @Column(name = "foto_url")
    var fotoUrl: String? = null,

    @Column(nullable = false)
    var activo: Boolean = true,

    @Column(name = "creado_en")
    var creadoEn: Instant = Instant.now(),

    @Column(name = "actualizado_en")
    var actualizadoEn: Instant = Instant.now()
)

@Entity
@Table(name = "productos")
class Producto(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(nullable = false, unique = true, length = 50)
    var codigo: String = "",

    @Column(nullable = false, length = 150)
    var nombre: String = "",

    @Column(nullable = false, length = 50)
    var categoria: String = "Hogar",

    @Column(nullable = false, length = 100)
    var material: String = "Algodón natural",

    @Column(nullable = false, length = 100)
    var dimensiones: String = "",

    @Column(nullable = false)
    var precio: Int = 0,

    @Column(nullable = false)
    var stock: Int = 0,

    @Column(name = "imagen_key", nullable = false, length = 50)
    var imagenKey: String = "hogar",

    @Column(nullable = false, columnDefinition = "TEXT")
    var fotos: String = "[]",

    @Column(nullable = false)
    var activo: Boolean = true,

    @Column(name = "creado_en")
    var creadoEn: Instant = Instant.now(),

    @Column(name = "actualizado_en")
    var actualizadoEn: Instant = Instant.now()
)

@Entity
@Table(name = "favoritos", uniqueConstraints = [UniqueConstraint(columnNames = ["usuario_id", "producto_id"])])
class Favorito(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    var usuario: Usuario? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    var producto: Producto? = null,

    @Column(name = "creado_en")
    var creadoEn: Instant = Instant.now()
)

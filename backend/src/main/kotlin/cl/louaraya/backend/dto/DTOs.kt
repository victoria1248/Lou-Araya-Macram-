package cl.louaraya.backend.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

// ==========================================
// AUTENTICACIÓN Y PERFIL DTOs
// ==========================================

data class LoginRequest(
    @field:NotBlank(message = "El usuario o correo es requerido")
    val identificador: String,

    @field:NotBlank(message = "La contraseña es requerida")
    val contrasena: String
)

data class RegistroRequest(
    @field:Email(message = "Correo inválido")
    @field:NotBlank(message = "El correo es requerido")
    val correo: String,

    @field:Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    val contrasena: String,

    @field:NotBlank(message = "El nombre es requerido")
    val nombre: String,

    @field:NotBlank(message = "El apellido es requerido")
    val apellido: String,

    @field:NotBlank(message = "El nombre de usuario es requerido")
    val usuario: String,

    val telefono: String? = null,
    val fechaNacimiento: String? = null
)

data class AuthResponse(
    val token: String,
    val tipo: String = "Bearer",
    val id: Long,
    val correo: String,
    val usuario: String,
    val nombre: String,
    val apellido: String,
    val rol: String,
    val fotoUrl: String? = null
)

data class PerfilResponse(
    val id: Long,
    val correo: String,
    val usuario: String,
    val nombre: String,
    val apellido: String,
    val telefono: String?,
    val fechaNacimiento: String?,
    val rol: String,
    val fotoUrl: String?
)

data class ActualizarPerfilRequest(
    val nombre: String,
    val apellido: String,
    val telefono: String?,
    val fotoUrl: String?,
    val contrasenaActual: String? = null,
    val nuevaContrasena: String? = null
)

data class RecuperarPasswordRequest(
    @field:Email(message = "Correo inválido")
    @field:NotBlank(message = "El correo es requerido")
    val correo: String
)

data class ResetPasswordConfirmRequest(
    @field:NotBlank(message = "El token es requerido")
    val token: String,

    @field:Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
    val nuevaContrasena: String
)

// ==========================================
// PRODUCTOS DTOs
// ==========================================

data class ProductoRequest(
    @field:NotBlank(message = "El código es requerido")
    val codigo: String,

    @field:NotBlank(message = "El nombre es requerido")
    val nombre: String,

    val categoria: String = "Hogar",
    val material: String = "Algodón natural",
    val dimensiones: String = "",

    @field:Positive(message = "El precio debe ser positivo")
    val precio: Int,

    val stock: Int = 0,
    val imagenKey: String = "hogar",
    val fotos: List<String> = emptyList()
)

data class ProductoResponse(
    val id: Int,
    val codigo: String,
    val nombre: String,
    val categoria: String,
    val material: String,
    val dimensiones: String,
    val precio: Int,
    val stock: Int,
    val imagenKey: String,
    val fotos: List<String>
)

// ==========================================
// SOLICITUDES Y PERSONALIZACIÓN DTOs
// ==========================================

data class CrearSolicitudRequest(
    val productoId: Int,

    @field:Positive(message = "La cantidad debe ser al menos 1")
    val unidades: Int = 1,

    @field:NotBlank(message = "La fecha requerida es obligatoria")
    val fechaRequerida: String,

    val comentario: String? = null,
    val personalizada: Boolean = false,
    val color: String = "Original",
    val material: String = "Original",
    val medidas: String = "Original"
)

data class CambiarEstadoSolicitudRequest(
    @field:NotBlank(message = "El nuevo estado es obligatorio")
    val nuevoEstado: String, // PENDIENTE, REVISION, APROBADA, ELABORACION, LISTA, ENTREGADA, CANCELADA
    val nota: String? = null
)

data class MensajeRequest(
    @field:NotBlank(message = "El mensaje no puede estar vacío")
    val contenido: String
)

data class MensajeResponse(
    val id: Long,
    val emisorId: Long,
    val emisorNombre: String,
    val esAdmin: Boolean,
    val contenido: String,
    val fecha: String
)

data class HistorialResponse(
    val id: Long,
    val estadoAnterior: String?,
    val estadoNuevo: String,
    val cambiadoPor: String?,
    val nota: String?,
    val fecha: String
)

data class SolicitudResponse(
    val id: String,
    val correoCliente: String,
    val clienteNombre: String,
    val productoId: Int,
    val productoNombre: String,
    val productoCodigo: String,
    val unidades: Int,
    val fechaRequerida: String,
    val comentario: String?,
    val estado: String,
    val personalizada: Boolean,
    val color: String,
    val material: String,
    val medidas: String,
    val precioEstimado: Int?,
    val creadaEnMillis: Long
)

// ==========================================
// FERIAS, PROMOCIONES Y DASHBOARD DTOs
// ==========================================

data class PromocionRequest(
    @field:NotBlank(message = "El título es obligatorio")
    val titulo: String,

    @field:NotBlank(message = "El mensaje es obligatorio")
    val mensaje: String,

    val tipo: String,
    val finMillis: Long
)

data class PromocionResponse(
    val id: Long,
    val titulo: String,
    val mensaje: String,
    val tipo: String,
    val finMillis: Long,
    val activa: Boolean
)

data class FeriaRequest(
    @field:NotBlank(message = "El nombre de la feria es obligatorio")
    val nombre: String,
    val tipo: String = "Feria",
    val fecha: String,
    val fechaFin: String = "",
    val lugar: String,
    val latitud: Double,
    val longitud: Double,
    val descripcion: String = ""
)

data class FeriaResponse(
    val id: Int,
    val nombre: String,
    val tipo: String,
    val fecha: String,
    val fechaFin: String,
    val lugar: String,
    val latitud: Double,
    val longitud: Double,
    val descripcion: String,
    val estaInscrito: Boolean = false,
    val totalInscritos: Long = 0
)

data class NotificacionResponse(
    val id: String,
    val titulo: String,
    val mensaje: String,
    val leida: Boolean,
    val tipo: String?,
    val fechaMillis: Long
)

data class DashboardStatsResponse(
    val totalProductos: Int,
    val productosDisponibles: Int,
    val stockBajo: Int,
    val solicitudesPendientes: Long,
    val solicitudesConfirmadas: Long,
    val totalClientes: Long
)

data class MensajeGenericoResponse(
    val exito: Boolean,
    val mensaje: String
)

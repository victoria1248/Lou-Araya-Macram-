package cl.louaraya.backend.service

import cl.louaraya.backend.dto.*
import cl.louaraya.backend.exception.BadRequestException
import cl.louaraya.backend.exception.ForbiddenException
import cl.louaraya.backend.exception.ResourceNotFoundException
import cl.louaraya.backend.model.*
import cl.louaraya.backend.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class SolicitudService(
    private val solicitudRepository: SolicitudRepository,
    private val productoRepository: ProductoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val historialRepository: HistorialSolicitudRepository,
    private val mensajeRepository: MensajeSolicitudRepository,
    private val notificacionRepository: NotificacionRepository
) {
    @Transactional
    fun crear(correo: String, req: CrearSolicitudRequest): SolicitudResponse {
        val u = usuarioRepository.findByCorreoIgnoreCase(correo)
            .orElseThrow { ResourceNotFoundException("Usuario no encontrado") }
        val p = productoRepository.findById(req.productoId)
            .orElseThrow { ResourceNotFoundException("El producto seleccionado ya no existe") }

        if (!p.activo) throw BadRequestException("El producto no está disponible")
        if (req.unidades < 1 || req.unidades > p.stock) {
            throw BadRequestException("La cantidad debe estar entre 1 y el stock disponible (${p.stock})")
        }

        val solicitudId = UUID.randomUUID().toString()
        val s = Solicitud(
            id = solicitudId,
            usuario = u,
            producto = p,
            unidades = req.unidades,
            fechaRequerida = req.fechaRequerida.trim(),
            comentario = req.comentario?.trim(),
            estado = EstadoSolicitud.PENDIENTE,
            personalizada = req.personalizada,
            color = if (req.personalizada) req.color.trim() else "Original",
            material = if (req.personalizada) req.material.trim() else "Original",
            medidas = if (req.personalizada) req.medidas.trim() else "Original",
            precioEstimado = p.precio * req.unidades
        )
        val guardada = solicitudRepository.save(s)

        // Registrar en historial
        historialRepository.save(
            HistorialSolicitud(
                solicitud = guardada,
                estadoAnterior = null,
                estadoNuevo = EstadoSolicitud.PENDIENTE.name,
                cambiadoPor = u,
                nota = "Solicitud creada por el cliente"
            )
        )

        // Notificar al cliente
        notificacionRepository.save(
            Notificacion(
                id = UUID.randomUUID().toString(),
                usuario = u,
                titulo = "Solicitud enviada con éxito",
                mensaje = "Tu solicitud para '${p.nombre}' está pendiente de confirmación.",
                tipo = "SOLICITUD",
                referenciaId = guardada.id
            )
        )

        // Notificar a todos los administradores
        usuarioRepository.findByRol(Rol.ADMIN).forEach { admin ->
            notificacionRepository.save(
                Notificacion(
                    id = UUID.randomUUID().toString(),
                    usuario = admin,
                    titulo = "Nueva solicitud artesanal",
                    mensaje = "${u.nombre} ha solicitado ${req.unidades} unidad(es) de '${p.nombre}'.",
                    tipo = "NUEVA_SOLICITUD",
                    referenciaId = guardada.id
                )
            )
        }

        return toResponse(guardada)
    }

    fun listar(correo: String, esAdmin: Boolean, estado: String?): List<SolicitudResponse> {
        val u = usuarioRepository.findByCorreoIgnoreCase(correo)
            .orElseThrow { ResourceNotFoundException("Usuario no encontrado") }

        val solicitudes = if (esAdmin) {
            if (!estado.isNullOrBlank() && estado != "Todas") {
                try {
                    val st = EstadoSolicitud.valueOf(estado.trim().uppercase())
                    solicitudRepository.findByEstadoOrderByCreadaEnDesc(st)
                } catch (_: Exception) {
                    solicitudRepository.findAllByOrderByCreadaEnDesc()
                }
            } else {
                solicitudRepository.findAllByOrderByCreadaEnDesc()
            }
        } else {
            solicitudRepository.findByUsuarioIdOrderByCreadaEnDesc(u.id!!)
        }

        return solicitudes.map { toResponse(it) }
    }

    fun obtenerPorId(id: String, correo: String, esAdmin: Boolean): SolicitudResponse {
        val s = solicitudRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Solicitud no encontrada") }
        if (!esAdmin && !s.usuario!!.correo.equals(correo, ignoreCase = true)) {
            throw ForbiddenException("No tienes permiso para ver esta solicitud")
        }
        return toResponse(s)
    }

    @Transactional
    fun cambiarEstado(id: String, adminCorreo: String, req: CambiarEstadoSolicitudRequest): SolicitudResponse {
        val s = solicitudRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Solicitud no encontrada") }
        val admin = usuarioRepository.findByCorreoIgnoreCase(adminCorreo)
            .orElseThrow { ResourceNotFoundException("Administrador no encontrado") }

        val nuevoEstado = try {
            EstadoSolicitud.valueOf(req.nuevoEstado.trim().uppercase())
        } catch (_: Exception) {
            throw BadRequestException("Estado '${req.nuevoEstado}' no reconocido")
        }

        val estadoAnterior = s.estado
        if (estadoAnterior == nuevoEstado) return toResponse(s)

        // Manejo y reserva atómica de inventario
        val p = s.producto!!
        if (nuevoEstado == EstadoSolicitud.APROBADA || nuevoEstado == EstadoSolicitud.ELABORACION) {
            if (estadoAnterior == EstadoSolicitud.PENDIENTE || estadoAnterior == EstadoSolicitud.REVISION) {
                if (p.stock < s.unidades) {
                    throw BadRequestException("No hay stock suficiente para aprobar el pedido (Stock: ${p.stock})")
                }
                p.stock -= s.unidades
                productoRepository.save(p)
            }
        } else if (nuevoEstado == EstadoSolicitud.CANCELADA) {
            if (estadoAnterior == EstadoSolicitud.APROBADA || estadoAnterior == EstadoSolicitud.ELABORACION || estadoAnterior == EstadoSolicitud.LISTA) {
                p.stock += s.unidades
                productoRepository.save(p)
            }
        }

        s.estado = nuevoEstado
        s.actualizadaEn = Instant.now()
        val guardada = solicitudRepository.save(s)

        // Guardar trazabilidad
        historialRepository.save(
            HistorialSolicitud(
                solicitud = guardada,
                estadoAnterior = estadoAnterior.name,
                estadoNuevo = nuevoEstado.name,
                cambiadoPor = admin,
                nota = req.nota ?: "Actualizado por administración"
            )
        )

        // Notificar al cliente dueño de la solicitud
        notificacionRepository.save(
            Notificacion(
                id = UUID.randomUUID().toString(),
                usuario = s.usuario!!,
                titulo = "Estado actualizado: ${p.nombre}",
                mensaje = "Tu solicitud pasó a: ${nuevoEstado.name}. ${req.nota ?: ""}".trim(),
                tipo = "ESTADO_SOLICITUD",
                referenciaId = guardada.id
            )
        )

        return toResponse(guardada)
    }

    fun listarHistorial(solicitudId: String): List<HistorialResponse> {
        return historialRepository.findBySolicitudIdOrderByFechaAsc(solicitudId).map {
            HistorialResponse(
                id = it.id!!,
                estadoAnterior = it.estadoAnterior,
                estadoNuevo = it.estadoNuevo,
                cambiadoPor = it.cambiadoPor?.nombre ?: "Sistema",
                nota = it.nota,
                fecha = it.fecha.toString()
            )
        }
    }

    @Transactional
    fun enviarMensaje(solicitudId: String, correo: String, req: MensajeRequest): MensajeResponse {
        val s = solicitudRepository.findById(solicitudId)
            .orElseThrow { ResourceNotFoundException("Solicitud no encontrada") }
        val emisor = usuarioRepository.findByCorreoIgnoreCase(correo)
            .orElseThrow { ResourceNotFoundException("Usuario no encontrado") }

        val esAdmin = emisor.rol == Rol.ADMIN
        val esDuenio = s.usuario!!.id == emisor.id

        if (!esAdmin && !esDuenio) {
            throw ForbiddenException("No puedes enviar mensajes en esta solicitud")
        }

        val m = MensajeSolicitud(
            solicitud = s,
            emisor = emisor,
            contenido = req.contenido.trim()
        )
        val guardado = mensajeRepository.save(m)

        // Notificar a la contraparte
        val receptor = if (esAdmin) s.usuario!! else usuarioRepository.findByRol(Rol.ADMIN).firstOrNull()
        if (receptor != null) {
            notificacionRepository.save(
                Notificacion(
                    id = UUID.randomUUID().toString(),
                    usuario = receptor,
                    titulo = "Nuevo mensaje en solicitud: ${s.producto?.nombre}",
                    mensaje = "${emisor.nombre}: ${req.contenido.take(80)}",
                    tipo = "MENSAJE_SOLICITUD",
                    referenciaId = s.id
                )
            )
        }

        return toMensajeResponse(guardado)
    }

    fun listarMensajes(solicitudId: String, correo: String, esAdmin: Boolean): List<MensajeResponse> {
        val s = solicitudRepository.findById(solicitudId)
            .orElseThrow { ResourceNotFoundException("Solicitud no encontrada") }
        if (!esAdmin && !s.usuario!!.correo.equals(correo, ignoreCase = true)) {
            throw ForbiddenException("No tienes permiso para ver los mensajes")
        }
        return mensajeRepository.findBySolicitudIdOrderByFechaAsc(solicitudId).map { toMensajeResponse(it) }
    }

    private fun toResponse(s: Solicitud): SolicitudResponse {
        return SolicitudResponse(
            id = s.id,
            correoCliente = s.usuario!!.correo,
            clienteNombre = "${s.usuario!!.nombre} ${s.usuario!!.apellido}".trim(),
            productoId = s.producto!!.id!!,
            productoNombre = s.producto!!.nombre,
            productoCodigo = s.producto!!.codigo,
            unidades = s.unidades,
            fechaRequerida = s.fechaRequerida,
            comentario = s.comentario,
            estado = s.estado.name,
            personalizada = s.personalizada,
            color = s.color,
            material = s.material,
            medidas = s.medidas,
            precioEstimado = s.precioEstimado,
            creadaEnMillis = s.creadaEn.toEpochMilli()
        )
    }

    private fun toMensajeResponse(m: MensajeSolicitud): MensajeResponse {
        return MensajeResponse(
            id = m.id!!,
            emisorId = m.emisor!!.id!!,
            emisorNombre = "${m.emisor!!.nombre} ${m.emisor!!.apellido}".trim(),
            esAdmin = m.emisor!!.rol == Rol.ADMIN,
            contenido = m.contenido,
            fecha = m.fecha.toString()
        )
    }
}

@Service
class FavoritoService(
    private val favoritoRepository: FavoritoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val productoRepository: ProductoRepository
) {
    fun listarIds(correo: String): Set<Int> {
        val u = usuarioRepository.findByCorreoIgnoreCase(correo)
            .orElseThrow { ResourceNotFoundException("Usuario no encontrado") }
        return favoritoRepository.findByUsuarioId(u.id!!).mapNotNull { it.producto?.id }.toSet()
    }

    @Transactional
    fun alternar(correo: String, productoId: Int): Boolean {
        val u = usuarioRepository.findByCorreoIgnoreCase(correo)
            .orElseThrow { ResourceNotFoundException("Usuario no encontrado") }
        val p = productoRepository.findById(productoId)
            .orElseThrow { ResourceNotFoundException("Producto no encontrado") }

        return if (favoritoRepository.existsByUsuarioIdAndProductoId(u.id!!, p.id!!)) {
            favoritoRepository.deleteByUsuarioIdAndProductoId(u.id!!, p.id!!)
            false
        } else {
            favoritoRepository.save(Favorito(usuario = u, producto = p))
            true
        }
    }
}

@Service
class CampanaService(private val campanaRepository: CampanaRepository) {
    fun listar(): List<Campana> = campanaRepository.findByActivaTrueOrderByOrdenAsc()
    fun guardar(c: Campana): Campana = campanaRepository.save(c)
}

@Service
class PromocionService(
    private val promocionRepository: PromocionRepository,
    private val usuarioRepository: UsuarioRepository,
    private val notificacionRepository: NotificacionRepository
) {
    fun activas(): List<PromocionResponse> {
        val ahora = System.currentTimeMillis()
        return promocionRepository.findByActivaTrueAndFinMillisGreaterThanOrderByFinMillisAsc(ahora)
            .map { toResponse(it) }
    }

    fun todas(): List<PromocionResponse> =
        promocionRepository.findAllByOrderByCreadaEnDesc().map { toResponse(it) }

    @Transactional
    fun crear(req: PromocionRequest): PromocionResponse {
        val p = Promocion(
            titulo = req.titulo.trim(),
            mensaje = req.mensaje.trim(),
            tipo = req.tipo.trim(),
            finMillis = req.finMillis,
            activa = true
        )
        val guardada = promocionRepository.save(p)

        // Difundir notificación a todos los clientes registrados
        usuarioRepository.findByRol(Rol.CLIENTE).forEach { cliente ->
            notificacionRepository.save(
                Notificacion(
                    id = UUID.randomUUID().toString(),
                    usuario = cliente,
                    titulo = "✦ ${guardada.titulo}",
                    mensaje = guardada.mensaje,
                    tipo = "PROMOCION",
                    referenciaId = guardada.id.toString()
                )
            )
        }

        return toResponse(guardada)
    }

    @Transactional
    fun eliminar(id: Long) {
        promocionRepository.deleteById(id)
    }

    private fun toResponse(p: Promocion) = PromocionResponse(
        id = p.id!!,
        titulo = p.titulo,
        mensaje = p.mensaje,
        tipo = p.tipo,
        finMillis = p.finMillis,
        activa = p.activa
    )
}

@Service
class FeriaService(
    private val feriaRepository: FeriaRepository,
    private val inscripcionRepository: InscripcionFeriaRepository,
    private val usuarioRepository: UsuarioRepository
) {
    fun listar(correoOpcional: String?): List<FeriaResponse> {
        val ferias = feriaRepository.findAllByOrderByIdAsc()
        return ferias.map { f ->
            val estaInscrito = if (!correoOpcional.isNullOrBlank()) {
                inscripcionRepository.existsByFeriaIdAndCorreoIgnoreCase(f.id!!, correoOpcional)
            } else false
            val inscritos = inscripcionRepository.countByFeriaId(f.id!!)
            FeriaResponse(
                id = f.id!!,
                nombre = f.nombre,
                tipo = f.tipo,
                fecha = f.fecha,
                fechaFin = f.fechaFin,
                lugar = f.lugar,
                latitud = f.latitud,
                longitud = f.longitud,
                descripcion = f.descripcion,
                estaInscrito = estaInscrito,
                totalInscritos = inscritos
            )
        }
    }

    @Transactional
    fun guardar(req: FeriaRequest, id: Int? = null): FeriaResponse {
        val f = if (id != null) {
            val existente = feriaRepository.findById(id).orElseThrow { ResourceNotFoundException("Feria no encontrada") }
            existente.nombre = req.nombre.trim()
            existente.tipo = req.tipo
            existente.fecha = req.fecha
            existente.fechaFin = req.fechaFin
            existente.lugar = req.lugar
            existente.latitud = req.latitud
            existente.longitud = req.longitud
            existente.descripcion = req.descripcion
            existente
        } else {
            Feria(
                nombre = req.nombre.trim(),
                tipo = req.tipo,
                fecha = req.fecha,
                fechaFin = req.fechaFin,
                lugar = req.lugar,
                latitud = req.latitud,
                longitud = req.longitud,
                descripcion = req.descripcion
            )
        }
        val guardada = feriaRepository.save(f)
        return FeriaResponse(
            id = guardada.id!!,
            nombre = guardada.nombre,
            tipo = guardada.tipo,
            fecha = guardada.fecha,
            fechaFin = guardada.fechaFin,
            lugar = guardada.lugar,
            latitud = guardada.latitud,
            longitud = guardada.longitud,
            descripcion = guardada.descripcion
        )
    }

    @Transactional
    fun eliminar(id: Int) {
        feriaRepository.deleteById(id)
    }

    @Transactional
    fun inscribir(feriaId: Int, correo: String): Boolean {
        val f = feriaRepository.findById(feriaId)
            .orElseThrow { ResourceNotFoundException("Feria no encontrada") }
        val email = correo.trim().lowercase()

        if (inscripcionRepository.existsByFeriaIdAndCorreoIgnoreCase(f.id!!, email)) {
            inscripcionRepository.deleteByFeriaIdAndCorreoIgnoreCase(f.id!!, email)
            return false
        } else {
            val u = usuarioRepository.findByCorreoIgnoreCase(email).orElse(null)
            inscripcionRepository.save(InscripcionFeria(feria = f, usuario = u, correo = email))
            return true
        }
    }
}

@Service
class NotificacionService(
    private val notificacionRepository: NotificacionRepository,
    private val usuarioRepository: UsuarioRepository
) {
    fun listar(correo: String): List<NotificacionResponse> {
        val u = usuarioRepository.findByCorreoIgnoreCase(correo)
            .orElseThrow { ResourceNotFoundException("Usuario no encontrado") }
        return notificacionRepository.findByUsuarioIdOrderByFechaDesc(u.id!!).map {
            NotificacionResponse(
                id = it.id,
                titulo = it.titulo,
                mensaje = it.mensaje,
                leida = it.leida,
                tipo = it.tipo,
                fechaMillis = it.fecha.toEpochMilli()
            )
        }
    }

    @Transactional
    fun marcarLeida(id: String, correo: String) {
        val notif = notificacionRepository.findById(id).orElse(null) ?: return
        if (notif.usuario!!.correo.equals(correo, ignoreCase = true)) {
            notif.leida = true
            notificacionRepository.save(notif)
        }
    }

    @Transactional
    fun marcarTodasLeidas(correo: String) {
        val u = usuarioRepository.findByCorreoIgnoreCase(correo).orElse(null) ?: return
        val lista = notificacionRepository.findByUsuarioIdOrderByFechaDesc(u.id!!)
        lista.forEach { it.leida = true }
        notificacionRepository.saveAll(lista)
    }
}

@Service
class DashboardService(
    private val productoRepository: ProductoRepository,
    private val solicitudRepository: SolicitudRepository,
    private val usuarioRepository: UsuarioRepository
) {
    fun obtenerEstadisticas(): DashboardStatsResponse {
        val productos = productoRepository.findByActivoTrueOrderByIdAsc()
        val total = productos.size
        val disponibles = productos.count { it.stock > 0 }
        val bajoStock = productos.count { it.stock in 0..3 }
        val pendientes = solicitudRepository.countByEstado(EstadoSolicitud.PENDIENTE)
        val confirmadas = solicitudRepository.countByEstado(EstadoSolicitud.APROBADA) +
                solicitudRepository.countByEstado(EstadoSolicitud.ELABORACION)
        val clientes = usuarioRepository.findByRol(Rol.CLIENTE).size.toLong()

        return DashboardStatsResponse(
            totalProductos = total,
            productosDisponibles = disponibles,
            stockBajo = bajoStock,
            solicitudesPendientes = pendientes,
            solicitudesConfirmadas = confirmadas,
            totalClientes = clientes
        )
    }
}

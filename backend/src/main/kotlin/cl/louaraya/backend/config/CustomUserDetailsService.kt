package cl.louaraya.backend.config

import cl.louaraya.backend.repository.UsuarioRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val usuarioRepository: UsuarioRepository
) : UserDetailsService {

    override fun loadUserByUsername(identificador: String): UserDetails {
        // Busca por correo electrónico o por nombre de usuario
        val usuario = usuarioRepository.findByCorreoIgnoreCase(identificador.trim())
            .or { usuarioRepository.findByUsuarioIgnoreCase(identificador.trim()) }
            .orElseThrow { UsernameNotFoundException("Usuario no encontrado con identificador: $identificador") }

        if (!usuario.activo) {
            throw UsernameNotFoundException("La cuenta está desactivada")
        }

        val authority = SimpleGrantedAuthority("ROLE_${usuario.rol.name}")

        return User(
            usuario.correo,
            usuario.contrasenaHash,
            listOf(authority)
        )
    }
}

package cl.louaraya.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val userDetailsService: CustomUserDetailsService,
    private val jwtAuthFilter: JwtAuthFilter
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder(10)

    @Bean
    fun authenticationProvider(): DaoAuthenticationProvider {
        val authProvider = DaoAuthenticationProvider()
        authProvider.setUserDetailsService(userDetailsService)
        authProvider.setPasswordEncoder(passwordEncoder())
        return authProvider
    }

    @Bean
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager =
        authConfig.authenticationManager

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // OpenAPI / Swagger endpoints públicos
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                    // Autenticación pública
                    .requestMatchers("/api/auth/login", "/api/auth/registro", "/api/auth/recuperar-password", "/api/auth/reset-password").permitAll()

                    // Catálogo público de productos, campañas y ferias
                    .requestMatchers(HttpMethod.GET, "/api/productos/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/campanas/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/promociones/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/ferias/**").permitAll()

                    // Endpoints exclusivos de ADMINISTRADOR
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/productos/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/promociones/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/promociones/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/campanas/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/campanas/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/ferias/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/ferias/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/ferias/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/solicitudes/*/estado").hasRole("ADMIN")

                    // Cualquier otra petición requiere autenticación
                    .anyRequest().authenticated()
            }
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOriginPatterns = listOf("*")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}

package freelancer_platform.config;

import freelancer_platform.security.JwtAuthenticationEntryPoint;
import freelancer_platform.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint authenticationEntryPoint,
            CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Allow CORS preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public endpoints
                        .requestMatchers(HttpMethod.GET, "/", "/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users/register", "/users/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/*/profile").permitAll()
                        .requestMatchers(HttpMethod.GET, "/projects", "/projects/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/proposals", "/proposals/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/reviews/**").permitAll()

                        // User profile endpoints (authenticated)
                        .requestMatchers(HttpMethod.GET, "/users/profile").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/users/profile").authenticated()

                        // Dashboard endpoints (authenticated)
                        .requestMatchers(HttpMethod.GET, "/dashboard/client").authenticated()
                        .requestMatchers(HttpMethod.GET, "/dashboard/freelancer").authenticated()

                        // Project endpoints
                        .requestMatchers(HttpMethod.POST, "/projects").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/projects/*/status").authenticated()

                        // Proposal endpoints
                        .requestMatchers(HttpMethod.POST, "/proposals").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/proposals/*/accept").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/proposals/*/reject").authenticated()

                        // Review endpoints
                        .requestMatchers(HttpMethod.POST, "/reviews").authenticated()

                        // Any other request
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

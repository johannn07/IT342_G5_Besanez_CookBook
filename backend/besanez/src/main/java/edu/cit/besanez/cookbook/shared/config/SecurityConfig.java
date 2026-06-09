package edu.cit.besanez.cookbook.shared.config;

import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final OAuth2SuccessHandler oauth2SuccessHandler;
        private final Filter jwtAuthenticationFilter;
        private final AdminAuthorizationFilter adminAuthorizationFilter;

        public SecurityConfig(OAuth2SuccessHandler oauth2SuccessHandler,
                        @Qualifier("jwtAuthenticationFilter") Filter jwtAuthenticationFilter,
                        AdminAuthorizationFilter adminAuthorizationFilter) {
                this.oauth2SuccessHandler = oauth2SuccessHandler;
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
                this.adminAuthorizationFilter = adminAuthorizationFilter;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())

                                // JWT is stateless — no session should ever be created
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                .authorizeHttpRequests(auth -> auth
                                                // Auth endpoints — always public
                                                .requestMatchers("/api/auth/**").permitAll()

                                                // OAuth2 flow — must be public
                                                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                                                // Public recipe browsing — no login required
                                                .requestMatchers(HttpMethod.GET, "/api/recipe/public").permitAll()

                                                // Shared recipe access via token — no login required
                                                .requestMatchers(HttpMethod.GET, "/api/share/*").permitAll()

                                                // Admin endpoints — must be authenticated and have admin role
                                                .requestMatchers("/api/admin/**").authenticated()

                                                // Mobile Google login endpoint — must be public for mobile app to use
                                                .requestMatchers("/api/auth/google/mobile").permitAll()

                                                // Everything else requires a valid JWT
                                                .anyRequest().authenticated())

                                // Wire the JWT filter before Spring's default username/password filter
                                .addFilterBefore(jwtAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class)

                                .oauth2Login(oauth2 -> oauth2
                                                .successHandler(oauth2SuccessHandler)
                                                .failureUrl("http://localhost:3000/?error=true"));

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(Arrays.asList(
                                "https://cook-book-mu-flame.vercel.app",
                                "http://localhost:3000",
                                "http://localhost:5173"));
                configuration.setAllowedMethods(Arrays.asList(
                                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                configuration.setAllowedHeaders(Arrays.asList("*"));
                configuration.setAllowCredentials(true);
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
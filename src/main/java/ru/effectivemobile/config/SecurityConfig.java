package ru.effectivemobile.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.effectivemobile.security.JwtAuthenticationEntryPoint;
import ru.effectivemobile.security.JwtAuthenticationFilter;

/**
 * Конфигурация безопасности приложения
 * @author EffectiveMobile Team
 */
@SuppressWarnings("ALL")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAuthenticationFilter authenticationFilter;

    public SecurityConfig(JwtAuthenticationEntryPoint authenticationEntryPoint,
                          JwtAuthenticationFilter authenticationFilter) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.authenticationFilter = authenticationFilter;
    }

    /**
     * Бин для кодирования паролей
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Бин менеджера аутентификации
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Конфигурация цепочки фильтров безопасности
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/cards/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/transfers/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
package pl.Dayfit.Florae.Configurations;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pl.Dayfit.Florae.Filters.JWTFilter;

/**
 * Security configuration class for customizing application security settings.
 * This class is annotated with {@code @Configuration} and {@code @EnableWebSecurity}
 * to indicate that it provides Spring Security configuration.

 * Key Responsibilities:
 * - Defines the SecurityFilterChain to configure security policies including CSRF,
 *   authorization rules, and session management.
 * - Integrates a custom JWT filter to validate tokens and ensure stateless security.
 * - Configures authentication mechanisms including password encoding
 *   and data sourcing from UserDetailsService to manage user-specific data.

 * Annotations:
 * - {@code @Configuration}: Marks this class as a Spring configuration class.
 * - {@code @EnableWebSecurity}: Enables Spring Security for the application.
 * - {@code @RequiredArgsConstructor}: Generates a constructor for final fields, simplifying dependency injection.

 * Beans:
 * - SecurityFilterChain: Configures HTTP security, including authentication rules,
 *   permitted endpoints, session policy, and adding custom filters.
 * - BCryptPasswordEncoder: A password encoder that uses the BCrypt hashing algorithm
 *   for secure password storage.
 * - AuthenticationProvider: Configures authentication via a DAO provider using
 *   the UserDetailsService and password encoder.
 * - AuthenticationManager: Manages authentication through the configuration defined in Spring Security.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final UserDetailsService userDetailsService;
    private final JWTFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
    {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> {
                    request.dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll(); //To allow the async servlet to work properly
                    request.requestMatchers("/register", "/login").permitAll();
                    request.anyRequest().authenticated();
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder()
    {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationProvider authenticationProvider()
    {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(bCryptPasswordEncoder());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}

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

import java.security.SecureRandom;

/**
 * Security configuration class for customizing application security settings.
 * This class is annotated with {@code @Configuration} and {@code @EnableWebSecurity}
 * to indicate that it provides Spring Security configuration.

 * <p>Key Responsibilities:</p>
 * <ul>
 *  <li> Defines the SecurityFilterChain to configure security policies including CSRF,
 *   authorization rules, and session management.</li>
 *  <li> Integrates a custom JWT filter to validate tokens and ensure stateless security.</li>
 *  <li> Configures authentication mechanisms including password encoding
 *   and data sourcing from UserDetailsService to manage user-specific data.</li>
 * </ul>
 * <p>Annotations:</p>
 * <ul>
 * <li> {@code @Configuration}: Marks this class as a Spring configuration class.</li>
 * <li> {@code @EnableWebSecurity}: Enables Spring Security for the application.</li>
 * <li> {@code @RequiredArgsConstructor}: Generates a constructor for final fields, simplifying dependency injection.</li>
 * </ul>
 * <p>Beans:</p>
 * <ul>
 *  <li> SecurityFilterChain: Configures HTTP security, including authentication rules,
 *   permitted endpoints, session policy, and adding custom filters.</li>
 *  <li> BCryptPasswordEncoder: A password encoder that uses the BCrypt hashing algorithm
 *   for secure password storage.</li>
 *  <li> AuthenticationProvider: Configures authentication via a DAO provider using
 *   the UserDetailsService and password encoder.</li>
 *  <li> AuthenticationManager: Manages authentication through the configuration defined in Spring Security.</li>
 *  <li> SecureRandom: Generates cryptographically secure pseudo-random values suitable for security-sensitive operations (e.g., tokens, keys)</li>
 * </ul>
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
                    request.requestMatchers("/auth/register", "/auth/login", "/auth/refresh").permitAll();
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
    public SecureRandom secureRandom()
    {
        return new SecureRandom();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}

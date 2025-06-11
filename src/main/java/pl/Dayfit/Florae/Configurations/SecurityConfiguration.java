package pl.Dayfit.Florae.Configurations;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.ForwardedHeaderFilter;
import pl.Dayfit.Florae.Auth.ApiKeyAuthenticationCandidate;
import pl.Dayfit.Florae.Auth.ApiKeyAuthenticationToken;
import pl.Dayfit.Florae.Auth.FloraeAuthenticationEntryPoint;
import pl.Dayfit.Florae.Entities.ApiKey;
import pl.Dayfit.Florae.Filters.ApiKeyFilter;
import pl.Dayfit.Florae.Filters.JWTFilter;
import pl.Dayfit.Florae.Services.Auth.API.ApiKeyService;

import java.security.SecureRandom;
import java.util.List;

/**
 * Security configuration class for customizing application security settings.
 * This class is annotated with {@code @Configuration} and {@code @EnableWebSecurity}
 * to indicate that it provides Spring Security configuration.

 * <p>Key Responsibilities:</p>
 * <ul>
 *  <li> Defines the SecurityFilterChain to configure security policies including CSRF,
 *   authorization rules, and session management.</li>
 *  <li> Integrates a custom JWT filter to validate tokens and ensure stateless security.</li>
 *  <li> Integrates an API Key filter for authenticating API calls with API keys.</li>
 *  <li> Configures multiple authentication mechanisms including JWT, API Key, and standard username/password.</li>
 *  <li> Configures authentication providers for both API Keys and user credentials.</li>
 *  <li> Manages user-specific data sourcing from UserDetailsService and secure password encoding.</li>
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
 *  <li> ApiKeyFilter: Filter that authenticates requests containing an API key header.</li>
 *  <li> AuthenticationManager: Manages authentication through multiple providers configured in Spring Security.</li>
 *  <li> AuthenticationEntryPoint: Custom entry point for handling authentication failures.</li>
 *  <li> ApiKeyAuthenticationProvider: Provider that validates and authenticates API key credentials.</li>
 *  <li> DaoAuthenticationProvider: Configures authentication via a DAO provider using
 *   the UserDetailsService and password encoder.</li>
 *  <li> SecureRandom: Generates cryptographically secure pseudo-random values suitable for security-sensitive operations (e.g., tokens, keys)</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    @Value("${allowed.origins.patterns:localhost*}")
    private String ALLOWED_ORIGINS_PATTERNS;

    @Value("${security.protected-paths}")
    private String PROTECTED_PATHS;

    private final ApiKeyService apiKeyService;
    private final UserDetailsService userDetailsService;
    private final JWTFilter jwtFilter;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authManager) throws Exception
    {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> {
                    request.dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll(); //To allow the async servlet to work properly
                    request.requestMatchers(PROTECTED_PATHS.split(",")).authenticated();
                    request.anyRequest().permitAll();
                })
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(apiKeyFilter(authManager), JWTFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(List.of(ALLOWED_ORIGINS_PATTERNS.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    @ConditionalOnProperty(name="florae.forwarded-headers.enabled", havingValue = "true")
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter()
    {
        FilterRegistrationBean<ForwardedHeaderFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new ForwardedHeaderFilter());
        filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return filterRegistrationBean;
    }

    @Bean
    public ApiKeyFilter apiKeyFilter(AuthenticationManager authenticationManager)
    {
        return new ApiKeyFilter(apiKeyService, authenticationManager);
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(apiKeyAuthenticationProvider(apiKeyService))
                .authenticationProvider(daoAuthenticationProvider())
                .build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new FloraeAuthenticationEntryPoint();
    }

    @Bean
    AuthenticationProvider apiKeyAuthenticationProvider(ApiKeyService apiKeyService)
    {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                if (!(authentication instanceof ApiKeyAuthenticationCandidate))
                {
                    return null;
                }

                String apiKeyValue = ((ApiKey) authentication.getCredentials()).getKeyValue();

                if (!apiKeyService.isValidByAuthentication(authentication))
                {
                    throw new BadCredentialsException("Invalid API key.");
                }

                ApiKey apiKey = apiKeyService.getApiKeyByHash(apiKeyValue);
                return new ApiKeyAuthenticationToken(apiKey);
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return ApiKeyAuthenticationCandidate.class.isAssignableFrom(authentication);
            }
        };
    }

    @Bean
    public AuthenticationProvider daoAuthenticationProvider()
    {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecureRandom secureRandom()
    {
        return new SecureRandom();
    }
}

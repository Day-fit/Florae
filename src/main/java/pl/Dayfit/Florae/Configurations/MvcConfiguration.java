package pl.Dayfit.Florae.Configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * A configuration class that implements the {@link WebMvcConfigurer} interface to customize
 * the Spring MVC settings, specifically to configure CORS (Cross-Origin Resource Sharing) mappings.

 * This class reads the allowed origin patterns from a configuration property and applies those
 * patterns to the application using the {@link CorsRegistry}.
 */
@Configuration
public class MvcConfiguration implements WebMvcConfigurer {

    @Value("${allowed.origins.patterns}:localhost*")
    private String[] ALLOWED_ORIGINS_PATTERNS;

    public void addCorsMappings(CorsRegistry registry)
    {
        registry
            .addMapping("/**")
            .allowCredentials(true)
            .allowedOriginPatterns(ALLOWED_ORIGINS_PATTERNS);
    }    
}

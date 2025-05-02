package pl.Dayfit.Florae.Configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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

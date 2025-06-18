package pl.Dayfit.Florae.Configurations;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "security")
public class SecurityPropertiesConfiguration {
    private @Setter @Getter List<String> protectedPaths;
}

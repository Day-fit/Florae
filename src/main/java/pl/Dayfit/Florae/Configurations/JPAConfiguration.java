package pl.Dayfit.Florae.Configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Configuration class for setting up JPA repositories in the application.

 * This class uses the {@code @Configuration} annotation to indicate that it is
 * a source of bean definitions and {@code @EnableJpaRepositories} to enable
 * support for JPA repositories. The base package for scanning JPA repository
 * interfaces is specified.
 *
 */
@Configuration
@EnableJpaRepositories(basePackages = "pl.Dayfit.Florae.Repositories.JPA")

public class JPAConfiguration {
}

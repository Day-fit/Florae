package pl.Dayfit.Florae.Configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "pl.Dayfit.Florae.Repositories")
public class RepositoriesConfiguration {
}

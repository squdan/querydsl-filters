package es.squdan.querydsl.filters.configuration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan("es.squdan")
@EnableJpaRepositories(basePackages = "es.squdan")
public class DatabaseTestConfiguration {
}

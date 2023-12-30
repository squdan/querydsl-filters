package io.github.squdan.querydsl.filters.configuration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan("io.github.squdan")
@EnableJpaRepositories(basePackages = "io.github.squdan")
public class DatabaseTestConfiguration {
}

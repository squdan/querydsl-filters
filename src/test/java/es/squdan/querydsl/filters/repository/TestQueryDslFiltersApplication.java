package es.squdan.querydsl.filters.repository;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.TimeZone;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "es.squdan")
@ComponentScan(basePackages = "es.squdan")
public class TestQueryDslFiltersApplication {

    public static void main(String[] args) {
        // Configure default timezone
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        new SpringApplicationBuilder(TestQueryDslFiltersApplication.class).run(args);
    }
}

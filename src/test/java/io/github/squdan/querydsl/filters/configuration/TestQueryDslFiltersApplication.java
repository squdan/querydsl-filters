package io.github.squdan.querydsl.filters.configuration;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

import java.util.TimeZone;

@SpringBootApplication
@ComponentScan(basePackages = "io.github.squdan")
public class TestQueryDslFiltersApplication {

    public static void main(String[] args) {
        // Configure default timezone
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        // Run app for testing
        new SpringApplicationBuilder(TestQueryDslFiltersApplication.class).run(args);
    }
}

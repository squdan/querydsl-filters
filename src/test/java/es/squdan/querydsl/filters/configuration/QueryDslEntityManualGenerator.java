package es.squdan.querydsl.filters.configuration;

import com.querydsl.codegen.GenericExporter;
import com.querydsl.codegen.Keywords;
import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
public class QueryDslEntityManualGenerator {

    public static void main(String[] args) {
        if (ArrayUtils.isNotEmpty(args)) {
            // Search entity files
            final List<Package> entityPackages = Stream.of(args)
                    .map(QueryDslEntityManualGenerator::getPackageByClassName)
                    .filter(Objects::nonNull)
                    .toList();

            // Generate QueryDsl files from entities
            generate(entityPackages.toArray(new Package[]{}));
        }
    }

    public static void generate(final Package... entityPackages) {
        log.info("Generating QueryDsl test entities: '{}'", Arrays.toString(entityPackages));

        // Generate QEntities from TestEntities
        GenericExporter exporter = new GenericExporter();
        exporter.setKeywords(Keywords.JPA);
        exporter.setEntityAnnotation(Entity.class);
        exporter.setEmbeddableAnnotation(Embeddable.class);
        exporter.setEmbeddedAnnotation(Embedded.class);
        exporter.setSupertypeAnnotation(MappedSuperclass.class);
        exporter.setSkipAnnotation(Transient.class);
        exporter.setTargetFolder(new File("target/generated-test-sources/java"));
        exporter.export(entityPackages);
    }

    private static Package getPackageByClassName(final String className) {
        Package entityPackage = null;

        try {
            final Class<?> entity = Class.forName(className);
            entityPackage = entity.getPackage();
        } catch (final ClassNotFoundException e) {
            log.error(String.format("Class '%s' not found for QueryDsl entity compilation.", className));
        }

        return entityPackage;
    }
}

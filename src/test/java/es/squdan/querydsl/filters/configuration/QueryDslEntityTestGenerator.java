package es.squdan.querydsl.filters.configuration;

import com.querydsl.codegen.GenericExporter;
import com.querydsl.codegen.Keywords;
import es.squdan.querydsl.filters.repository.entity.UserEntity;
import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class QueryDslEntityTestGenerator {

    // Configuration
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryDslEntityTestGenerator.class);
    private static final Package[] TEST_ENTITIES = List.of(
            UserEntity.class.getPackage()
    ).toArray(new Package[]{});

    public static void main(String[] args) {
        LOGGER.info("Generating QueryDsl test entities: '{}'", Arrays.toString(TEST_ENTITIES));

        // Generate QEntities from TestEntities
        GenericExporter exporter = new GenericExporter();
        exporter.setKeywords(Keywords.JPA);
        exporter.setEntityAnnotation(Entity.class);
        exporter.setEmbeddableAnnotation(Embeddable.class);
        exporter.setEmbeddedAnnotation(Embedded.class);
        exporter.setSupertypeAnnotation(MappedSuperclass.class);
        exporter.setSkipAnnotation(Transient.class);
        exporter.setTargetFolder(new File("target/generated-test-sources/java"));
        exporter.export(TEST_ENTITIES);
    }
}

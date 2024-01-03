QueryDsl Filters
================
QueryDsl-Filters is an open-source library that extends QueryDsl framework functionality with dynamic filters that
programmers can use to execute database queries easily.

[![Build Status](https://github.com/squdan/querydsl-filters/workflows/querydsl-filters/badge.svg)](https://github.com/squdan/querydsl-filters/actions)
[![Coverage Status](https://coveralls.io/repos/github/squdan/querydsl-filters/badge.svg?branch=master)](https://coveralls.io/github/squdan/querydsl-filters?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.squdan/querydsl-filters/badge.svg?style=flat-square&color=007ec6)](https://maven-badges.herokuapp.com/maven-central/io.github.squdan/querydsl-filters/)

## Requirements

1. Java 17+
2. Maven 3.9.5+
3. [Querydsl 5.0.0+ (classifier: jakarta)](https://github.com/querydsl/querydsl?tab=readme-ov-file) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.querydsl/querydsl-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.querydsl/querydsl-core/)
4. [Lombok 1.18.30+](https://github.com/projectlombok/lombok) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.projectlombok/lombok/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.projectlombok/lombok/)

## Getting started

### Setting up the dependency

The first step is to include QueryDsl-Filters into your project. You can download Maven dependency from
**Maven Central**.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.squdan/querydsl-filters/badge.svg?style=flat-square&color=007ec6)](https://maven-badges.herokuapp.com/maven-central/io.github.squdan/querydsl-filters/)

```maven
<dependency>
    <groupId>io.github.squdan</groupId>
    <artifactId>querydsl-filters</artifactId>
    <version>0.0.1</version>
</dependency>
```

## Project configuration

Once you have the dependency into your project, it requires a minimum configuration to work into your project.

### Maven configuration

QueryDsl framework needs to generate QueryDsl types from your entities, to enable this generation you must add next
plugin to your maven **pom.xml**. Also, you may need to configure your project to detect generated resources from
**target/generated-sources/java** folder.

```maven
<!-- Plugin to generate QueryDsl resources -->
<plugin>
    <groupId>com.mysema.maven</groupId>
    <artifactId>apt-maven-plugin</artifactId>
    <version>1.1.3</version>
    <executions>
        <execution>
            <phase>generate-sources</phase>
            <goals>
                <goal>process</goal>
            </goals>
            <configuration>
                <outputDirectory>target/generated-sources/java</outputDirectory>
                <processor>com.querydsl.apt.jpa.JPAAnnotationProcessor</processor>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Repositories configuration

To enable **QueryDslFilter** method in your **Spring-Data** repositories you have to extends your repositories from
QueryDslRepository and define a method returning the entity class. Example:

```java
package io.github.squdan.querydsl.filters.examples;

import io.github.squdan.querydsl.filters.repository.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ExampleRepository extends JpaRepository<ExampleEntity, ID_TYPE>, QueryDslRepository<ExampleEntity, EntityPathBase<ExampleEntity>> {

    // Required configuration
    default Class<UserEntity> getEntityType() {
        return ExampleEntity.class;
    }
}
```

Extending from QueryDslRepository you repository will offer you a new method **findAll** that accepts your
QueryDslFilters and return founds results.

```
import org.springframework.data.domain.Pageable;
import io.github.squdan.querydsl.filters.QueryDslFilter;

List<T> findAll(final List<QueryDslFilter> filters, final Pageable pageable);
```

### Custom types configuration

QueryDsl-Filters supports next types by default:

* Collection: fields inside the collection, not the collection itself.
* Dates: Instant, LocalDate, LocalDateTime, Date, etc...
* Numbers: Integer, BigDecimal, Double, etc...
* UUID
* Strings

You may find some undesirable behaviour at this default type management or you may want to support new types like
**enums**. To achieve this you can configure your custom implementation of **QueryDslTypeManager** and configure your
own behaviour.

```java
package io.github.squdan.querydsl.filters.examples;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.SimplePath;
import io.github.squdan.querydsl.filters.QueryDslFilter;
import io.github.squdan.querydsl.filters.QueryDslFiltersException;
import io.github.squdan.querydsl.filters.repository.type.QueryDslTypeManager;

public final class QueryDslExampleEnumTypeManager implements QueryDslTypeManager {

    // Conditions to execute this QueryDslTypeManager
    public <T> boolean isSupported(final Class<T> entityType, final PathBuilder<T> entityPath, final QueryDslFilter filter) {
        return ExampleEnum.class.isAssignableFrom(getTypeFrom(entityType, filter.getKey()));
    }

    // Manage filter for custom type field
    public <T> BooleanExpression manage(final Class<T> entityType, final PathBuilder<T> entityPath, final QueryDslFilter filter) {
        BooleanExpression result;

        // Create field path
        final SimplePath<ExampleEnum> path = entityPath.getSimple(filter.getKey(), ExampleEnum.class);

        // Parse value
        final ExampleEnum value = ExampleEnum.valueOf(String.valueOf(filter.getValue()));

        // Process operator
        switch (filter.getOperator()) {
            case IS_NULL_FUNCTION:
                result = path.isNull();
                break;
            case NON_NULL_FUNCTION:
                result = path.isNotNull();
                break;
            case EQUALS:
            case EQUALS_FUNCTION:
            case EQUALS_FUNCTION_EQ:
                result = path.eq(value);
                break;
            case NOT_EQUALS:
            case NON_EQUALS_FUNCTION:
            case NON_EQUALS_FUNCTION_NE:
                result = path.ne(value);
                break;
            default:
                throw new QueryDslFiltersException(String.format("Operation '%s' not supported for type 'ExampleEnum'.", filter.getOperator()));
        }

        return result;
    }
}
```

Also you may want to configure your custom date format to be managed at **QueryDslDateTypeManager**. To achieve this,
you just need to call to **DateTimeUtils.addDateTimeFormat** to register your date format.

```java
package io.github.squdan.querydsl.filters.examples;

import io.github.squdan.querydsl.filters.util.DateTimeUtils;

public class ConfigurationClass {

  public void configure() {
    DateTimeUtils.addDateTimeFormat(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
  }
}
```

Once implemented your QueryDslTypeManager you must configure it into the repositories where you want to manage this
types.

```java
package io.github.squdan.querydsl.filters.examples;

import io.github.squdan.querydsl.filters.repository.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ExampleRepository extends JpaRepository<ExampleEntity, ID_TYPE>, QueryDslRepository<ExampleEntity, EntityPathBase<ExampleEntity>> {

    // Required configuration
    default Class<UserEntity> getEntityType() {
        return ExampleEntity.class;
    }

    // Optional configuration
    QueryDslExampleEnumTypeManager CUSTOM_TYPES = new QueryDslExampleEnumTypeManager();

    default QueryDslExampleEnumTypeManager getCustomTypesManager() {
        return CUSTOM_TYPES;
    }
}
```

## Usage

Now, you can use your custom filters in your application. These filters can be directly used in your java code or they
can be received from out and mapping.

There are 2 kind of filters available to use (check at **io.github.squdan.querydsl.filters.QueryDslOperators**):

* Simple-Operators: format **key{{operator}}value**. Example: key=value
* Functions:
    - Single parameter: format **{{function}}({{key}})**. Example: isNull(key)
    - Double parameter: format **{{function}}({{key}}, {{value}})**. Example: equals(key, value)

### Programming Dynamic Filters

Developing QueryDsl-Filters in java is quite easy. Example:

```java
package io.github.squdan.querydsl.filters.examples;

import io.github.squdan.querydsl.filters.QueryDslFilter;
import io.github.squdan.querydsl.filters.QueryDslOperators;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
public class TestingFilters {

    // Dependencies
    private final ExampleRepository exampleRepository;

    public void simpleFilters() {

        // Is null
        QueryDslFilter isNullFilter = new QueryDslFilter("test_field_string", QueryDslOperators.IS_NULL_FUNCTION);

        // Not null
        QueryDslFilter notNullFilter = new QueryDslFilter("test_field_string", QueryDslOperators.NON_NULL_FUNCTION);

        // Equals
        QueryDslFilter equalsFilter = new QueryDslFilter("test_field_string", QueryDslOperators.EQUALS, "test_value");

        // Not equals
        QueryDslFilter notEqualsFilter = new QueryDslFilter("test_field_string", QueryDslOperators.NOT_EQUALS, "test_value");

        // Starts with
        QueryDslFilter startsWithFilter = new QueryDslFilter("test_field_string", QueryDslOperators.STARTS_WITH_FUNCTION, "test_starts");

        // Ends with
        QueryDslFilter endsWithFilter = new QueryDslFilter("test_field_string", QueryDslOperators.ENDS_WITH_FUNCTION, "test_ends");

        // Contains
        QueryDslFilter containsFilter = new QueryDslFilter("test_field_string", QueryDslOperators.CONTAIN_FUNCTION, "test_contains");

        // Greater than
        QueryDslFilter greaterThanNumberFilter = new QueryDslFilter("test_field_decimal", QueryDslOperators.GREATER_THAN, "35.4");
        QueryDslFilter greaterThanDateFilter = new QueryDslFilter("test_field_date", QueryDslOperators.GREATER_THAN, ADMIN.getCreatedOn().minus(1, ChronoUnit.DAYS));

        // Greater than or equals
        QueryDslFilter greaterThanOrEqualsNumberFilter = new QueryDslFilter("test_field_decimal", QueryDslOperators.GREATER_THAN_OR_EQUALS, "35.4");
        QueryDslFilter greaterThanOrEqualsDateFilter = new QueryDslFilter("test_field_date", QueryDslOperators.GREATER_THAN_OR_EQUALS, ADMIN.getCreatedOn().minus(1, ChronoUnit.DAYS));

        // Lower than
        QueryDslFilter lowerThanNumberFilter = new QueryDslFilter("test_field_integer", QueryDslOperators.LOWER_THAN, "35");
        QueryDslFilter lowerThanDateFilter = new QueryDslFilter("test_field_date", QueryDslOperators.LOWER_THAN, Instant.now());

        // Lower than or equals
        QueryDslFilter lowerThanOrEqualsNumberFilter = new QueryDslFilter("test_field_decimal", QueryDslOperators.LOWER_THAN_OR_EQUALS, "35.6");
        QueryDslFilter lowerThanOrEqualsDateFilter = new QueryDslFilter("test_field_date", QueryDslOperators.LOWER_THAN_OR_EQUALS, Instant.now());

        // Optional: pagination
        Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "test_field"));

        // Execution
        List<ExampleEntity> mayResults = exampleRepository.findAll(List.of(isNullFilter, equalsFilter), pageable);
    }

    public void nestedCollectionFilters() {
        // Filter
        QueryDslFilter isNullFilter = new QueryDslFilter("nested_collection.field_name", QueryDslOperators.IS_NULL_FUNCTION);

        // Optional: pagination
        Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "test_field"));

        // Execution
        List<ExampleEntity> mayResults = exampleRepository.findAll(List.of(isNullFilter), pageable);
    }

    public void nestedObjectFiltersAtNestedCollection() {
        // Filter
        QueryDslFilter isNullFilter = new QueryDslFilter("nested_collection.nested_object.field_name", QueryDslOperators.IS_NULL_FUNCTION);

        // Optional: pagination
        Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "field_name"));

        // Execution
        List<ExampleEntity> mayResults = exampleRepository.findAll(List.of(isNullFilter), pageable);
    }
}
```

### REST Dynamic Filters

You may want to receive or configure dynamic filters as String, you can use **QueryDslFiltersMapper** to map this String
to **QueryDslFilter**.

```java
package io.github.squdan.querydsl.filters.examples;

import io.github.squdan.querydsl.filters.QueryDslFilter;
import io.github.squdan.querydsl.filters.QueryDslFiltersMapper;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class TestingFilters {

    // Dependencies
    private final ExampleRepository exampleRepository;

    public List<ExampleEntity> source() {
        String notNullFilter = "isNull(test_key)";
        String equalsFilter = "eq(test_key : test_value)";

        return search(List.of(notNullFilter, equalsFilter));
    }

    public List<ExampleEntity> search(List<String> filters) {
        List<QueryDslFilter> queryDslFilters = QueryDslFiltersMapper.map(filters);
        return exampleRepository.findAll(queryDslFilters, null);
    }
}
```

## Bugs and Feedback

For bugs, questions and discussions please use the [Github Issues](https://github.com/squdan/querydsl-filters/issues).

## Communication

- Email: i02torod@gmail.com

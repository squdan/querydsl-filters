package io.github.squdan.querydsl.filters.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import io.github.squdan.querydsl.filters.QueryDslFilter;
import io.github.squdan.querydsl.filters.repository.type.*;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * Factory to generate BooleanExpression from {@link QueryDslFilter} with multiple entity field types available.
 * <p>
 * Supported types:
 * - Collection: fields inside the collection, not the collection itself.
 * - Dates: Instant, LocalDate, LocalDateTime, Date, etc...
 * - Numbers: Integer, BigDecimal, Double, etc...
 * - UUID
 * - Strings
 * <p>
 * If you need to support extra types in your repository, you implement your own {@link QueryDslTypeManager}
 * and configure using method "addCustomTypeManager".
 */
public final class QueryDslPredicateFactory {

    // Configuration - Type Managers
    private QueryDslCollectionTypeManager QUERY_DSL_COLLECTION_TYPE_MANAGER;
    private static final QueryDslDateTypeManager QUERY_DSL_DATE_TYPE_MANAGER = new QueryDslDateTypeManager();
    private static final QueryDslNumberTypeManager QUERY_DSL_NUMBER_TYPE_MANAGER = new QueryDslNumberTypeManager();
    private static final QueryDslUuidTypeManager QUERY_DSL_UUID_TYPE_MANAGER = new QueryDslUuidTypeManager();
    private static final QueryDslStringTypeManager QUERY_DSL_STRING_TYPE_MANAGER = new QueryDslStringTypeManager();

    // Optional configuration
    private QueryDslTypeManager customTypesManager;

    /**
     * Some entities may own custom field types like Enums.
     * <p>
     * Using {@link QueryDslTypeManager} you can implement your own types management.
     *
     * @param customTypesManager: implementation of {@link QueryDslTypeManager} to manage filters of custom types.
     */
    public void addCustomTypeManager(@NotNull final QueryDslTypeManager customTypesManager) {
        this.customTypesManager = customTypesManager;
    }

    /**
     * Generate BooleanExpression predicate for entity with received filter.
     *
     * @param entityType: entity type class.
     * @param filter:     filter to apply.
     * @param <T>:        entity type.
     * @return BooleanExpression from received filter.
     */
    public <T> BooleanExpression getPredicate(final Class<T> entityType, final QueryDslFilter filter) {
        final PathBuilder<T> entityPath = new PathBuilder<T>(entityType, getEntityName(entityType));
        return getPredicate(entityType, entityPath, filter);
    }

    /**
     * Generate BooleanExpression predicate for entity with received filter.
     *
     * @param entityType: entity type class.
     * @param entityPath: QueryDsl path, used for nested collections into {@link QueryDslCollectionTypeManager}.
     * @param filter:     filter to apply.
     * @param <T>:        entity type.
     * @return BooleanExpression from received filter.
     */
    public <T> BooleanExpression getPredicate(final Class<T> entityType, final PathBuilder<T> entityPath, final QueryDslFilter filter) {
        BooleanExpression result = null;

        // Initialize lazy parameters
        lazyInitializations();

        // Manage Collections
        if (QUERY_DSL_COLLECTION_TYPE_MANAGER.isSupported(entityType, entityPath, filter)) {
            result = QUERY_DSL_COLLECTION_TYPE_MANAGER.manage(entityType, entityPath, filter);
        }

        // Manage custom types configured by App
        else if (Objects.nonNull(customTypesManager) && customTypesManager.isSupported(entityType, entityPath, filter)) {
            result = customTypesManager.manage(entityType, entityPath, filter);
        }

        // Manage Dates
        else if (QUERY_DSL_DATE_TYPE_MANAGER.isSupported(entityType, entityPath, filter)) {
            result = QUERY_DSL_DATE_TYPE_MANAGER.manage(entityType, entityPath, filter);
        }

        // Manage Numbers
        else if (QUERY_DSL_NUMBER_TYPE_MANAGER.isSupported(entityType, entityPath, filter)) {
            result = QUERY_DSL_NUMBER_TYPE_MANAGER.manage(entityType, entityPath, filter);
        }

        // Manage UUIDs
        else if (QUERY_DSL_UUID_TYPE_MANAGER.isSupported(entityType, entityPath, filter)) {
            result = QUERY_DSL_UUID_TYPE_MANAGER.manage(entityType, entityPath, filter);
        }

        // Default: Manage as String
        else {
            result = QUERY_DSL_STRING_TYPE_MANAGER.manage(entityType, entityPath, filter);
        }

        return result;
    }

    private void lazyInitializations() {
        if (Objects.isNull(QUERY_DSL_COLLECTION_TYPE_MANAGER)) {
            QUERY_DSL_COLLECTION_TYPE_MANAGER = new QueryDslCollectionTypeManager(this);
        }
    }

    private <T> String getEntityName(final Class<T> entityType) {
        String result = entityType.getSimpleName();

        // Parse first letter to lower case
        char[] c = result.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        result = new String(c);

        return result;
    }
}

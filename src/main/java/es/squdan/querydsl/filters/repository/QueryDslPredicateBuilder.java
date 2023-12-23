package es.squdan.querydsl.filters.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import es.squdan.querydsl.filters.QueryDslFilter;
import es.squdan.querydsl.filters.QueryDslFiltersException;
import es.squdan.querydsl.filters.QueryDslOperators;
import es.squdan.querydsl.filters.repository.type.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class will process all QueryDslFilters configured to generate a BooleanExpression predicated that will be used
 * at {@link QueryDslRepository} to retrieve results that matches with the filters.
 *
 * @param <T>: entity type.
 */
@Slf4j
@Validated
@RequiredArgsConstructor
public class QueryDslPredicateBuilder<T> {

    // Configuration - Type Managers
    private static final QueryDslDateTypeManager QUERY_DSL_DATE_TYPE_MANAGER = new QueryDslDateTypeManager();
    private static final QueryDslNumberTypeManager QUERY_DSL_NUMBER_TYPE_MANAGER = new QueryDslNumberTypeManager();
    private static final QueryDslUuidTypeManager QUERY_DSL_UUID_TYPE_MANAGER = new QueryDslUuidTypeManager();
    private static final QueryDslStringTypeManager QUERY_DSL_STRING_TYPE_MANAGER = new QueryDslStringTypeManager();

    // Data
    private final Class<T> entityType;
    private final List<QueryDslFilter> queryDslFilters = new ArrayList<>();

    // Optional configuration
    private QueryDslCustomTypeManager customTypesManager;

    /**
     * Some entities may own custom field types like Enums. Using QueryDslCustomTypesManager you can implement your own
     * types management.
     *
     * @param customTypesManager: implementation of {@link QueryDslCustomTypeManager} to manage filters of custom types.
     * @return QueryDslPredicateBuilder.
     */
    public QueryDslPredicateBuilder<T> addCustomTypesManager(@NotNull final QueryDslCustomTypeManager customTypesManager) {
        this.customTypesManager = customTypesManager;
        return this;
    }

    /**
     * Adds new filter to the QueryDslPredicateBuilder.
     *
     * @param key:      entity field name.
     * @param operator: operation to apply (This operator must be supported at {@link QueryDslOperators}).
     * @param value:    entity value.
     * @return QueryDslPredicateBuilder.
     */
    public QueryDslPredicateBuilder<T> with(@NotBlank final String key, @NotBlank final String operator,
                                            @NotNull final Object value) {
        return with(QueryDslFilter.from(key, operator, value));
    }

    /**
     * Adds new filter to the QueryDslPredicateBuilder.
     *
     * @param key:      entity field name.
     * @param operator: operation to apply.
     * @param value:    entity value.
     * @return QueryDslPredicateBuilder.
     */
    public QueryDslPredicateBuilder<T> with(@NotBlank final String key, @NotNull final QueryDslOperators operator,
                                            @NotNull final Object value) {
        return with(new QueryDslFilter(key, operator, value));
    }

    /**
     * Adds new filter to the QueryDslPredicateBuilder.
     *
     * @param filter: QueryDslFilter to add.
     * @return QueryDslPredicateBuilder.
     */
    public QueryDslPredicateBuilder<T> with(@NotNull final QueryDslFilter filter) {
        queryDslFilters.add(filter);
        return this;
    }

    /**
     * Adds new filter to the QueryDslPredicateBuilder.
     *
     * @param filters: QueryDslFilters to add.
     * @return QueryDslPredicateBuilder.
     */
    public QueryDslPredicateBuilder<T> with(@NotNull final List<QueryDslFilter> filters) {
        queryDslFilters.addAll(filters);
        return this;
    }

    /**
     * Process configured filters and generate a BooleanExpression.
     *
     * @return BooleanExpression.
     */
    public BooleanExpression build() {
        BooleanExpression result = null;

        if (CollectionUtils.isNotEmpty(queryDslFilters)) {
            // Process QueryDsl filters to convert into Predicates
            final List<BooleanExpression> predicates = queryDslFilters.stream()
                    .filter(Objects::nonNull)
                    .map(filter -> (new PredicateGenerator(entityType, filter)).getPredicate())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(predicates)) {
                result = Expressions.asBoolean(true).isTrue();

                for (BooleanExpression predicate : predicates) {
                    result = result.and(predicate);
                }
            } else {
                final String errorMsg = "Error processing QueryDslFilters.";
                log.error(errorMsg);
                throw new QueryDslFiltersException(errorMsg);
            }
        }

        return result;
    }

    @AllArgsConstructor
    private class PredicateGenerator {

        private Class<T> entityType;
        private QueryDslFilter filter;

        public BooleanExpression getPredicate() {
            BooleanExpression result = null;
            PathBuilder<T> entityPath = new PathBuilder<T>(entityType, getEntityName());

            // Manage custom types configured by App
            if (Objects.nonNull(customTypesManager) && customTypesManager.isAsignable(filter)) {
                result = customTypesManager.manage(entityPath, filter);
            }

            // Manage Dates
            else if (QUERY_DSL_DATE_TYPE_MANAGER.isAsignable(filter)) {
                result = QUERY_DSL_DATE_TYPE_MANAGER.manage(entityPath, filter);
            }

            // Manage Numbers
            else if (QUERY_DSL_NUMBER_TYPE_MANAGER.isAsignable(filter)) {
                result = QUERY_DSL_NUMBER_TYPE_MANAGER.manage(entityPath, filter);
            }

            // Manage UUIDs
            else if (QUERY_DSL_UUID_TYPE_MANAGER.isAsignable(filter)) {
                result = QUERY_DSL_UUID_TYPE_MANAGER.manage(entityPath, filter);
            }

            // Default: Manage as String
            else {
                result = QUERY_DSL_STRING_TYPE_MANAGER.manage(entityPath, filter);
            }

            return result;
        }

        private String getEntityName() {
            String result = entityType.getSimpleName();

            // Parse first letter to lower case
            char[] c = result.toCharArray();
            c[0] = Character.toLowerCase(c[0]);
            result = new String(c);

            return result;
        }
    }
}

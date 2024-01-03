package io.github.squdan.querydsl.filters.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import io.github.squdan.querydsl.filters.QueryDslFilter;
import io.github.squdan.querydsl.filters.QueryDslFiltersException;
import io.github.squdan.querydsl.filters.QueryDslOperators;
import io.github.squdan.querydsl.filters.repository.type.QueryDslTypeManager;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    // Private configuration
    private final QueryDslPredicateFactory queryDslPredicateFactory = new QueryDslPredicateFactory();

    // Required configuration
    private final Class<T> entityType;
    private final List<QueryDslFilter> queryDslFilters = new ArrayList<>();

    /**
     * Some entities may own custom field types like Enums. Using QueryDslCustomTypesManager you can implement your own
     * types management.
     *
     * @param customTypesManager: implementation of {@link QueryDslTypeManager} to manage filters of custom types.
     * @return QueryDslPredicateBuilder.
     */
    public QueryDslPredicateBuilder<T> addCustomTypeManager(@NotNull final QueryDslTypeManager customTypesManager) {
        queryDslPredicateFactory.addCustomTypeManager(customTypesManager);
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
                    .map(filter -> queryDslPredicateFactory.getPredicate(entityType, filter))
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
}

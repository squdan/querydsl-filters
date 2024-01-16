package io.github.squdan.querydsl.filters.repository.type;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.SimplePath;
import io.github.squdan.querydsl.filters.QueryDslFilter;
import io.github.squdan.querydsl.filters.QueryDslFiltersException;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * QueryDslTypeManager implementation to manage UUIDs.
 */
@Slf4j
public final class QueryDslUuidTypeManager implements QueryDslTypeManager {

    public <T> boolean isSupported(final Class<T> entityType, final PathBuilder<T> entityPath, final QueryDslFilter filter) {
        return UUID.class.isAssignableFrom(getTypeFrom(entityType, filter.getKey()));
    }

    public <T> BooleanExpression manage(final Class<T> entityType, final PathBuilder<T> entityPath, final QueryDslFilter filter) {
        BooleanExpression result = null;

        // Create field path
        final SimplePath<UUID> path = entityPath.getSimple(filter.getKey(), UUID.class);

        // Parse value
        UUID value;

        try {
            value = UUID.fromString(String.valueOf(filter.getValue()));
        } catch (final IllegalArgumentException e) {
            final String errorMsg = String.format(
                    "Operation '%s' error, value '%s' couldn't be parsed.",
                    filter.getOperator(),
                    filter.getValue()
            );
            log.error(errorMsg);
            throw new QueryDslFiltersException(errorMsg);
        }

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
                final String errorMsg = String.format("Operation '%s' not supported for type 'UUID'.", filter.getOperator());
                log.error(errorMsg);
                throw new QueryDslFiltersException(errorMsg);
        }

        return result;
    }

}

package io.github.squdan.querydsl.filters.repository.type;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.PathBuilder;
import io.github.squdan.querydsl.filters.QueryDslFilter;
import io.github.squdan.querydsl.filters.QueryDslFiltersException;
import lombok.extern.slf4j.Slf4j;

/**
 * QueryDslTypeManager implementation to manage Booleans.
 */
@Slf4j
public final class QueryDslBooleanTypeManager implements QueryDslTypeManager {

    public <T> boolean isSupported(final Class<T> entityType, final PathBuilder<T> entityPath, final QueryDslFilter filter) {
        final Class<?> type = getTypeFrom(entityType, filter.getKey());
        return Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type);
    }

    public <T> BooleanExpression manage(final Class<T> entityType, final PathBuilder<T> entityPath, final QueryDslFilter filter) {
        BooleanExpression result = null;

        // Create field path
        final BooleanPath path = entityPath.getBoolean(filter.getKey());

        // Parse value
        boolean value;

        try {
            value = Boolean.parseBoolean(filter.getValue().toString());
        } catch (final NumberFormatException e) {
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
                final String errorMsg = String.format("Operation '%s' not supported for type 'Boolean'.", filter.getOperator());
                log.error(errorMsg);
                throw new QueryDslFiltersException(errorMsg);
        }

        return result;
    }
}

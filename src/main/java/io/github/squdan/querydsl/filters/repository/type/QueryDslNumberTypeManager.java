package io.github.squdan.querydsl.filters.repository.type;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathBuilder;
import io.github.squdan.querydsl.filters.QueryDslFilter;
import io.github.squdan.querydsl.filters.QueryDslFiltersException;
import lombok.extern.slf4j.Slf4j;

/**
 * QueryDslTypeManager implementation to manage Numbers.
 */
@Slf4j
public final class QueryDslNumberTypeManager implements QueryDslTypeManager {

    public <T> boolean isSupported(final Class<T> entityType, final PathBuilder<T> entityPath, final QueryDslFilter filter) {
        return Number.class.isAssignableFrom(getTypeFrom(entityType, filter.getKey()));
    }

    public <T> BooleanExpression manage(final Class<T> entityType, final PathBuilder<T> entityPath, final QueryDslFilter filter) {
        BooleanExpression result = null;

        // Create field path
        final NumberPath<Double> path = entityPath.getNumber(filter.getKey(), Double.class);

        // Parse value
        Double value;

        try {
            value = Double.parseDouble(filter.getValue().toString());
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
            case GREATER_THAN:
            case GREATER_THAN_FUNCTION_GT:
                result = path.gt(value);
                break;
            case GREATER_THAN_OR_EQUALS:
            case GREATER_THAN_OR_EQUALS_FUNCTION_GTE:
                result = path.goe(value);
                break;
            case LOWER_THAN:
            case LOWER_THAN_FUNCTION_LT:
                result = path.lt(value);
                break;
            case LOWER_THAN_OR_EQUALS:
            case LOWER_THAN_OR_EQUALS_FUNCTION_LTE:
                result = path.loe(value);
                break;
            default:
                final String errorMsg = String.format("Operation '%s' not supported for type 'Number'.", filter.getOperator());
                log.error(errorMsg);
                throw new QueryDslFiltersException(errorMsg);
        }

        return result;
    }
}

package io.github.squdan.querydsl.filters.repository.type;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.PathBuilder;
import io.github.squdan.querydsl.filters.QueryDslFilter;
import io.github.squdan.querydsl.filters.QueryDslFiltersException;
import io.github.squdan.querydsl.filters.util.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.Objects;

/**
 * QueryDslTypeManager implementation to manage Dates.
 */
@Slf4j
public final class QueryDslDateTypeManager implements QueryDslTypeManager {

    public <T> boolean isSupported(final Class<T> entityType, final PathBuilder<T> entityPath, final QueryDslFilter filter) {
        final Class<?> fieldType = getTypeFrom(entityType, filter.getKey());
        return Temporal.class.isAssignableFrom(fieldType) || Date.class.isAssignableFrom(fieldType);
    }

    public <T> BooleanExpression manage(final Class<T> entityType, final PathBuilder<T> entityPath, final QueryDslFilter filter) {
        final Class<?> fieldType = getTypeFrom(entityType, filter.getKey());
        BooleanExpression result;

        if (Instant.class.isAssignableFrom(fieldType) || LocalDateTime.class.isAssignableFrom(fieldType)) {
            // Create field path
            final DatePath<Instant> path = entityPath.getDate(filter.getKey(), Instant.class);

            // Parse value
            final Instant value = DateTimeUtils.toInstantUtc(filter.getValue().toString());

            // Generate filter expression
            result = generateExpression(filter, path, value);
        } else if (LocalDate.class.isAssignableFrom(fieldType)) {
            // Create field path
            final DatePath<LocalDate> path = entityPath.getDate(filter.getKey(), LocalDate.class);

            // Parse value
            final LocalDate value = DateTimeUtils.toLocalDate(filter.getValue().toString());

            // Generate filter expression
            result = generateExpression(filter, path, value);
        } else {
            final String errorMsg = String.format("Date type not supported '%s'.", fieldType);
            log.error(errorMsg);
            throw new QueryDslFiltersException(errorMsg);
        }

        return result;
    }

    private <T extends Comparable<?>> BooleanExpression generateExpression(final QueryDslFilter filter, final DatePath<T> path, final T value) {
        BooleanExpression result = null;

        if (Objects.isNull(value)) {
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
                final String errorMsg = String.format("Operation '%s' not supported for type 'Date - %s'.", filter.getOperator(), value.getClass());
                log.error(errorMsg);
                throw new QueryDslFiltersException(errorMsg);
        }

        return result;
    }
}

package es.squdan.querydsl.filters.repository.type;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.PathBuilder;
import es.squdan.querydsl.filters.QueryDslFilter;
import es.squdan.querydsl.filters.QueryDslFiltersException;
import es.squdan.querydsl.filters.util.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Objects;

@Slf4j
public final class QueryDslDateTypeManager implements QueryDslCustomTypeManager {

    public boolean isAsignable(final QueryDslFilter filter) {
        boolean result = false;

        if (Objects.nonNull(filter.getValue())) {
            final Instant date = DateTimeUtils.toInstantUtc(filter.getValue().toString());

            if (Objects.nonNull(date)) {
                result = true;
            }
        }

        return result;
    }

    public <T> BooleanExpression manage(final PathBuilder<T> entityPath, final QueryDslFilter filter) {
        BooleanExpression result = null;

        // Create field path
        final DatePath<Instant> path = entityPath.getDate(filter.getKey(), Instant.class);

        // Parse value
        final Instant value = DateTimeUtils.toInstantUtc(filter.getValue().toString());

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
                final String errorMsg = String.format("Operation '%s' not supported for type 'Date'.", filter.getOperator());
                log.error(errorMsg);
                throw new QueryDslFiltersException(errorMsg);
        }

        return result;
    }

}

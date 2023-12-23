package es.squdan.querydsl.filters.repository.type;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.SimplePath;
import es.squdan.querydsl.filters.QueryDslFilter;
import es.squdan.querydsl.filters.QueryDslFiltersException;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.UUID;

@Slf4j
public final class QueryDslUuidTypeManager implements QueryDslCustomTypeManager {

    public boolean isAsignable(final QueryDslFilter filter) {
        boolean result = false;

        if (Objects.nonNull(filter.getValue())) {
            try {
                UUID.fromString(filter.getValue().toString());
                result = true;
            } catch (final IllegalArgumentException e) {
                // Do nothing
            }
        }

        return result;
    }

    public <T> BooleanExpression manage(final PathBuilder<T> entityPath, final QueryDslFilter filter) {
        BooleanExpression result = null;

        // Create field path
        final SimplePath<UUID> path = entityPath.getSimple(filter.getKey(), UUID.class);

        // Parse value
        final UUID value = UUID.fromString(String.valueOf(filter.getValue()));

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

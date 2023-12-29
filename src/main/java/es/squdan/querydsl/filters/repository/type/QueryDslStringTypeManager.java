package es.squdan.querydsl.filters.repository.type;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;
import es.squdan.querydsl.filters.QueryDslFilter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class QueryDslStringTypeManager implements QueryDslTypeManager {

    public <T> boolean isSupported(final Class<T> entityType, final PathBuilder<T> entityPath, final QueryDslFilter filter) {
        // If no other QueryDslCustomTypesManager has managed the field, then it will be treated as String
        return true;
    }

    public <T> BooleanExpression manage(final Class<T> entityType, final PathBuilder<T> entityPath, final QueryDslFilter filter) {
        BooleanExpression result = null;

        // Create field path
        final StringPath path = entityPath.getString(filter.getKey());

        // Parse value
        final String value = String.valueOf(filter.getValue());

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
            case STARTS_WITH_FUNCTION:
            case STARTS_WITH_FUNCTION_SW:
                result = path.startsWithIgnoreCase(value);
                break;
            case ENDS_WITH_FUNCTION:
            case ENDS_WITH_FUNCTION_EW:
                result = path.endsWithIgnoreCase(value);
                break;
            case CONTAIN_FUNCTION:
            case CONTAIN_FUNCTION_C:
            case CONTAINS_FUNCTION_LIKE:
                result = path.containsIgnoreCase(value);
                break;
            default:
                log.warn("Operación '{}' no soportada para el tipo String.", filter.getOperator());
                break;
        }

        return result;
    }

}

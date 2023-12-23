package es.squdan.querydsl.filters.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.SimplePath;
import es.squdan.querydsl.filters.QueryDslFilter;
import es.squdan.querydsl.filters.QueryDslFiltersException;
import es.squdan.querydsl.filters.repository.type.QueryDslCustomTypeManager;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public final class QueryDslRolesTypeManager implements QueryDslCustomTypeManager {

    public boolean isAsignable(final QueryDslFilter filter) {
        return Objects.nonNull(getCustomType(filter));
    }

    public <T> BooleanExpression manage(final PathBuilder<T> entityPath, final QueryDslFilter filter) {
        BooleanExpression result = null;

        // Select custom value type to execute
        final Class<?> valueType = getCustomType(filter);

        if (Roles.class == valueType) {
            result = manageRoles(entityPath, filter);
        }

        return result;
    }

    private Class<?> getCustomType(final QueryDslFilter filter) {
        Class<?> result = null;

        if (filter.getKey().equals("role") && Roles.isRoleValue(filter.getValue())) {
            result = Roles.class;
        }

        return result;
    }

    private <T> BooleanExpression manageRoles(final PathBuilder<T> entityPath, final QueryDslFilter filter) {
        BooleanExpression result = null;

        // Create field path
        final SimplePath<Roles> path = entityPath.getSimple(filter.getKey(), Roles.class);

        // Parse value
        final Roles value = Roles.valueOf(String.valueOf(filter.getValue()));

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
                final String errorMsg = String.format("Operation '%s' not supported for type 'Roles'.", filter.getOperator());
                log.error(errorMsg);
                throw new QueryDslFiltersException(errorMsg);
        }

        return result;
    }

}

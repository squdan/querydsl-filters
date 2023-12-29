package es.squdan.querydsl.filters.repository.type;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import es.squdan.querydsl.filters.QueryDslFilter;
import es.squdan.querydsl.filters.QueryDslFiltersException;

import java.lang.reflect.Field;

/**
 * Class to manage field types at {@link es.squdan.querydsl.filters.repository.QueryDslPredicateBuilder},
 * used by {@link es.squdan.querydsl.filters.repository.QueryDslRepository}
 */
public interface QueryDslTypeManager {

    /**
     * Returns true if this QueryDslTypeManager implementation can manage the filter.
     *
     * @param entityType: entity type class.
     * @param entityPath: QueryDsl entity path.
     * @param filter:     filter to apply.
     * @param <T>:        entity type.
     * @return true if filter can be managed.
     */
    <T> boolean isSupported(Class<T> entityType, PathBuilder<T> entityPath, QueryDslFilter filter);

    /**
     * Proces received filter and generate BooleanExpression predicate.
     *
     * @param entityType: entity type class.
     * @param entityPath: QueryDsl entity path.
     * @param filter:     filter to apply.
     * @param <T>:        entity type.
     * @return BooleanExpression from received filter.
     */
    <T> BooleanExpression manage(Class<T> entityType, PathBuilder<T> entityPath, QueryDslFilter filter);

    /**
     * Returns Class type from las field in the path.
     * <p>
     * Example: user.accounts.id -> UUID.class
     * Example: user.accounts -> List.class
     *
     * @param sourceType: entity type class.
     * @param path:       fields path from source type.
     * @return Class from last field in the path.
     */
    default Class<?> getTypeFrom(final Class<?> sourceType, final String path) {
        Class<?> result;

        // Get first field
        final String[] entityFieldsToCollection = path.split("\\.", 2);

        try {
            final Field field = sourceType.getDeclaredField(entityFieldsToCollection[0]);
            result = field.getType();

            if (entityFieldsToCollection.length == 2) {
                result = getTypeFrom(result, entityFieldsToCollection[1]);
            }
        } catch (final NoSuchFieldException e) {
            final String errorMsg = String.format("Error searching type from '%s' for class '%s'", path, sourceType.getSimpleName());
            throw new QueryDslFiltersException(e, errorMsg);
        }

        return result;
    }
}

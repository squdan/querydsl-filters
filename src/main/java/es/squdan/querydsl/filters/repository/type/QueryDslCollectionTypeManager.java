package es.squdan.querydsl.filters.repository.type;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import es.squdan.querydsl.filters.QueryDslFilter;
import es.squdan.querydsl.filters.QueryDslFiltersException;
import es.squdan.querydsl.filters.repository.QueryDslPredicateFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
public final class QueryDslCollectionTypeManager implements QueryDslTypeManager {

    // Configuration
    private final QueryDslPredicateFactory queryDslPredicateFactory;

    public <T> boolean isSupported(final Class<T> entityType, final PathBuilder<T> entityPath, final QueryDslFilter filter) {
        return pathHasCollection(entityType, filter.getKey());
    }

    public <T> BooleanExpression manage(final Class<T> entityType, final PathBuilder<T> entityPath, final QueryDslFilter filter) {
        // Create collection field path
        final PathToProcess pathToProcess = getQueryDslPathToProcess(entityType, filter.getKey());
        final Class<?> collectionType = getTypeFrom(entityType, pathToProcess.collectionPath);

        // Update filter key
        final QueryDslFilter updatedFilter = QueryDslFilter.builder()
                .key(pathToProcess.fieldCollectionPath)
                .operator(filter.getOperator())
                .value(filter.getValue())
                .build();

        return manageCollection(entityPath, pathToProcess, collectionType, updatedFilter);
    }

    private <T, K> BooleanExpression manageCollection(
            final PathBuilder<T> entityPath,
            final PathToProcess pathToProcess,
            final Class<K> collectionType,
            final QueryDslFilter filter) {
        final PathBuilder<K> collectionPath = entityPath.getCollection(pathToProcess.collectionPath, collectionType).any();
        return queryDslPredicateFactory.getPredicate(collectionType, collectionPath, filter);
    }

    record PathToProcess(String collectionPath, String fieldCollectionPath) {
    }

    private boolean pathHasCollection(final Class<?> sourceType, final String path) {
        return StringUtils.isNotBlank(getCollectionFieldName(sourceType, path));
    }

    private String getCollectionFieldName(final Class<?> sourceType, final String path) {
        String result = null;

        // Get first field
        final String[] entityFieldsToCollection = path.split("\\.", 2);

        try {
            final Field field = sourceType.getDeclaredField(entityFieldsToCollection[0]);
            final Class<?> fieldType = field.getType();

            if (Collection.class.isAssignableFrom(fieldType)) {
                result = entityFieldsToCollection[0];
            } else if (entityFieldsToCollection.length == 2) {
                result = getCollectionFieldName(getCollectionType(field), entityFieldsToCollection[1]);
            }
        } catch (final NoSuchFieldException e) {
            final String errorMsg = String.format("Error checking if path '%s' for class '%s' has a collection.", path, sourceType.getSimpleName());
            log.error(errorMsg);
            throw new QueryDslFiltersException(e, errorMsg);
        }

        return result;
    }

    private PathToProcess getQueryDslPathToProcess(final Class<?> sourceType, final String path) {
        PathToProcess result;

        final String collectionFieldName = getCollectionFieldName(sourceType, path);
        final String[] pathComponents = path.split(collectionFieldName + "\\.", 2);

        // Get field name
        if (pathComponents.length > 1) {
            result = new PathToProcess(pathComponents[0] + collectionFieldName, pathComponents[1]);
        } else {
            final String errorMsg = String.format("Error searching collection field for query '%s'", path);
            log.error(errorMsg);
            throw new QueryDslFiltersException(errorMsg);
        }

        return result;
    }

    @Override
    public Class<?> getTypeFrom(final Class<?> sourceType, final String path) {
        Class<?> result;

        // Get first field
        final String[] entityFieldsToCollection = path.split("\\.", 2);

        try {
            final Field field = sourceType.getDeclaredField(entityFieldsToCollection[0]);
            result = getCollectionType(field);

            if (entityFieldsToCollection.length == 2) {
                result = getTypeFrom(result, entityFieldsToCollection[1]);
            }
        } catch (final NoSuchFieldException e) {
            final String errorMsg = String.format("Error searching type from '%s' for class '%s'", path, sourceType.getSimpleName());
            throw new QueryDslFiltersException(e, errorMsg);
        }

        return result;
    }

    private Class<?> getCollectionType(final Field collectionField) {
        Class<?> result = collectionField.getType();

        if (Collection.class.isAssignableFrom(result)) {
            final Type collectionSuperType = collectionField.getGenericType();

            if (collectionSuperType instanceof ParameterizedType parameterizedType) {
                final Type[] collectionTypeArguments = parameterizedType.getActualTypeArguments();

                if (collectionTypeArguments.length > 0) {
                    final Type firstArgument = collectionTypeArguments[0];

                    if (firstArgument instanceof Class) {
                        result = (Class<?>) firstArgument;
                    }
                }
            }
        }

        return result;
    }
}

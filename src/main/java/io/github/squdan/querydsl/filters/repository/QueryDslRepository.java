package io.github.squdan.querydsl.filters.repository;

import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import io.github.squdan.querydsl.filters.QueryDslFilter;
import io.github.squdan.querydsl.filters.repository.type.QueryDslTypeManager;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Spring-Data repository with default implementation of findAll(List<QueryDslFilter> filters, final Pageable pageable)
 * and required configuration to use filters at your App repositories.
 * <p>
 * EntityType is required to be configured at "getEntityType()" method to work.
 * <p>
 * If your entity has custom fields like Enums or you want to customize filters for this repository, you can implements
 * {@link QueryDslTypeManager} and return it at "getCustomTypesManager()" method in your repository.
 *
 * @param <T>: entity type.
 * @param <K>: entity ID type.
 */
public interface QueryDslRepository<T, K extends EntityPathBase<T>>
        extends QuerydslPredicateExecutor<T>, QuerydslBinderCustomizer<K> {

    Class<T> getEntityType();

    default List<T> findAll(final List<QueryDslFilter> filters, final Pageable pageable) {
        List<T> result = null;

        if (CollectionUtils.isNotEmpty(filters)) {
            final QueryDslPredicateBuilder<T> queryDslPredicateBuilder = new QueryDslPredicateBuilder<T>(getEntityType())
                    .with(filters);

            if (Objects.nonNull(getCustomTypesManager())) {
                queryDslPredicateBuilder.addCustomTypeManager(getCustomTypesManager());
            }

            Iterable<T> queryResult;

            if (Objects.isNull(pageable)) {
                queryResult = this.findAll(queryDslPredicateBuilder.build());
            } else {
                queryResult = this.findAll(queryDslPredicateBuilder.build(), pageable);
            }

            result = StreamSupport.stream(queryResult.spliterator(), false).collect(Collectors.toList());
        }

        return result;
    }

    default QueryDslTypeManager getCustomTypesManager() {
        return null;
    }

    @Override
    default void customize(final QuerydslBindings bindings, final K root) {
        bindings.bind(String.class)
                .first((SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);
    }

}

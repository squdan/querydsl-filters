package io.github.squdan.querydsl.filters.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import io.github.squdan.querydsl.filters.QueryDslFilter;
import io.github.squdan.querydsl.filters.repository.type.QueryDslTypeManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;

import java.util.List;
import java.util.Objects;

/**
 * Spring-Data repository with default implementation of findAll(List of QueryDslFilter filters, final Pageable pageable)
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

    /**
     * Entity class.
     *
     * @return Class from entity.
     */
    Class<T> getEntityType();

    /**
     * Searchs into the repository usin received filters.
     *
     * @param filters  to apply.
     * @param pageable to apply (optional).
     * @return List entity found elements.
     */
    default Page<T> findAll(final List<QueryDslFilter> filters, final Pageable pageable) {
        Page<T> result = Page.empty();

        if (CollectionUtils.isNotEmpty(filters)) {
            final QueryDslPredicateBuilder<T> queryDslPredicateBuilder = new QueryDslPredicateBuilder<T>(getEntityType())
                    .with(filters);

            if (Objects.nonNull(getCustomTypesManager())) {
                queryDslPredicateBuilder.addCustomTypeManager(getCustomTypesManager());
            }

            if (Objects.isNull(pageable)) {
                result = new PageImpl<>(IteratorUtils.toList(this.findAll(queryDslPredicateBuilder.build()).iterator()));
            } else {
                result = this.findAll(queryDslPredicateBuilder.build(), pageable);
            }
        } else {
            if (Objects.isNull(pageable)) {
                result = new PageImpl<>(IteratorUtils.toList(this.findAll().iterator()));
            } else {
                result = this.findAll(new BooleanBuilder(), pageable);
            }
        }

        return result;
    }

    /**
     * Override this method and return your own implementation of QueryDslTypeManager to support new types.
     *
     * @return QueryDslTypeManager implementation.
     */
    default QueryDslTypeManager getCustomTypesManager() {
        return null;
    }

    @Override
    default void customize(final QuerydslBindings bindings, final K root) {
        bindings.bind(String.class)
                .first((SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);
    }

}

package es.squdan.querydsl.filters.repository.type;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import es.squdan.querydsl.filters.QueryDslFilter;

public interface QueryDslCustomTypeManager {

    boolean isAsignable(QueryDslFilter filter);

    <T> BooleanExpression manage(PathBuilder<T> entityPath, QueryDslFilter filter);

}

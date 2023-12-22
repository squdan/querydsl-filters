package es.squdan.querydsl.filters.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import es.squdan.querydsl.filters.QueryDslFilter;

public interface QueryDslCustomTypesManager {

    boolean isCustomType(QueryDslFilter filter);

    <T> BooleanExpression manageCustomType(PathBuilder<T> entityPath, QueryDslFilter filter);

}

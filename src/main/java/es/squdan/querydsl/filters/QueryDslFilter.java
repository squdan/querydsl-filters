package es.squdan.querydsl.filters;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Optional;

/**
 * Class with filter information to apply in the searching over {@link es.squdan.querydsl.filters.repository.QueryDslRepository}
 */
@Slf4j
@Value
@Builder
@AllArgsConstructor
public class QueryDslFilter {

    // Formats supported
    public static final String NORMAL_OPERATOR = "%s%s%s";
    public static final String FUNCTION_OPERATOR = "%s(%s : %s)";
    public static final String FUNCTION_OPERATOR_SINGLE_ARGUMENT = "%s(%s)";

    /**
     * Entity field name
     */
    String key;

    /**
     * Operator to apply (=, !=, >, isNull, equals...)
     */
    QueryDslOperators operator;

    /**
     * Value to use in the filter
     */
    Object value;

    /**
     * Constructor for functions with single parameter key.
     *
     * @param key:      entity field name.
     * @param operator: operator to apply (isNull, nonNull).
     */
    public QueryDslFilter(final String key, final QueryDslOperators operator) {
        this.key = key;
        this.operator = operator;
        this.value = null;
    }

    /**
     * Generates a QueryDslFilters allowing String operator as input.
     * <p>
     * This String operator must be supported at {@link QueryDslOperators}
     * and be a function of single argument (example: QueryDslOperators.IS_NULL_FUNCTION).
     *
     * @param key:      entity field name.
     * @param operator: operator to apply.
     * @return QueryDslFilter from received information.
     */
    public static QueryDslFilter from(final String key, final String operator) {
        return from(key, operator, null);
    }

    /**
     * Generates a QueryDslFilters allowing String operator as input.
     * <p>
     * This String operator must be supported at {@link QueryDslOperators}.
     *
     * @param key:      entity field name.
     * @param operator: operator to apply.
     * @param value:    value to apply in the operation.
     * @return QueryDslFilter from received information.
     */
    public static QueryDslFilter from(final String key, final String operator, final Object value) {
        QueryDslFilter result = null;

        if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(operator)) {
            final Optional<QueryDslOperators> maySupportedOperator = QueryDslOperators.from(operator);

            if (maySupportedOperator.isPresent()) {
                result = new QueryDslFilter(key, maySupportedOperator.get(), value);
            }
        } else {
            final String errorMsg = String.format("QueryDslFilter couldn't be generated with key='%s', operator='%s'.", key, operator);
            log.error(errorMsg);
            throw new QueryDslFiltersException(errorMsg);
        }

        return result;
    }

    @Override
    public String toString() {
        String result;

        if (Objects.nonNull(this.operator)) {
            if (this.operator.isFunction()) {
                if (Objects.nonNull(this.key) && Objects.nonNull(this.value)) {
                    result = String.format(FUNCTION_OPERATOR, this.operator.getOperator(), this.key, this.value);
                } else if (Objects.nonNull(this.key) && Objects.isNull(this.value)) {
                    result = String.format(FUNCTION_OPERATOR_SINGLE_ARGUMENT, this.operator.getOperator(), this.key);
                } else {
                    final String errorMsg = String.format("""
                            QueryDslFilter couldn't be parsed to String a function operator='%s' has been configured without arguments.
                            """, this.operator);
                    log.error(errorMsg);
                    throw new QueryDslFiltersException(errorMsg);
                }
            } else {
                result = String.format(NORMAL_OPERATOR, this.key, this.operator.getOperator(), this.value);
            }
        } else {
            final String errorMsg = "QueryDslFilter couldn't be parsed to String, filter needs an operator.";
            log.error(errorMsg);
            throw new QueryDslFiltersException(errorMsg);
        }

        return result;
    }

}

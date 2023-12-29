package es.squdan.querydsl.filters;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Enum with all supported QueryDsl operators that may be applied into {@link QueryDslFilter}.
 * <p>
 * There are 2 differen types of operators.
 * <p>
 * - Normal: =, !=, >, etc...
 * +++ Example: username = test
 * <p>
 * - Functions: isNull, equals, contains, etc...
 * +++ Example: isNull(username)
 * +++ Example: equals(username, test)
 */
@Getter
@AllArgsConstructor
public enum QueryDslOperators {
    // Null-NonNull
    IS_NULL_FUNCTION("isNull", true), NON_NULL_FUNCTION("nonNull", true),
    // Equals
    EQUALS("=", false), EQUALS_FUNCTION("equals", true), EQUALS_FUNCTION_EQ("eq", true),
    // NotEquals
    NOT_EQUALS("!=", false), NON_EQUALS_FUNCTION("nonEquals", true), NON_EQUALS_FUNCTION_NE("ne", true),
    // Starts
    STARTS_WITH_FUNCTION("starts", true), STARTS_WITH_FUNCTION_SW("sw", true),
    // Ends
    ENDS_WITH_FUNCTION("ends", true), ENDS_WITH_FUNCTION_EW("ew", true),
    // Contains
    CONTAIN_FUNCTION("contains", true), CONTAIN_FUNCTION_C("c", true), CONTAINS_FUNCTION_LIKE("like", true),
    // Greater
    GREATER_THAN(">", false), GREATER_THAN_FUNCTION_GT("gt", true),
    GREATER_THAN_OR_EQUALS(">=", false), GREATER_THAN_OR_EQUALS_FUNCTION_GTE("gte", true),
    // Lower
    LOWER_THAN("<", false), LOWER_THAN_FUNCTION_LT("lt", true),
    LOWER_THAN_OR_EQUALS("<=", false), LOWER_THAN_OR_EQUALS_FUNCTION_LTE("lte", true);

    private final String operator;
    private final boolean function;

    /**
     * Returns QueryDslOperator that matches with received String.
     *
     * @param operator: String operator name.
     * @return QueryDslOperator if found.
     */
    public static Optional<QueryDslOperators> from(final String operator) {
        return Stream.of(QueryDslOperators.values()).filter(o -> o.getOperator().equals(operator)).findFirst();
    }
}

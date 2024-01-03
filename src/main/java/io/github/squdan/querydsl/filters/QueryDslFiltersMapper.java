package io.github.squdan.querydsl.filters;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Class to map from String to QueryDslFilters.
 * <p>
 * String filters must follow QueryDslFilters formats:
 * <p>
 * - Operations:
 * --> Example: user.name = test
 * <p>
 * Functions:
 * --> Example: nonNull(currency)
 * --> Example: contains(username, test)
 */
public final class QueryDslFiltersMapper {

    // Configuration - Allowed characters
    private static final String ALLOWED_CHARACTERS_KEYS = "[A-Za-z0-9._()-]";
    private static final String ALLOWED_CHARACTERS_VALUES = "[A-Za-z0-9.:_-]";
    private static final String ALLOWED_FUNCTION_SEPARATOR_CHARACTERS = "[,:;]";

    // Configuration - Operators formats
    // --> Example: user.name = test
    // --> Example: price <= 5.5
    private static final String REGEX_QUERY_DSL_OPERATORS = "(%s+)(%s|%s)(%s+)";
    // --> Example: nonNull(currency)
    // --> Example: contains(username, test)
    private static final String REGEX_QUERY_DSL_FUNCTION_OPERATORS = "(%s)\\((%s+) *%s? *(%s*)\\)";

    // Configuration - Patterns to apply to build QueryDslFilters
    private static final Pattern PATTERN_QUERY_DSL_OPERATORS;
    private static final Pattern PATTERN_QUERY_DSL_FUNCTION_OPERATORS;

    static {
        final StringBuilder regexQueryDslOperatorsSimpleBuilder = new StringBuilder();
        final StringBuilder regexQueryDslOperatorsMultipleBuilder = new StringBuilder();
        final StringBuilder regexQueryDslFunctionOperatorsBuilder = new StringBuilder();

        Stream.of(QueryDslOperators.values()).forEach(o -> {
            // Normal operators
            if (!o.isFunction()) {
                // Split simple operators (=, >...) from multiple operators (>=, <=...)
                if (o.getOperator().length() == 1) {
                    if (!regexQueryDslOperatorsSimpleBuilder.isEmpty()) {
                        regexQueryDslOperatorsSimpleBuilder.append("|");
                    }

                    regexQueryDslOperatorsSimpleBuilder.append(o.getOperator());
                } else {
                    if (!regexQueryDslOperatorsMultipleBuilder.isEmpty()) {
                        regexQueryDslOperatorsMultipleBuilder.append("|");
                    }

                    regexQueryDslOperatorsMultipleBuilder.append(o.getOperator());
                }
            }

            // Function operators
            else {
                if (!regexQueryDslFunctionOperatorsBuilder.isEmpty()) {
                    regexQueryDslFunctionOperatorsBuilder.append("|");
                }

                regexQueryDslFunctionOperatorsBuilder.append(o.getOperator());
            }
        });

        // Prepare regex expressions
        final String regexQueryDslOperators = String.format(REGEX_QUERY_DSL_OPERATORS, ALLOWED_CHARACTERS_KEYS, regexQueryDslOperatorsMultipleBuilder, regexQueryDslOperatorsSimpleBuilder, ALLOWED_CHARACTERS_VALUES);
        final String regexQueryDslFunctionOperators = String.format(REGEX_QUERY_DSL_FUNCTION_OPERATORS, regexQueryDslFunctionOperatorsBuilder, ALLOWED_CHARACTERS_KEYS, ALLOWED_FUNCTION_SEPARATOR_CHARACTERS, ALLOWED_CHARACTERS_VALUES);

        // Prepare patterns
        PATTERN_QUERY_DSL_OPERATORS = Pattern.compile(regexQueryDslOperators);
        PATTERN_QUERY_DSL_FUNCTION_OPERATORS = Pattern.compile(regexQueryDslFunctionOperators);
    }

    /**
     * Maps received QueryDslFilter in String format to QueryDslFilter.
     * <p>
     * If format not correct, null will be returned.
     *
     * @param filter: QueryDslFilter in String format.
     * @return QueryDslFilter.
     */
    public static QueryDslFilter map(final String filter) {
        QueryDslFilter result = processAsOperatorFilters(filter);

        // if processAsOperatorFilters returns null, then received filter must be function
        if (Objects.isNull(result)) {
            result = processAsFunctionOperatorFilters(filter);
        }

        // if still null, then received filter has a wrong format
        if (Objects.isNull(result)) {
            throw new QueryDslFiltersException(String.format("Filter '%s' has wrong format.", filter));
        }

        return result;
    }

    /**
     * Maps received QueryDslFilter list in String format to QueryDslFilter.
     * <p>
     * If format not correct, null will be returned.
     *
     * @param filters: List QueryDslFilter in String format.
     * @return List of QueryDslFilter mapped filters.
     */
    public static List<QueryDslFilter> map(final List<String> filters) {
        List<QueryDslFilter> result = null;

        if (CollectionUtils.isNotEmpty(filters)) {
            result = new ArrayList<>();

            for (String filter : filters) {
                result.add(map(filter));
            }
        }

        return result;
    }

    private static QueryDslFilter processAsOperatorFilters(final String filter) {
        QueryDslFilter result = null;

        if (StringUtils.isNotBlank(filter)) {
            final Matcher matcher = PATTERN_QUERY_DSL_OPERATORS.matcher(filter);

            if (matcher.find()) {
                result = QueryDslFilter.from(matcher.group(1), matcher.group(2), matcher.group(3));
            }
        }

        return result;
    }

    private static QueryDslFilter processAsFunctionOperatorFilters(final String filter) {
        QueryDslFilter result = null;

        if (StringUtils.isNotBlank(filter)) {
            final Matcher matcher = PATTERN_QUERY_DSL_FUNCTION_OPERATORS.matcher(filter);

            if (matcher.find()) {
                if (StringUtils.isBlank(matcher.group(3))) {
                    result = QueryDslFilter.from(matcher.group(2), matcher.group(1));
                } else {
                    result = QueryDslFilter.from(matcher.group(2), matcher.group(1), matcher.group(3));
                }
            }
        }

        return result;
    }
}

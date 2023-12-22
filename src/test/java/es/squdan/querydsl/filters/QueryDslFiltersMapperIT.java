package es.squdan.querydsl.filters;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
public class QueryDslFiltersMapperIT {

    // Constants
    private static final String TEST_KEY = "test_key";
    private static final String TEST_VALUE = "test_value";

    private static Stream<Arguments> provideSingleFilterTestCases() {
        return Stream.of(
                Arguments.of("isNull(test_key)", new QueryDslFilter(TEST_KEY, QueryDslOperators.IS_NULL_FUNCTION)),
                Arguments.of("nonNull(test_key)", new QueryDslFilter(TEST_KEY, QueryDslOperators.NON_NULL_FUNCTION)),
                Arguments.of("test_key=test_value", new QueryDslFilter(TEST_KEY, QueryDslOperators.EQUALS, TEST_VALUE)),
                Arguments.of("equals(test_key : test_value)", new QueryDslFilter(TEST_KEY, QueryDslOperators.EQUALS_FUNCTION, TEST_VALUE)),
                Arguments.of("eq(test_key : test_value)", new QueryDslFilter(TEST_KEY, QueryDslOperators.EQUALS_FUNCTION_EQ, TEST_VALUE)),
                Arguments.of("test_key!=test_value", new QueryDslFilter(TEST_KEY, QueryDslOperators.NOT_EQUALS, TEST_VALUE)),
                Arguments.of("nonEquals(test_key : test_value)", new QueryDslFilter(TEST_KEY, QueryDslOperators.NON_EQUALS_FUNCTION, TEST_VALUE)),
                Arguments.of("ne(test_key : test_value)", new QueryDslFilter(TEST_KEY, QueryDslOperators.NON_EQUALS_FUNCTION_NE, TEST_VALUE)),
                Arguments.of("starts(test_key : test_value)", new QueryDslFilter(TEST_KEY, QueryDslOperators.STARTS_WITH_FUNCTION, TEST_VALUE)),
                Arguments.of("sw(test_key : test_value)", new QueryDslFilter(TEST_KEY, QueryDslOperators.STARTS_WITH_FUNCTION_SW, TEST_VALUE)),
                Arguments.of("ends(test_key : test_value)", new QueryDslFilter(TEST_KEY, QueryDslOperators.ENDS_WITH_FUNCTION, TEST_VALUE)),
                Arguments.of("ew(test_key : test_value)", new QueryDslFilter(TEST_KEY, QueryDslOperators.ENDS_WITH_FUNCTION_EW, TEST_VALUE)),
                Arguments.of("contains(test_key : test_value)", new QueryDslFilter(TEST_KEY, QueryDslOperators.CONTAIN_FUNCTION, TEST_VALUE)),
                Arguments.of("c(test_key : test_value)", new QueryDslFilter(TEST_KEY, QueryDslOperators.CONTAIN_FUNCTION_C, TEST_VALUE)),
                Arguments.of("like(test_key : test_value)", new QueryDslFilter(TEST_KEY, QueryDslOperators.CONTAINS_FUNCTION_LIKE, TEST_VALUE)),
                Arguments.of("test_key>test_value", new QueryDslFilter(TEST_KEY, QueryDslOperators.GREATER_THAN, TEST_VALUE)),
                Arguments.of("gt(test_key : test_value)", new QueryDslFilter(TEST_KEY, QueryDslOperators.GREATER_THAN_FUNCTION_GT, TEST_VALUE)),
                Arguments.of("test_key>=test_value", new QueryDslFilter(TEST_KEY, QueryDslOperators.GREATER_THAN_OR_EQUALS, TEST_VALUE)),
                Arguments.of("gte(test_key : test_value)", new QueryDslFilter(TEST_KEY, QueryDslOperators.GREATER_THAN_OR_EQUALS_FUNCTION_GTE, TEST_VALUE)),
                Arguments.of("test_key<test_value", new QueryDslFilter(TEST_KEY, QueryDslOperators.LOWER_THAN, TEST_VALUE)),
                Arguments.of("lt(test_key : test_value)", new QueryDslFilter(TEST_KEY, QueryDslOperators.LOWER_THAN_FUNCTION_LT, TEST_VALUE)),
                Arguments.of("test_key<=test_value", new QueryDslFilter(TEST_KEY, QueryDslOperators.LOWER_THAN_OR_EQUALS, TEST_VALUE)),
                Arguments.of("lte(test_key : test_value)", new QueryDslFilter(TEST_KEY, QueryDslOperators.LOWER_THAN_OR_EQUALS_FUNCTION_LTE, TEST_VALUE))
        );
    }

    @ParameterizedTest
    @MethodSource("provideSingleFilterTestCases")
    void test_map_singleFilter_returnExpectedFilter(final String filter, final QueryDslFilter expectedFilter) throws Exception {
        // Test execution
        final QueryDslFilter queryDslFilter = QueryDslFiltersMapper.map(filter);

        // Response validation
        Assertions.assertTrue(Objects.nonNull(queryDslFilter), "QueryDslFiltersMapper returned empty or null.");
        compareQueryDslFilters(expectedFilter, queryDslFilter);
        Assertions.assertEquals(expectedFilter.toString(), filter, "QueryDslFilter.toString() not equals.");
    }

    private static Stream<Arguments> provideMultipleFiltersTestCases() {
        return Stream.of(
                Arguments.of(
                        List.of(
                                "isNull(test_key)",
                                "test_key=test_value"
                        ),
                        List.of(
                                new QueryDslFilter(TEST_KEY, QueryDslOperators.IS_NULL_FUNCTION),
                                new QueryDslFilter(TEST_KEY, QueryDslOperators.EQUALS, TEST_VALUE)
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("provideMultipleFiltersTestCases")
    void test_map_multipleFilters_returnExpectedFilters(final List<String> filters, final List<QueryDslFilter> expectedFilters) throws Exception {
        // Test execution
        final List<QueryDslFilter> queryDslFilters = QueryDslFiltersMapper.map(filters);

        // Response validation
        Assertions.assertTrue(CollectionUtils.isNotEmpty(queryDslFilters), "QueryDslFiltersMapper returned empty or null.");
        Assertions.assertEquals(filters.size(), queryDslFilters.size(), "QueryDslFiltersMapper returned filters size not matching.");

        IntStream.range(0, filters.size()).forEach(i -> {
            compareQueryDslFilters(searchFilterByOperatorType(expectedFilters, queryDslFilters.get(i).getOperator()), queryDslFilters.get(i));
            Assertions.assertEquals(expectedFilters.get(i).toString(), filters.get(i), "QueryDslFilter.toString() not equals.");
        });
    }

    private static Stream<Arguments> provideSingleFilterWrongFormatTestCases() {
        return Stream.of(
                Arguments.of("isNull[test_key]"),
                Arguments.of("test_ke??test_value"),
                Arguments.of("equals(test_key ,_: test_value)")
        );
    }

    @ParameterizedTest
    @MethodSource("provideSingleFilterWrongFormatTestCases")
    void test_map_wrongFormat_returnQueryDslFiltersException(final String filter) throws Exception {
        // Test execution
        final QueryDslFiltersException thrown = Assertions.assertThrows(
                QueryDslFiltersException.class,
                () -> QueryDslFiltersMapper.map(filter));

        // Response validation
        Assertions.assertTrue(Objects.nonNull(thrown));
    }

    private QueryDslFilter searchFilterByOperatorType(final List<QueryDslFilter> filters, final QueryDslOperators operator) {
        return filters.stream().filter(f -> f.getOperator().equals(operator)).findFirst().orElseThrow();
    }

    private void compareQueryDslFilters(final QueryDslFilter expected, final QueryDslFilter returned) {
        Assertions.assertEquals(expected.getKey(), returned.getKey(), "QueryDslFilter returned key don't match.");
        Assertions.assertEquals(expected.getOperator(), returned.getOperator(), "QueryDslFilter returned operator don't match.");
        Assertions.assertEquals(expected.getValue(), returned.getValue(), "QueryDslFilter returned value don't match.");
    }
}

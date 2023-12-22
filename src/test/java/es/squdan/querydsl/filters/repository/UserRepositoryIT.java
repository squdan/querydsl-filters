package es.squdan.querydsl.filters.repository;

import es.squdan.querydsl.filters.QueryDslFilter;
import es.squdan.querydsl.filters.QueryDslOperators;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootTest(classes = {TestQueryDslFiltersApplication.class, UserRepository.class})
@ExtendWith(SpringExtension.class)
public class UserRepositoryIT {

    // Constants
    private static final Instant NOW = Instant.now();
    private static final UserEntity ADMIN = UserEntity.builder()
            .id(UUID.fromString("24930b8e-ff6e-476a-8758-4bd3ff5ebd6b"))
            .username("admin")
            .password("test")
            .role(Roles.ADMIN)
            .name("Admin Name")
            .lastName("Admin Lastname")
            .savings(new BigDecimal("35.5"))
            .build();
    private static final UserEntity USER = UserEntity.builder()
            .id(UUID.fromString("26ad7565-ba11-4914-bf91-84557b8b8764"))
            .username("user")
            .password("test")
            .role(Roles.USER)
            .name("Use Name")
            .build();

    // Class to test
    @Autowired
    private UserRepository userRepository;

    private static Stream<Arguments> provideSingleFilterTestCases() {
        return Stream.of(
                Arguments.of(new QueryDslFilter("lastName", QueryDslOperators.IS_NULL_FUNCTION), List.of(USER)),
                Arguments.of(new QueryDslFilter("username", QueryDslOperators.NON_NULL_FUNCTION), List.of(ADMIN, USER)),
                Arguments.of(new QueryDslFilter("name", QueryDslOperators.EQUALS, ADMIN.getName()), List.of(ADMIN)),
                Arguments.of(new QueryDslFilter("role", QueryDslOperators.EQUALS_FUNCTION, Roles.USER), List.of(USER)),
                Arguments.of(new QueryDslFilter("id", QueryDslOperators.EQUALS_FUNCTION_EQ, ADMIN.getId()), List.of(ADMIN)),
                Arguments.of(new QueryDslFilter("password", QueryDslOperators.NOT_EQUALS, "unknown"), List.of(ADMIN, USER)),
                Arguments.of(new QueryDslFilter("id", QueryDslOperators.NON_EQUALS_FUNCTION, "e4cc6317-3197-4bed-9707-978bb9d54630"), List.of(ADMIN, USER)),
                Arguments.of(new QueryDslFilter("id", QueryDslOperators.NON_EQUALS_FUNCTION_NE, ADMIN.getId()), List.of(USER)),
                Arguments.of(new QueryDslFilter("name", QueryDslOperators.STARTS_WITH_FUNCTION, "Adm"), List.of(ADMIN)),
                Arguments.of(new QueryDslFilter("name", QueryDslOperators.STARTS_WITH_FUNCTION_SW, "Us"), List.of(USER)),
                Arguments.of(new QueryDslFilter("name", QueryDslOperators.ENDS_WITH_FUNCTION, "me"), List.of(ADMIN, USER)),
                Arguments.of(new QueryDslFilter("name", QueryDslOperators.ENDS_WITH_FUNCTION_EW, "Admin Name"), List.of(USER)),
                Arguments.of(new QueryDslFilter("lastName", QueryDslOperators.CONTAIN_FUNCTION, "min"), List.of(ADMIN)),
                Arguments.of(new QueryDslFilter("username", QueryDslOperators.CONTAIN_FUNCTION_C, "user"), List.of(USER)),
                Arguments.of(new QueryDslFilter("name", QueryDslOperators.CONTAINS_FUNCTION_LIKE, "anm"), List.of(ADMIN, USER)),
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.GREATER_THAN, "30.1"), List.of(ADMIN)),
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.GREATER_THAN_FUNCTION_GT, "35.6"), CollectionUtils.emptyCollection()),
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.GREATER_THAN_OR_EQUALS, "35.5"), List.of(ADMIN)),
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.GREATER_THAN_OR_EQUALS_FUNCTION_GTE, "51"), CollectionUtils.emptyCollection()),
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.LOWER_THAN, "35.6"), List.of(ADMIN)),
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.LOWER_THAN_FUNCTION_LT, "35.45"), CollectionUtils.emptyCollection()),
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.LOWER_THAN_OR_EQUALS_FUNCTION_LTE, "35.5"), List.of(ADMIN))
        );
    }

    @ParameterizedTest
    @MethodSource("provideSingleFilterTestCases")
    void test_map_singleFilter_returnExpectedFilter(final QueryDslFilter filters, final List<UserEntity> expectedResult) throws Exception {
        // Test execution
        final List<UserEntity> mayResults = userRepository.findAll(List.of(filters), null);

        // Response validation
        Assertions.assertTrue(Objects.nonNull(mayResults), "Results searching with QueryDslFilters is null.");
        Assertions.assertEquals(expectedResult.size(), mayResults.size(), "Results number aren't equals.");

        mayResults.forEach(r -> {
            final UserEntity expectedUser = searchUserEntityById(expectedResult, r.getId());
            Assertions.assertEquals(expectedUser.getId(), r.getId(), "Wrong id");
            Assertions.assertEquals(expectedUser.getRole(), r.getRole(), "Wrong role");
            Assertions.assertEquals(expectedUser.getUsername(), r.getUsername(), "Wrong username");
            Assertions.assertEquals(expectedUser.getPassword(), r.getPassword(), "Wrong password");
            Assertions.assertEquals(expectedUser.getName(), r.getName(), "Wrong name");
            Assertions.assertEquals(expectedUser.getLastName(), r.getLastName(), "Wrong lastname");
            Assertions.assertEquals(expectedUser.getSavings(), r.getSavings(), "Wrong savings");
        });
    }

    private UserEntity searchUserEntityById(final List<UserEntity> users, final UUID id) {
        return users.stream().filter(u -> u.getId().equals(id)).findFirst().orElseThrow();
    }
}

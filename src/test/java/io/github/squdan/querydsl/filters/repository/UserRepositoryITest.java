package io.github.squdan.querydsl.filters.repository;

import io.github.squdan.querydsl.filters.QueryDslFilter;
import io.github.squdan.querydsl.filters.QueryDslOperators;
import io.github.squdan.querydsl.filters.configuration.DatabaseTestConfiguration;
import io.github.squdan.querydsl.filters.configuration.TestQueryDslFiltersApplication;
import io.github.squdan.querydsl.filters.repository.entity.BankAccountEntity;
import io.github.squdan.querydsl.filters.repository.entity.BankAccountId;
import io.github.squdan.querydsl.filters.repository.entity.Roles;
import io.github.squdan.querydsl.filters.repository.entity.UserEntity;
import io.github.squdan.querydsl.filters.util.DateTimeUtils;
import jakarta.transaction.Transactional;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootTest(classes = {TestQueryDslFiltersApplication.class, DatabaseTestConfiguration.class, UserRepository.class})
@ExtendWith(SpringExtension.class)
public class UserRepositoryITest {

    // Constants
    private static final Instant NOW = Instant.now();
    private static final UserEntity ADMIN = UserEntity.builder()
            .id(UUID.fromString("24930b8e-ff6e-476a-8758-4bd3ff5ebd6b"))
            .username("admin")
            .password("test")
            .role(Roles.ADMIN)
            .name("Admin Name")
            .lastName("Admin Lastname")
            .savings(new BigDecimal("35.50"))
            .createdOn(DateTimeUtils.toInstantUtc("2020-06-14T00:00:00Z"))
            .lastUpdatedOn(DateTimeUtils.toInstantUtc("2021-11-27T16:41:32Z"))
            .build();
    private static final UserEntity USER = UserEntity.builder()
            .id(UUID.fromString("26ad7565-ba11-4914-bf91-84557b8b8764"))
            .username("user")
            .password("test")
            .role(Roles.USER)
            .name("User Name")
            .accounts(List.of(
                    BankAccountEntity.builder()
                            .id(BankAccountId.builder()
                                    .bank("bank_name")
                                    .account("ES12 1234 1234 12 1234567891")
                                    .build()
                            )
                            .amount(new BigDecimal("4987.33"))
                            .build(),
                    BankAccountEntity.builder()
                            .id(BankAccountId.builder()
                                    .bank("bank_name")
                                    .account("ES12 1234 1234 12 1234567892")
                                    .build()
                            )
                            .amount(new BigDecimal("10"))
                            .build(),
                    BankAccountEntity.builder()
                            .id(BankAccountId.builder()
                                    .bank("bank_name2")
                                    .account("ES12 1234 1234 12 1234567891")
                                    .build()
                            )
                            .amount(new BigDecimal("10000.00"))
                            .build()
            ))
            .build();

    // Class to test
    @Autowired
    private UserRepository userRepository;

    private static Stream<Arguments> provideSingleFilterTestCases() {
        return Stream.of(
                // Is null
                Arguments.of(new QueryDslFilter("lastName", QueryDslOperators.IS_NULL_FUNCTION), List.of(USER)),

                // Not null
                Arguments.of(new QueryDslFilter("username", QueryDslOperators.NON_NULL_FUNCTION), List.of(ADMIN, USER)),

                // Equals
                Arguments.of(new QueryDslFilter("name", QueryDslOperators.EQUALS, ADMIN.getName()), List.of(ADMIN)),
                Arguments.of(new QueryDslFilter("role", QueryDslOperators.EQUALS_FUNCTION, Roles.USER), List.of(USER)),
                Arguments.of(new QueryDslFilter("id", QueryDslOperators.EQUALS_FUNCTION_EQ, ADMIN.getId()), List.of(ADMIN)),

                // Not equals
                Arguments.of(new QueryDslFilter("password", QueryDslOperators.NOT_EQUALS, "unknown"), List.of(ADMIN, USER)),
                Arguments.of(new QueryDslFilter("id", QueryDslOperators.NON_EQUALS_FUNCTION, "e4cc6317-3197-4bed-9707-978bb9d54630"), List.of(ADMIN, USER)),
                Arguments.of(new QueryDslFilter("role", QueryDslOperators.NON_EQUALS_FUNCTION_NE, Roles.ADMIN), List.of(USER)),

                // Starts with (String)
                Arguments.of(new QueryDslFilter("name", QueryDslOperators.STARTS_WITH_FUNCTION, "Adm"), List.of(ADMIN)),
                Arguments.of(new QueryDslFilter("name", QueryDslOperators.STARTS_WITH_FUNCTION_SW, "Us"), List.of(USER)),

                // Ends with (String)
                Arguments.of(new QueryDslFilter("name", QueryDslOperators.ENDS_WITH_FUNCTION, "me"), List.of(ADMIN, USER)),
                Arguments.of(new QueryDslFilter("name", QueryDslOperators.ENDS_WITH_FUNCTION_EW, "dmin Name"), List.of(ADMIN)),

                // Contains (String)
                Arguments.of(new QueryDslFilter("lastName", QueryDslOperators.CONTAIN_FUNCTION, "min"), List.of(ADMIN)),
                Arguments.of(new QueryDslFilter("username", QueryDslOperators.CONTAIN_FUNCTION_C, "user"), List.of(USER)),
                Arguments.of(new QueryDslFilter("name", QueryDslOperators.CONTAINS_FUNCTION_LIKE, "am"), List.of(ADMIN, USER)),

                // Greater than (Numbers)
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.GREATER_THAN, "35.4"), List.of(ADMIN)),
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.GREATER_THAN, "35.5"), CollectionUtils.emptyCollection()),
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.GREATER_THAN, "35.6"), CollectionUtils.emptyCollection()),
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.GREATER_THAN_FUNCTION_GT, "35.4"), List.of(ADMIN)),
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.GREATER_THAN_FUNCTION_GT, "35.5"), CollectionUtils.emptyCollection()),
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.GREATER_THAN_FUNCTION_GT, "35.6"), CollectionUtils.emptyCollection()),

                // Greater than (Dates)
                Arguments.of(new QueryDslFilter("createdOn", QueryDslOperators.GREATER_THAN, ADMIN.getCreatedOn().minus(1, ChronoUnit.DAYS)), List.of(ADMIN, USER)),
                Arguments.of(new QueryDslFilter("createdOn", QueryDslOperators.GREATER_THAN, ADMIN.getCreatedOn()), List.of(USER)),
                Arguments.of(new QueryDslFilter("createdOn", QueryDslOperators.GREATER_THAN_FUNCTION_GT, NOW.minus(1, ChronoUnit.HOURS)), List.of(USER)),
                Arguments.of(new QueryDslFilter("createdOn", QueryDslOperators.GREATER_THAN_FUNCTION_GT, NOW.plus(1, ChronoUnit.DAYS)), CollectionUtils.emptyCollection()),

                // Greater than or equals (Numbers)
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.GREATER_THAN_OR_EQUALS, "35.4"), List.of(ADMIN)),
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.GREATER_THAN_OR_EQUALS, "35.5"), List.of(ADMIN)),
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.GREATER_THAN_OR_EQUALS, "35.6"), CollectionUtils.emptyCollection()),
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.GREATER_THAN_OR_EQUALS_FUNCTION_GTE, "35.4"), List.of(ADMIN)),
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.GREATER_THAN_OR_EQUALS_FUNCTION_GTE, "35.5"), List.of(ADMIN)),
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.GREATER_THAN_OR_EQUALS_FUNCTION_GTE, "35.6"), CollectionUtils.emptyCollection()),

                // Greater than or equals (Dates)
                Arguments.of(new QueryDslFilter("createdOn", QueryDslOperators.GREATER_THAN_OR_EQUALS, ADMIN.getCreatedOn().minus(1, ChronoUnit.DAYS)), List.of(ADMIN, USER)),
                Arguments.of(new QueryDslFilter("createdOn", QueryDslOperators.GREATER_THAN_OR_EQUALS, ADMIN.getCreatedOn()), List.of(ADMIN, USER)),
                Arguments.of(new QueryDslFilter("createdOn", QueryDslOperators.GREATER_THAN_OR_EQUALS_FUNCTION_GTE, NOW.minus(1, ChronoUnit.HOURS)), List.of(USER)),
                Arguments.of(new QueryDslFilter("createdOn", QueryDslOperators.GREATER_THAN_OR_EQUALS_FUNCTION_GTE, NOW.plus(1, ChronoUnit.DAYS)), CollectionUtils.emptyCollection()),

                // Lower than (Numbers)
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.LOWER_THAN, "35.6"), List.of(ADMIN)),
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.LOWER_THAN, "35.5"), CollectionUtils.emptyCollection()),
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.LOWER_THAN, "35.4"), CollectionUtils.emptyCollection()),
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.LOWER_THAN_FUNCTION_LT, "35.6"), List.of(ADMIN)),
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.LOWER_THAN_FUNCTION_LT, "35.5"), CollectionUtils.emptyCollection()),
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.LOWER_THAN_FUNCTION_LT, "35.4"), CollectionUtils.emptyCollection()),

                // Lower than (Dates)
                Arguments.of(new QueryDslFilter("lastUpdatedOn", QueryDslOperators.LOWER_THAN, ADMIN.getLastUpdatedOn().plus(1, ChronoUnit.DAYS)), List.of(ADMIN)),
                Arguments.of(new QueryDslFilter("lastUpdatedOn", QueryDslOperators.LOWER_THAN, ADMIN.getLastUpdatedOn()), CollectionUtils.emptyCollection()),
                Arguments.of(new QueryDslFilter("lastUpdatedOn", QueryDslOperators.LOWER_THAN_FUNCTION_LT, NOW.plus(1, ChronoUnit.HOURS)), List.of(ADMIN, USER)),
                Arguments.of(new QueryDslFilter("lastUpdatedOn", QueryDslOperators.LOWER_THAN_FUNCTION_LT, NOW.minus(1, ChronoUnit.DAYS)), List.of(ADMIN)),

                // Lower than or equals (Numbers)
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.LOWER_THAN_OR_EQUALS, "35.6"), List.of(ADMIN)),
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.LOWER_THAN_OR_EQUALS, "35.5"), List.of(ADMIN)),
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.LOWER_THAN_OR_EQUALS, "35.4"), CollectionUtils.emptyCollection()),
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.LOWER_THAN_OR_EQUALS_FUNCTION_LTE, "35.6"), List.of(ADMIN)),
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.LOWER_THAN_OR_EQUALS_FUNCTION_LTE, "35.5"), List.of(ADMIN)),
                Arguments.of(new QueryDslFilter("savings", QueryDslOperators.LOWER_THAN_OR_EQUALS_FUNCTION_LTE, "35.4"), CollectionUtils.emptyCollection()),

                // Lower than or equals (Dates)
                Arguments.of(new QueryDslFilter("lastUpdatedOn", QueryDslOperators.LOWER_THAN_OR_EQUALS, ADMIN.getLastUpdatedOn().plus(1, ChronoUnit.DAYS)), List.of(ADMIN)),
                Arguments.of(new QueryDslFilter("lastUpdatedOn", QueryDslOperators.LOWER_THAN_OR_EQUALS, ADMIN.getLastUpdatedOn()), List.of(ADMIN)),
                Arguments.of(new QueryDslFilter("lastUpdatedOn", QueryDslOperators.LOWER_THAN_OR_EQUALS_FUNCTION_LTE, NOW.plus(1, ChronoUnit.HOURS)), List.of(ADMIN, USER)),
                Arguments.of(new QueryDslFilter("lastUpdatedOn", QueryDslOperators.LOWER_THAN_OR_EQUALS_FUNCTION_LTE, NOW.minus(1, ChronoUnit.DAYS)), List.of(ADMIN)),

                // ------------------ Nested List ------------------
                // Is null
                Arguments.of(new QueryDslFilter("accounts.id.bank", QueryDslOperators.IS_NULL_FUNCTION), CollectionUtils.emptyCollection()),

                // Not null
                Arguments.of(new QueryDslFilter("accounts.id.bank", QueryDslOperators.NON_NULL_FUNCTION), List.of(USER)),

                // Equals
                Arguments.of(new QueryDslFilter("accounts.id.account", QueryDslOperators.EQUALS, USER.getAccounts().get(0).getId().getAccount()), List.of(USER)),
                Arguments.of(new QueryDslFilter("accounts.id.account", QueryDslOperators.EQUALS, "unknown"), CollectionUtils.emptyCollection()),

                // Not equals
                Arguments.of(new QueryDslFilter("accounts.id.bank", QueryDslOperators.NOT_EQUALS, USER.getAccounts().get(0).getId().getBank()), List.of(USER)),
                Arguments.of(new QueryDslFilter("accounts.id.bank", QueryDslOperators.NOT_EQUALS, "unknown"), List.of(USER)),

                // Starts with (String)
                Arguments.of(new QueryDslFilter("accounts.id.account", QueryDslOperators.STARTS_WITH_FUNCTION, "ES12 12"), List.of(USER)),
                Arguments.of(new QueryDslFilter("accounts.id.account", QueryDslOperators.STARTS_WITH_FUNCTION, "unknown"), CollectionUtils.emptyCollection()),

                // Ends with (String)
                Arguments.of(new QueryDslFilter("accounts.id.account", QueryDslOperators.ENDS_WITH_FUNCTION, "67892"), List.of(USER)),
                Arguments.of(new QueryDslFilter("accounts.id.account", QueryDslOperators.ENDS_WITH_FUNCTION, "unknown"), CollectionUtils.emptyCollection()),

                // Contains (String)
                Arguments.of(new QueryDslFilter("accounts.id.bank", QueryDslOperators.CONTAIN_FUNCTION, "nk_na"), List.of(USER)),
                Arguments.of(new QueryDslFilter("accounts.id.bank", QueryDslOperators.CONTAIN_FUNCTION, "unknown"), CollectionUtils.emptyCollection()),

                // Greater than (Numbers)
                Arguments.of(new QueryDslFilter("accounts.amount", QueryDslOperators.GREATER_THAN, "10"), List.of(USER)),
                Arguments.of(new QueryDslFilter("accounts.amount", QueryDslOperators.GREATER_THAN, "10000.00"), CollectionUtils.emptyCollection()),
                Arguments.of(new QueryDslFilter("accounts.amount", QueryDslOperators.GREATER_THAN, "11000.00"), CollectionUtils.emptyCollection()),

                // Greater than or equals (Numbers)
                Arguments.of(new QueryDslFilter("accounts.amount", QueryDslOperators.GREATER_THAN_OR_EQUALS, "10"), List.of(USER)),
                Arguments.of(new QueryDslFilter("accounts.amount", QueryDslOperators.GREATER_THAN_OR_EQUALS, "10000.00"), List.of(USER)),
                Arguments.of(new QueryDslFilter("accounts.amount", QueryDslOperators.GREATER_THAN_OR_EQUALS, "11000.00"), CollectionUtils.emptyCollection()),

                // Lower than (Numbers)
                Arguments.of(new QueryDslFilter("accounts.amount", QueryDslOperators.LOWER_THAN, "9"), CollectionUtils.emptyCollection()),
                Arguments.of(new QueryDslFilter("accounts.amount", QueryDslOperators.LOWER_THAN, "10"), CollectionUtils.emptyCollection()),
                Arguments.of(new QueryDslFilter("accounts.amount", QueryDslOperators.LOWER_THAN, "11"), List.of(USER)),

                // Lower than or equals (Numbers)
                Arguments.of(new QueryDslFilter("accounts.amount", QueryDslOperators.LOWER_THAN_OR_EQUALS, "9"), CollectionUtils.emptyCollection()),
                Arguments.of(new QueryDslFilter("accounts.amount", QueryDslOperators.LOWER_THAN_OR_EQUALS, "10"), List.of(USER)),
                Arguments.of(new QueryDslFilter("accounts.amount", QueryDslOperators.LOWER_THAN_OR_EQUALS, "11"), List.of(USER)),

                // ------------------ Nested from Nested List ------------------
                // Is null
                Arguments.of(new QueryDslFilter("accounts.user.lastName", QueryDslOperators.IS_NULL_FUNCTION), List.of(USER)),

                // Not null
                Arguments.of(new QueryDslFilter("accounts.user.username", QueryDslOperators.NON_NULL_FUNCTION), List.of(USER)),

                // Equals
                Arguments.of(new QueryDslFilter("accounts.user.role", QueryDslOperators.EQUALS, Roles.USER), List.of(USER)),
                Arguments.of(new QueryDslFilter("accounts.user.role", QueryDslOperators.EQUALS, Roles.ADMIN), CollectionUtils.emptyCollection()),

                // Not equals
                Arguments.of(new QueryDslFilter("accounts.user.id", QueryDslOperators.NOT_EQUALS, USER.getId()), CollectionUtils.emptyCollection()),
                Arguments.of(new QueryDslFilter("accounts.user.id", QueryDslOperators.NOT_EQUALS, ADMIN.getId()), List.of(USER)),

                // Starts with (String)
                Arguments.of(new QueryDslFilter("accounts.user.username", QueryDslOperators.STARTS_WITH_FUNCTION, "ad"), CollectionUtils.emptyCollection()),
                Arguments.of(new QueryDslFilter("accounts.user.username", QueryDslOperators.STARTS_WITH_FUNCTION, "u"), List.of(USER)),

                // Ends with (String)
                Arguments.of(new QueryDslFilter("accounts.user.username", QueryDslOperators.ENDS_WITH_FUNCTION, "in"), CollectionUtils.emptyCollection()),
                Arguments.of(new QueryDslFilter("accounts.user.username", QueryDslOperators.ENDS_WITH_FUNCTION, "er"), List.of(USER)),

                // Contains (String)
                Arguments.of(new QueryDslFilter("accounts.user.lastName", QueryDslOperators.CONTAIN_FUNCTION, "min"), CollectionUtils.emptyCollection()),
                Arguments.of(new QueryDslFilter("accounts.user.username", QueryDslOperators.CONTAIN_FUNCTION, "amin"), CollectionUtils.emptyCollection()),

                // Greater than (Dates)
                Arguments.of(new QueryDslFilter("accounts.user.createdOn", QueryDslOperators.GREATER_THAN, ADMIN.getCreatedOn().minus(1, ChronoUnit.DAYS)), List.of(USER)),
                Arguments.of(new QueryDslFilter("accounts.user.createdOn", QueryDslOperators.GREATER_THAN, ADMIN.getCreatedOn()), List.of(USER)),

                // Greater than or equals (Numbers)
                Arguments.of(new QueryDslFilter("accounts.user.savings", QueryDslOperators.GREATER_THAN_OR_EQUALS, "35.4"), CollectionUtils.emptyCollection()),
                Arguments.of(new QueryDslFilter("accounts.user.savings", QueryDslOperators.GREATER_THAN_OR_EQUALS, "35.5"), CollectionUtils.emptyCollection()),
                Arguments.of(new QueryDslFilter("accounts.user.savings", QueryDslOperators.GREATER_THAN_OR_EQUALS, "35.6"), CollectionUtils.emptyCollection()),

                // Lower than (Numbers)
                Arguments.of(new QueryDslFilter("accounts.user.savings", QueryDslOperators.LOWER_THAN, "35.6"), CollectionUtils.emptyCollection()),
                Arguments.of(new QueryDslFilter("accounts.user.savings", QueryDslOperators.LOWER_THAN, "35.5"), CollectionUtils.emptyCollection()),
                Arguments.of(new QueryDslFilter("accounts.user.savings", QueryDslOperators.LOWER_THAN, "35.4"), CollectionUtils.emptyCollection()),

                // Lower than or equals (Dates)
                Arguments.of(new QueryDslFilter("accounts.user.lastUpdatedOn", QueryDslOperators.LOWER_THAN_OR_EQUALS, ADMIN.getLastUpdatedOn().plus(1, ChronoUnit.DAYS)), CollectionUtils.emptyCollection()),
                Arguments.of(new QueryDslFilter("accounts.user.lastUpdatedOn", QueryDslOperators.LOWER_THAN_OR_EQUALS, ADMIN.getLastUpdatedOn()), CollectionUtils.emptyCollection())
        );
    }

    @Transactional
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

            if (CollectionUtils.isNotEmpty(expectedUser.getAccounts())) {
                Assertions.assertEquals(expectedUser.getAccounts().size(), r.getAccounts().size(), "Wrong accounts number");
            }
        });
    }

    private UserEntity searchUserEntityById(final List<UserEntity> users, final UUID id) {
        return users.stream().filter(u -> u.getId().equals(id)).findFirst().orElseThrow();
    }
}

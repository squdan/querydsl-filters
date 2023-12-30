package io.github.squdan.querydsl.filters.repository.entity;

import java.util.Objects;

public enum Roles {

    ADMIN, USER;

    public static boolean isRoleValue(final Object value) {
        return value instanceof Roles || Objects.nonNull(Roles.valueOf(String.valueOf(value)));
    }
}

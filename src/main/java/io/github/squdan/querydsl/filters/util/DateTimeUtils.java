package io.github.squdan.querydsl.filters.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Utility class to parse from String to Date.
 * <p>
 * Multiple formats are supported by default.
 * <p>
 * Users can add their own formats using method: DateTimeUtils.addDateTimeFormat(DateTimeFormatter)
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateTimeUtils {

    // Supported date formatters
    private final static List<DateTimeFormatter> DATE_FORMATTERS = new ArrayList<>(
            List.of(
                    DateTimeFormatter.ISO_DATE
            )
    );

    // Supported date time formatters
    private final static List<DateTimeFormatter> DATE_TIME_FORMATTERS = new ArrayList<>(
            List.of(
                    DateTimeFormatter.ISO_DATE_TIME,
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
            )
    );

    /**
     * Adds an extra DateTimeFormatter to support new format when parsing from String-Instant.
     *
     * @param newDateFormat: new format to support.
     */
    public static void addDateFormat(final DateTimeFormatter newDateFormat) {
        DATE_FORMATTERS.add(newDateFormat);
    }

    /**
     * Adds an extra DateTimeFormatter to support new format when parsing from String-Instant.
     *
     * @param newDateTimeFormat: new format to support.
     */
    public static void addDateTimeFormat(final DateTimeFormatter newDateTimeFormat) {
        DATE_TIME_FORMATTERS.add(newDateTimeFormat);
    }

    public static void setTimezone(final ZoneId zoneId) {
        if (Objects.isNull(zoneId)) {
            log.info("Configurando timezone 'UTC'.");
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        } else {
            log.info("Configurando timezone '{}'.", zoneId);
            TimeZone.setDefault(TimeZone.getTimeZone(zoneId));
        }
    }

    /**
     * Returns received String as Instant.
     * <p>
     * String format must be supported, otherwise you must add the new format to support before.
     * <p>
     * If null or empty String is received then this method will return null.
     *
     * @param date (java.lang.String) to parse to Date.
     * @return (java.time.LocalDateTime) Date.
     */
    public static Instant toInstantUtc(final String date) {
        Instant result = null;

        if (StringUtils.isNotBlank(date)) {
            // Try to parse from String to LocalDateTime
            final LocalDateTime localDateTime = toLocalDateTime(date);

            if (Objects.nonNull(localDateTime)) {
                result = localDateTime.atZone(TimeZone.getDefault().toZoneId()).toInstant();
            }

            // Try to parse from String to LocalDate
            if (Objects.isNull(result)) {
                final LocalDate localDate = toLocalDate(date);

                if (Objects.nonNull(localDate)) {
                    result = localDate.atStartOfDay().atZone(TimeZone.getDefault().toZoneId()).toInstant();
                }
            }
        }

        return result;
    }

    /**
     * Returns received String as LocalDateTime.
     * <p>
     * String format must be supported, otherwise you must add the new format to support before.
     * <p>
     * If null or empty String is received then this method will return null.
     *
     * @param date (java.lang.String) to parse to Date.
     * @return (java.time.LocalDateTime) Date.
     */
    public static LocalDateTime toLocalDateTime(final String date) {
        LocalDateTime result = null;

        if (StringUtils.isNotBlank(date)) {
            for (DateTimeFormatter formatter : DATE_TIME_FORMATTERS) {
                if (Objects.isNull(result)) {
                    result = toLocalDateTime(formatter, date);
                }
            }
        }

        return result;
    }

    /**
     * Returns received String as LocalDate.
     * <p>
     * String format must be supported, otherwise you must add the new format to support before.
     * <p>
     * If null or empty String is received then this method will return null.
     *
     * @param date (java.lang.String) to parse to Date.
     * @return (java.time.LocalTime) Date.
     */
    public static LocalDate toLocalDate(final String date) {
        LocalDate result = null;

        if (StringUtils.isNotBlank(date)) {
            for (DateTimeFormatter formatter : DATE_FORMATTERS) {
                if (Objects.isNull(result)) {
                    result = toLocalDate(formatter, date);
                }
            }
        }

        return result;
    }

    private static LocalDateTime toLocalDateTime(final DateTimeFormatter formatter, final String date) {
        LocalDateTime result = null;

        try {
            result = LocalDateTime.parse(date, formatter);
        } catch (final DateTimeParseException e) {
            // Do nothing
        }

        return result;
    }

    private static LocalDate toLocalDate(final DateTimeFormatter formatter, final String date) {
        LocalDate result = null;

        try {
            result = LocalDate.parse(date, formatter);
        } catch (final DateTimeParseException e) {
            // Do nothing
        }

        return result;
    }
}
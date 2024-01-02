package io.github.squdan.querydsl.filters.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Utility class to parse from String to Date.
 * <p>
 * Multiple formats are supported {@link SuportedDateTimeFormats}.
 * <p>
 * Users can add their own formats using method: DateTimeUtils.addDateTimeFormat(DateTimeFormatter)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateTimeUtils {

    // Supported date formats
    @Getter
    @AllArgsConstructor
    private enum SuportedDateTimeFormats {
        DATE_TIME_FORMAT_1("yyyy-MM-dd'T'HH:mm:ss'Z'"),
        DATE_TIME_FORMAT_2("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"),
        DATE_TIME_FORMAT_3("yyyy-MM-dd'T'HH:mm:ss"),
        DATE_TIME_FORMAT_4("yyyy-MM-dd'T'HH:mm:ss.SSS");

        final String format;

        public DateTimeFormatter getDateTimeFormatter() {
            return DateTimeFormatter.ofPattern(this.format);
        }
    }

    private final static List<DateTimeFormatter> FORMATTERS = new ArrayList<>(
            List.of(
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME
            )
    );

    static {
        FORMATTERS.addAll(Arrays.stream(SuportedDateTimeFormats.values()).map(SuportedDateTimeFormats::getDateTimeFormatter).toList());
    }

    /**
     * Adds an extra DateTimeFormatter to support new format when parsing from String-Instant.
     *
     * @param newDateTimeFormat: new format to support.
     */
    public static void addDateTimeFormat(final DateTimeFormatter newDateTimeFormat) {
        FORMATTERS.add(newDateTimeFormat);
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
            LocalDateTime localDateTime = toLocalDateTime(date);

            if (Objects.nonNull(localDateTime)) {
                result = localDateTime.toInstant(ZoneOffset.UTC);
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
            for (DateTimeFormatter formatter : FORMATTERS) {
                if (Objects.isNull(result)) {
                    result = toLocalDateTime(formatter, date);
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
}
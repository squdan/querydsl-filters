package io.github.squdan.querydsl.filters;

/**
 * QueryDslFilter context exception.
 */
public class QueryDslFiltersException extends RuntimeException {

    /**
     * Constructor from message.
     *
     * @param msg: error message.
     */
    public QueryDslFiltersException(final String msg) {
        super(msg);
    }

    /**
     * Constructor from exception and message.
     *
     * @param exception: source exception.
     * @param msg:       error message.
     */
    public QueryDslFiltersException(final Throwable exception, final String msg) {
        super(msg, exception);
    }

}

package es.squdan.querydsl.filters;

/**
 * QueryDslFilter context exception.
 */
public class QueryDslFiltersException extends RuntimeException {

    public QueryDslFiltersException(final String msg) {
        super(msg);
    }

    public QueryDslFiltersException(final Throwable exception, final String msg) {
        super(msg, exception);
    }

}

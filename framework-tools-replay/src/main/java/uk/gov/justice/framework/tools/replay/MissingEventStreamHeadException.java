package uk.gov.justice.framework.tools.replay;

public class MissingEventStreamHeadException extends RuntimeException {

    public MissingEventStreamHeadException(final String message) {
        super(message);
    }
}

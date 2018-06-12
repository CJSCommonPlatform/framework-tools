package uk.gov.justice.framework.tools.replay;

import org.slf4j.MDC;

public class LoggingMdc {

    public void put(final String key, final String value) {
        MDC.put(key, value);
    }

    public void remove(final String key) {
        MDC.remove(key);
    }

    public void clear() {
        MDC.clear();
    }
}

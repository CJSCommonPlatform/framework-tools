package uk.gov.justice.framework.tools.replay;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.MDC;


@RunWith(MockitoJUnitRunner.class)
public class LoggingMdcTest {


    @InjectMocks
    private LoggingMdc loggingMdc;

    @Test
    public void shouldWrapTheMdcStaticCall() throws Exception {

        final String key_1 = "key_1";
        final String value_1 = "value_1";
        final String key_2 = "key_2";
        final String value_2 = "value_2";

        loggingMdc.put(key_1, value_1);
        loggingMdc.put(key_2, value_2);

        assertThat(MDC.get(key_1), is(value_1));
        assertThat(MDC.get(key_2), is(value_2));

        loggingMdc.remove(key_1);

        assertThat(MDC.get(key_1), is(nullValue()));
        assertThat(MDC.get(key_2), is(value_2));

        loggingMdc.remove(key_2);

        assertThat(MDC.get(key_1), is(nullValue()));
        assertThat(MDC.get(key_2), is(nullValue()));
    }

    @Test
    public void shouldClearTheMdc() throws Exception {

        final String key_1 = "key_1";
        final String value_1 = "value_1";
        final String key_2 = "key_2";
        final String value_2 = "value_2";

        loggingMdc.put(key_1, value_1);
        loggingMdc.put(key_2, value_2);

        assertThat(MDC.get(key_1), is(value_1));
        assertThat(MDC.get(key_2), is(value_2));

        loggingMdc.clear();

        assertThat(MDC.get(key_1), is(nullValue()));
        assertThat(MDC.get(key_2), is(nullValue()));
    }
}

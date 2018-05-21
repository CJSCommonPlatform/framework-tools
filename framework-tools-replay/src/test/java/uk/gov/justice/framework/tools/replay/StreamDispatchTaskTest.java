package uk.gov.justice.framework.tools.replay;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.enterprise.concurrent.ManagedTaskListener;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class StreamDispatchTaskTest {

    @Mock
    private Stream<JsonEnvelope> stream;
    @Mock
    private AsyncStreamDispatcher dispatcher;
    @Mock
    private ManagedTaskListener taskListener;

    private StreamDispatchTask streamDispatchTask;

    @Before
    public void setup() {
//        streamDispatchTask = new StreamDispatchTask(uuid, stream, dispatcher, taskListener);
    }


    @Test
    public void shouldCallDispatcher() {
        streamDispatchTask.call();

//        verify(dispatcher).dispatch(eq(stream));
    }

    @Test
    public void shouldReturnNullExecutionProperties() {
        assertNull(streamDispatchTask.getExecutionProperties());
    }

    @Test
    public void shouldReturnTaskListener() {
        assertEquals(streamDispatchTask.getManagedTaskListener(), taskListener);
    }
}

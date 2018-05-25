package uk.gov.justice.framework.tools.replay;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import javax.enterprise.concurrent.ManagedTaskListener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StreamDispatchTaskTest {

    @Mock
    private AsyncStreamDispatcher dispatcher;

    @Mock
    private ManagedTaskListener taskListener;

    @Test
    public void shouldCallDispatcher() {
        final UUID streamId = randomUUID();
        final StreamDispatchTask streamDispatchTask = new StreamDispatchTask(streamId, dispatcher, taskListener);

        streamDispatchTask.call();

        verify(dispatcher).dispatch(streamId);
    }

    @Test
    public void shouldReturnNullExecutionProperties() {
        final StreamDispatchTask streamDispatchTask = new StreamDispatchTask(randomUUID(), dispatcher, taskListener);

        assertNull(streamDispatchTask.getExecutionProperties());
    }

    @Test
    public void shouldReturnTaskListener() {
        final StreamDispatchTask streamDispatchTask = new StreamDispatchTask(randomUUID(), dispatcher, taskListener);

        assertThat(streamDispatchTask.getManagedTaskListener(), is(taskListener));
    }
}

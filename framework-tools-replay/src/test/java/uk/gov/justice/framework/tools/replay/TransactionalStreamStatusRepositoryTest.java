package uk.gov.justice.framework.tools.replay;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.event.buffer.core.repository.subscription.StreamStatusJdbcRepository;
import uk.gov.justice.services.event.buffer.core.repository.subscription.Subscription;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransactionalStreamStatusRepositoryTest {

    @Mock
    private StreamStatusJdbcRepository streamStatusJdbcRepository;

    @InjectMocks
    private TransactionalStreamStatusRepository streamDispatcherDelegate;

    @Test
    public void shouldDelegateTheInsertOfStreamStatusToTheRepositoryToAllowForTransactionalAnnotation() throws Exception {

        final Subscription subscription = mock(Subscription.class);

        streamDispatcherDelegate.insert(subscription);

        verify(streamStatusJdbcRepository).insert(subscription);
    }
}

package uk.gov.justice.framework.tools.replay;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.event.buffer.core.repository.subscription.Subscription;
import uk.gov.justice.services.event.buffer.core.repository.subscription.SubscriptionJdbcRepository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransactionalStreamStatusRepositoryTest {

    @Mock
    private SubscriptionJdbcRepository streamStatusRepository;

    @InjectMocks
    private TransactionalStreamStatusRepository streamDispatcherDelegate;

    @Test
    public void shouldDelegateTheInsertOfStreamStatusToTheRepositoryToAllowForTransactionalAnnotation() throws Exception {

        final Subscription subscription = mock(Subscription.class);

        streamDispatcherDelegate.insert(subscription);

        verify(streamStatusRepository).insert(subscription);
    }
}

package uk.gov.justice.framework.tools.replay;

import static javax.transaction.Transactional.TxType.REQUIRED;

import uk.gov.justice.services.event.buffer.core.repository.subscription.Subscription;
import uk.gov.justice.services.event.buffer.core.repository.subscription.StreamStatusJdbcRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class TransactionalStreamStatusRepository {

    @Inject
    private StreamStatusJdbcRepository streamStatusJdbcRepository;

    @Transactional(REQUIRED)
    public void insert(final Subscription subscription) {
        streamStatusJdbcRepository.insert(subscription);
    }
}

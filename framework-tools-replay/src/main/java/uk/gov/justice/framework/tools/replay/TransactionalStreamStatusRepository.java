package uk.gov.justice.framework.tools.replay;

import static javax.transaction.Transactional.TxType.REQUIRED;

import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatus;
import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatusJdbcRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class TransactionalStreamStatusRepository {

    @Inject
    private StreamStatusJdbcRepository streamStatusRepository;

    @Transactional(REQUIRED)
    public void insert(final StreamStatus streamStatus) {
        streamStatusRepository.insert(streamStatus);
    }
}

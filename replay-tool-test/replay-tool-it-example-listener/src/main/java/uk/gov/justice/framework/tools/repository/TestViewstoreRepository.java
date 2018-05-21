package uk.gov.justice.framework.tools.repository;

import uk.gov.justice.framework.tools.entity.TestEvent;

import java.util.UUID;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;

@Repository
public interface TestViewstoreRepository extends EntityRepository<TestEvent, UUID> {

}

package uk.gov.justice.framework.tools.repository;


import uk.gov.justice.framework.tools.database.domain.User;

import java.util.UUID;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;

@Repository
public interface TestViewstoreRepository extends EntityRepository<User, UUID> {

}

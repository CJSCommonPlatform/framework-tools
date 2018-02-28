package uk.gov.justice.framework.tools.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.framework.tools.entity.Document;
import uk.gov.justice.framework.tools.entity.Test;
import uk.gov.justice.framework.tools.repository.TestViewstoreRepository;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.slf4j.Logger;

@ServiceComponent(EVENT_LISTENER)
public class TestEventListener {

    @Inject
    private Logger logger;

    @Inject
    private TestViewstoreRepository testViewstoreRepository;

    @Handles("framework.example-test")
    public void handle(final JsonEnvelope envelope) {
        logger.info("******************** framework.example-test");

        final UUID testId = envelope.metadata().streamId().orElse(null);

        final Test test = new Test(
                testId,
                envelope.payloadAsJsonObject().getString("data"));

        final JsonArray documents = envelope.payloadAsJsonObject().getJsonArray("documents");

        final List<Document> documentsList = new ArrayList<>();
        for (JsonObject object : documents.getValuesAs(JsonObject.class)) {
            documentsList.add(new Document(
                    UUID.fromString(object.getString("documentId")),
                    object.getString("name"),
                    testId));
        }
        test.setDocuments(documentsList);

        testViewstoreRepository.save(test);
    }

    @Handles("framework.example.update")
    public void handler(final JsonEnvelope envelope) {
        logger.info("******************** framework.example.update");

        final JsonObject payload = envelope.payloadAsJsonObject();

        final String testId = payload.getString("testId");
        final String documentId = payload.getString("documentId");
        final String documentName = payload.getString("name");

        final Test foundEntity = testViewstoreRepository.findBy(UUID.fromString(testId));

        foundEntity.getDocuments().stream()
                .filter(d -> d.getDocumentId().equals(UUID.fromString(documentId)))
                .forEach(d -> d.setName(documentName));
    }
}

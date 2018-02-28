package uk.gov.justice.framework.tools.entity;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "document")
public class Document implements Serializable{

    @Id
    @Column(name = "document_id")
    private UUID documentId;

    @Column(name = "name")
    private String name;

    @Column(name = "test_id", nullable = false)
    private UUID testId;

    public Document(final UUID documentId, final String name, final UUID testId) {
        this.documentId = documentId;
        this.name = name;
        this.testId = testId;
    }

    public UUID getTestId() {
        return testId;
    }

    public void setTestId(UUID testId) {
        this.testId = testId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }
}

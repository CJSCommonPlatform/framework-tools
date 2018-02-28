package uk.gov.justice.framework.tools.entity;

import static java.util.Collections.emptyList;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "test")
public class Test implements Serializable {

    @Id
    @Column(name = "test_id")
    private UUID testId;

    @Column(name = "data")
    private String data;

    @OneToMany(cascade = ALL, fetch = EAGER, mappedBy = "testId")
    private List<Document> documents = new ArrayList<>();

    public Test(final UUID testId, final String data) {
        this.testId = testId;
        this.data = data;
        documents = emptyList();
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public UUID getTestId() {
        return testId;
    }

    public void setTestId(UUID testId) {
        this.testId = testId;
    }

    @Override
    public String toString() {
        return "Test{" +
                "testId=" + testId +
                ", data='" + data + '\'' +
                '}';
    }
}

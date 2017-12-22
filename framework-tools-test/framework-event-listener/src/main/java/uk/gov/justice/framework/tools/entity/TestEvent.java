package uk.gov.justice.framework.tools.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "test")
public class TestEvent implements Serializable {

    @Id
    @Column(name = "stream_id")
    private UUID streamId;

    @Column(name = "version_id")
    private Integer versionId;

    @Column(name = "data")
    private String data;



    public TestEvent(UUID streamId, Integer versionId, String data) {
        this.streamId = streamId;
        this.versionId = versionId;
        this.data = data;
    }

    public UUID getStreamId() {
        return streamId;
    }

    public void setStreamId(UUID streamId) {
        this.streamId = streamId;
    }

    public Integer getVersionId() {
        return versionId;
    }

    public void setVersionId(int versionId) {
        this.versionId = versionId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "TestEvent{" +
                "streamId=" + streamId +
                ", versionId=" + versionId +
                ", data='" + data + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestEvent testEvent = (TestEvent) o;
        return versionId == testEvent.versionId &&
                Objects.equals(streamId, testEvent.streamId) &&
                Objects.equals(data, testEvent.data);
    }

    @Override
    public int hashCode() {

        return Objects.hash(streamId, versionId, data);
    }
}

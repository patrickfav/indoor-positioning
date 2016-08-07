package at.ac.tuwien.inso.indoor.sensorserver.persistence.model;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.couchdb.TypeAbleCouchDBDocument;

import java.util.Date;

/**
 * Created by PatrickF on 16.09.2014.
 */
public class PingLog extends TypeAbleCouchDBDocument implements Comparable<PingLog> {

    private String nodeId;
    private Date created;
    private boolean success;
    private boolean error=false;
    private String jobId;
    private String url;

    public PingLog() {
        this.created = new Date();
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "PingLog{" +
                "nodeId='" + nodeId + '\'' +
                ", created=" + created +
                ", success=" + success +
                ", error=" + error +
                ", jobId='" + jobId + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

    @Override
    public int compareTo(PingLog o) {
        return o.getCreated().compareTo(created);
    }
}

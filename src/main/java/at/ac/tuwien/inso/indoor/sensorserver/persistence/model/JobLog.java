package at.ac.tuwien.inso.indoor.sensorserver.persistence.model;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.couchdb.TypeAbleCouchDBDocument;

import java.util.Date;
import java.util.UUID;

/**
 * Created by PatrickF on 16.09.2014.
 */
public class JobLog extends TypeAbleCouchDBDocument {
    public enum Status {
        CREATED,
        RUNNING,
        ERROR,
        SUCCESS,
        CANCEL
    }

    private String jobId;
    private String networkId;
    private String nodeId;
    private Status status;

    private String jobTitle;
    private String jobDescription;
    private long estimatedRuntimeMs;
    private Date start;
    private Date end;
    private Date created;
    private Double progress;
    private String statusDescription;

    public JobLog() {
        jobId = UUID.randomUUID().toString();
        status = Status.CREATED;
        created = new Date();
        progress = 0d;
    }

    public JobLog(String jobId, String networkId, String nodeId) {
        this();
        this.jobId = jobId;
        this.networkId = networkId;
        this.nodeId = nodeId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public long getEstimatedRuntimeMs() {
        return estimatedRuntimeMs;
    }

    public void setEstimatedRuntimeMs(long estimatedRuntimeMs) {
        this.estimatedRuntimeMs = estimatedRuntimeMs;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Double getProgress() {
        return progress;
    }

    public void setProgress(Double progress) {
        this.progress = progress;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }
}

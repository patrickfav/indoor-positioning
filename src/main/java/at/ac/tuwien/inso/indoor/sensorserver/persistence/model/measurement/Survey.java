package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.couchdb.TypeAbleCouchDBDocument;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by PatrickF on 08.09.2014.
 */
public class Survey extends TypeAbleCouchDBDocument implements Comparable<Survey> {

    private String surveyId;
    private String nodeId;
    private String networkId;
    private String adapter;
    private Date created;
    private String jobId;

    private List<AverageWlanScanMeasurement> averageScanNodes;

    public Survey() {
        surveyId = UUID.randomUUID().toString();
        created = new Date();
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(String surveyId) {
        this.surveyId = surveyId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getAdapter() {
        return adapter;
    }

    public void setAdapter(String adapter) {
        this.adapter = adapter;
    }

    public List<AverageWlanScanMeasurement> getAverageScanNodes() {
        return averageScanNodes;
    }

    public void setAverageScanNodes(List<AverageWlanScanMeasurement> averageScanNodes) {
        this.averageScanNodes = averageScanNodes;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    @Override
    public int compareTo(Survey o) {
        return created.compareTo(o.getCreated());
    }
}

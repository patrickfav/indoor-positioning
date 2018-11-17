package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.couchdb.TypeAbleCouchDBDocument;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.SensorManager;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by PatrickF on 01.10.2014.
 */
public class AnalysisMetaData extends TypeAbleCouchDBDocument {
    private String analysisId;
    private Date from;
    private Date to;
    private Date created;
    private int surveySum;
    private int surveyPerNodeSum;
    private String networkId;
    private boolean pinnedActive = false;

    public AnalysisMetaData() {
        this.analysisId = UUID.randomUUID().toString();
        this.created = new Date();
    }

    public AnalysisMetaData(Analysis analysis) {
        setId(analysis.getId());
        setRevision(analysis.getRevision());
        analysisId = analysis.getAnalysisId();
        from = analysis.getFrom();
        to = analysis.getTo();
        created = analysis.getCreated();
        surveySum = analysis.getSurveySum();
        surveyPerNodeSum = analysis.getSurveyPerNodeSum();
        networkId = analysis.getNetworkId();
    }

    public AnalysisMetaData(List<Survey> surveyList, String networkId) {
        this();

        this.surveySum = surveyList.size();
        this.surveyPerNodeSum = surveyList.size() / SensorManager.getInstance().getAllNodesFromNetwork(networkId).size();
        this.networkId = networkId;

        setFromAndToDate(surveyList);
    }

    private void setFromAndToDate(List<Survey> surveyList) {
        this.from = new Date(Long.MAX_VALUE);
        this.to = new Date(0);

        for (Survey survey : surveyList) {
            if (survey.getCreated().getTime() < from.getTime()) {
                from = new Date(survey.getCreated().getTime());
            }
            if (survey.getCreated().getTime() > to.getTime()) {
                to = new Date(survey.getCreated().getTime());
            }
        }
    }

    public String getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(String analysisId) {
        this.analysisId = analysisId;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public int getSurveySum() {
        return surveySum;
    }

    public void setSurveySum(int surveySum) {
        this.surveySum = surveySum;
    }

    public int getSurveyPerNodeSum() {
        return surveyPerNodeSum;
    }

    public void setSurveyPerNodeSum(int surveyPerNodeSum) {
        this.surveyPerNodeSum = surveyPerNodeSum;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public boolean isPinnedActive() {
        return pinnedActive;
    }

    public void setPinnedActive(boolean pinnedActive) {
        this.pinnedActive = pinnedActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnalysisMetaData that = (AnalysisMetaData) o;

        if (surveyPerNodeSum != that.surveyPerNodeSum) return false;
        if (surveySum != that.surveySum) return false;
        if (!analysisId.equals(that.analysisId)) return false;
        if (created != null ? !created.equals(that.created) : that.created != null) return false;
        if (from != null ? !from.equals(that.from) : that.from != null) return false;
        if (networkId != null ? !networkId.equals(that.networkId) : that.networkId != null) return false;
        if (to != null ? !to.equals(that.to) : that.to != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = analysisId.hashCode();
        result = 31 * result + (from != null ? from.hashCode() : 0);
        result = 31 * result + (to != null ? to.hashCode() : 0);
        result = 31 * result + (created != null ? created.hashCode() : 0);
        result = 31 * result + surveySum;
        result = 31 * result + surveyPerNodeSum;
        result = 31 * result + (networkId != null ? networkId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AnalysisMetaData{" +
                "analysisId='" + analysisId + '\'' +
                ", from=" + from +
                ", to=" + to +
                ", created=" + created +
                ", surveySum=" + surveySum +
                ", surveyPerNodeSum=" + surveyPerNodeSum +
                ", networkId='" + networkId + '\'' +
                '}';
    }
}

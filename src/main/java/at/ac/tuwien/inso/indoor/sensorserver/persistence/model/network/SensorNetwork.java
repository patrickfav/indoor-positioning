package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network;

import at.ac.tuwien.inso.indoor.sensorserver.math.radiomodels.EEnvironmentModel;
import at.ac.tuwien.inso.indoor.sensorserver.math.radiomodels.ITUIndoorModelDegradingDist;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.couchdb.TypeAbleCouchDBDocument;

import java.util.Date;
import java.util.UUID;

/**
 * Created by PatrickF on 08.09.2014.
 */
public class SensorNetwork extends TypeAbleCouchDBDocument implements Comparable<SensorNetwork> {
    private String networkId;
    private String networkName;
    private String description;
    private Date createDate;
    private EEnvironmentModel environmentModel = EEnvironmentModel.INDOOR_OBSTRUCTED_OFFICE;
    private ITUIndoorModelDegradingDist.ITUDegradingDistConfig pathLossConfig = new ITUIndoorModelDegradingDist.ITUDegradingDistConfig();

    private String cronSchedulePing;
    private String cronScheduleSurvey;
    private String cronScheduleAnalysis;
    private boolean cronEnabled;
    private int surveysPerNodeForAnalysis = 150;

    private boolean deleted;

    public SensorNetwork() {
        networkId = UUID.randomUUID().toString();
        createDate = new Date();
        cronEnabled = true;
        deleted = false;
    }

    public EEnvironmentModel getEnvironmentModel() {
        return environmentModel;
    }

    public void setEnvironmentModel(EEnvironmentModel environmentModel) {
        this.environmentModel = environmentModel;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String id) {
        this.networkId = id;
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getCronSchedulePing() {
        return cronSchedulePing;
    }

    public void setCronSchedulePing(String cronSchedulePing) {
        this.cronSchedulePing = cronSchedulePing;
    }

    public String getCronScheduleSurvey() {
        return cronScheduleSurvey;
    }

    public void setCronScheduleSurvey(String cronScheduleSurvey) {
        this.cronScheduleSurvey = cronScheduleSurvey;
    }

    public String getCronScheduleAnalysis() {
        return cronScheduleAnalysis;
    }

    public void setCronScheduleAnalysis(String cronScheduleAnalysis) {
        this.cronScheduleAnalysis = cronScheduleAnalysis;
    }

    public boolean isCronEnabled() {
        return cronEnabled;
    }

    public void setCronEnabled(boolean cronEnabled) {
        this.cronEnabled = cronEnabled;
    }

    public int getSurveysPerNodeForAnalysis() {
        return surveysPerNodeForAnalysis;
    }

    public void setSurveysPerNodeForAnalysis(int surveysPerNodeForAnalysis) {
        this.surveysPerNodeForAnalysis = surveysPerNodeForAnalysis;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public ITUIndoorModelDegradingDist.ITUDegradingDistConfig getPathLossConfig() {
        return pathLossConfig;
    }

    public void setPathLossConfig(ITUIndoorModelDegradingDist.ITUDegradingDistConfig pathLossConfig) {
        this.pathLossConfig = pathLossConfig;
    }

    @Override
    public int compareTo(SensorNetwork o) {
        return o.getCreateDate().compareTo(createDate);
    }
}

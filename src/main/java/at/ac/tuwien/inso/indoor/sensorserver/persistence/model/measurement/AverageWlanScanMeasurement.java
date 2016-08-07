package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.SimpleStatistics;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.Statistics;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by PatrickF on 14.09.2014.
 */
public class AverageWlanScanMeasurement extends WlanScanNode {
    private List<Double> signalStrengths = new ArrayList<Double>();
    private Double confidence = 1.0;
    private SimpleStatistics statistics;

    public AverageWlanScanMeasurement() {
    }

    public AverageWlanScanMeasurement(WlanScanNode node) {
        setMacAddress(node.getMacAddress());
        setSsid(node.getSsid());
        setFrequencyRange(node.getFrequencyRange());
        setEncryption(node.getEncryption());
        setChannel(node.getChannel());
        setDate(new Date());
    }

    public List<Double> getSignalStrengths() {
        return signalStrengths;
    }

    public void setSignalStrengths(List<Double> signalStrengths) {
        statistics = new Statistics(signalStrengths);
        this.signalStrengths = signalStrengths;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public SimpleStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(SimpleStatistics statistics) {
        this.statistics = statistics;
    }


    public abstract static class AverageWlanScanMeasurementDBMixin {
        @JsonIgnore SimpleStatistics statistics;
        @JsonIgnore public abstract SimpleStatistics getStatistics();
        @JsonIgnore public abstract void setStatistics(SimpleStatistics statistics);
    }
}
package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.Statistics;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by PatrickF on 18.09.2014.
 */
public class SurveyStatistics implements Comparable<SurveyStatistics> {
    private String macAddress;
    private EFrequencyRange frequencyRange;
    private Set<String> ssidSet = new HashSet<String>();
    private Statistics statistics;
    private RadioModelData radioModelData;
    private boolean ignored;

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public EFrequencyRange getFrequencyRange() {
        return frequencyRange;
    }

    public void setFrequencyRange(EFrequencyRange frequencyRange) {
        this.frequencyRange = frequencyRange;
    }

    public Set<String> getSsidSet() {
        return ssidSet;
    }

    public void setSsidSet(Set<String> ssidSet) {
        this.ssidSet = ssidSet;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    public RadioModelData getRadioModelData() {
        return radioModelData;
    }

    public void setRadioModelData(RadioModelData radioModelData) {
        this.radioModelData = radioModelData;
    }

    @Override
    public int compareTo(SurveyStatistics o) {
        return o.getStatistics().getMean().compareTo(statistics.getMean());
    }
}

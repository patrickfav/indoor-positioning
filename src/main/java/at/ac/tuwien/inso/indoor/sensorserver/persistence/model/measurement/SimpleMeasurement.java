package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.SimpleStatistics;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by PatrickF on 11.11.2014.
 */
public class SimpleMeasurement {
    private String ssid;
    private String macAddress;
    private int channel;
    private List<Double> signalStrengths = new ArrayList<Double>();
    private Date date;
    private SimpleStatistics statistics;

    public SimpleMeasurement(AverageWlanScanMeasurement averageWlanScanMeasurement) {
        this();
        this.ssid = averageWlanScanMeasurement.getSsid();
        this.macAddress = averageWlanScanMeasurement.getMacAddress();
        this.channel = averageWlanScanMeasurement.getChannel();
        this.signalStrengths = averageWlanScanMeasurement.getSignalStrengths();
    }

    public SimpleMeasurement() {
        date = new Date();
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public List<Double> getSignalStrengths() {
        return signalStrengths;
    }

    public void setSignalStrengths(List<Double> signalStrengths) {
        this.signalStrengths = signalStrengths;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public SimpleStatistics getStatistics() {
        if (statistics == null && signalStrengths != null) {
            statistics = new SimpleStatistics(signalStrengths);
        }
        return statistics;
    }

    public void setStatistics(SimpleStatistics statistics) {
        this.statistics = statistics;
    }
}

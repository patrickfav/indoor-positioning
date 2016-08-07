package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.couchdb.TypeAbleCouchDBDocument;

import java.util.Date;

/**
 * Created by PatrickF on 08.09.2014.
 */
public class WlanScanNode extends TypeAbleCouchDBDocument implements Comparable<WlanScanNode> {

    private String ssid;
    private String macAddress;
    private Double signalStrengthDbm;
    private int channel;
    private String encryption;
    private EFrequencyRange frequencyRange;
    private Date date;

    public WlanScanNode() {
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

    public Double getSignalStrengthDbm() {
        return signalStrengthDbm;
    }

    public void setSignalStrengthDbm(Double signalStrengthDbm) {
        this.signalStrengthDbm = signalStrengthDbm;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public String getEncryption() {
        return encryption;
    }

    public void setEncryption(String encryption) {
        this.encryption = encryption;
    }

    public EFrequencyRange getFrequencyRange() {
        return frequencyRange;
    }

    public void setFrequencyRange(EFrequencyRange frequencyRange) {
        this.frequencyRange = frequencyRange;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    @Override
    public String toString() {
        return "ScanNode{" +
                "ssid='" + ssid + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", signalStrengthDbm=" + signalStrengthDbm +
                ", channel=" + channel +
                ", encryption='" + encryption + '\'' +
                ", frequencyRange=" + frequencyRange +
                ", date=" + date +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WlanScanNode wlanScanNode = (WlanScanNode) o;

        if (channel != wlanScanNode.channel) return false;
        if (Double.compare(wlanScanNode.signalStrengthDbm, signalStrengthDbm) != 0) return false;
        if (date != null ? !date.equals(wlanScanNode.date) : wlanScanNode.date != null) return false;
        if (encryption != null ? !encryption.equals(wlanScanNode.encryption) : wlanScanNode.encryption != null) return false;
        if (frequencyRange != wlanScanNode.frequencyRange) return false;
        if (macAddress != null ? !macAddress.equals(wlanScanNode.macAddress) : wlanScanNode.macAddress != null) return false;
        if (ssid != null ? !ssid.equals(wlanScanNode.ssid) : wlanScanNode.ssid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = ssid != null ? ssid.hashCode() : 0;
        result = 31 * result + (macAddress != null ? macAddress.hashCode() : 0);
        temp = Double.doubleToLongBits(signalStrengthDbm);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + channel;
        result = 31 * result + (encryption != null ? encryption.hashCode() : 0);
        result = 31 * result + (frequencyRange != null ? frequencyRange.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(WlanScanNode o) {
        return new Double(o.getSignalStrengthDbm()).compareTo(signalStrengthDbm);
    }
}

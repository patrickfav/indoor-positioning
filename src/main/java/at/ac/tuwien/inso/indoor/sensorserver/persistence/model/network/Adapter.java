package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.EFrequencyRange;

import java.util.Date;

/**
 * Created by PatrickF on 12.09.2014.
 */
public class Adapter implements Comparable<Adapter> {
    private String name;
    private String ssid;
    private Integer channel;
    private Integer txPowerDbm;
    private Integer noiseDbm;
    private String macAddress;
    private String mode;
    private Date created;
    private OUIMacInfo ouiMacInfo;
    private EFrequencyRange frequencyRange;

    public Adapter() {
        created = new Date();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public Integer getChannel() {
        return channel;
    }

    public void setChannel(Integer channel) {
        this.channel = channel;
    }

    public Integer getTxPowerDbm() {
        return txPowerDbm;
    }

    public void setTxPowerDbm(Integer txPowerDbm) {
        this.txPowerDbm = txPowerDbm;
    }

    public Integer getNoiseDbm() {
        return noiseDbm;
    }

    public void setNoiseDbm(Integer noiseDbm) {
        this.noiseDbm = noiseDbm;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public EFrequencyRange getFrequencyRange() {
        return frequencyRange;
    }

    public void setFrequencyRange(EFrequencyRange frequencyRange) {
        this.frequencyRange = frequencyRange;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public OUIMacInfo getOuiMacInfo() {
        return ouiMacInfo;
    }

    public void setOuiMacInfo(OUIMacInfo ouiMacInfo) {
        this.ouiMacInfo = ouiMacInfo;
    }

    @Override
    public String toString() {
        return "Adapter{" +
                "name='" + name + '\'' +
                ", ssid='" + ssid + '\'' +
                ", channel=" + channel +
                ", txPowerDbm=" + txPowerDbm +
                ", noiseDbm=" + noiseDbm +
                ", macAddress='" + macAddress + '\'' +
                ", created=" + created +
                ", frequencyRange=" + frequencyRange +
                '}';
    }

    @Override
    public int compareTo(Adapter o) {
        return name.compareTo(o.getName());
    }
}

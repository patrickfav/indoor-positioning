package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement;

import at.ac.tuwien.inso.indoor.sensorserver.util.ServerUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
* Created by PatrickF on 03.11.2014.
*/
public class PhysicalAdapter implements Comparable<PhysicalAdapter>{
    private String macAddress;
    private String nodeId;
    private String nodeName;
    private Set<String> ssidSet = new TreeSet<String>();
    private Set<String> adapterNames = new HashSet<String>();
    private EFrequencyRange frequencyRange;
    private String roomId;
    private double multiplier = 1d;

    public PhysicalAdapter() {
    }

    public PhysicalAdapter(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Set<String> getSsidSet() {
        return ssidSet;
    }

    public void setSsidSet(Set<String> ssidSet) {
        this.ssidSet = ssidSet;
    }

    public Set<String> getAdapterNames() {
        return adapterNames;
    }

    public void setAdapterNames(Set<String> adapterNames) {
        this.adapterNames = adapterNames;
    }

    public EFrequencyRange getFrequencyRange() {
        return frequencyRange;
    }

    public void setFrequencyRange(EFrequencyRange frequencyRange) {
        this.frequencyRange = frequencyRange;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PhysicalAdapter that = (PhysicalAdapter) o;

        if (macAddress != null ? !macAddress.equals(that.macAddress) : that.macAddress != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return macAddress != null ? macAddress.hashCode() : 0;
    }

    @Override
    public int compareTo(PhysicalAdapter o) {
        return ServerUtil.implode("", new ArrayList<String>(ssidSet)).compareTo(ServerUtil.implode("",new ArrayList<String>(o.getSsidSet())));
    }
}

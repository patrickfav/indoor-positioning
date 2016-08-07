package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.couchdb.TypeAbleCouchDBDocument;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.EFrequencyRange;
import at.ac.tuwien.inso.indoor.sensorserver.services.ApiConst;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;

/**
 * Created by PatrickF on 08.09.2014.
 */
public class SensorNode extends TypeAbleCouchDBDocument {

    private String nodeId;
    private String networkId;
    private String nodeName;
    private List<Adapter> adapters;
    private String ip;
    private int port;
    private String customResourcePath;
    private boolean coreNode;
    private Date createDate;
    private boolean httpsEnabled;
    private double signalStrengthMultiplicator;
    private boolean enabled;
    private MachineInfo machineInfo;
    private Integer antennaDBi=2;
    private String roomId;
    private Map<EFrequencyRange,Double> multiplierMap = new HashMap<EFrequencyRange, Double>();

    public SensorNode() {
        nodeId = UUID.randomUUID().toString();
        createDate = new Date();
        enabled=true;
        for (EFrequencyRange eFrequencyRange : EFrequencyRange.values()) {
            multiplierMap.put(eFrequencyRange,1d);
        }
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getCustomResourcePath() {
        return customResourcePath;
    }

    public void setCustomResourcePath(String customResourcePath) {
        this.customResourcePath = customResourcePath;
    }

    public boolean isCoreNode() {
        return coreNode;
    }

    public void setCoreNode(boolean coreNode) {
        this.coreNode = coreNode;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public List<Adapter> getAdapters() {
        return adapters;
    }

    public void setAdapters(List<Adapter> adapters) {
        this.adapters = adapters;
    }

    public boolean isHttpsEnabled() {
        return httpsEnabled;
    }

    public void setHttpsEnabled(boolean httpsEnabled) {
        this.httpsEnabled = httpsEnabled;
    }

    public double getSignalStrengthMultiplicator() {
        return signalStrengthMultiplicator;
    }

    public void setSignalStrengthMultiplicator(double signalStrengthMultiplicator) {
        this.signalStrengthMultiplicator = signalStrengthMultiplicator;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public MachineInfo getMachineInfo() {
        return machineInfo;
    }

    public void setMachineInfo(MachineInfo machineInfo) {
        this.machineInfo = machineInfo;
    }

    public Integer getAntennaDBi() {
        return antennaDBi;
    }

    public void setAntennaDBi(Integer antennaDBi) {
        this.antennaDBi = antennaDBi;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public Map<EFrequencyRange, Double> getMultiplierMap() {
        return multiplierMap;
    }

    public void setMultiplierMap(Map<EFrequencyRange, Double> multiplierMap) {
        this.multiplierMap = multiplierMap;
    }

    @JsonIgnore
    public String getFullUrl() {
        StringBuilder sb = new StringBuilder();
        sb.append(isHttpsEnabled() ? "https://":"http://");
        sb.append(getIp());
        if(port != 0) {
            sb.append(":").append(port);
        }
        if(customResourcePath != null && !customResourcePath.isEmpty() && customResourcePath.contains("/")) {
            sb.append(customResourcePath);
        } else {
            sb.append(ApiConst.DEFAULT_ROUTER_SERVICES_RES);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SensorNode that = (SensorNode) o;

        if (coreNode != that.coreNode) return false;
        if (enabled != that.enabled) return false;
        if (httpsEnabled != that.httpsEnabled) return false;
        if (port != that.port) return false;
        if (Double.compare(that.signalStrengthMultiplicator, signalStrengthMultiplicator) != 0) return false;
        if (adapters != null ? !adapters.equals(that.adapters) : that.adapters != null) return false;
        if (createDate != null ? !createDate.equals(that.createDate) : that.createDate != null) return false;
        if (customResourcePath != null ? !customResourcePath.equals(that.customResourcePath) : that.customResourcePath != null)
            return false;
        if (ip != null ? !ip.equals(that.ip) : that.ip != null) return false;
        if (networkId != null ? !networkId.equals(that.networkId) : that.networkId != null) return false;
        if (!nodeId.equals(that.nodeId)) return false;
        if (nodeName != null ? !nodeName.equals(that.nodeName) : that.nodeName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = nodeId.hashCode();
        result = 31 * result + (networkId != null ? networkId.hashCode() : 0);
        result = 31 * result + (nodeName != null ? nodeName.hashCode() : 0);
        result = 31 * result + (adapters != null ? adapters.hashCode() : 0);
        result = 31 * result + (ip != null ? ip.hashCode() : 0);
        result = 31 * result + port;
        result = 31 * result + (customResourcePath != null ? customResourcePath.hashCode() : 0);
        result = 31 * result + (coreNode ? 1 : 0);
        result = 31 * result + (createDate != null ? createDate.hashCode() : 0);
        result = 31 * result + (httpsEnabled ? 1 : 0);
        temp = Double.doubleToLongBits(signalStrengthMultiplicator);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (enabled ? 1 : 0);
        return result;
    }
}

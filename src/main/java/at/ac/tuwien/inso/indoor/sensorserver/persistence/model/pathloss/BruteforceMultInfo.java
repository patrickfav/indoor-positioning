package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.pathloss;

/**
 * Created by PatrickF on 03.11.2014.
 */
public class BruteforceMultInfo {
    private String nodeId1;
    private String nodeId2;

    private String mac1;
    private String mac2;

    private double dBm1;
    private double dBm2;

    public String getNodeId1() {
        return nodeId1;
    }

    public void setNodeId1(String nodeId1) {
        this.nodeId1 = nodeId1;
    }

    public String getNodeId2() {
        return nodeId2;
    }

    public void setNodeId2(String nodeId2) {
        this.nodeId2 = nodeId2;
    }

    public String getMac1() {
        return mac1;
    }

    public void setMac1(String mac1) {
        this.mac1 = mac1;
    }

    public String getMac2() {
        return mac2;
    }

    public void setMac2(String mac2) {
        this.mac2 = mac2;
    }

    public double getdBm1() {
        return dBm1;
    }

    public void setdBm1(double dBm1) {
        this.dBm1 = dBm1;
    }

    public double getdBm2() {
        return dBm2;
    }

    public void setdBm2(double dBm2) {
        this.dBm2 = dBm2;
    }
}

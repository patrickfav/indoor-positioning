package at.ac.tuwien.inso.indoor.sensorserver.services.positioner;

/**
 * Created by PatrickF on 10.10.2014.
 */
public class SensorPosition {
    private int xTiles;
    private int yTiles;
    private int xCm;
    private int yCm;
    private String macAddress;
    private NodeProbabilityDetails nodeProbabilityDetails;

    public SensorPosition(int xCm, int yCm, String macAddress, int tileLength) {
        this.xCm = xCm;
        this.yCm = yCm;
        this.xTiles = PosHelper.toTiles(xCm, tileLength);
        this.yTiles = PosHelper.toTiles(yCm, tileLength);
        this.macAddress = macAddress;
    }

    public int getxCm() {
        return xCm;
    }

    public void setxCm(int xCm) {
        this.xCm = xCm;
    }

    public int getyCm() {
        return yCm;
    }

    public void setyCm(int yCm) {
        this.yCm = yCm;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public int getxTiles() {
        return xTiles;
    }

    public void setxTiles(int xTiles) {
        this.xTiles = xTiles;
    }

    public int getyTiles() {
        return yTiles;
    }

    public void setyTiles(int yTiles) {
        this.yTiles = yTiles;
    }

    public NodeProbabilityDetails getNodeProbabilityDetails() {
        return nodeProbabilityDetails;
    }

    public void setNodeProbabilityDetails(NodeProbabilityDetails nodeProbabilityDetails) {
        this.nodeProbabilityDetails = nodeProbabilityDetails;
    }
}

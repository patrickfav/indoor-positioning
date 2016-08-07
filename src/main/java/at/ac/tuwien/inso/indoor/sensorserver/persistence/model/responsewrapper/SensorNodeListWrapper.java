package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.responsewrapper;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.PingLog;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.RoomList;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNetwork;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by PatrickF on 10.09.2014.
 */
public class SensorNodeListWrapper {
    private List<SensorNode> nodeList = new ArrayList<SensorNode>();
    private SensorNetwork sensorNetwork;
    private List<PingLog> pingLogList = new ArrayList<PingLog>();
    private RoomList roomList;

    public SensorNodeListWrapper() {
    }

    public List<PingLog> getPingLogList() {
        return pingLogList;
    }

    public void setPingLogList(List<PingLog> pingLogList) {
        this.pingLogList = pingLogList;
    }

    public List<SensorNode> getNodeList() {
        return nodeList;
    }

    public void setNodeList(List<SensorNode> nodeList) {
        this.nodeList = nodeList;
    }

    public SensorNetwork getSensorNetwork() {
        return sensorNetwork;
    }

    public void setSensorNetwork(SensorNetwork sensorNetwork) {
        this.sensorNetwork = sensorNetwork;
    }

    public RoomList getRoomList() {
        return roomList;
    }

    public void setRoomList(RoomList roomList) {
        this.roomList = roomList;
    }
}

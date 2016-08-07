package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.responsewrapper;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.PingLog;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.RoomList;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNetwork;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNode;

import java.util.List;

/**
 * Created by PatrickF on 10.09.2014.
 */
public class SensorNodeDetailsWrapper {
    private SensorNode sensorNode;
    private SensorNetwork sensorNetwork;
    private List<PingLog> pingList;
    private RoomList roomList;

    public SensorNodeDetailsWrapper() {
    }

    public SensorNode getSensorNode() {
        return sensorNode;
    }

    public void setSensorNode(SensorNode sensorNode) {
        this.sensorNode = sensorNode;
    }

    public SensorNetwork getSensorNetwork() {
        return sensorNetwork;
    }

    public void setSensorNetwork(SensorNetwork sensorNetwork) {
        this.sensorNetwork = sensorNetwork;
    }

    public List<PingLog> getPingList() {
        return pingList;
    }

    public void setPingList(List<PingLog> pingList) {
        this.pingList = pingList;
    }

    public RoomList getRoomList() {
        return roomList;
    }

    public void setRoomList(RoomList roomList) {
        this.roomList = roomList;
    }
}

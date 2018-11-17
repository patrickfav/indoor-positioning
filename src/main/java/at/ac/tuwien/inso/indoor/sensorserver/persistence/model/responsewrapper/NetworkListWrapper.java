package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.responsewrapper;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.PingLog;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNetwork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by PatrickF on 27.09.2014.
 */
public class NetworkListWrapper {
    private List<SensorNetwork> networks = new ArrayList<SensorNetwork>();
    private Map<String, List<PingLog>> pingMap = new HashMap<String, List<PingLog>>();

    public List<SensorNetwork> getNetworks() {
        return networks;
    }

    public void setNetworks(List<SensorNetwork> networks) {
        this.networks = networks;
    }

    public Map<String, List<PingLog>> getPingMap() {
        return pingMap;
    }

    public void setPingMap(Map<String, List<PingLog>> pingMap) {
        this.pingMap = pingMap;
    }
}

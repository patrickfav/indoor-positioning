package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.responsewrapper;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.MiscManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.SensorManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.PingLog;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNode;

import java.util.*;

/**
 * Created by PatrickF on 05.10.2014.
 */
public class PingLogHistory {
    Map<Date,Double> onlineMap = new TreeMap<Date, Double>();

    private Map<Date,Double> createOnlineMap(String networkId) {
        Date startDate = new Date(new Date().getTime() - 1000 * 60 * 60 * 12);
        List<SensorNode> nodes = SensorManager.getInstance().getAllNodesFromNetwork(networkId);
        Map<SensorNode,List<PingLog>> sensorMap = new HashMap<SensorNode, List<PingLog>>();
        for (SensorNode node : nodes) {
            sensorMap.put(node, MiscManager.getInstance().getAllPingLogsFromNodeSorted(node.getNodeId(), 80));
        }
        return null;
    }


    //private
}

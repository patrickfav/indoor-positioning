package at.ac.tuwien.inso.indoor.sensorserver.services.positioner;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.MiscManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.SensorManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.Analysis;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.EFrequencyRange;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.ExtendedNodeInfo;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.SimpleMeasurement;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNetwork;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by PatrickF on 11.11.2014.
 */
public class FindFittingNetwork {
    private static Logger log = LogManager.getLogger(FindFittingNetwork.class);
    private EFrequencyRange frequencyRange;

    public FindFittingNetwork(EFrequencyRange frequencyRange) {
        this.frequencyRange = frequencyRange;
    }

    public List<SensorNetwork> findNetwork(Map<String, SimpleMeasurement> measurements) {

        Map<SensorNetwork, Integer> probabilityMap = new HashMap<SensorNetwork, Integer>();

        for (SensorNetwork network : SensorManager.getInstance().getAllSensorNetworksNonDeleted()) {
            int foundNodes = 0;
            Analysis analysis = MiscManager.getInstance().getLatestAnalysis(network.getNetworkId());

            if (analysis != null) {
                for (ExtendedNodeInfo node : analysis.getExtendedNodeMap().get(frequencyRange)) {
                    if (measurements.containsKey(node.getMacAddress())) {
                        foundNodes++;
                    }
                }

                probabilityMap.put(network, foundNodes);
                log.debug(network.getNetworkName() + " contains " + foundNodes + " of " + measurements.values().size() + " BSSI");
            } else {
                log.warn(network.getNetworkName() + " does not seem to have a single analysis");
            }
        }

        int bestFound = Integer.MIN_VALUE;
        for (Integer found : probabilityMap.values()) {
            if (found > bestFound) {
                bestFound = found;
            }
        }

        List<SensorNetwork> bestFittingNetworks = new ArrayList<SensorNetwork>();
        for (Map.Entry<SensorNetwork, Integer> entry : probabilityMap.entrySet()) {
            if (entry.getValue() <= bestFound) {
                bestFittingNetworks.add(entry.getKey());
            }
        }

        return bestFittingNetworks;
    }
}

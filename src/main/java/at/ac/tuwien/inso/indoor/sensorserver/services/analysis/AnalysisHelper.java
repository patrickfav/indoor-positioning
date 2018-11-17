package at.ac.tuwien.inso.indoor.sensorserver.services.analysis;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.MiscManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.SensorManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.*;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.Adapter;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNode;
import at.ac.tuwien.inso.indoor.sensorserver.services.ServerConfig;
import at.ac.tuwien.inso.indoor.sensorserver.services.positioner.ManagedNodesPositioner;

import java.util.*;

/**
 * Created by PatrickF on 03.10.2014.
 */
public final class AnalysisHelper {

    private AnalysisHelper() {
    }

    public static Map<EFrequencyRange, Double> initDistanceMultiplier(Map<EFrequencyRange, Double> distMultiMap) {
        for (EFrequencyRange eFrequencyRange : EFrequencyRange.values()) {
            distMultiMap.put(eFrequencyRange, 1d);
        }
        return distMultiMap;
    }

    public static Map<EFrequencyRange, List<PhysicalAdapter>> createPhysicalAdapterMap(Map<EFrequencyRange, List<ExtendedNodeInfo>> extendedNodeMap) {
        Map<EFrequencyRange, List<PhysicalAdapter>> physicalAdaptersMap = new HashMap<EFrequencyRange, List<PhysicalAdapter>>();

        for (EFrequencyRange eFrequencyRange : extendedNodeMap.keySet()) {
            physicalAdaptersMap.put(eFrequencyRange, getAllDistinctPhysicalAdapter(extendedNodeMap.get(eFrequencyRange)));

        }
        return physicalAdaptersMap;
    }

    private static List<PhysicalAdapter> getAllDistinctPhysicalAdapter(List<ExtendedNodeInfo> extendedNodesLists) {
        Set<PhysicalAdapter> physicalAdapters = new TreeSet<PhysicalAdapter>();
        for (ExtendedNodeInfo extendedNodesList : extendedNodesLists) {
            for (ExtendedNodeInfo.ManagedNode managedNode : extendedNodesList.getManagedNodes()) {
                physicalAdapters.add(managedNode.getPhysicalAdapter());
            }
        }
        List<PhysicalAdapter> list = new ArrayList<PhysicalAdapter>(physicalAdapters);
        Collections.sort(list);
        return list;
    }

    public static Map<EFrequencyRange, Double> getPrevAnalysisMultiplier(String networkId) {
        List<Analysis> analysises = MiscManager.getInstance().getAnalysisListForNetworkId(networkId, 1);
        if (!analysises.isEmpty()) {
            return analysises.get(0).getDistMultiMap();
        }
        return initDistanceMultiplier(new HashMap<EFrequencyRange, Double>());
    }

    public static boolean isSameAnalysisAsPrevious(Analysis currentAnalysis, String networkId) {
        List<Analysis> analysises = MiscManager.getInstance().getAnalysisListForNetworkId(networkId, 1);
        return !analysises.isEmpty() && isBasicallySameAnalysis(currentAnalysis, analysises.get(0));
    }

    private static boolean isBasicallySameAnalysis(Analysis newAnalysis, Analysis oldAnalysis) {
        return newAnalysis.getFrom().equals(oldAnalysis.getFrom()) && newAnalysis.getTo().equals(oldAnalysis.getTo()) &&
                newAnalysis.getSurveySum() == oldAnalysis.getSurveySum() && newAnalysis.getSurveyPerNodeSum() == oldAnalysis.getSurveyPerNodeSum();
    }

    public static Map<EFrequencyRange, SignalMap> createBasicSignalMap(Analysis analysis) {
        Map<EFrequencyRange, SignalMap> map = new HashMap<EFrequencyRange, SignalMap>();

        Analysis prevAnalysis = MiscManager.getInstance().getLatestAnalysis(analysis.getNetworkId());


        EFrequencyRange freq = EFrequencyRange.WLAN_2_4Ghz;
        ManagedNodesPositioner matrix = new ManagedNodesPositioner(analysis, freq, ServerConfig.getInstance().getSignalMapConfig()); //TODO: add only needed freqs
        map.put(freq, matrix.createSignalMap());

        if (prevAnalysis != null && prevAnalysis.getSignalMap().containsKey(freq) && prevAnalysis.getSignalMap().get(freq).getFloorplanConfig().getPosX() != -1) {
            map.get(freq).setFloorplanConfig(new SignalMap.FloorplanConfig(prevAnalysis.getSignalMap().get(freq).getFloorplanConfig()));
        }

        return map;
    }

    public static Map<EFrequencyRange, Map<String, Double>> getNodeMultiMap(String networkId) {
        List<SensorNode> nodes = SensorManager.getInstance().getAllNodesFromNetwork(networkId);
        Map<EFrequencyRange, Map<String, Double>> map = new HashMap<EFrequencyRange, Map<String, Double>>();
        for (SensorNode node : nodes) {
            for (EFrequencyRange freq : node.getMultiplierMap().keySet()) {
                if (!map.containsKey(freq)) {
                    map.put(freq, new HashMap<String, Double>());
                }
                for (Adapter adapter : node.getAdapters()) {
                    if (adapter.getFrequencyRange().equals(freq)) {
                        map.get(freq).put(adapter.getMacAddress(), node.getMultiplierMap().get(freq));
                    }
                }
            }
        }
        return map;
    }
}

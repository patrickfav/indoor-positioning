package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by PatrickF on 22.09.2014.
 */
public class Analysis extends AnalysisMetaData {

    private Map<EFrequencyRange, List<ExtendedNodeInfo>> extendedNodeMap = new HashMap<EFrequencyRange, List<ExtendedNodeInfo>>();
    private Map<EFrequencyRange, List<PhysicalAdapter>> physicalAdaptersMap = new HashMap<EFrequencyRange, List<PhysicalAdapter>>();
    private Map<EFrequencyRange, Double> distMultiMap = new HashMap<EFrequencyRange, Double>();
    private Map<EFrequencyRange, SignalMap> signalMap = new HashMap<EFrequencyRange, SignalMap>();
    private Map<EFrequencyRange, Map<String, Double>> nodeMultiMap = new HashMap<EFrequencyRange, Map<String, Double>>();

    public Analysis() {
    }

    public Analysis(List<Survey> surveyList, String networkId) {
        super(surveyList, networkId);
    }

    public Map<EFrequencyRange, Double> getDistMultiMap() {
        return distMultiMap;
    }

    public void setDistMultiMap(Map<EFrequencyRange, Double> distMultiMap) {
        this.distMultiMap = distMultiMap;
    }

    public Map<EFrequencyRange, List<ExtendedNodeInfo>> getExtendedNodeMap() {
        return extendedNodeMap;
    }

    public void setExtendedNodeMap(Map<EFrequencyRange, List<ExtendedNodeInfo>> extendedNodeMap) {
        this.extendedNodeMap = extendedNodeMap;
    }

    public Map<EFrequencyRange, List<PhysicalAdapter>> getPhysicalAdaptersMap() {
        return physicalAdaptersMap;
    }

    public void setPhysicalAdaptersMap(Map<EFrequencyRange, List<PhysicalAdapter>> physicalAdaptersMap) {
        this.physicalAdaptersMap = physicalAdaptersMap;
    }

    public Map<EFrequencyRange, SignalMap> getSignalMap() {
        return signalMap;
    }

    public void setSignalMap(Map<EFrequencyRange, SignalMap> signalMap) {
        this.signalMap = signalMap;
    }

    public Map<EFrequencyRange, Map<String, Double>> getNodeMultiMap() {
        return nodeMultiMap;
    }

    public void setNodeMultiMap(Map<EFrequencyRange, Map<String, Double>> nodeMultiMap) {
        this.nodeMultiMap = nodeMultiMap;
    }

}

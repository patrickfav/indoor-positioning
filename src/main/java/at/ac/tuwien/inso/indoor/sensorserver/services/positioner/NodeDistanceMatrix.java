package at.ac.tuwien.inso.indoor.sensorserver.services.positioner;

import at.ac.tuwien.inso.indoor.sensorserver.math.radiomodels.ITUIndoorModelDegradingDist;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.Analysis;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.EFrequencyRange;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.ExtendedNodeInfo;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.PhysicalAdapter;

import java.util.*;

/**
 * Created by PatrickF on 14.10.2014.
 */
public class NodeDistanceMatrix {

    private Map<MacCombKey,DataEntry> lookupMap;
    private Map<MacCombKey,Double> normalizedDistanceMap;
    private Map<String,ManagedNodeEntry> managedNodesMacMap;
    private Map<String,ExtendedNodeInfo> extendedNodesMacList;

    private EFrequencyRange freq;
    private Analysis analysis;

    private List<SensorPosition> managedNodesFixedPosition;

    public NodeDistanceMatrix(Analysis analysis,EFrequencyRange freq) {
        this.freq = freq;
        this.analysis = analysis;
        this.lookupMap = new HashMap<MacCombKey, DataEntry>();
        this.managedNodesMacMap = new HashMap<String, ManagedNodeEntry>();
        this.extendedNodesMacList = new HashMap<String, ExtendedNodeInfo>();
        this.normalizedDistanceMap = new HashMap<MacCombKey, Double>();
        this.managedNodesFixedPosition = new ArrayList<SensorPosition>();

        for (ExtendedNodeInfo e : analysis.getExtendedNodeMap().get(freq)) {
            for (ExtendedNodeInfo.ManagedNode managedNode : e.getManagedNodes()) {
                lookupMap.put(new MacCombKey(managedNode.getPhysicalAdapter().getMacAddress(),e.getMacAddress()),new DataEntry(managedNode,e));

                if(!managedNodesMacMap.containsKey(managedNode.getPhysicalAdapter().getMacAddress())) {
                    managedNodesMacMap.put(managedNode.getPhysicalAdapter().getMacAddress(), new ManagedNodeEntry(managedNode.getPhysicalAdapter()));
                }

                if(!e.isIgnored()) {
                    if(e.isManagedNode()) {
                        managedNodesMacMap.get(managedNode.getPhysicalAdapter().getMacAddress()).getScannedMacsManaged().add(e.getMacAddress());
                    } else {
                        managedNodesMacMap.get(managedNode.getPhysicalAdapter().getMacAddress()).getScannedMacsExtended().add(e.getMacAddress());
                    }
                }
            }

            if(!e.isManagedNode() && !e.isIgnored()) {
                extendedNodesMacList.put(e.getMacAddress(), e);
            }
        }

        for (String srcMac : managedNodesMacMap.keySet()) {
            for (String targetMac : managedNodesMacMap.get(srcMac).getScannedMacsManaged()) {
                double distToTarget = getDistance(getData(srcMac, targetMac).getManagedNode());
                double distToSrc = getDistance(getData(targetMac,srcMac).getManagedNode());
                normalizedDistanceMap.put(new MacCombKey(srcMac, targetMac), normalizeDistance(distToTarget, distToSrc));
            }
            for (String targetMac : managedNodesMacMap.get(srcMac).getScannedMacsExtended()) {
                double distToTarget = getDistance(getData(srcMac, targetMac).getManagedNode());
                normalizedDistanceMap.put(new MacCombKey(srcMac, targetMac), distToTarget);
            }
        }
    }

    private double normalizeDistance(double dist1,double dist2) {
        return (dist1 +dist2) /2;
    }

    private double getDistance(ExtendedNodeInfo.ManagedNode managedNode) {
        return managedNode.getRadioModelData().getDistanceMap().get(ITUIndoorModelDegradingDist.class.getSimpleName()).getMultMeanDistance() * analysis.getDistMultiMap().get(freq);
    }

    public boolean isManagedNode(String mac) {
        return managedNodesMacMap.containsKey(mac);
    }

    public DataEntry getData(String macSrc,String macTarget) {
        return lookupMap.get(new MacCombKey(macSrc, macTarget));
    }

    public Set<String> getManagedNodeMacCopy(Set<String> withOutGivenMacSet) {
        Set<String> part = new HashSet<String>(managedNodesMacMap.keySet());
        part.removeAll(withOutGivenMacSet);
        return part;
    }

    public Set<String> getAllManagedNodeMacsWhichSeeGivenExtendedNode(String macExtendedNode) {
        Set<String> macList = new HashSet<String>();
        for (NodeDistanceMatrix.MacCombKey macCombKey : getLookupMap().keySet()) {
            if(macCombKey.getTarget().equalsIgnoreCase(macExtendedNode)) {
                macList.add(macCombKey.getSrc());
            }
        }
        return macList;
    }

    public Map<MacCombKey, DataEntry> getLookupMap() {
        return lookupMap;
    }

    public Map<String, ManagedNodeEntry> getManagedNodesMacMap() {
        return managedNodesMacMap;
    }

    public Map<String, ExtendedNodeInfo> getExtendedNodesMacList() {
        return extendedNodesMacList;
    }

    public EFrequencyRange getFreq() {
        return freq;
    }

    public Map<MacCombKey, Double> getNormalizedDistanceMap() {
        return normalizedDistanceMap;
    }

    public List<SensorPosition> getManagedNodesFixedPosition() {
        return managedNodesFixedPosition;
    }

    public boolean isManagedNodeMac(String mac) {
        return managedNodesMacMap.keySet().contains(mac);
    }

    public static class MacCombKey {
        private final String src;
        private final String target;

        private final String comb;

        public MacCombKey(String src, String target) {
            this.src = src;
            this.target = target;
            this.comb = src+"_"+target;
        }

        public String getSrc() {
            return src;
        }

        public String getTarget() {
            return target;
        }

        public MacCombKey createOppositeMacCombKey() {
            return new MacCombKey(target,src);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MacCombKey that = (MacCombKey) o;

            if (!comb.equals(that.comb)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return comb.hashCode();
        }

        @Override
        public String toString() {
            return comb;
        }
    }

    public static class ManagedNodeEntry {
        private final PhysicalAdapter physicalAdapter;
        private final Set<String> scannedMacsExtended;
        private final Set<String> scannedMacsManaged;


        public ManagedNodeEntry(PhysicalAdapter physicalAdapter) {
            this.physicalAdapter = physicalAdapter;
            this.scannedMacsExtended = new HashSet<String>();
            this.scannedMacsManaged = new HashSet<String>();
        }

        public PhysicalAdapter getPhysicalAdapter() {
            return physicalAdapter;
        }

        public Set<String> getScannedMacsExtended() {
            return scannedMacsExtended;
        }

        public Set<String> getScannedMacsManaged() {
            return scannedMacsManaged;
        }

        public Set<String> getScannedMacs() {
            Set<String> all = new HashSet<String>();
            all.addAll(scannedMacsExtended);
            all.addAll(scannedMacsManaged);
            return all;
        }
    }

    public static class DataEntry {
        private final ExtendedNodeInfo.ManagedNode managedNode;
        private final ExtendedNodeInfo nodeInfo;

        public DataEntry(ExtendedNodeInfo.ManagedNode managedNode, ExtendedNodeInfo nodeInfo) {
            this.managedNode = managedNode;
            this.nodeInfo = nodeInfo;
        }

        public ExtendedNodeInfo.ManagedNode getManagedNode() {
            return managedNode;
        }

        public ExtendedNodeInfo getNodeInfo() {
            return nodeInfo;
        }
    }

}

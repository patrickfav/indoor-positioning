package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.SimpleStatistics;
import at.ac.tuwien.inso.indoor.sensorserver.util.ServerUtil;

import java.util.*;

/**
 * Created by PatrickF on 22.09.2014.
 */
public class ExtendedNodeInfo implements Comparable<ExtendedNodeInfo> {

    private String macAddress;
    private Set<String> ssidSet = new HashSet<String>();
    private Set<Integer> channels = new HashSet<Integer>();
    private Set<EFrequencyRange> frequencyRanges = new HashSet<EFrequencyRange>();
    private boolean isManagedNode=false;
    private List<ManagedNode> managedNodes = new ArrayList<ManagedNode>();
    private boolean ignored =false;
    private TrendInfo trendInfo;

    public boolean isManagedNode() {
        return isManagedNode;
    }

    public void setManagedNode(boolean isManagedNode) {
        this.isManagedNode = isManagedNode;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public Set<String> getSsidSet() {
        return ssidSet;
    }

    public void setSsidSet(Set<String> ssidSet) {
        this.ssidSet = ssidSet;
    }

    public Set<Integer> getChannels() {
        return channels;
    }

    public void setChannels(Set<Integer> channels) {
        this.channels = channels;
    }

    public List<ManagedNode> getManagedNodes() {
        return managedNodes;
    }

    public void setManagedNodes(List<ManagedNode> managedNodes) {
        this.managedNodes = managedNodes;
    }

    public Set<EFrequencyRange> getFrequencyRanges() {
        return frequencyRanges;
    }

    public void setFrequencyRanges(Set<EFrequencyRange> frequencyRanges) {
        this.frequencyRanges = frequencyRanges;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean isIgnored) {
        this.ignored = isIgnored;
    }

    public TrendInfo getTrendInfo() {
        return trendInfo;
    }

    public void setTrendInfo(TrendInfo trendInfo) {
        this.trendInfo = trendInfo;
    }

    @Override
    public int compareTo(ExtendedNodeInfo o) {
        return ServerUtil.implode("",new ArrayList<String>(ssidSet)).compareTo(ServerUtil.implode("",new ArrayList<String>(o.getSsidSet())));
    }

    public static class ManagedNode implements Comparable<ManagedNode> {
        private PhysicalAdapter physicalAdapter;
        private SimpleStatistics statistics;
        private RadioModelData radioModelData;
        private TrendInfo trendInfo;

        public PhysicalAdapter getPhysicalAdapter() {
            return physicalAdapter;
        }

        public void setPhysicalAdapter(PhysicalAdapter physicalAdapter) {
            this.physicalAdapter = physicalAdapter;
        }

        public SimpleStatistics getStatistics() {
            return statistics;
        }

        public void setStatistics(SimpleStatistics statistics) {
            this.statistics = statistics;
        }

        public RadioModelData getRadioModelData() {
            return radioModelData;
        }

        public void setRadioModelData(RadioModelData radioModelData) {
            this.radioModelData = radioModelData;
        }

        public TrendInfo getTrendInfo() {
            return trendInfo;
        }

        public void setTrendInfo(TrendInfo trendInfo) {
            this.trendInfo = trendInfo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ManagedNode that = (ManagedNode) o;

            if (physicalAdapter != null ? !physicalAdapter.equals(that.physicalAdapter) : that.physicalAdapter != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            return physicalAdapter != null ? physicalAdapter.hashCode() : 0;
        }

        @Override
        public int compareTo(ManagedNode o) {
            return physicalAdapter.compareTo(o.getPhysicalAdapter());
        }
    }

    public static class TrendInfo {
        private double shortTermTrend = 0;
        private double longTermTrend= 0;
        private int longTermTrendSampleSize=0;

        public double getShortTermTrend() {
            return shortTermTrend;
        }

        public void setShortTermTrend(double shortTermTrend) {
            this.shortTermTrend = shortTermTrend;
        }

        public double getLongTermTrend() {
            return longTermTrend;
        }

        public void setLongTermTrend(double longTermTrend) {
            this.longTermTrend = longTermTrend;
        }

        public int getLongTermTrendSampleSize() {
            return longTermTrendSampleSize;
        }

        public void setLongTermTrendSampleSize(int longTermTrendSampleSize) {
            this.longTermTrendSampleSize = longTermTrendSampleSize;
        }
    }

}

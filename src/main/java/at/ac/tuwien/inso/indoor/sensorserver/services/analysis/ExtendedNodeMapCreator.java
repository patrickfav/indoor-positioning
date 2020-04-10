package at.ac.tuwien.inso.indoor.sensorserver.services.analysis;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.MiscManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.SensorManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.Statistics;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.Analysis;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.AverageWlanScanMeasurement;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.EFrequencyRange;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.ExtendedNodeInfo;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.PhysicalAdapter;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.RadioModelData;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.Survey;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.Adapter;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.Blacklist;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.RoomList;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNetwork;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNode;
import at.ac.tuwien.inso.indoor.sensorserver.util.RadioUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by PatrickF on 03.10.2014.
 */
public class ExtendedNodeMapCreator {
    private static Logger log = LogManager.getLogger(ExtendedNodeMapCreator.class);

    private List<Analysis> prevAnalysis;

    public Map<EFrequencyRange, List<ExtendedNodeInfo>> create(List<Survey> surveyList, String networkId) {
        prevAnalysis = MiscManager.getInstance().getAnalysisListForNetworkId(networkId, 10);
        Map<String, PhysicalAdapter> physicalAdapterMap = createPhysicalAdapterMap(networkId);
        Map<String, ExtendedNodeInfo> extendedNodeMap = fillMap(networkId, physicalAdapterMap, surveyList);
        Map<EFrequencyRange, List<ExtendedNodeInfo>> resultMap = createResultingMap(extendedNodeMap);
        checkForIgnoredMac(resultMap, networkId);
        return resultMap;
    }

    private Map<String, PhysicalAdapter> createPhysicalAdapterMap(String networkId) {
        Map<String, PhysicalAdapter> physicalAdapterMap = new HashMap<String, PhysicalAdapter>();

        for (SensorNode node : SensorManager.getInstance().getAllNodesFromNetwork(networkId)) {
            for (Adapter adapter : node.getAdapters()) {

                if (!physicalAdapterMap.containsKey(adapter.getMacAddress())) {
                    physicalAdapterMap.put(adapter.getMacAddress(), new PhysicalAdapter(adapter.getMacAddress()));
                    physicalAdapterMap.get(adapter.getMacAddress()).setNodeId(node.getNodeId());
                    physicalAdapterMap.get(adapter.getMacAddress()).setFrequencyRange(adapter.getFrequencyRange());
                    physicalAdapterMap.get(adapter.getMacAddress()).setNodeName(node.getNodeName());
                    physicalAdapterMap.get(adapter.getMacAddress()).setRoomId(node.getRoomId());
                    physicalAdapterMap.get(adapter.getMacAddress()).setMultiplier(node.getMultiplierMap().get(adapter.getFrequencyRange()));
                }

                physicalAdapterMap.get(adapter.getMacAddress()).getSsidSet().add(adapter.getSsid());
                physicalAdapterMap.get(adapter.getMacAddress()).getAdapterNames().add(adapter.getName());
            }
        }
        return physicalAdapterMap;
    }

    private Map<String, ExtendedNodeInfo> fillMap(String networkId, Map<String, PhysicalAdapter> physicalAdapterMap, List<Survey> surveyList) {
        Map<String, ExtendedNodeInfo> extendedNodeMap = new HashMap<String, ExtendedNodeInfo>();

        SensorNetwork sensorNetwork = SensorManager.getInstance().getSensorNetworkById(networkId);
        RoomList roomList = MiscManager.getInstance().getRoomlistByNetworkId(networkId);

        for (PhysicalAdapter physicalAdapter : physicalAdapterMap.values()) {
            Map<String, ScanInfo> scanInfoMap = new HashMap<String, ScanInfo>();

            for (Survey survey : surveyList) {
                if (survey.getNodeId().equals(physicalAdapter.getNodeId()) &&
                        physicalAdapter.getAdapterNames().contains(survey.getAdapter())) {

                    for (AverageWlanScanMeasurement scan : survey.getAverageScanNodes()) {

                        if (!scanInfoMap.containsKey(scan.getMacAddress())) {
                            scanInfoMap.put(scan.getMacAddress(), new ScanInfo());
                            scanInfoMap.get(scan.getMacAddress()).setMacAddress(scan.getMacAddress());
                            scanInfoMap.get(scan.getMacAddress()).setScannedBy(physicalAdapter);
                        }

                        scanInfoMap.get(scan.getMacAddress()).getSignalStrengthList().addAll(scan.getSignalStrengths());
                        scanInfoMap.get(scan.getMacAddress()).getSsidList().add(scan.getSsid());
                        scanInfoMap.get(scan.getMacAddress()).getChannelList().add(scan.getChannel());
                    }
                }
            }

            for (ScanInfo scanInfo : scanInfoMap.values()) {
                if (!extendedNodeMap.containsKey(scanInfo.getMacAddress())) {
                    extendedNodeMap.put(scanInfo.getMacAddress(), new ExtendedNodeInfo());
                    extendedNodeMap.get(scanInfo.getMacAddress()).setMacAddress(scanInfo.getMacAddress());
                    if (physicalAdapterMap.containsKey(scanInfo.getMacAddress())) {
                        extendedNodeMap.get(scanInfo.getMacAddress()).setManagedNode(true);
                    }
                }

                extendedNodeMap.get(scanInfo.getMacAddress()).getSsidSet().addAll(scanInfo.getSsidList());
                extendedNodeMap.get(scanInfo.getMacAddress()).getChannels().addAll(scanInfo.getChannelList());
                extendedNodeMap.get(scanInfo.getMacAddress()).getFrequencyRanges().add(scanInfo.getScannedBy().getFrequencyRange());

                ExtendedNodeInfo.ManagedNode managedNode = new ExtendedNodeInfo.ManagedNode();
                managedNode.setPhysicalAdapter(scanInfo.getScannedBy());
                managedNode.setStatistics(new Statistics(scanInfo.getSignalStrengthList()));
                int channel = 1;
                if (!scanInfo.getChannelList().isEmpty()) {
                    channel = scanInfo.getChannelList().get(0);
                }
                managedNode.setRadioModelData(new RadioModelData(managedNode.getStatistics(), RadioUtil.guessFrequencyFromChannel(channel), channel,
                        sensorNetwork.getEnvironmentModel(), scanInfo.getScannedBy().getRoomId(), roomList.getMacToRoomIdMap().get(scanInfo.getMacAddress()), scanInfo.getScannedBy().getMultiplier(), sensorNetwork.getPathLossConfig()));
                managedNode.setTrendInfo(createTrendInfo(scanInfo.getMacAddress(), managedNode, networkId));
                extendedNodeMap.get(scanInfo.getMacAddress()).getManagedNodes().add(managedNode);
                Collections.sort(extendedNodeMap.get(scanInfo.getMacAddress()).getManagedNodes());
            }
        }

        for (ExtendedNodeInfo extendedNodeInfo : extendedNodeMap.values()) {
            double shortTrend = 0, longTrend = 0, dataSize = 0;
            for (ExtendedNodeInfo.ManagedNode managedNode : extendedNodeInfo.getManagedNodes()) {
                shortTrend += managedNode.getTrendInfo().getShortTermTrend();
                longTrend += managedNode.getTrendInfo().getLongTermTrend();
                dataSize += managedNode.getTrendInfo().getLongTermTrendSampleSize();
            }
            ExtendedNodeInfo.TrendInfo trendInfo = new ExtendedNodeInfo.TrendInfo();
            trendInfo.setShortTermTrend(shortTrend / (double) extendedNodeInfo.getManagedNodes().size());
            trendInfo.setLongTermTrend(longTrend / (double) extendedNodeInfo.getManagedNodes().size());
            trendInfo.setLongTermTrendSampleSize((int) dataSize);
            extendedNodeInfo.setTrendInfo(trendInfo);
        }
        return extendedNodeMap;
    }

    private ExtendedNodeInfo.TrendInfo createTrendInfo(String scanNodeMac, ExtendedNodeInfo.ManagedNode managedNode, String networkId) {
        ExtendedNodeInfo.TrendInfo trendInfo = new ExtendedNodeInfo.TrendInfo();

        if (prevAnalysis != null && !prevAnalysis.isEmpty()) {
            List<Double> prevValues = new ArrayList<Double>();

            for (Analysis a : prevAnalysis) {
                Map<EFrequencyRange, List<ExtendedNodeInfo>> extMap = a.getExtendedNodeMap();
                if (extMap.containsKey(managedNode.getPhysicalAdapter().getFrequencyRange())) {
                    outerLoop:
                    for (ExtendedNodeInfo extendedNodeInfo : extMap.get(managedNode.getPhysicalAdapter().getFrequencyRange())) {
                        if (extendedNodeInfo.getMacAddress().equals(scanNodeMac)) {
                            for (ExtendedNodeInfo.ManagedNode node : extendedNodeInfo.getManagedNodes()) {
                                if (node.getPhysicalAdapter().getMacAddress().equals(managedNode.getPhysicalAdapter().getMacAddress())) {
                                    prevValues.add(node.getStatistics().getMean());
                                    break outerLoop;
                                }
                            }
                        }
                    }
                }
            }

            if (!prevValues.isEmpty()) {
                trendInfo.setShortTermTrend(managedNode.getStatistics().getMean() - prevValues.get(0));

                //TODO
                double avg = 0;
                for (Double prevValue : prevValues) {
                    avg += prevValue;
                }
                avg = avg / (double) prevValues.size();
                trendInfo.setLongTermTrend(managedNode.getStatistics().getMean() - avg);

                trendInfo.setLongTermTrendSampleSize(prevValues.size());
            }
        }
        return trendInfo;
    }

    private Map<EFrequencyRange, List<ExtendedNodeInfo>> createResultingMap(Map<String, ExtendedNodeInfo> extendedNodeMap) {
        Map<EFrequencyRange, List<ExtendedNodeInfo>> map = new HashMap<EFrequencyRange, List<ExtendedNodeInfo>>();
        for (ExtendedNodeInfo extendedNodeInfo : extendedNodeMap.values()) {
            for (EFrequencyRange eFrequencyRange : extendedNodeInfo.getFrequencyRanges()) {
                if (!map.containsKey(eFrequencyRange)) {
                    map.put(eFrequencyRange, new ArrayList<ExtendedNodeInfo>());
                }
                map.get(eFrequencyRange).add(extendedNodeInfo);
            }
        }

        for (EFrequencyRange eFrequencyRange : map.keySet()) {
            Collections.sort(map.get(eFrequencyRange));
        }

        return map;
    }

    private void checkForIgnoredMac(Map<EFrequencyRange, List<ExtendedNodeInfo>> extendedNodeMap, String networkId) {
        try {
            Blacklist blacklist = MiscManager.getInstance().getBlacklistByNetworkId(networkId);

            for (EFrequencyRange eFrequencyRange : extendedNodeMap.keySet()) {
                for (ExtendedNodeInfo extendedNodeInfo : extendedNodeMap.get(eFrequencyRange)) {
                    if (blacklist.isActAsWhiteList()) {
                        extendedNodeInfo.setIgnored(true);

                        for (String mac : blacklist.getMacList()) {
                            if (extendedNodeInfo.getMacAddress().equalsIgnoreCase(mac)) {
                                extendedNodeInfo.setIgnored(false);
                                break;
                            }
                        }
                    } else {
                        extendedNodeInfo.setIgnored(false);

                        for (String mac : blacklist.getMacList()) {
                            if (extendedNodeInfo.getMacAddress().equalsIgnoreCase(mac)) {
                                extendedNodeInfo.setIgnored(true);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not get and apply blacklist", e);
        }
    }

    private static class ScanInfo {
        private String macAddress;
        private List<String> ssidList = new ArrayList<String>();
        private List<Integer> channelList = new ArrayList<Integer>();
        private List<Double> signalStrengthList = new ArrayList<Double>();
        private PhysicalAdapter scannedBy;

        public String getMacAddress() {
            return macAddress;
        }

        public void setMacAddress(String macAddress) {
            this.macAddress = macAddress;
        }

        public List<String> getSsidList() {
            return ssidList;
        }

        public void setSsidList(List<String> ssidList) {
            this.ssidList = ssidList;
        }

        public List<Integer> getChannelList() {
            return channelList;
        }

        public void setChannelList(List<Integer> channelList) {
            this.channelList = channelList;
        }

        public List<Double> getSignalStrengthList() {
            return signalStrengthList;
        }

        public void setSignalStrengthList(List<Double> signalStrengthList) {
            this.signalStrengthList = signalStrengthList;
        }

        public PhysicalAdapter getScannedBy() {
            return scannedBy;
        }

        public void setScannedBy(PhysicalAdapter scannedBy) {
            this.scannedBy = scannedBy;
        }
    }
}

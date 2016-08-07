package at.ac.tuwien.inso.indoor.sensorserver.services.positioner;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.MiscManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.SensorManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.Analysis;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.AnalysisMetaData;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.EFrequencyRange;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNetwork;
import at.ac.tuwien.inso.indoor.sensorserver.services.ServerConfig;

import java.util.List;

/**
 * Created by PatrickF on 10.10.2014.
 */
public class TrilateralMatrixTester {
    public static void main(String[] args) {
        System.out.println("Start Test");

        SensorNetwork network = SensorManager.getInstance().getSensorNetworkById("5dfb0df9-6967-4fd7-814c-1f749d4d3bd1");
        List<AnalysisMetaData> metaDatas = MiscManager.getInstance().getAnalysisMetaListSortedByDate(network.getNetworkId(), 1);
        Analysis analysis = MiscManager.getInstance().getByAnalysisId(metaDatas.get(0).getAnalysisId());

        //ManagedNodesPositioner matrix = new ManagedNodesPositioner(analysis, EFrequencyRange.WLAN_2_4Ghz,new SignalMapConfig());
        RSSMatrixCreator creator = new RSSMatrixCreator(analysis.getSignalMap().get(EFrequencyRange.WLAN_2_4Ghz), ServerConfig.getInstance().getSignalMapConfig(),network,EFrequencyRange.WLAN_2_4Ghz,true);
        System.out.println("Test Done");
    }
}

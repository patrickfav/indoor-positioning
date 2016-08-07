package at.ac.tuwien.inso.indoor.sensorserver.services.analysis;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.MiscManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.SensorManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.*;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNetwork;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNode;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by PatrickF on 03.10.2014.
 */
public class AnalysisBuilder {
    private static Logger log = Logger.getLogger(AnalysisBuilder.class);
    private static final boolean SHOULD_CHECK_IF_SAME_AS_PREVIOUS = false;

    public Analysis createAndPresistAnalysis(String networkId) {
        Analysis analysis = createAnalysis(networkId);

        if(SHOULD_CHECK_IF_SAME_AS_PREVIOUS && AnalysisHelper.isSameAnalysisAsPrevious(analysis, networkId)) {
            log.info("There is no new information compared to previous analysis. Skip saving.");
        } else {
            MiscManager.getInstance().addAnaylsis(analysis);
            log.info("Analysis "+analysis+" saved.");
        }
        return analysis;
    }

    public Analysis createAnalysis(String networkId) {
        SensorNetwork network = SensorManager.getInstance().getSensorNetworkById(networkId);
        log.info("Start create analysis with "+network.getSurveysPerNodeForAnalysis()+" surveys per node.");
        List<SensorNode> nodes = SensorManager.getInstance().getAllNodesFromNetwork(networkId);
        List<Survey> surveyList = new ArrayList<Survey>();
        for (SensorNode node : nodes) {
            if(node.isEnabled()) {
                List<Survey> surveys = MiscManager.getInstance().getAllSurveysFromNodeSorted(node.getNodeId(),network.getSurveysPerNodeForAnalysis());
                log.info("Add surveys from "+node.getNodeName()+" - "+surveys.size()+" surveys found.");
                surveyList.addAll(surveys);
            } else {
                log.info("SensorNode "+node.getNodeName()+" is disabled, ignore surveys for analysis.");
            }
        }

        Analysis analysis = new Analysis(surveyList,networkId);

        log.debug("create extendednodemap");
        Map<EFrequencyRange,List<ExtendedNodeInfo>> extendedNodeMap = new ExtendedNodeMapCreator().create(surveyList, networkId);
        log.debug("create PhysicalAdapter");
        Map<EFrequencyRange,List<PhysicalAdapter>> physicalAdaptersMap = AnalysisHelper.createPhysicalAdapterMap(extendedNodeMap);
        log.debug("create distMultiMap");
        Map<EFrequencyRange,Double> distMultiMap = AnalysisHelper.getPrevAnalysisMultiplier(networkId);
	    log.debug("create nodeMultiMap");
	    Map<EFrequencyRange,Map<String,Double>> nodeMultiMap = AnalysisHelper.getNodeMultiMap(networkId);


        analysis.setExtendedNodeMap(extendedNodeMap);
        analysis.setPhysicalAdaptersMap(physicalAdaptersMap);
        analysis.setDistMultiMap(distMultiMap);
	    analysis.setNodeMultiMap(nodeMultiMap);

        log.debug("create signalmap");
        analysis.setSignalMap(AnalysisHelper.createBasicSignalMap(analysis));


        log.info("Analysis created: "+analysis);
        return analysis;
    }
}

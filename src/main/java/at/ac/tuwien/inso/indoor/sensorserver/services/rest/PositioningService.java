package at.ac.tuwien.inso.indoor.sensorserver.services.rest;

import at.ac.tuwien.inso.indoor.sensorserver.math.positioning.IPositionAlgorithm;
import at.ac.tuwien.inso.indoor.sensorserver.math.positioning.ManhattanDistanceDetermination;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.MiscManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.SensorManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.*;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNetwork;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNode;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.responsewrapper.BaseResponse;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.responsewrapper.PositionAnalysisWrapper;
import at.ac.tuwien.inso.indoor.sensorserver.services.ServerConfig;
import at.ac.tuwien.inso.indoor.sensorserver.services.exceptions.ExceptionHandler;
import at.ac.tuwien.inso.indoor.sensorserver.services.positioner.FindFittingNetwork;
import at.ac.tuwien.inso.indoor.sensorserver.services.positioner.RSSMatrixCreator;
import at.ac.tuwien.inso.indoor.sensorserver.services.scheduler.SurveyCallable;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by PatrickF on 13.11.2014.
 */
@Path("/positioning")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class PositioningService {

    @GET
    @Path("/survey")
    public Map<String, SimpleMeasurement> getSurveyForPositioning(@QueryParam("adapterName") String adapterName, @QueryParam("ip") String ip, @QueryParam("port") Integer port, @QueryParam("htttps") Boolean shouldUseHttps, @QueryParam("count") Integer count, @QueryParam("delayMs") Integer delayMs) {
        try {
            SensorNode shell = new SensorNode();
            shell.setIp(ip);
            if (port != null) {
                shell.setPort(port);
            }
            shell.setHttpsEnabled(shouldUseHttps);

            SurveyCallable surveyWorker = new SurveyCallable(delayMs, count, adapterName, shell);

            Survey survey = surveyWorker.call();

            Map<String, SimpleMeasurement> measurementMap = new HashMap<String, SimpleMeasurement>();
            for (AverageWlanScanMeasurement averageWlanScanMeasurement : survey.getAverageScanNodes()) {
                measurementMap.put(averageWlanScanMeasurement.getMacAddress(), new SimpleMeasurement(averageWlanScanMeasurement));
            }

            return measurementMap;
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return null;
    }

    @POST
    @Path("/networks")
    public List<SensorNetwork> getPossibleNetworksForGivenSurvey(@QueryParam("freq") EFrequencyRange frequencyRange, Map<String, SimpleMeasurement> measurements) {
        try {
            FindFittingNetwork networkFinder = new FindFittingNetwork(frequencyRange);
            return networkFinder.findNetwork(measurements);
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return null;
    }

    @POST
    @Path("/position")
    public PositionAnalysisWrapper getProbablePositions(@QueryParam("freq") EFrequencyRange frequencyRange, @QueryParam("networkId") String sensorNetworkId,
                                                        Map<String, SimpleMeasurement> measurements, @QueryParam("multi") Double multi,
                                                        @QueryParam("shouldIncludeExtended") Boolean shouldIncludeExtended) {
        try {
            if (multi == null) {
                multi = 0d;
            }
            if (shouldIncludeExtended == null) {
                shouldIncludeExtended = true;
            }

            SensorNetwork network = SensorManager.getInstance().getSensorNetworkById(sensorNetworkId);
            Analysis analysis = MiscManager.getInstance().getLatestAnalysis(sensorNetworkId);

            RSSMatrixCreator creator = new RSSMatrixCreator(analysis.getSignalMap().get(frequencyRange), ServerConfig.getInstance().getSignalMapConfig(), network, frequencyRange, shouldIncludeExtended);
            IPositionAlgorithm algorithm = new ManhattanDistanceDetermination(analysis.getSignalMap().get(frequencyRange));
            PositionAnalysisWrapper wrapper = new PositionAnalysisWrapper();
            wrapper.setProbablePositions(algorithm.getMostLikelyPositions(measurements, creator.getReferencePoints(), multi));
            wrapper.setAnalysis(MiscManager.getInstance().getLatestAnalysis(sensorNetworkId));
            wrapper.setFreq(frequencyRange);
            wrapper.setNetwork(network);
            return wrapper;
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return null;
    }
}

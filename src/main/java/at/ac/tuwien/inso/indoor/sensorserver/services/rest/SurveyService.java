package at.ac.tuwien.inso.indoor.sensorserver.services.rest;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.EtagManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.MiscManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.SensorManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.AverageWlanScanMeasurement;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.Survey;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.Blacklist;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.responsewrapper.BaseResponse;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.responsewrapper.SuccessResponse;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.responsewrapper.SurveyStatWrapper;
import at.ac.tuwien.inso.indoor.sensorserver.services.ApiConst;
import at.ac.tuwien.inso.indoor.sensorserver.services.exceptions.ExceptionHandler;
import at.ac.tuwien.inso.indoor.sensorserver.util.CacheUtil;
import at.ac.tuwien.inso.indoor.sensorserver.util.ServerUtil;
import at.ac.tuwien.inso.indoor.sensorserver.util.SurveyUtil;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by PatrickF on 07.10.2014.
 */
@Path("/survey")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class SurveyService {
    @GET
    public Response getSurveysFromNode(@QueryParam("nodeId") String nodeId, @QueryParam("limit") Integer limit, @QueryParam("adapter") String adapter, @Context Request request) {
        Response.ResponseBuilder builder = null;
        try {
            ServerUtil.checkParameter(new ServerUtil.RestParam("nodeId", nodeId), new ServerUtil.RestParam("adapter", adapter));

            builder = request.evaluatePreconditions(CacheUtil.getEtag(EtagManager.getInstance().getETag(Survey.class)));
            CacheControl cc = CacheUtil.getCacheControl(ApiConst.CacheControl.SURVEY);
            if (builder == null) {

                if (limit == null || limit == 0) {
                    limit = 15;
                }

                List<Survey> surveys = MiscManager.getInstance().getAllSurveysFromNodeSorted(nodeId, adapter, limit);

                SurveyStatWrapper wrapper = new SurveyStatWrapper();
                wrapper.setSurveyList(filterBlacklisted(surveys, MiscManager.getInstance().getBlacklistByNetworkId(SensorManager.getInstance().getSensorNodeById(nodeId).getNetworkId())));
                wrapper.setStatistics(SurveyUtil.createAverageFromSurveys(surveys, SensorManager.getInstance().getSensorNodeById(nodeId).getNetworkId()));

                builder = Response.ok(wrapper).cacheControl(cc).tag(CacheUtil.getEtag(EtagManager.getInstance().getETag(Survey.class)));
            }
            return builder.cacheControl(cc).build();
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return null;
    }

    private List<Survey> filterBlacklisted(List<Survey> original, Blacklist blacklist) {
        List<Survey> filtered = new ArrayList<Survey>(original);

        for (Survey survey : filtered) {
            List<AverageWlanScanMeasurement> scanNodes = new ArrayList<AverageWlanScanMeasurement>();

            for (AverageWlanScanMeasurement scanNode : survey.getAverageScanNodes()) {
                boolean found = false;
                for (String mac : blacklist.getMacList()) {
                    if (mac.equals(scanNode.getMacAddress())) {
                        found = true;
                        break;
                    }
                }

                if ((blacklist.isActAsWhiteList() && found) || (!blacklist.isActAsWhiteList() && !found)) {
                    scanNodes.add(scanNode);
                }
            }
            survey.getAverageScanNodes().clear();
            survey.getAverageScanNodes().addAll(scanNodes);
        }

        return filtered;
    }

    @PUT
    @Path("/compact")
    public SuccessResponse removeOldSurveys(@QueryParam("nodeId") String nodeId, @QueryParam("keep") Integer keep) {
        SuccessResponse successResponse = new SuccessResponse();
        try {
            ServerUtil.checkParameter(new ServerUtil.RestParam("nodeId", nodeId), new ServerUtil.RestParam("keep", keep));

            MiscManager.getInstance().deleteOldSurveys(nodeId, keep);
            successResponse.setSuccess(true);
            return successResponse;
        } catch (Exception e) {
            ExceptionHandler.handle(successResponse, e);
        }
        return null;
    }
}

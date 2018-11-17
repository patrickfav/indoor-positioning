package at.ac.tuwien.inso.indoor.sensorserver.services.rest;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.EtagManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.MiscManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.Analysis;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.EFrequencyRange;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.SignalMap;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.responsewrapper.BaseResponse;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.responsewrapper.SuccessResponse;
import at.ac.tuwien.inso.indoor.sensorserver.services.ApiConst;
import at.ac.tuwien.inso.indoor.sensorserver.services.ServerConfig;
import at.ac.tuwien.inso.indoor.sensorserver.services.exceptions.ExceptionHandler;
import at.ac.tuwien.inso.indoor.sensorserver.services.positioner.ExtendedNodePositionManager;
import at.ac.tuwien.inso.indoor.sensorserver.services.positioner.ManagedNodesPositioner;
import at.ac.tuwien.inso.indoor.sensorserver.util.CacheUtil;
import at.ac.tuwien.inso.indoor.sensorserver.util.ServerUtil;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

/**
 * Created by PatrickF on 07.10.2014.
 */
@Path("/analysis")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class AnalysisService {

    @GET
    public Response getAnalysisMetaListPagination(@QueryParam("networkId") String networkId, @QueryParam("start-key-date") String startKeyDate, @QueryParam("start-doc-id") String startDocId, @QueryParam("limit") Integer limit, @Context Request request) {
        Response.ResponseBuilder builder = null;

        try {
            ServerUtil.checkParameter(new ServerUtil.RestParam("networkId", networkId));
            builder = request.evaluatePreconditions(CacheUtil.getEtag(EtagManager.getInstance().getETag(Analysis.class)));
            CacheControl cc = CacheUtil.getCacheControl(ApiConst.CacheControl.ANALYSIS);
            if (builder == null) {
                builder = Response.ok(MiscManager.getInstance().getAnalysisMetaListSortedByDatePagination(networkId, startKeyDate, startDocId, limit)).cacheControl(cc).tag(CacheUtil.getEtag(EtagManager.getInstance().getETag(Analysis.class)));
            }
            return builder.cacheControl(cc).build();
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return null;
    }

    @GET
    @Path("/{analysisId}")
    public Response getAnalysisById(@PathParam("analysisId") String analysisId, @Context Request request) {
        Response.ResponseBuilder builder = null;

        try {
            Analysis analysis = MiscManager.getInstance().getByAnalysisId(analysisId);
            ServerUtil.checkParameter(new ServerUtil.RestParam("analysisId", analysisId));
            builder = request.evaluatePreconditions(CacheUtil.getEtag(analysis.getRevision()));
            CacheControl cc = CacheUtil.getCacheControl(ApiConst.CacheControl.ANALYSIS);
            if (builder == null) {
                builder = Response.ok(MiscManager.getInstance().getByAnalysisId(analysisId)).cacheControl(cc).tag(CacheUtil.getEtag(analysis.getRevision()));
            }
            return builder.cacheControl(cc).build();
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return null;
    }

    @PUT
    @Path("/{analysisId}/multiplier")
    public SuccessResponse setMultiplierForAnalysis(@PathParam("analysisId") String analysisId, @QueryParam("freq") EFrequencyRange freq, @QueryParam("multiplier") Double multiplier) {
        SuccessResponse response = new SuccessResponse();
        try {
            ServerUtil.checkParameter(new ServerUtil.RestParam("analysisId", analysisId), new ServerUtil.RestParam("multiplier", multiplier), new ServerUtil.RestParam("freq", freq));
            response.setUpdatedRev(MiscManager.getInstance().updateDistanceMultiplier(analysisId, freq, multiplier).getRevision());
            response.setSuccess(true);
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return response;
    }

    @DELETE
    @Path("/{analysisId}")
    public SuccessResponse deleteAnalysisById(@PathParam("analysisId") String analysisId) {
        SuccessResponse successResponse = new SuccessResponse();
        try {
            MiscManager.getInstance().deleteAnalysis(analysisId);
            successResponse.setSuccess(true);
            return successResponse;
        } catch (Exception e) {
            ExceptionHandler.handle(successResponse, e);
        }
        return null;
    }

    @POST
    @Path("/{analysisId}/calc-extended")
    public SignalMap calculateExtendedNodesBasedOnGivenConfig(@PathParam("analysisId") String analysisId, @QueryParam("freq") EFrequencyRange range, @QueryParam("spreadCm") Integer spreadCm, SignalMap signalMap) {
        try {
            ServerUtil.checkParameter(new ServerUtil.RestParam("analysisId", analysisId), new ServerUtil.RestParam("freq", range), new ServerUtil.RestParam("signalMap", signalMap), new ServerUtil.RestParam("spreadCm", spreadCm));

            Analysis analysis = MiscManager.getInstance().getByAnalysisId(analysisId);
            ExtendedNodePositionManager manager = new ExtendedNodePositionManager(spreadCm, signalMap, analysis, range, ServerConfig.getInstance().getSignalMapConfig());
            return manager.calculate();
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return null;
    }

    @POST
    @Path("/{analysisId}/save-signal-map")
    public Analysis saveSignalMap(@PathParam("analysisId") String analysisId, @QueryParam("freq") EFrequencyRange range, SignalMap signalMap) {
        try {
            ServerUtil.checkParameter(new ServerUtil.RestParam("analysisId", analysisId), new ServerUtil.RestParam("freq", range), new ServerUtil.RestParam("signalMap", signalMap));

            Analysis analysis = MiscManager.getInstance().getByAnalysisId(analysisId);
            analysis.getSignalMap().put(range, signalMap);

            for (SignalMap.Vertex vertex : signalMap.getExtendedNodes().values()) {
                vertex.setOriginalPos(vertex.getCurrentPos());
            }
            for (SignalMap.Vertex vertex : signalMap.getManagedNodes().values()) {
                vertex.setOriginalPos(vertex.getCurrentPos());
            }

            MiscManager.getInstance().updateAnaylsis(analysis);
            return analysis;
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return null;
    }

    @POST
    @Path("/{analysisId}/recalc-signalmap")
    public Analysis reCalculateSignalMap(@PathParam("analysisId") String analysisId, @QueryParam("freq") EFrequencyRange range) {
        try {
            ServerUtil.checkParameter(new ServerUtil.RestParam("analysisId", analysisId), new ServerUtil.RestParam("freq", range));
            Analysis analysis = MiscManager.getInstance().getByAnalysisId(analysisId);
            SignalMap.FloorplanConfig oldFloorPlanConfigCopy = new SignalMap.FloorplanConfig(analysis.getSignalMap().get(range).getFloorplanConfig());

            analysis.getSignalMap().put(range, ((new ManagedNodesPositioner(analysis, range, ServerConfig.getInstance().getSignalMapConfig())).createSignalMap()));
            analysis.getSignalMap().get(range).setFloorplanConfig(oldFloorPlanConfigCopy);
            MiscManager.getInstance().updateAnaylsis(analysis);
            return analysis;
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return null;
    }
}

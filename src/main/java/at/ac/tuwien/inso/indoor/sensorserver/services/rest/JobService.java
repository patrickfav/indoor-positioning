package at.ac.tuwien.inso.indoor.sensorserver.services.rest;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.EtagManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.MiscManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.SensorManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.JobLog;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNetwork;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.responsewrapper.BaseResponse;
import at.ac.tuwien.inso.indoor.sensorserver.services.ApiConst;
import at.ac.tuwien.inso.indoor.sensorserver.services.exceptions.ExceptionHandler;
import at.ac.tuwien.inso.indoor.sensorserver.services.exceptions.InvalidAPICallException;
import at.ac.tuwien.inso.indoor.sensorserver.services.scheduler.SchedulerManager;
import at.ac.tuwien.inso.indoor.sensorserver.util.CacheUtil;
import at.ac.tuwien.inso.indoor.sensorserver.util.ServerUtil;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

/**
 * Created by PatrickF on 17.09.2014.
 */
@Path("/jobs")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class JobService {

    @GET
    @Path("/node/{nodeId}")
    public Response getAllJobsByNodeId(@PathParam("nodeId") String nodeId, @Context Request request) {
        Response.ResponseBuilder builder = null;
        try {
            ServerUtil.checkParameter(new ServerUtil.RestParam("nodeId", nodeId));

            builder = request.evaluatePreconditions(CacheUtil.getEtag(EtagManager.getInstance().getETag(JobLog.class)));
            CacheControl cc = CacheUtil.getCacheControl(ApiConst.CacheControl.JOB);
            if (builder == null) {
                List<JobLog> jobList = MiscManager.getInstance().getAllJobLogsFromNode(nodeId);
                builder = Response.ok(jobList).cacheControl(cc).tag(CacheUtil.getEtag(EtagManager.getInstance().getETag(JobLog.class)));
            }
            return builder.cacheControl(cc).build();
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return null;
    }

    @GET
    @Path("/network/{networkId}")
    public Response getAllJobsByNetworkId(@PathParam("networkId") String networkId, @Context Request request) {
        Response.ResponseBuilder builder = null;
        try {
            ServerUtil.checkParameter(new ServerUtil.RestParam("networkId", networkId));

            builder = request.evaluatePreconditions(CacheUtil.getEtag(EtagManager.getInstance().getETag(JobLog.class)));
            CacheControl cc = CacheUtil.getCacheControl(ApiConst.CacheControl.JOB);
            if (builder == null) {
                List<JobLog> jobList = MiscManager.getInstance().getAllJobLogsFromNetworkSorted(networkId, 12);

                builder = Response.ok(jobList).cacheControl(cc).tag(CacheUtil.getEtag(EtagManager.getInstance().getETag(JobLog.class)));
            }
            return builder.cacheControl(cc).build();
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return null;
    }

    @POST
    @Path("/schedule")
    public Boolean scheduleJob(@QueryParam("networkId") String networkId, @QueryParam("type") String type, @Context Request request) {
        try {
            ServerUtil.checkParameter(new ServerUtil.RestParam("networkId", networkId), new ServerUtil.RestParam("type", type));

            if (type.equalsIgnoreCase("survey")) {
                SchedulerManager.getInstance().scheduleSurveyJob(SensorManager.getInstance().getSensorNetworkById(networkId), true);
            } else if (type.equalsIgnoreCase("ping")) {
                SchedulerManager.getInstance().schedulePingJob(SensorManager.getInstance().getSensorNetworkById(networkId), true);
            } else if (type.equalsIgnoreCase("analysis")) {
                SchedulerManager.getInstance().scheduleAnalysisJob(SensorManager.getInstance().getSensorNetworkById(networkId), true);
            } else {
                throw new InvalidAPICallException("wrong type given, try e.g. 'survey'");
            }
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return true;
    }

    @POST
    @Path("/schedule/config/")
    public Response changeSchedule(@QueryParam("networkId") String networkId, @QueryParam("enable") boolean scheduleEnable, @Context Request request) {
        Response.ResponseBuilder builder = null;
        try {
            ServerUtil.checkParameter(new ServerUtil.RestParam("networkId", networkId), new ServerUtil.RestParam("enable", scheduleEnable));

            SensorNetwork network = SensorManager.getInstance().getSensorNetworkById(networkId);
            network.setCronEnabled(scheduleEnable);
            SensorManager.getInstance().updateSensorNetwork(network, true);

            if (scheduleEnable) {
                if (!SchedulerManager.getInstance().isScheduledPingJob(network.getNetworkId())) {
                    SchedulerManager.getInstance().addPingLogJob(network);
                }
                if (!SchedulerManager.getInstance().isScheduledSurveyJob(network.getNetworkId())) {
                    SchedulerManager.getInstance().addSurveyJob(network);
                }
                if (!SchedulerManager.getInstance().isScheduledAnalysisJob(network.getNetworkId())) {
                    SchedulerManager.getInstance().addAnalysisLogJob(network);
                }
            } else {
                if (SchedulerManager.getInstance().isScheduledPingJob(network.getNetworkId())) {
                    SchedulerManager.getInstance().cancelPingJob(network.getNetworkId());
                }
                if (SchedulerManager.getInstance().isScheduledSurveyJob(network.getNetworkId())) {
                    SchedulerManager.getInstance().cancelSurveyJob(network.getNetworkId());
                }
                if (SchedulerManager.getInstance().isScheduledAnalysisJob(network.getNetworkId())) {
                    SchedulerManager.getInstance().cancelAnalysisJob(network.getNetworkId());
                }
            }

            return Response.ok(true).build();
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return null;
    }
}

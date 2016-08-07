package at.ac.tuwien.inso.indoor.sensorserver.services.rest;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.MiscManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.PingLog;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNode;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.WlanScanNode;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.responsewrapper.BaseResponse;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.responsewrapper.SuccessResponse;
import at.ac.tuwien.inso.indoor.sensorserver.services.exceptions.ExceptionHandler;
import at.ac.tuwien.inso.indoor.sensorserver.services.exceptions.SensorRequestException;
import at.ac.tuwien.inso.indoor.sensorserver.services.requests.*;
import at.ac.tuwien.inso.indoor.sensorserver.services.scheduler.SurveyCallable;
import org.apache.log4j.Logger;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by PatrickF on 13.09.2014.
 */
@Path("/sensor")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class SensorAccessService {
    protected static Logger log = Logger.getLogger(SensorAccessService.class);

    @POST
    @Path("/info")
    public Response sensorInfo(SensorNode node) {
        try {
            return Response.ok(new RouterAdapterInfoRequest(node).startRequest()).build();
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return null;
    }

    @POST
    @Path("/ping")
    public SuccessResponse pingSensorSimple(SensorNode node,@QueryParam("persist") Boolean persist) {
        if(persist == null) {persist = true;}
        return ping(node,new RouterWebserverPingRequest(node),persist);
    }

    @POST
    @Path("/ping-cgi")
    public SuccessResponse pingSensorCgi(SensorNode node,@QueryParam("persist") Boolean persist) {
        if(persist == null) {persist = true;}
        return ping(node,new RouterScriptPingRequest(node),persist);
    }

    private SuccessResponse ping(SensorNode node, IPingRequest request, boolean persist) {
        SuccessResponse response = new SuccessResponse();

        try {
            PingLog pingLog = new PingLog();
            boolean success = false;
            try {
                success = request.startRequest();
                pingLog.setSuccess(success);
                pingLog.setNodeId(node.getNodeId());
                pingLog.setJobId(null);
                pingLog.setUrl(node.getFullUrl());
            } catch (SensorRequestException e) {
                pingLog.setSuccess(success);
                pingLog.setError(true);
            }

            if(persist) {
                log.debug("add new pinglog " + pingLog);
                MiscManager.getInstance().addPing(pingLog);
            }
            response.setSuccess(success);
        } catch (Exception e) {
            ExceptionHandler.handle(response, e);
        }

        return response;
    }

    @POST
    @Path("/scan")
    public Response simpleScan(SensorNode node, @QueryParam("adapter") String adapterName) {
        try {
            List<WlanScanNode> scanList =  new RouterScanRequest(node,adapterName).startRequest();
            return Response.ok(scanList).build();
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return null;
    }

    @POST
    @Path("/survey")
    public static Response survey(SensorNode node, @QueryParam("adapter") String adapterName, @QueryParam("delay") Long delay, @QueryParam("repeat") Integer repeat) {
        try {
            if(delay == null || delay == 0) {
                delay = 1500l;
            }
            if(repeat == null || repeat == 0) {
                repeat = 4;
            }
            SurveyCallable surveyWorker = new SurveyCallable(delay,repeat,adapterName,node);
            return Response.ok(surveyWorker.call()).build();
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return null;
    }

    @POST
    @Path("/reboot")
    public Response reboot(SensorNode node) {
        SuccessResponse response = new SuccessResponse();
        try {
            response.setSuccess(new RouterRebootRequest(node).startRequest());
            return Response.ok(response).build();
        } catch (Exception e) {
            ExceptionHandler.handle(response, e);
        }
        return null;
    }

}

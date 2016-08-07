package at.ac.tuwien.inso.indoor.sensorserver.services.rest;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.EtagManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.MiscManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.SensorManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.PingLog;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.EFrequencyRange;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.Adapter;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.RoomList;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNetwork;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNode;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.pathloss.BruteforceMultResult;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.responsewrapper.*;
import at.ac.tuwien.inso.indoor.sensorserver.services.ApiConst;
import at.ac.tuwien.inso.indoor.sensorserver.services.exceptions.ExceptionHandler;
import at.ac.tuwien.inso.indoor.sensorserver.services.exceptions.InvalidAPICallException;
import at.ac.tuwien.inso.indoor.sensorserver.util.CacheUtil;
import at.ac.tuwien.inso.indoor.sensorserver.util.ServerUtil;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.quartz.CronExpression;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by PatrickF on 08.09.2014.
 */

@Path("/network")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class SensorNetworkManagementService {

    @POST
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public BaseResponse addSensorNetwork(SensorNetwork network) {
        BaseResponse b = new BaseResponse();
        try {
            SensorManager.getInstance().addSensorNetwork(network);
            validateCronSchedules(network);
        } catch (Exception e) {
            ExceptionHandler.handle(b, e);
        }
        return b;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public BaseResponse updateSensorNetwork(SensorNetwork network) {
        BaseResponse b = new BaseResponse();
        try {
            SensorManager.getInstance().updateSensorNetwork(network,true);
            validateCronSchedules(network);
        } catch (Exception e) {
            ExceptionHandler.handle(b, e);
        }
        return b;
    }

    private void validateCronSchedules(SensorNetwork network) {
        if(!CronExpression.isValidExpression(network.getCronSchedulePing())) {
            throw new InvalidAPICallException("Ping Schedule Cron Schedule "+network.getCronSchedulePing()+" is invalid. Check the Quartz Syntax.");
        }
        if(!CronExpression.isValidExpression(network.getCronScheduleSurvey())) {
            throw new InvalidAPICallException("Survey Schedule Cron Schedule "+network.getCronScheduleSurvey()+" is invalid. Check the Quartz Syntax.");
        }
        if(!CronExpression.isValidExpression(network.getCronScheduleAnalysis())) {
            throw new InvalidAPICallException("Survey Schedule Cron Schedule "+network.getCronScheduleAnalysis()+" is invalid. Check the Quartz Syntax.");
        }
    }

    @GET
    public Response getAllSensorNetworks(@Context Request request) {
        Response.ResponseBuilder builder = null;
        try {

            builder = request.evaluatePreconditions(CacheUtil.getEtag(EtagManager.getInstance().getETag(SensorNetwork.class,PingLog.class)));
            CacheControl cc = CacheUtil.getCacheControl(ApiConst.CacheControl.SENSOR_NETWORK);
            if (builder == null) {

                NetworkListWrapper wrapper = new NetworkListWrapper();
                wrapper.setNetworks(SensorManager.getInstance().getAllSensorNetworksNonDeleted());
                for (SensorNetwork network : wrapper.getNetworks()) {
                    for (SensorNode sensorNode : SensorManager.getInstance().getAllNodesFromNetwork(network.getNetworkId())) {
                        if(!wrapper.getPingMap().containsKey(network.getNetworkId())) {
                            wrapper.getPingMap().put(network.getNetworkId(), new ArrayList<PingLog>());
                        }
                        wrapper.getPingMap().get(network.getNetworkId()).addAll(MiscManager.getInstance().getAllPingLogsFromNodeSorted(sensorNode.getNodeId(), 1));
                    }
                }

                builder = Response.ok(wrapper).cacheControl(cc).tag(CacheUtil.getEtag(EtagManager.getInstance().getETag(SensorNetwork.class,PingLog.class)));
            }
            return builder.cacheControl(cc).build();
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return null;
    }

    @GET
    @Path("/{networkId}")
    public Response getSensorNetworkDetails(@PathParam("networkId") String networkId, @Context Request request) {
        Response.ResponseBuilder builder = null;
        try {
            ServerUtil.checkParameter(new ServerUtil.RestParam("networkId",networkId));

            builder = request.evaluatePreconditions(CacheUtil.getEtag(EtagManager.getInstance().getETag(SensorNetwork.class,SensorNode.class,RoomList.class,PingLog.class)));
            CacheControl cc = CacheUtil.getCacheControl(ApiConst.CacheControl.SENSOR_NETWORK);
            if (builder == null) {
                SensorNodeListWrapper wrapper = new SensorNodeListWrapper();
                wrapper.setNodeList(SensorManager.getInstance().getAllNodesFromNetwork(networkId));
                wrapper.setSensorNetwork(SensorManager.getInstance().getSensorNetworkById(networkId));
                wrapper.setRoomList(MiscManager.getInstance().getRoomlistByNetworkId(networkId));

                for (SensorNode sensorNode : wrapper.getNodeList()) {
                    List<PingLog> pingLogs = MiscManager.getInstance().getAllPingLogsFromNode(sensorNode.getNodeId());
                    Collections.sort(pingLogs);

                    if(!pingLogs.isEmpty()) {
                        wrapper.getPingLogList().add(pingLogs.get(0));
                    }
                }

                builder = Response.ok(wrapper).cacheControl(cc).tag(CacheUtil.getEtag(EtagManager.getInstance().getETag(SensorNetwork.class,SensorNode.class,RoomList.class,PingLog.class)));
            }
            return builder.cacheControl(cc).build();
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return null;
    }

    @DELETE
    @Path("/{networkId}")
    public SuccessResponse setSensorNetworkDeleted(@PathParam("networkId") String networkId, @Context Request request) {
        SuccessResponse successResponse = new SuccessResponse();
        try {
            SensorManager.getInstance().setNetworkDeleted(networkId);
            successResponse.setSuccess(true);
            return successResponse;
        } catch (Exception e) {
            ExceptionHandler.handle(successResponse, e);
        }
        return null;
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Path("/{networkId}/node")
    public BaseResponse addSensorNode(@PathParam("networkId") String networkId, SensorNode node) {
        BaseResponse b = new BaseResponse();
        try {
            node.setNetworkId(networkId);
            SensorManager.getInstance().addSensorNode(node);
        } catch (Exception e) {
            ExceptionHandler.handle(b, e);
        }
        return b;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Path("/{networkId}/node")
    public BaseResponse updateSensorNode(SensorNode node) {
        BaseResponse b = new BaseResponse();
        try {
            SensorManager.getInstance().updateSensorNode(node);
        } catch (Exception e) {
            ExceptionHandler.handle(b, e);
        }
        return b;
    }

    @GET
    @Path("/{networkId}/node/{nodeId}")
    public Response getNodeDetails(@PathParam("networkId") String networkId,@PathParam("nodeId") String nodeId, @Context Request request) {
        Response.ResponseBuilder builder = null;
        try {
            ServerUtil.checkParameter(new ServerUtil.RestParam("networkId",networkId),new ServerUtil.RestParam("nodeId",nodeId));
            builder = request.evaluatePreconditions(CacheUtil.getEtag(EtagManager.getInstance().getETag(SensorNetwork.class,SensorNode.class,RoomList.class,PingLog.class)));
            CacheControl cc = CacheUtil.getCacheControl(ApiConst.CacheControl.SENSOR_NETWORK);
            if (builder == null) {
                SensorNodeDetailsWrapper wrapper = new SensorNodeDetailsWrapper();
                wrapper.setSensorNode(SensorManager.getInstance().getSensorNodeById(nodeId));
                wrapper.setSensorNetwork(SensorManager.getInstance().getSensorNetworkById(networkId));
                wrapper.setPingList(MiscManager.getInstance().getAllPingLogsFromNodeSorted(nodeId, 1));
                wrapper.setRoomList(MiscManager.getInstance().getRoomlistByNetworkId(networkId));

                builder = Response.ok(wrapper).cacheControl(cc).tag(CacheUtil.getEtag(EtagManager.getInstance().getETag(SensorNetwork.class,SensorNode.class,RoomList.class,PingLog.class)));
            }
            return builder.cacheControl(cc).build();
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return null;
    }

    @POST
    @Path("/{networkId}/upload-blueprint")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public SensorNetwork uploadBlueprintImage(
            @PathParam("networkId") String networkId,
            @QueryParam("content-type") String contentType,
            @FormDataParam("file") InputStream file,
            @FormDataParam("file") FormDataContentDisposition fileDisposition) {
        try {
            ServerUtil.checkParameter(new ServerUtil.RestParam("networkId",networkId));
            return SensorManager.getInstance().saveBlueprintImage(networkId,file,contentType);
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return null;
    }

	@POST
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Path("/{networkId}/node/multiplier")
	public BaseResponse  setMultiplierForNodes(@PathParam("networkId") String networkId, Map<EFrequencyRange,BruteforceMultResult> multiplier) {
		BaseResponse b = new BaseResponse();
		try {
			List<SensorNode> allNodes = SensorManager.getInstance().getAllNodesFromNetwork(networkId);

			for (SensorNode node : allNodes) {
				boolean anythingChanged = false;
				for (Adapter adapter : node.getAdapters()) {
					if(multiplier.containsKey(adapter.getFrequencyRange()) && multiplier.get(adapter.getFrequencyRange()).getMacMultMap().containsKey(adapter.getMacAddress())) {
						anythingChanged = true;
						node.getMultiplierMap().put(adapter.getFrequencyRange(),multiplier.get(adapter.getFrequencyRange()).getMacMultMap().get(adapter.getMacAddress()));
					}
				}

				if(anythingChanged) {
					SensorManager.getInstance().updateSensorNode(node);
				}
			}

		} catch (Exception e) {
			ExceptionHandler.handle(b, e);
		}
		return b;
	}

}

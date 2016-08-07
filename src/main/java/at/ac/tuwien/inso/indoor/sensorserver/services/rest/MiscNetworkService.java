package at.ac.tuwien.inso.indoor.sensorserver.services.rest;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.EtagManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.MiscManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.SensorManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.Blacklist;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.PingLog;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.RoomList;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNode;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.responsewrapper.BaseResponse;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.responsewrapper.SuccessResponse;
import at.ac.tuwien.inso.indoor.sensorserver.services.ApiConst;
import at.ac.tuwien.inso.indoor.sensorserver.services.exceptions.ExceptionHandler;
import at.ac.tuwien.inso.indoor.sensorserver.util.CacheUtil;
import at.ac.tuwien.inso.indoor.sensorserver.util.ServerUtil;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by PatrickF on 07.10.2014.
 */
@Path("/networkservice")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class MiscNetworkService {
    @GET
    @Path("/{networkId}/ping")
    public Response getRecentPingFromAllNodes(@PathParam("networkId") String networkId, @Context Request request) {
        Response.ResponseBuilder builder = null;
        try {
            ServerUtil.checkParameter(new ServerUtil.RestParam("networkId", networkId));

            builder = request.evaluatePreconditions(CacheUtil.getEtag(EtagManager.getInstance().getETag(PingLog.class)));
            CacheControl cc = CacheUtil.getCacheControl(ApiConst.CacheControl.PING);
            if (builder == null) {
                List<PingLog> recentLogs = new ArrayList<PingLog>();

                for (SensorNode sensorNode : SensorManager.getInstance().getAllNodesFromNetwork(networkId)) {
                    List<PingLog> pingLogs = MiscManager.getInstance().getAllPingLogsFromNodeSorted(sensorNode.getNodeId(), 5);
                    Collections.sort(pingLogs);

                    if(!pingLogs.isEmpty()) {
                        recentLogs.add(pingLogs.get(0));
                    }
                }

                builder = Response.ok(recentLogs).cacheControl(cc).tag(CacheUtil.getEtag(EtagManager.getInstance().getETag(PingLog.class)));
            }
            return builder.cacheControl(cc).build();
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return null;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Path("/{networkId}/blacklist")
    public Blacklist updateBlacklist(@PathParam("networkId") String networkId, Blacklist blacklist) {
        try {
            ServerUtil.checkParameter(new ServerUtil.RestParam("networkId", networkId));
            MiscManager.getInstance().updateBlacklist(blacklist);
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return blacklist;
    }

    @GET
    @Path("/{networkId}/blacklist")
    public Response getBlackListBySensorNetwork(@PathParam("networkId") String networkId, @Context Request request) {
        Response.ResponseBuilder builder = null;
        try {
            ServerUtil.checkParameter(new ServerUtil.RestParam("networkId", networkId));

            builder = request.evaluatePreconditions(CacheUtil.getEtag(EtagManager.getInstance().getETag(Blacklist.class)));
            CacheControl cc = CacheUtil.getCacheControl(ApiConst.CacheControl.BLACKLIST);
            if (builder == null) {
                builder = Response.ok(MiscManager.getInstance().getBlacklistByNetworkId(networkId)).cacheControl(cc).tag(CacheUtil.getEtag(EtagManager.getInstance().getETag(Blacklist.class)));
            }
            return builder.cacheControl(cc).build();
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return null;
    }


    @PUT
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Path("/{networkId}/roomlist")
    public RoomList updateRoomlist(@PathParam("networkId") String networkId, RoomList roomList) {
        try {
            ServerUtil.checkParameter(new ServerUtil.RestParam("networkId", networkId));
            MiscManager.getInstance().updateRoomlist(roomList);
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return roomList;
    }

    @GET
    @Path("/{networkId}/roomlist")
    public Response getRoomListBySensorNetwork(@PathParam("networkId") String networkId, @Context Request request) {
        Response.ResponseBuilder builder = null;
        try {
            ServerUtil.checkParameter(new ServerUtil.RestParam("networkId", networkId));

            builder = request.evaluatePreconditions(CacheUtil.getEtag(EtagManager.getInstance().getETag(RoomList.class)));
            CacheControl cc = CacheUtil.getCacheControl(ApiConst.CacheControl.ROOMLIST);
            if (builder == null) {
                builder = Response.ok(MiscManager.getInstance().getRoomlistByNetworkId(networkId)).cacheControl(cc).tag(CacheUtil.getEtag(EtagManager.getInstance().getETag(RoomList.class)));
            }
            return builder.cacheControl(cc).build();
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return null;
    }

    @PUT
    @Path("/{networkId}/roomlist/mapping")
    public SuccessResponse setNewRoomMacMapping(@PathParam("networkId") String networkId,@QueryParam("roomId") String roomId,@QueryParam("macAddress") String macAddress) {
        SuccessResponse response = new SuccessResponse();
        try {
            ServerUtil.checkParameter(new ServerUtil.RestParam("networkId", networkId),new ServerUtil.RestParam("roomId", roomId),new ServerUtil.RestParam("macAddress", macAddress));
            response.setUpdatedRev(MiscManager.getInstance().addNewMacRoomMapping(networkId,macAddress,roomId).getRevision());
            response.setSuccess(true);
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return response;
    }
}

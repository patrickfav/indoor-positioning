package at.ac.tuwien.inso.indoor.sensorserver.services.rest;

import at.ac.tuwien.inso.indoor.sensorserver.math.radiomodels.EEnvironmentModel;
import at.ac.tuwien.inso.indoor.sensorserver.math.radiomodels.ITUIndoorModelDegradingDist;
import at.ac.tuwien.inso.indoor.sensorserver.math.solver.NodeMultiNormalizer;
import at.ac.tuwien.inso.indoor.sensorserver.math.solver.PathLossBruteforceSolver;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.EtagManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.EFrequencyRange;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNetwork;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.pathloss.*;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.responsewrapper.BaseResponse;
import at.ac.tuwien.inso.indoor.sensorserver.services.ApiConst;
import at.ac.tuwien.inso.indoor.sensorserver.services.exceptions.ExceptionHandler;
import at.ac.tuwien.inso.indoor.sensorserver.util.CacheUtil;
import at.ac.tuwien.inso.indoor.sensorserver.util.ServerUtil;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by PatrickF on 01.11.2014.
 */
@Path("/util")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class PathLossUtilityService {
	private static Logger log = Logger.getLogger(PathLossUtilityService.class);

	@POST
    @Path("/itu-degr-dist-values")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getITUDegrDistGraphValues(ITUIndoorModelDegradingDist.ITUDegradingDistConfig config,@Context Request request) {
        Response.ResponseBuilder builder = null;
        try {
            builder = request.evaluatePreconditions(CacheUtil.getEtag(EtagManager.getInstance().getETag(SensorNetwork.class)));
            CacheControl cc = CacheUtil.getCacheControl(ApiConst.CacheControl.SENSOR_NETWORK);
            if (builder == null) {
                Map<EEnvironmentModel,List<List<Double>>> map = new HashMap<EEnvironmentModel, List<List<Double>>>();

                map.put(EEnvironmentModel.INDOOR_OBSTRUCTED_OFFICE,calculateModelValues(EEnvironmentModel.INDOOR_OBSTRUCTED_OFFICE,config));
                map.put(EEnvironmentModel.INDOOR_OBSTRUCTED_COMMERCIAL,calculateModelValues(EEnvironmentModel.INDOOR_OBSTRUCTED_COMMERCIAL,config));
                map.put(EEnvironmentModel.INDOOR_OBSTRUCTED_RESIDENTIAL,calculateModelValues(EEnvironmentModel.INDOOR_OBSTRUCTED_RESIDENTIAL,config));
	            map.put(EEnvironmentModel.INDOOR_LINE_OF_SIGHT,calculateModelValues(EEnvironmentModel.INDOOR_LINE_OF_SIGHT,config));
	            map.put(EEnvironmentModel.FREE_SPACE,calculateModelValues(EEnvironmentModel.FREE_SPACE,config));

                builder = Response.ok(map).cacheControl(cc).tag(CacheUtil.getEtag(EtagManager.getInstance().getETag(SensorNetwork.class)));
            }
            return builder.cacheControl(cc).build();
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return null;
    }

    private List<List<Double>> calculateModelValues(EEnvironmentModel envModel, ITUIndoorModelDegradingDist.ITUDegradingDistConfig config) {
        List<List<Double>> values = new ArrayList<List<Double>>();
        ITUIndoorModelDegradingDist model = new ITUIndoorModelDegradingDist(envModel,config);
        for (int i = 30; i < 100; i++) {
            List<Double> xy = new ArrayList<Double>();
            xy.add((double) i); //db x-value
            xy.add(model.getDistanceInMeter(i, EFrequencyRange.frequencyHz(EFrequencyRange.WLAN_2_4Ghz,1),0)); //m y-value
            values.add(xy);
        }
        return values;
    }

	@POST
	@Path("/itu-degr-dist-values-for-input")
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Map<Double,Double> getITUDegrDistValuesForInput(@QueryParam("envModel")EEnvironmentModel envModel,@QueryParam("freq")EFrequencyRange range,DistanceValueInput input) {
		try {
			ServerUtil.checkParameter(new ServerUtil.RestParam("envModel", envModel),new ServerUtil.RestParam("freq", range));

			Map<Double,Double> map = new HashMap<Double, Double>();
			ITUIndoorModelDegradingDist model = new ITUIndoorModelDegradingDist(envModel,input.getConfig());

			for (Double value : input.getValues()) {
				map.put(value, model.getDistanceInMeter(value, EFrequencyRange.frequencyHz(range, 1), 0));
			}

			return map;
		} catch (Exception e) {
			ExceptionHandler.handle(new BaseResponse(), e);
		}
		return null;
	}

	@POST
	@Path("/itu-degr-dist-bruteforce-solver")
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public BruteforceDistanceResult getBestConfigByBruteforceSolver(@QueryParam("envModel")EEnvironmentModel envModel,@QueryParam("freq")EFrequencyRange range , List<BruteforceDistanceInfo> list) {
		try {
			ServerUtil.checkParameter(new ServerUtil.RestParam("envModel", envModel),new ServerUtil.RestParam("freq", range),new ServerUtil.RestParam("List<BruteforceDistanceInfo>", list),
					new ServerUtil.RestParam("elments in list", list.get(0)));
			PathLossBruteforceSolver solver = new PathLossBruteforceSolver(list,range,envModel);
			return solver.calculate();
		} catch (Exception e) {
			ExceptionHandler.handle(new BaseResponse(), e);
		}
		return null;
	}

	@POST
	@Path("/itu-degr-dist-bruteforce-calibrate-mult")
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public BruteforceMultResult getBestNodeMultiByBruteforceSolver(List<BruteforceMultInfo> list) {
		try {
			ServerUtil.checkParameter(new ServerUtil.RestParam("List<BruteforceMultInfo>", list));

			NodeMultiNormalizer normalizer = new NodeMultiNormalizer(list);
			return normalizer.calculate();
		} catch (Exception e) {
			ExceptionHandler.handle(new BaseResponse(), e);
		}
		return null;
	}
}

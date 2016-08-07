package at.ac.tuwien.inso.indoor.sensorserver.services.requests;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.Adapter;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNode;
import at.ac.tuwien.inso.indoor.sensorserver.services.ApiConst;
import at.ac.tuwien.inso.indoor.sensorserver.services.exceptions.SensorRequestException;
import at.ac.tuwien.inso.indoor.sensorserver.services.parser.IwinfoAdapterListParser;
import at.ac.tuwien.inso.indoor.sensorserver.services.parser.IwinfoXmlReader;
import at.ac.tuwien.inso.indoor.sensorserver.util.ServerUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by PatrickF on 12.09.2014.
 */
public class RouterSingleAdapterInfoRequest extends ARequest<List<Adapter>> {
    private String adapterName;

    public RouterSingleAdapterInfoRequest(SensorNode node, String adapterName) {
        super(node);
        this.adapterName = adapterName;
    }

    @Override
    public List<Adapter> startRequest() throws SensorRequestException {
        try {
            Map<String,String> queryMap= new HashMap<String, String>();
            queryMap.put(ApiConst.ROUTER_ADAPTER_QUERY_PARAM,adapterName);
            queryMap.put(ApiConst.ROUTER_SCAN_BOOL_QUERY_PARAM,"false");

            ResponseWrapper responseWrapper = runRequest("GET",getNode().getFullUrl()+ ApiConst.ROUTER_SERVICE_IWINFO+ ServerUtil.createQueryString(queryMap),"",false);
            IwinfoXmlReader.IwinfoAdapter iwinfoAdapter = IwinfoXmlReader.parseSpecificIwinfoAdapter(responseWrapper.getBody());
            return  IwinfoAdapterListParser.parse(iwinfoAdapter.getInfo(), true);
        } catch (Exception e) {
            throw new SensorRequestException("Could not complete "+getClass().getSimpleName(),e);
        }
    }
}

package at.ac.tuwien.inso.indoor.sensorserver.services.requests;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.WlanScanNode;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNode;
import at.ac.tuwien.inso.indoor.sensorserver.services.ApiConst;
import at.ac.tuwien.inso.indoor.sensorserver.services.exceptions.SensorRequestException;
import at.ac.tuwien.inso.indoor.sensorserver.services.parser.IwinfoScanParser;
import at.ac.tuwien.inso.indoor.sensorserver.services.parser.IwinfoXmlReader;
import at.ac.tuwien.inso.indoor.sensorserver.util.ServerUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by PatrickF on 12.09.2014.
 */
public class RouterScanRequest extends ARequest<List<WlanScanNode>> {
    private String adapterName;
    private String url;

    public RouterScanRequest(SensorNode node, String adapterName) {
        super(node);
        this.adapterName = adapterName;
    }

    public RouterScanRequest(String url, String adapterName) {
        this.adapterName = adapterName;
        this.url = url;
    }

    @Override
    public List<WlanScanNode> startRequest() throws SensorRequestException {
        try {
            Map<String, String> queryMap = new HashMap<String, String>();
            queryMap.put(ApiConst.ROUTER_ADAPTER_QUERY_PARAM, adapterName);
            queryMap.put(ApiConst.ROUTER_SCAN_BOOL_QUERY_PARAM, "true");


            ResponseWrapper responseWrapper = runRequest("GET", getUrl() + ApiConst.ROUTER_SERVICE_IWINFO + ServerUtil.createQueryString(queryMap), "", true);
            IwinfoXmlReader.IwinfoAdapter iwinfoAdapter = IwinfoXmlReader.parseSpecificIwinfoAdapter(responseWrapper.getBody());
            List<WlanScanNode> list = IwinfoScanParser.parse(iwinfoAdapter.getScan(), false);
            Collections.sort(list);
            log.debug("Found in scan: " + ServerUtil.foldTooLongString(list.toString(), 2000));
            return list;
        } catch (Exception e) {
            throw new SensorRequestException("Could not complete " + getClass().getSimpleName(), e);
        }
    }

    private String getUrl() {
        if (url == null) {
            return getNode().getFullUrl();
        } else {
            return url;
        }
    }
}

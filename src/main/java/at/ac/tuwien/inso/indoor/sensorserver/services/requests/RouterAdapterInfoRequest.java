package at.ac.tuwien.inso.indoor.sensorserver.services.requests;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.Adapter;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNode;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.responsewrapper.AdapterInfoWrapper;
import at.ac.tuwien.inso.indoor.sensorserver.services.ApiConst;
import at.ac.tuwien.inso.indoor.sensorserver.services.exceptions.SensorRequestException;
import at.ac.tuwien.inso.indoor.sensorserver.services.parser.IwinfoIfConfigAdapterListParser;
import at.ac.tuwien.inso.indoor.sensorserver.services.parser.IwinfoMachinInfoParser;
import at.ac.tuwien.inso.indoor.sensorserver.services.parser.IwinfoXmlReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by PatrickF on 12.09.2014.
 */
public class RouterAdapterInfoRequest extends ARequest<AdapterInfoWrapper> {

    public RouterAdapterInfoRequest(SensorNode node) {
        super(node);
    }

    @Override
    public AdapterInfoWrapper startRequest() throws SensorRequestException {
        try {
            ResponseWrapper responseWrapper = runRequest("GET", getNode().getFullUrl() + ApiConst.ROUTER_SERVICE_IWINFO, "", false);
            IwinfoXmlReader.IwinfoList iwinfoList = IwinfoXmlReader.parseIwinfoAdapterList(responseWrapper.getBody());

            List<Adapter> adapters = new ArrayList<Adapter>();
            for (String adapterName : IwinfoIfConfigAdapterListParser.parse(iwinfoList.getIfconfig(), true)) {
                adapters.addAll(new RouterSingleAdapterInfoRequest(getNode(), adapterName).startRequest());
            }
            Collections.sort(adapters);

            AdapterInfoWrapper adapterInfoWrapper = new AdapterInfoWrapper();
            adapterInfoWrapper.setMachineInfo(IwinfoMachinInfoParser.parse(iwinfoList, true));
            adapterInfoWrapper.setAdapterList(adapters);

            return adapterInfoWrapper;
        } catch (Exception e) {
            throw new SensorRequestException("Could not complete " + getClass().getSimpleName(), e);
        }
    }
}

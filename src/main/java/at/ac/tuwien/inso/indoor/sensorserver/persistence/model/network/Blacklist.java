package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.couchdb.TypeAbleCouchDBDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by PatrickF on 26.09.2014.
 */
public class Blacklist extends TypeAbleCouchDBDocument{
    private String networkId;
    private List<String> macList = new ArrayList<String>();
    private boolean actAsWhiteList =false;

    public boolean isActAsWhiteList() {
        return actAsWhiteList;
    }

    public void setActAsWhiteList(boolean actAsWhiteList) {
        this.actAsWhiteList = actAsWhiteList;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public List<String> getMacList() {
        return macList;
    }

    public void setMacList(List<String> macList) {
        this.macList = macList;
    }
}

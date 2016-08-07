package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.responsewrapper;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.Adapter;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.MachineInfo;

import java.util.List;

/**
 * Created by PatrickF on 22.09.2014.
 */
public class AdapterInfoWrapper {
    private List<Adapter> adapterList;
    private MachineInfo machineInfo;

    public List<Adapter> getAdapterList() {
        return adapterList;
    }

    public void setAdapterList(List<Adapter> adapterList) {
        this.adapterList = adapterList;
    }

    public MachineInfo getMachineInfo() {
        return machineInfo;
    }

    public void setMachineInfo(MachineInfo machineInfo) {
        this.machineInfo = machineInfo;
    }
}

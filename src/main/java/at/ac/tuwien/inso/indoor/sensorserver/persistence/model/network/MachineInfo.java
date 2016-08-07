package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network;

/**
 * Created by PatrickF on 22.09.2014.
 */
public class MachineInfo {
    private String systemType;
    private String machine;
    private String cpuModel;

    private String version;
    private Integer totalRamKb;

    public String getSystemType() {
        return systemType;
    }

    public void setSystemType(String systemType) {
        this.systemType = systemType;
    }

    public String getMachine() {
        return machine;
    }

    public void setMachine(String machine) {
        this.machine = machine;
    }

    public String getCpuModel() {
        return cpuModel;
    }

    public void setCpuModel(String cpuModel) {
        this.cpuModel = cpuModel;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getTotalRamKb() {
        return totalRamKb;
    }

    public void setTotalRamKb(Integer totalRamKb) {
        this.totalRamKb = totalRamKb;
    }

    @Override
    public String toString() {
        return "MachineInfo{" +
                "systemType='" + systemType + '\'' +
                ", machine='" + machine + '\'' +
                ", cpuModel='" + cpuModel + '\'' +
                ", version='" + version + '\'' +
                ", totalRamKb=" + totalRamKb +
                '}';
    }
}

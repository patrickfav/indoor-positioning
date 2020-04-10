package at.ac.tuwien.inso.indoor.sensorserver.services.parser;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.MachineInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by PatrickF on 12.09.2014.
 */
public final class IwinfoMachinInfoParser {
    private static Logger log = LogManager.getLogger(IwinfoMachinInfoParser.class);

    private IwinfoMachinInfoParser() {
    }

    public static MachineInfo parse(IwinfoXmlReader.IwinfoList info) {
        return parse(info, false);
    }

    public static MachineInfo parse(IwinfoXmlReader.IwinfoList info, boolean shouldLog) {

        MachineInfo machineInfo = new MachineInfo();
        machineInfo.setVersion(info.getVersion());
        machineInfo = parseCpuInfo(info.getCpuInfo(), machineInfo, shouldLog);
        machineInfo = parseMemInfo(info.getMemInfo(), machineInfo, shouldLog);

        if (shouldLog) log.debug("parsed info: " + machineInfo);
        return machineInfo;
    }

    private static MachineInfo parseCpuInfo(String cpuInfo, MachineInfo info, boolean shouldLog) {
        if (shouldLog) log.debug("CpuInfo: " + cpuInfo);

        try {
            Pattern systemTypePattern = Pattern.compile("system type\\s*\\t*:\\s*(.*)\\n", Pattern.CASE_INSENSITIVE);
            Matcher systemTypeMatcher = systemTypePattern.matcher(cpuInfo);
            systemTypeMatcher.find();
            info.setSystemType(systemTypeMatcher.group(1).trim());
        } catch (Exception e) {
            log.warn("Could not find system type", e);
        }

        try {
            Pattern machinePattern = Pattern.compile("machine\\s*\\t*:\\s*(.*)\\n", Pattern.CASE_INSENSITIVE);
            Matcher machineMatcher = machinePattern.matcher(cpuInfo);
            machineMatcher.find();
            info.setMachine(machineMatcher.group(1).trim());
        } catch (Exception e) {
            log.warn("Could not find machine", e);
        }

        try {
            Pattern cpuModelPattern = Pattern.compile("cpu model\\s*\\t*:\\s*(.*)\\n", Pattern.CASE_INSENSITIVE);
            Matcher cpuModelMatcher = cpuModelPattern.matcher(cpuInfo);
            cpuModelMatcher.find();
            info.setCpuModel(cpuModelMatcher.group(1).trim());
        } catch (Exception e) {
            log.warn("Could not find cpu model", e);
        }
        return info;
    }

    private static MachineInfo parseMemInfo(String memInfo, MachineInfo info, boolean shouldLog) {
        if (shouldLog) log.debug("MemInfo: " + memInfo);

        try {
            Pattern totalMemPattern = Pattern.compile("MemTotal\\s*\\t*:\\s*(\\d+)\\s*kB", Pattern.CASE_INSENSITIVE);
            Matcher totalMemMatcher = totalMemPattern.matcher(memInfo);
            totalMemMatcher.find();
            info.setTotalRamKb(Integer.valueOf(totalMemMatcher.group(1).trim()));
        } catch (Exception e) {
            log.warn("Could not find total mem", e);
        }
        return info;
    }
}

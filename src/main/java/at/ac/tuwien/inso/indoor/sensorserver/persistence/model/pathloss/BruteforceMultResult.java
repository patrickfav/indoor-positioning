package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.pathloss;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by PatrickF on 03.11.2014.
 */
public class BruteforceMultResult {
    private Map<String, Double> macMultMap = new HashMap<String, Double>();
    private double offset;
    private double baseOffset;
    private double incrResolution;

    public BruteforceMultResult(Map<String, Double> macMultMap, double offset) {
        this.macMultMap = macMultMap;
        this.offset = offset;
    }

    public BruteforceMultResult() {
    }

    public Map<String, Double> getMacMultMap() {
        return macMultMap;
    }

    public void setMacMultMap(Map<String, Double> macMultMap) {
        this.macMultMap = macMultMap;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    public double getBaseOffset() {
        return baseOffset;
    }

    public void setBaseOffset(double baseOffset) {
        this.baseOffset = baseOffset;
    }

    public double getIncrResolution() {
        return incrResolution;
    }

    public void setIncrResolution(double incrResolution) {
        this.incrResolution = incrResolution;
    }

    @Override
    public String toString() {
        return "BruteforceMultResult{" +
                "macMultMap=" + macMultMap +
                ", offset=" + offset +
                ", baseOffset=" + baseOffset +
                ", incrResolution=" + incrResolution +
                '}';
    }
}

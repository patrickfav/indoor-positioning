package at.ac.tuwien.inso.indoor.sensorserver.services.positioner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by PatrickF on 10.10.2014.
 */
public class NodeProbabilityPoint {
    private static final String SENSOR_REPRESENTATION = "$";

    private Map<String,Map<String,Double>> probMap = new HashMap<String, Map<String, Double>>();
    private String fixedPosMacAddress;

    private int x;
    private int y;

    private Map<String,Integer> foundTargetMacCache;

    public NodeProbabilityPoint(int x, int y) {
        this.x = x;
        this.y = y;
        resetCache();
    }

    private void resetCache() {
        foundTargetMacCache = null;
    }

    public String getFixedPosMacAddress() {
        return fixedPosMacAddress;
    }

    public void setFixedPosMacAddress(String fixedPosMacAddress) {
        this.fixedPosMacAddress = fixedPosMacAddress;
    }

    public boolean isFixedPosNode() {
        return fixedPosMacAddress != null;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public Set<Double> getProbabilitySet(String subjMac,Set<String> neededSrcMac) {
        Set<Double> probList = new HashSet<Double>();
        for (String macSrc : neededSrcMac) {
            if(!probMap.containsKey(macSrc) || !probMap.get(macSrc).containsKey(subjMac)) {
                return new HashSet<Double>();
            }
            probList.add(probMap.get(macSrc).get(subjMac));
        }

        if(probList.size() < neededSrcMac.size()) {
            return new HashSet<Double>();
        }

        return probList;
    }

    public Double getProbabilitySum(String subjMac,Set<String> neededSrcMac) {
        double sum = 0;
        for (Double prob : getProbabilitySet(subjMac, neededSrcMac)) {
            sum += prob;
        }
        return sum;
    }

    public Double getProbabilityFac(String subjMac,Set<String> neededSrcMac) {
        if(neededSrcMac.isEmpty()) {
            return 0d;
        }

        double fac = 1;
        for (Double prob : getProbabilitySet(subjMac, neededSrcMac)) {
            fac *= prob;
        }
        return fac;
    }

    public void addProbability(String sourceMacAddr, String subjMacAddress,Double probability) {
        if (!probMap.containsKey(sourceMacAddr)) {
            probMap.put(sourceMacAddr, new HashMap<String, Double>());
        }

        if (!probMap.get(sourceMacAddr).containsKey(subjMacAddress) || probMap.get(sourceMacAddr).get(subjMacAddress) < probability) {
            probMap.get(sourceMacAddr).put(subjMacAddress, probability);
        }
        resetCache();
    }

    public String getSingleCharStringRepresentation(String sourceMacAddr) {
        if(isFixedPosNode() && getFixedPosMacAddress().equalsIgnoreCase(sourceMacAddr)) {
            return SENSOR_REPRESENTATION;
        } else {
            if (!probMap.containsKey(sourceMacAddr)) {
                return PosHelper.EMPTY_STRING_REPRESENTATION;
            } else {
                if (probMap.get(sourceMacAddr).keySet().size() == 1) {
                    double prob = probMap.get(sourceMacAddr).values().iterator().next();

                    if(prob > 0.9) {
                        return "#";
                    } else if(prob > 0.7) {
                        return "*";
                    } else if(prob > 0.5) {
                        return "~";
                    } else if(prob > 0.2) {
                        return ";";
                    } else if(prob > 0.1) {
                        return ":";
                    } else if(prob <= 0.1) {
                        return ".";
                    }
                }
                if (probMap.get(sourceMacAddr).keySet().size() > 9) {
                    return "+";
                } else {
                    return String.valueOf(probMap.get(sourceMacAddr).keySet().size());
                }
            }
        }
    }

    public String getSingleCharStringRepresentation() {
        if(isFixedPosNode()) {
            return SENSOR_REPRESENTATION;
        } else {
            if (probMap.keySet().size() == 0) {
                return PosHelper.EMPTY_STRING_REPRESENTATION;
            } else {
                int max=1;

                Map<String,Integer> foundTargetMac = createAndGetFoundTargetMacCache();

                for (String s : foundTargetMac.keySet()) {
                    if(foundTargetMac.get(s) > max) {
                        max = foundTargetMac.get(s);
                    }
                }

                if (max > 9) {
                    return "+";
                } else {
                    return String.valueOf(max);
                }
            }
        }
    }

    private Map<String,Integer> createAndGetFoundTargetMacCache() {
        if(foundTargetMacCache == null) {
            foundTargetMacCache = new HashMap<String, Integer>();
            for (String src : probMap.keySet()) {
                for (String target : probMap.get(src).keySet()) {
                    if(!foundTargetMacCache.containsKey(target)) {
                        foundTargetMacCache.put(target,1);
                    } else {
                        foundTargetMacCache.put(target,foundTargetMacCache.get(target)+1);
                    }
                }
            }
        }
        return foundTargetMacCache;
    }
}

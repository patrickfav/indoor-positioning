package at.ac.tuwien.inso.indoor.sensorserver.persistence.model;

import java.util.*;

/**
 * Created by PatrickF on 05.10.2014.
 */
public class SimpleStatistics {
    private Double mean =0d;
    private Double median=0d;
    private Double mode=0d;
    private Double variance=0d;
    private Double stdDev=0d;
    private Double max=0d;
    private Double min=0d;
    private Integer dataSize=0;
    private Double sdtErr80Interval=0d;

    public SimpleStatistics() {
    }

    public SimpleStatistics(List<Double> dataList) {
        dataSize = dataList.size();
        if(!dataList.isEmpty()) {
            Collections.sort(dataList);
            mean = getArithmeticMean(dataList);
            median = getMedian(dataList);
            mode = getMode(dataList);
            variance = getVariance(dataList, mean);
            stdDev = Math.sqrt(variance);
            min = dataList.get(0);
            max = dataList.get(dataList.size() - 1);
            sdtErr80Interval = getConfidenceInterval(1.282d, getVariance());
        }
    }

    protected static Double getArithmeticMean(List<Double> dataSet) {
        double avg = 0.0d;
        for (Double data : dataSet) {
            avg += data;
        }
        avg = avg / dataSet.size();
        return avg;
    }

    protected static Double getVariance(List<Double> dataSet, Double avg) {
        double variance =0;
        if(dataSet.size() > 1) {
            double varTemp = 0.0d;
            for (Double data : dataSet) {
                varTemp += Math.pow(data - avg, 2);
            }
            variance = varTemp / (dataSet.size() - 1);
        }
        return variance;
    }

    protected static double getMedian(List<Double> dataSet) {
        List<Double> array = new ArrayList<Double>(dataSet);
        int middle = array.size()  / 2;
        double mean=0;
        if(array.isEmpty()) {
            return mean;
        } else if (array.size() % 2 == 0) {
            double left = array.get(middle - 1);
            double right = array.get(middle);
            mean = (left + right) / 2d;
        } else {
            mean = array.get(middle);
        }
        return mean;
    }

    protected static Double getMode(List<Double> dataList) {
        Map<Double,Integer> freqMap = new HashMap<Double, Integer>();
        for (Double data : dataList) {
            if(!freqMap.containsKey(data)) {
                freqMap.put(data,0);
            }
            freqMap.put(data,freqMap.get(data)+1);
        }

        int max = 0;
        double mode=0;
        for (Double data : freqMap.keySet()) {
            if(freqMap.get(data) > max) {
                mode = data;
                max = freqMap.get(data);
            }
        }
        return mode;
    }

    protected static Double getConfidenceInterval(double confidenceLevel, double variance) {
        double stddev = Math.sqrt(variance);
        return confidenceLevel * stddev;
    }

    /* ******************************************************************************* */

    public Double getMean() {
        return mean;
    }

    public void setMean(Double mean) {
        this.mean = mean;
    }

    public Double getMedian() {
        return median;
    }

    public void setMedian(Double median) {
        this.median = median;
    }

    public Double getMode() {
        return mode;
    }

    public void setMode(Double mode) {
        this.mode = mode;
    }

    public Double getVariance() {
        return variance;
    }

    public void setVariance(Double variance) {
        this.variance = variance;
    }

    public Double getStdDev() {
        return stdDev;
    }

    public void setStdDev(Double stdDev) {
        this.stdDev = stdDev;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Integer getDataSize() {
        return dataSize;
    }

    public void setDataSize(Integer dataSize) {
        this.dataSize = dataSize;
    }

    public Double getSdtErr80Interval() {
        return sdtErr80Interval;
    }

    public void setSdtErr80Interval(Double sdtErr80Interval) {
        this.sdtErr80Interval = sdtErr80Interval;
    }
}

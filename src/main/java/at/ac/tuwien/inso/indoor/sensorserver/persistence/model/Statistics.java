package at.ac.tuwien.inso.indoor.sensorserver.persistence.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by PatrickF on 18.09.2014.
 */
public class Statistics extends SimpleStatistics {
    private Double geometricMean = 0d;
    private Double skewness = 0d;
    private Double kurtosis = 0d;
    private Double sum = 0d;
    private Double sdtErr60Interval = 0d;
    private Double sdtErr70Interval = 0d;
    private Double sdtErr90Interval = 0d;
    private Double sdtErr95Interval = 0d;
    private Double sdtErr99Interval = 0d;
    private List<Integer> frequencyDistribution = new ArrayList<Integer>();

    public Statistics() {
    }

    public Statistics(List<Double> dataList) {
        super(dataList);
        if (!dataList.isEmpty()) {
            Collections.sort(dataList);
            sum = getSum(dataList);
            geometricMean = getGeometricMean(dataList);
            skewness = getSkewness(dataList, getMean(), getStdDev());
            kurtosis = getKurtosis(dataList, getMean(), getStdDev());
            sdtErr60Interval = getConfidenceInterval(0.842d, getVariance());
            sdtErr70Interval = getConfidenceInterval(1.036d, getVariance());
            sdtErr90Interval = getConfidenceInterval(1.645d, getVariance());
            sdtErr95Interval = getConfidenceInterval(1.96d, getVariance());
            sdtErr99Interval = getConfidenceInterval(2.576d, getVariance());
            frequencyDistribution = getDistributionList(dataList, (int) Math.floor(getMin()), (int) Math.ceil(getMax()));
        }
    }

    protected static Double getSum(List<Double> dataSet) {
        double sum = 0.0d;
        for (Double data : dataSet) {
            sum += data;
        }
        return sum;
    }

    protected static Double getGeometricMean(List<Double> dataSet) {
        double avg = 1.0d;
        for (Double data : dataSet) {
            avg *= data;
        }
        avg = Math.pow(avg, 1.0d / (double) dataSet.size());
        return avg;
    }

    protected static Double getSkewness(List<Double> dataList, Double mean, Double stdDev) {
        double skew = 0.0d;
        for (Double data : dataList) {
            skew += Math.pow(data - mean, 3);
        }
        return skew / ((dataList.size() - 1) * Math.pow(stdDev, 3));
    }

    protected static Double getKurtosis(List<Double> dataList, Double mean, Double stdDev) {
        double kurt = 0.0d;
        for (Double data : dataList) {
            kurt += Math.pow(data - mean, 4);
        }
        return (kurt / ((dataList.size() - 1) * Math.pow(stdDev, 4)));
    }

    private static List<Integer> getDistributionList(List<Double> dataList, int min, int max) {
        List<Integer> frequency = new ArrayList<Integer>(1);

        if (min == max) {
            frequency.add(0, dataList.size());
        } else {
            int count = Math.abs(max - min) + 1;

            frequency = new ArrayList<Integer>(count);

            for (int i = 0; i < count; i++) {
                frequency.add(i, 0);
            }

            for (Double data : dataList) {
                int pos = (int) Math.round(Math.abs(data)) - Math.abs(max);
                frequency.set(pos, frequency.get(pos) + 1);
            }
            Collections.reverse(frequency);
        }
        return frequency;
    }

    /* ******************************************************************************* */

    public Double getGeometricMean() {
        return geometricMean;
    }

    public void setGeometricMean(Double geometricMean) {
        this.geometricMean = geometricMean;
    }

    public Double getSkewness() {
        return skewness;
    }

    public void setSkewness(Double skewness) {
        this.skewness = skewness;
    }

    public Double getKurtosis() {
        return kurtosis;
    }

    public void setKurtosis(Double kurtosis) {
        this.kurtosis = kurtosis;
    }

    public Double getSum() {
        return sum;
    }

    public void setSum(Double sum) {
        this.sum = sum;
    }

    public Double getSdtErr60Interval() {
        return sdtErr60Interval;
    }

    public void setSdtErr60Interval(Double sdtErr60Interval) {
        this.sdtErr60Interval = sdtErr60Interval;
    }

    public Double getSdtErr70Interval() {
        return sdtErr70Interval;
    }

    public void setSdtErr70Interval(Double sdtErr70Interval) {
        this.sdtErr70Interval = sdtErr70Interval;
    }

    public Double getSdtErr90Interval() {
        return sdtErr90Interval;
    }

    public void setSdtErr90Interval(Double sdtErr90Interval) {
        this.sdtErr90Interval = sdtErr90Interval;
    }

    public Double getSdtErr95Interval() {
        return sdtErr95Interval;
    }

    public void setSdtErr95Interval(Double sdtErr95Interval) {
        this.sdtErr95Interval = sdtErr95Interval;
    }

    public Double getSdtErr99Interval() {
        return sdtErr99Interval;
    }

    public void setSdtErr99Interval(Double sdtErr99Interval) {
        this.sdtErr99Interval = sdtErr99Interval;
    }

    public List<Integer> getFrequencyDistribution() {
        return frequencyDistribution;
    }

    public void setFrequencyDistribution(List<Integer> frequencyDistribution) {
        this.frequencyDistribution = frequencyDistribution;
    }
}

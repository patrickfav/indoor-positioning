package at.ac.tuwien.inso.indoor.sensorserver.services.scheduler;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.*;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.comparator.AverageScanMeasurementConfidenceComparator;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.AverageWlanScanMeasurement;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.Survey;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.WlanScanNode;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNode;
import at.ac.tuwien.inso.indoor.sensorserver.services.requests.RouterScanRequest;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by PatrickF on 14.09.2014.
 */
public class SurveyCallable implements Callable<Survey>{
    protected static Logger log = Logger.getLogger(SurveyCallable.class);

    private static final int MAX_REPEAT_COUNT = 100;
    private static final int MIN_DELAY = 100;

    private long delayMs;
    private int repeatCount;
    private String adapterName;
    private SensorNode node;
    private Callback<Survey> resultsCallback;
    private Callback<Double> progressCallback;

    private double currentRepeatCycle = 0;

    public SurveyCallable(long delayMs, int repeatCount, String adapterName, SensorNode node) {
        this.delayMs = delayMs;
        this.repeatCount = repeatCount;
        this.adapterName = adapterName;
        this.node = node;

        if(this.repeatCount > MAX_REPEAT_COUNT) {
            this.repeatCount = MAX_REPEAT_COUNT;
            log.warn("Repeat count too high, reduce it to "+MAX_REPEAT_COUNT);
        }

        if(this.delayMs < MIN_DELAY) {
            this.delayMs = MIN_DELAY;
            log.warn("Delay too low, setting it to "+MIN_DELAY+"ms");
        }
    }

    public SurveyCallable(long delayMs, int repeatCount, String adapterName, SensorNode node, Callback<Survey> resultCallback, Callback<Double> progressCallback) {
        this(delayMs,repeatCount,adapterName,node);
        this.resultsCallback = resultCallback;
        this.progressCallback = progressCallback;
    }

    @Override
    public Survey call() throws Exception {
        return survey();
    }

    private Survey survey() throws Exception {
        //randomize start
        Thread.sleep((long) new Random().nextInt(150));

        Survey survey = new Survey();
        List<List<WlanScanNode>> scans = new ArrayList<List<WlanScanNode>>();

        for(int i=0;i<repeatCount;i++) {
            scans.add(scan(node,adapterName));
            currentRepeatCycle+=0.5;
            updateProgress();

            Thread.sleep(delayMs);
            currentRepeatCycle+=0.5;
            updateProgress();
        }

        survey.setAdapter(adapterName);
        survey.setNodeId(node.getNodeId());
        survey.setNetworkId(node.getNetworkId());
        survey.setAverageScanNodes(getAvgScanNodes(scans,repeatCount));

        if(resultsCallback != null) {
            resultsCallback.callback(survey);
        }

        return survey;
    }

    private void updateProgress() {
        if(progressCallback != null) {
            progressCallback.callback(Math.min(0.99d, getCurrentProgress()));
        }
    }

    protected List<WlanScanNode> scan(SensorNode node,String adapterName) throws Exception{
        return new RouterScanRequest(node, adapterName).startRequest();
    }

    private List<AverageWlanScanMeasurement> getAvgScanNodes(List<List<WlanScanNode>> nodes,int measureCount) {
        Map<String,List<WlanScanNode>> scanMap = new HashMap<String, List<WlanScanNode>>();
        List<AverageWlanScanMeasurement> avgList = new ArrayList<AverageWlanScanMeasurement>();

        for (List<WlanScanNode> nodeList : nodes) {
            for (WlanScanNode wlanScanNode : nodeList) {
                String id = wlanScanNode.getMacAddress()+"-"+ wlanScanNode.getSsid();

                if(!scanMap.containsKey(id)) {
                    scanMap.put(id,new ArrayList<WlanScanNode>());
                }

                scanMap.get(id).add(wlanScanNode);
            }
        }

        for (String id : scanMap.keySet()) {
            List<WlanScanNode> measurements = scanMap.get(id);
            Collections.sort(measurements);

            List<Double> signalStrengthList = new ArrayList<Double>();

            for (WlanScanNode measurement : measurements) {
                signalStrengthList.add(measurement.getSignalStrengthDbm());
            }

            AverageWlanScanMeasurement avgMeasurement = new AverageWlanScanMeasurement(measurements.get(0));
            avgMeasurement.setStatistics(new Statistics(signalStrengthList));
            avgMeasurement.setConfidence(getConfidence(measurements, measureCount, avgMeasurement.getStatistics().getVariance(), avgMeasurement.getStatistics().getMean()));
            avgMeasurement.setSignalStrengths(signalStrengthList);
            avgList.add(avgMeasurement);

            log.debug(id + " has " + measurements.size() + " of " + measureCount + " valid measurements: Avg Signal Strength: "
                    + avgMeasurement.getStatistics().getMean() + "; Var: " + avgMeasurement.getStatistics().getVariance() + "; confidence: " + (avgMeasurement.getConfidence() * 100) + "%");

        }

        Collections.sort(avgList,new AverageScanMeasurementConfidenceComparator());

        return avgList;
    }

    private Double getConfidence(List<WlanScanNode> measurements,int measureCount,double variance, double avg) {
        double confidence = 0;
        if(measureCount > 0) {
            confidence = (double)measurements.size()/(double)measureCount;
        }
        double variancePercent = Math.abs(avg) * variance / 10000;
        return confidence - (variancePercent * 2);
    }

    public long getEstimatedFullRuntime() {
        return repeatCount * delayMs + repeatCount * 2000 + 500;
    }

    public double getCurrentProgress() {
        return currentRepeatCycle / repeatCount;
    }

}

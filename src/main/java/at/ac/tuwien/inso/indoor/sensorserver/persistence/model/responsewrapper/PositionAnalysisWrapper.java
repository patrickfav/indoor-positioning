package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.responsewrapper;

import at.ac.tuwien.inso.indoor.sensorserver.math.positioning.IPositionAlgorithm;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.Analysis;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.EFrequencyRange;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNetwork;

/**
 * Created by PatrickF on 17.02.2015.
 */
public class PositionAnalysisWrapper {
    private Analysis analysis;
    private SensorNetwork network;
    private IPositionAlgorithm.PositionData probablePositions;
    private EFrequencyRange freq;

    public SensorNetwork getNetwork() {
        return network;
    }

    public void setNetwork(SensorNetwork network) {
        this.network = network;
    }

    public Analysis getAnalysis() {
        return analysis;
    }

    public void setAnalysis(Analysis analysis) {
        this.analysis = analysis;
    }

    public IPositionAlgorithm.PositionData getProbablePositions() {
        return probablePositions;
    }

    public void setProbablePositions(IPositionAlgorithm.PositionData probablePositions) {
        this.probablePositions = probablePositions;
    }

    public EFrequencyRange getFreq() {
        return freq;
    }

    public void setFreq(EFrequencyRange freq) {
        this.freq = freq;
    }
}

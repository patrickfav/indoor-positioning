package at.ac.tuwien.inso.indoor.sensorserver.math.positioning;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.SimpleMeasurement;
import at.ac.tuwien.inso.indoor.sensorserver.services.positioner.RSSMatrixCreator;

import java.util.List;
import java.util.Map;

/**
 * Created by PatrickF on 11.11.2014.
 */
public class BayesRuleDetermination implements IPositionAlgorithm {
    @Override
    public PositionData getMostLikelyPositions(Map<String, SimpleMeasurement> measurements, List<RSSMatrixCreator.RSSPoint> referencePoints, double multiplicator) {
        return null;
    }
}

package at.ac.tuwien.inso.indoor.sensorserver.math.positioning;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.SignalMap;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.SimpleMeasurement;
import at.ac.tuwien.inso.indoor.sensorserver.services.positioner.RSSMatrixCreator;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Manhattan/Euclidean distance determination is a simple
 * choice for RSS fingerprinting that measures the distance
 * between an online RSS value and the offline training
 * databaseRSS records
 */
public class ManhattanDistanceDetermination implements IPositionAlgorithm {
    private static Logger log = Logger.getLogger(ManhattanDistanceDetermination.class);
	private static final int MIN_REQUIRED_MEASUREMENTS = 2;
    private SignalMap signalMap;

    public ManhattanDistanceDetermination(SignalMap signalMap) {
        this.signalMap = signalMap;
    }

    @Override
    public PositionData getMostLikelyPositions(Map<String, SimpleMeasurement> measurements,
                                                         List<RSSMatrixCreator.RSSPoint> referencePoints, double multiplicator) {
        List<ProbablePosition> positions = new ArrayList<ProbablePosition>();
        for (RSSMatrixCreator.RSSPoint referencePoint : referencePoints) {
	        if(referencePoint.getPathLossMap().entrySet().size() > MIN_REQUIRED_MEASUREMENTS) {
		        double offset = 1000;

		        for(Map.Entry<String, SimpleMeasurement> measurement: measurements.entrySet()) {
			        if(measurement.getValue().getStatistics().getMean() > -60) {
				        if (referencePoint.getPathLossMap().containsKey(measurement.getKey())) {
					        offset += Math.pow(Math.abs(measurement.getValue().getStatistics().getMean() * multiplicator) - Math.abs(referencePoint.getPathLossMap().get(measurement.getKey())), 2);
				        } else {
					        offset += Math.pow(100 - Math.abs(measurement.getValue().getStatistics().getMean() * multiplicator), 2);
				        }
			        }
		        }

		        positions.add(new ProbablePosition(referencePoint.getX(), referencePoint.getY(), signalMap.getTileLengthCm(), offset));
	        }
        }

	    PositionData data = new PositionData();
	    data.setBestPositions(findWithBestOffset(positions));
	    data.setGoodPositions(findWithBestPercentageOffset(positions, 4));
        return data;
    }

	private List<ProbablePosition> findWithBestOffset(List<ProbablePosition> positions) {
        List<ProbablePosition> bestPositions = new ArrayList<ProbablePosition>();

        double lowestOffset = Double.MAX_VALUE;
        for (ProbablePosition position : positions) {
            if (position.getProbabilityValue() < lowestOffset) {
                lowestOffset = position.getProbabilityValue();
            }
        }

        for (ProbablePosition position : positions) {
            if (position.getProbabilityValue() <= lowestOffset) {
                bestPositions.add(position);
            }
        }

        log.debug(bestPositions.size()+" positions found with best offset "+lowestOffset);

        return bestPositions;
    }
	private List<ProbablePosition> findWithBestPercentageOffset(List<ProbablePosition> positions,double percenatage) {
		List<ProbablePosition> bestPositions = new ArrayList<ProbablePosition>();

		double lowestOffset = Double.MAX_VALUE;
		for (ProbablePosition position : positions) {
			if (position.getProbabilityValue() < lowestOffset) {
				lowestOffset = position.getProbabilityValue();
			}
		}

		double lowestPlusXPercent;
		do {
			bestPositions.clear();
			lowestPlusXPercent = lowestOffset + (lowestOffset * percenatage / 100);

			for (ProbablePosition position : positions) {
				if (position.getProbabilityValue() <= lowestPlusXPercent) {
					bestPositions.add(position);
				}
			}
			percenatage /= 2;
		} while (bestPositions.size() > 100);

		log.debug(bestPositions.size()+" positions found with good offset in "+percenatage+"% range. Lowest offset was "+lowestOffset+" percentage tolerance up to "+lowestPlusXPercent+".");

		return bestPositions;
	}
}

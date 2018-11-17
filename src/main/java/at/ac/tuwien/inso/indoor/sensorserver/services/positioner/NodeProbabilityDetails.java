package at.ac.tuwien.inso.indoor.sensorserver.services.positioner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by PatrickF on 31.10.2014.
 */
public class NodeProbabilityDetails {
    private NodeProbabilityPoint selectedPoint;
    private double maxProbability;
    private List<NodeProbabilityPoint> allPossiblePoints = new ArrayList<NodeProbabilityPoint>();

    public NodeProbabilityPoint getSelectedPoint() {
        return selectedPoint;
    }

    public void setSelectedPoint(NodeProbabilityPoint selectedPoint) {
        this.selectedPoint = selectedPoint;
    }

    public double getMaxProbability() {
        return maxProbability;
    }

    public void setMaxProbability(double maxProbability) {
        this.maxProbability = maxProbability;
    }

    public List<NodeProbabilityPoint> getAllPossiblePoints() {
        return allPossiblePoints;
    }

    public void setAllPossiblePoints(List<NodeProbabilityPoint> allPossiblePoints) {
        this.allPossiblePoints = allPossiblePoints;
    }
}

package at.ac.tuwien.inso.indoor.sensorserver.math.positioning;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.SimpleMeasurement;
import at.ac.tuwien.inso.indoor.sensorserver.services.positioner.RSSMatrixCreator;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by PatrickF on 11.11.2014.
 */
public interface IPositionAlgorithm {

    PositionData getMostLikelyPositions(Map<String, SimpleMeasurement> measurements, List<RSSMatrixCreator.RSSPoint> referencePoints, double multiplicator);

    enum Type {

    }

    class ProbablePosition {
        private int x;
        private int y;
        private int tileLengthCm;
        private double probabilityValue;

        public ProbablePosition(int x, int y, int tileLengthCm, double probabilityValue) {
            this.x = x;
            this.y = y;
            this.tileLengthCm = tileLengthCm;
            this.probabilityValue = probabilityValue;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getTileLengthCm() {
            return tileLengthCm;
        }

        public void setTileLengthCm(int tileLengthCm) {
            this.tileLengthCm = tileLengthCm;
        }

        public double getProbabilityValue() {
            return probabilityValue;
        }

        public void setProbabilityValue(double probabilityValue) {
            this.probabilityValue = probabilityValue;
        }
    }

    class PositionData {
        private List<ProbablePosition> bestPositions;
        private List<ProbablePosition> goodPositions;
        private Date positionTime;

        public PositionData() {
            positionTime = new Date();
        }

        public List<ProbablePosition> getBestPositions() {
            return bestPositions;
        }

        public void setBestPositions(List<ProbablePosition> bestPositions) {
            this.bestPositions = bestPositions;
        }

        public List<ProbablePosition> getGoodPositions() {
            return goodPositions;
        }

        public void setGoodPositions(List<ProbablePosition> goodPositions) {
            this.goodPositions = goodPositions;
        }

        public Date getPositionTime() {
            return positionTime;
        }

        public void setPositionTime(Date positionTime) {
            this.positionTime = positionTime;
        }
    }
}

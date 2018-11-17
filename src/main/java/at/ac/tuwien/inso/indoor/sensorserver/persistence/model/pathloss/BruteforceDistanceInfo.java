package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.pathloss;

/**
 * Created by PatrickF on 02.11.2014.
 */
public class BruteforceDistanceInfo {
    private double dBmMean;
    private double targetDistanceMeter;

    public double getdBmMean() {
        return dBmMean;
    }

    public void setdBmMean(double dBmMean) {
        this.dBmMean = dBmMean;
    }

    public double getTargetDistanceMeter() {
        return targetDistanceMeter;
    }

    public void setTargetDistanceMeter(double targetDistanceMeter) {
        this.targetDistanceMeter = targetDistanceMeter;
    }

    public double getOffset(double calculatedDistanceMeter) {
        return calculatedDistanceMeter - targetDistanceMeter;
    }

    public double getPathLoss() {
        return Math.abs(dBmMean);
    }
}

package at.ac.tuwien.inso.indoor.sensorserver.math.radiomodels;

/**
 * Created by PatrickF on 03.10.2014.
 */
public class LogDistancePathLossModel implements IRadioPropagationModel {
    @Override
    public double getPathLossDb(double distanceMeter, double frequencyHz, int roomsBetween) {
        return 0;
    }

    @Override
    public double getDistanceInMeter(double pathLossDb, double frequencyHz, int roomsBetween) {
        return 0;
    }
}

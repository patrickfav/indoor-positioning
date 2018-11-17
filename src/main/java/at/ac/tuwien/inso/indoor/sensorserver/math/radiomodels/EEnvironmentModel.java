package at.ac.tuwien.inso.indoor.sensorserver.math.radiomodels;

/**
 * Created by PatrickF on 03.10.2014.
 */
public enum EEnvironmentModel {
    FREE_SPACE(2, 10),
    INDOOR_LINE_OF_SIGHT(1.8, 20),
    INDOOR_OBSTRUCTED_RESIDENTIAL(4, 28),
    INDOOR_OBSTRUCTED_OFFICE(5, 30),
    INDOOR_OBSTRUCTED_COMMERCIAL(4, 22);

    private final double friisPathLossExp;
    private final double iTUdistancePowerLossCoefficient;

    EEnvironmentModel(double friisPathLossExp, double iTUdistancePowerLossCoefficient) {
        this.friisPathLossExp = friisPathLossExp;
        this.iTUdistancePowerLossCoefficient = iTUdistancePowerLossCoefficient;
    }

    public double getFriisPathLossExp() {
        return friisPathLossExp;
    }

    public double getiTUdistancePowerLossCoefficient() {
        return iTUdistancePowerLossCoefficient;
    }
}

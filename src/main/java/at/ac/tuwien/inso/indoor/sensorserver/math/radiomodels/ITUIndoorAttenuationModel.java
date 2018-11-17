package at.ac.tuwien.inso.indoor.sensorserver.math.radiomodels;

import at.ac.tuwien.inso.indoor.sensorserver.util.RadioUtil;

/**
 * The ITU indoor propagation model, also known as ITU model for indoor attenuation, is a radio propagation model
 * that estimates the path loss inside a room or a closed area inside a building delimited by walls of any form.
 * Suitable for appliances designed for indoor use, this model approximates the total path loss an indoor link may
 * experience.
 * <p>
 * Calculation of distance power loss coefficient
 * <p>
 * Frequency band Residential area Office area Commercial area
 * 900 MHz N/A 33 20
 * 1.2 GHz N/A 32 22
 * 1.3 GHz N/A 32 22
 * 1.8 GHz 28 30 22
 * 4 GHz N/A 28 22
 * 5.2 GHz N/A 31 N/A
 * 60 GHz N/A 22 17
 * <p>
 * Calculation of floor penetration loss factor
 * <p>
 * <p>
 * Frequency band Number of floors Residential area Office area Commercial area
 * 900 MHz 1 N/A 9 N/A
 * 900 MHz 2 N/A 19 N/A
 * 900 MHz 3 N/A 24 N/A
 * 1.8 GHz n 4n 15+4(n-1) 6 + 3(n-1)
 * 2.0 GHz n 4n 15+4(n-1) 6 + 3(n-1)
 * 5.2 GHz 1 N/A 16 N/A
 * <p>
 * https://en.wikipedia.org/wiki/ITU_model_for_indoor_attenuation#cite_ref-ITU_2_1-0
 */
public class ITUIndoorAttenuationModel implements IRadioPropagationModel {

    private static final double CONSTANT = 28d;

    private EEnvironmentModel environmentModel;

    public ITUIndoorAttenuationModel(EEnvironmentModel environmentModel) {
        this.environmentModel = environmentModel;
    }

    public ITUIndoorAttenuationModel() {
    }

    @Override
    public double getPathLossDb(double distanceMeter, double frequencyHz, int roomsBetween) {
        double n_coefficient = getDistancePowerLossCoefficient(frequencyHz, environmentModel);
        return 20 * Math.log10(RadioUtil.convertToMhz(frequencyHz)) + n_coefficient * Math.log10(distanceMeter) + getFloorLossPenetrationFactor(roomsBetween, frequencyHz, environmentModel) - CONSTANT;
    }

    @Override
    public double getDistanceInMeter(double pathLossDb, double frequencyHz, int roomsBetween) {
        double n_coefficient = getDistancePowerLossCoefficient(frequencyHz, environmentModel);
        return Math.pow(RadioUtil.convertToMhz(frequencyHz), (-20.0) / n_coefficient) * Math.pow(10.0, (pathLossDb - getFloorLossPenetrationFactor(roomsBetween, frequencyHz, environmentModel) + CONSTANT) / n_coefficient);
    }

    protected static double getDistancePowerLossCoefficient(double frequencyHz, EEnvironmentModel environmentModel) {
        return environmentModel.getiTUdistancePowerLossCoefficient();
    }

    protected static int getFloorLossPenetrationFactor(int floorsCount, double frequencyHz, EEnvironmentModel environmentModel) {
        if (floorsCount <= 0)
            return 0;

        if (frequencyHz > 5E12 && frequencyHz < 6E12 && floorsCount == 1) {
            switch (environmentModel) {
                case INDOOR_OBSTRUCTED_OFFICE:
                    return 16;
            }
        }

        switch (environmentModel) {
            case INDOOR_OBSTRUCTED_RESIDENTIAL:
                return 4 * floorsCount;
            case INDOOR_OBSTRUCTED_OFFICE:
                return 15 + 4 * (floorsCount - 1);
            case INDOOR_OBSTRUCTED_COMMERCIAL:
                return 6 + 3 * (floorsCount - 1);
        }

        return 4 * floorsCount;
    }

    protected void setEnvironmentModel(EEnvironmentModel environmentModel) {
        this.environmentModel = environmentModel;
    }
}

package at.ac.tuwien.inso.indoor.sensorserver.math.radiomodels;

import at.ac.tuwien.inso.indoor.sensorserver.util.ServerUtil;

/**
 * Based on the ITUIndoorModel this is a configurable version of that model.
 * The following parameter can be set:
 * - bound
 * - fac
 * - offset
 * <p>
 * Together bound and fac create kind of a axis in the function, so that every
 * output with a path loss higher than bound will be multiplied by fac. Offset
 * is just a simple addition after the function.
 */
public class ITUIndoorModelDegradingDist extends ITUIndoorAttenuationModel {
    private double bound;
    private double fac;
    private double offsetM;

    public ITUIndoorModelDegradingDist(EEnvironmentModel environmentModel, ITUDegradingDistConfig config) {
        this(environmentModel, config.getBound(), config.getFac(), config.getOffsetM());
    }

    public ITUIndoorModelDegradingDist(EEnvironmentModel environmentModel, double bound, double fac, double offsetM) {
        super(environmentModel);
        this.bound = bound;
        this.fac = fac;
        this.offsetM = offsetM;
    }

    @Override
    public double getPathLossDb(double distanceMeter, double frequencyHz, int roomsBetween) {
        return enhancePathLoss(super.getPathLossDb(distanceMeter - offsetM, frequencyHz, roomsBetween));
    }

    @Override
    public double getDistanceInMeter(double pathLossDb, double frequencyHz, int roomsBetween) {
        return super.getDistanceInMeter(degradePathLoss(pathLossDb), frequencyHz, roomsBetween) + offsetM;
    }

    private double degradePathLoss(double pathLoss) {
        if (pathLoss > bound) {
            return bound + ((pathLoss - bound) * fac);
        }
        return pathLoss;
    }

    private double enhancePathLoss(double pathLoss) {
        if (pathLoss > bound) {
            return bound + ((pathLoss - bound) / fac);
        }
        return pathLoss;
    }

    public void setConfig(ITUDegradingDistConfig config) {
        this.bound = config.getBound();
        this.fac = config.getFac();
        this.offsetM = config.getOffsetM();
    }

    @Override
    public String toString() {
        return "ITUIndoorModelDegradingDist{" +
                "bound=" + bound +
                ", fac=" + fac +
                ", offsetM=" + offsetM +
                '}';
    }

    /**
     * Wraps the config params of the model
     */
    public static class ITUDegradingDistConfig {
        private double bound = 60;
        private double fac = 0.2;
        private double offsetM = 0;

        public ITUDegradingDistConfig() {
        }

        public ITUDegradingDistConfig(double bound, double fac, double offsetM) {
            this.bound = bound;
            this.fac = fac;
            this.offsetM = offsetM;
        }

        public double getBound() {
            return bound;
        }

        public void setBound(double bound) {
            this.bound = bound;
        }

        public double getFac() {
            return fac;
        }

        public void setFac(double fac) {
            this.fac = fac;
        }

        public double getOffsetM() {
            return offsetM;
        }

        public void setOffsetM(double offsetM) {
            this.offsetM = offsetM;
        }

        public void roundNumbers(int scale) {
            bound = ServerUtil.round(bound, scale);
            fac = ServerUtil.round(fac, scale);
            offsetM = ServerUtil.round(offsetM, scale);
        }

        @Override
        public String toString() {
            return "ITUDegradingDistConfig{" +
                    "bound=" + bound +
                    ", fac=" + fac +
                    ", offsetM=" + offsetM +
                    '}';
        }
    }
}

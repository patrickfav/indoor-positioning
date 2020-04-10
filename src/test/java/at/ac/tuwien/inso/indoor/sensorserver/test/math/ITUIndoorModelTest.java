package at.ac.tuwien.inso.indoor.sensorserver.test.math;

import at.ac.tuwien.inso.indoor.sensorserver.math.radiomodels.EEnvironmentModel;
import at.ac.tuwien.inso.indoor.sensorserver.math.radiomodels.ITUIndoorAttenuationModel;
import at.ac.tuwien.inso.indoor.sensorserver.math.radiomodels.ITUIndoorModelDegradingDist;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.EFrequencyRange;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by PatrickF on 10.11.2014.
 */
public class ITUIndoorModelTest {
    private static Logger log = LogManager.getLogger(ITUIndoorModelTest.class);
    private static final double delta = 1e-10;
    private static final double freq_24ghz = EFrequencyRange.frequencyHz(EFrequencyRange.WLAN_2_4Ghz, 1);

    @Test
    public void testNormalITUAlgoShouldHaveSameValuesBothWays() {
        for (EEnvironmentModel model : EEnvironmentModel.values()) {
            ITUIndoorAttenuationModel ituModel = new ITUIndoorAttenuationModel(model);
            //log.info("ITUIndoorModelDegradingDist: " + ituModel);

            Map<Double, Double> pathLossDistMap = new HashMap<Double, Double>();
            for (int i = 0; i < 150; i++) {
                pathLossDistMap.put((double) i, ituModel.getDistanceInMeter(i, freq_24ghz, 0));
            }

            for (Map.Entry<Double, Double> entry : pathLossDistMap.entrySet()) {
                double pathLoss = ituModel.getPathLossDb(entry.getValue(), freq_24ghz, 0);
                //log.info("For Distance "+entry.getValue()+" the pathloss is "+pathLoss+" - reference value is "+entry.getKey());
                Assert.assertEquals("Calculated Pathloss vs given", entry.getKey(), pathLoss, delta);

            }
        }
    }

    @Test
    public void testDegradingDistAlgoShouldHaveSameValuesBothWays() {
        ITUIndoorModelDegradingDist modelDegradingDist = null;
        for (int bound = 50; bound < 70; bound++) {
            for (double fac = 0.1; fac < 1; fac += 0.1) {
                for (double offset = -0.5; offset < 1; offset += 0.1) {
                    modelDegradingDist = new ITUIndoorModelDegradingDist(EEnvironmentModel.INDOOR_OBSTRUCTED_OFFICE, bound, fac, offset);
                    //log.info("ITUIndoorModelDegradingDist: " + modelDegradingDist);

                    Map<Double, Double> pathLossDistMap = new HashMap<Double, Double>();
                    for (int i = 35; i < 90; i++) {
                        pathLossDistMap.put((double) i, modelDegradingDist.getDistanceInMeter(i, freq_24ghz, 0));
                    }

                    for (Map.Entry<Double, Double> entry : pathLossDistMap.entrySet()) {
                        double pathLoss = modelDegradingDist.getPathLossDb(entry.getValue(), freq_24ghz, 0);
                        //log.info("For Distance "+entry.getValue()+" the pathloss is "+pathLoss+" - reference value is "+entry.getKey());
                        Assert.assertEquals("Calculated Pathloss vs given", entry.getKey(), pathLoss, delta);

                    }
                }
            }
        }
    }

    @Test
    public void testNormalITUAlgoShouldNotHaveSameValues() {
        ITUIndoorAttenuationModel ituModel = new ITUIndoorAttenuationModel(EEnvironmentModel.INDOOR_OBSTRUCTED_OFFICE);

        for (int i = 0; i < 150; i++) {
            double dist1 = ituModel.getDistanceInMeter(i, freq_24ghz, 0);
            double dist2 = ituModel.getDistanceInMeter(i + 1, freq_24ghz, 0);

            Assert.assertNotEquals("Different inputs should have different output", dist1, dist2, delta);

            double pathLoss1 = ituModel.getPathLossDb(dist1, freq_24ghz, 0);
            double pathLoss2 = ituModel.getPathLossDb(dist2, freq_24ghz, 0);

            Assert.assertNotEquals("Different inputs should have different output", pathLoss1, pathLoss2, delta);
        }
    }
}

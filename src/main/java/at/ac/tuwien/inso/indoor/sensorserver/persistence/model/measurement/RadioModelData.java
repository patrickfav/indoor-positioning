package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement;

import at.ac.tuwien.inso.indoor.sensorserver.math.radiomodels.EEnvironmentModel;
import at.ac.tuwien.inso.indoor.sensorserver.math.radiomodels.FreeSpacePathLoss;
import at.ac.tuwien.inso.indoor.sensorserver.math.radiomodels.IRadioPropagationModel;
import at.ac.tuwien.inso.indoor.sensorserver.math.radiomodels.ITUIndoorModelDegradingDist;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.SimpleStatistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by PatrickF on 30.09.2014.
 */
public class RadioModelData {
    private Map<String, DistanceData> distanceMap = new HashMap<String, DistanceData>();

    public RadioModelData(SimpleStatistics statistics, EFrequencyRange frequencyRange, int channel, EEnvironmentModel environmentModel, String receiverRoomId, String apRoomId, double multi, ITUIndoorModelDegradingDist.ITUDegradingDistConfig config) {
        IRadioPropagationModel ituDegradingModel = new ITUIndoorModelDegradingDist(environmentModel, config);
        IRadioPropagationModel friis = new FreeSpacePathLoss();

        double hz = EFrequencyRange.frequencyHz(frequencyRange, channel);
//        int roomsBetween = calculateRoomsInBetween(receiverRoomId, apRoomId, statistics.getMean());
        int roomsBetween = 0;
        List<IRadioPropagationModel> modelList = new ArrayList<IRadioPropagationModel>();
        modelList.add(friis);
        modelList.add(ituDegradingModel);

        for (IRadioPropagationModel iRadioPropagationModel : modelList) {
            DistanceData data = new DistanceData();
            data.setRoomsBetween(roomsBetween);

            data.setMeanDistance(iRadioPropagationModel.getDistanceInMeter(Math.abs(statistics.getMean()), hz, roomsBetween));
            data.setMedianDistance(iRadioPropagationModel.getDistanceInMeter(Math.abs(statistics.getMedian()), hz, roomsBetween));
            data.setModeDistance(iRadioPropagationModel.getDistanceInMeter(Math.abs(statistics.getMode()), hz, roomsBetween));

            data.setMultMeanDistance(iRadioPropagationModel.getDistanceInMeter(Math.abs(statistics.getMean()) * multi, hz, roomsBetween));
            data.setMultMedianDistance(iRadioPropagationModel.getDistanceInMeter(Math.abs(statistics.getMedian()) * multi, hz, roomsBetween));
            data.setMultModeDistance(iRadioPropagationModel.getDistanceInMeter(Math.abs(statistics.getMode()) * multi, hz, roomsBetween));

            distanceMap.put(iRadioPropagationModel.getClass().getSimpleName(), data);
        }
    }

    public RadioModelData() {
    }

    public Map<String, DistanceData> getDistanceMap() {
        return distanceMap;
    }

    public void setDistanceMap(Map<String, DistanceData> distanceMap) {
        this.distanceMap = distanceMap;
    }

    public static class DistanceData {
        private int roomsBetween;
        private double modeDistance;
        private double medianDistance;
        private double meanDistance;

        private double multModeDistance;
        private double multMedianDistance;
        private double multMeanDistance;

        public double getMultModeDistance() {
            return multModeDistance;
        }

        public void setMultModeDistance(double multModeDistance) {
            this.multModeDistance = multModeDistance;
        }

        public double getMultMedianDistance() {
            return multMedianDistance;
        }

        public void setMultMedianDistance(double multMedianDistance) {
            this.multMedianDistance = multMedianDistance;
        }

        public double getMultMeanDistance() {
            return multMeanDistance;
        }

        public void setMultMeanDistance(double multMeanDistance) {
            this.multMeanDistance = multMeanDistance;
        }

        public int getRoomsBetween() {
            return roomsBetween;
        }

        public void setRoomsBetween(int roomsBetween) {
            this.roomsBetween = roomsBetween;
        }

        public double getModeDistance() {
            return modeDistance;
        }

        public void setModeDistance(double modeDistance) {
            this.modeDistance = modeDistance;
        }

        public double getMedianDistance() {
            return medianDistance;
        }

        public void setMedianDistance(double medianDistance) {
            this.medianDistance = medianDistance;
        }

        public double getMeanDistance() {
            return meanDistance;
        }

        public void setMeanDistance(double meanDistance) {
            this.meanDistance = meanDistance;
        }
    }

    public static int calculateRoomsInBetween(String rId1, String rId2, double signalStrength) {
        if (rId1 != null && rId2 != null && !rId1.isEmpty() && !rId2.isEmpty() && rId1.equals(rId2)) {
            return 0;
        }

        if (signalStrength > -60) {
            return 0;
        }
        if (signalStrength > -85) {
            return 1;
        } else {
            return 2;
        }
    }
}

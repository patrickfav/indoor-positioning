package at.ac.tuwien.inso.indoor.sensorserver.util;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.MiscManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.SensorManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.Statistics;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.*;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.Blacklist;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNetwork;

import java.util.*;

/**
 * Created by PatrickF on 18.09.2014.
 */
public class SurveyUtil {

    public static List<SurveyStatistics> createAverageFromSurveys(List<Survey> surveys, String networkId) {
        Map<String, SurveyStatistics> map = new HashMap<String, SurveyStatistics>();

        for (Survey survey : surveys) {
            for (AverageWlanScanMeasurement avgM : survey.getAverageScanNodes()) {
                if (!map.containsKey(avgM.getMacAddress())) {
                    map.put(avgM.getMacAddress(), getAverageFromSurvey(surveys, avgM.getMacAddress(), avgM.getFrequencyRange(), networkId));
                }
            }
        }

        List<SurveyStatistics> list = new ArrayList<SurveyStatistics>(map.values());
        Collections.sort(list);
        return list;
    }

    private static SurveyStatistics getAverageFromSurvey(List<Survey> surveys, String macAddress, EFrequencyRange frequencyRange, String networkId) {
        SensorNetwork sensorNetwork = SensorManager.getInstance().getSensorNetworkById(networkId);

        SurveyStatistics avgSurvey = new SurveyStatistics();
        avgSurvey.setMacAddress(macAddress);
        avgSurvey.setFrequencyRange(frequencyRange);
        List<Double> signalStrengths = new ArrayList<Double>();

        for (Survey survey : surveys) {
            for (AverageWlanScanMeasurement avgM : survey.getAverageScanNodes()) {
                if (avgM.getMacAddress().equals(macAddress)) {
                    avgSurvey.getSsidSet().add(avgM.getSsid());
                    signalStrengths.addAll(avgM.getSignalStrengths());
                }
            }
        }

        avgSurvey.setStatistics(new Statistics(signalStrengths));
        avgSurvey.setRadioModelData(new RadioModelData(avgSurvey.getStatistics(), frequencyRange, 1, sensorNetwork.getEnvironmentModel(), null, null, 1.0, sensorNetwork.getPathLossConfig())); //TODO

        Blacklist blacklist = MiscManager.getInstance().getBlacklistByNetworkId(networkId);
        for (String checkMac : blacklist.getMacList()) {
            if (checkMac.equalsIgnoreCase(avgSurvey.getMacAddress())) {
                if (blacklist.isActAsWhiteList()) {
                    avgSurvey.setIgnored(true);

                    for (String mac : blacklist.getMacList()) {
                        if (avgSurvey.getMacAddress().equalsIgnoreCase(mac)) {
                            avgSurvey.setIgnored(false);
                            break;
                        }
                    }
                } else {
                    avgSurvey.setIgnored(false);

                    for (String mac : blacklist.getMacList()) {
                        if (avgSurvey.getMacAddress().equalsIgnoreCase(mac)) {
                            avgSurvey.setIgnored(true);
                            break;
                        }
                    }
                }
            }
        }

        return avgSurvey;
    }

    public static boolean testIfOutlinerDixonsQTest(List<Double> numbers) {
        Collections.sort(numbers);
        return true;
    }
}

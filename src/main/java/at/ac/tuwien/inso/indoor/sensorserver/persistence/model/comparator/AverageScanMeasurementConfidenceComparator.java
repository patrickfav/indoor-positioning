package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.comparator;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.AverageWlanScanMeasurement;

import java.util.Comparator;

/**
 * Created by PatrickF on 14.09.2014.
 */
public class AverageScanMeasurementConfidenceComparator implements Comparator<AverageWlanScanMeasurement> {
    @Override
    public int compare(AverageWlanScanMeasurement o1, AverageWlanScanMeasurement o2) {
        return o1.getConfidence().compareTo(o2.getConfidence())* -1;
    }
}

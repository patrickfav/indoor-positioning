package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.pathloss;

import at.ac.tuwien.inso.indoor.sensorserver.math.radiomodels.ITUIndoorModelDegradingDist;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by PatrickF on 02.11.2014.
 */
public class DistanceValueInput {
	private ITUIndoorModelDegradingDist.ITUDegradingDistConfig config;
	private List<Double> values = new ArrayList<Double>();

	public ITUIndoorModelDegradingDist.ITUDegradingDistConfig getConfig() {
		return config;
	}

	public void setConfig(ITUIndoorModelDegradingDist.ITUDegradingDistConfig config) {
		this.config = config;
	}

	public List<Double> getValues() {
		return values;
	}

	public void setValues(List<Double> values) {
		this.values = values;
	}
}

package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.pathloss;

import at.ac.tuwien.inso.indoor.sensorserver.math.radiomodels.ITUIndoorModelDegradingDist;
import at.ac.tuwien.inso.indoor.sensorserver.util.ServerUtil;

/**
 * Created by PatrickF on 02.11.2014.
 */
public class BruteforceDistanceResult {
	private ITUIndoorModelDegradingDist.ITUDegradingDistConfig config;
	private double mult;
	private double offset;
    private int iterations;

	public BruteforceDistanceResult() {
	}

	public BruteforceDistanceResult(ITUIndoorModelDegradingDist.ITUDegradingDistConfig config, double mult, double offset,int iterations) {
		this.config = config;
		this.mult = mult;
		this.offset = offset;
        this.iterations = iterations;
	}

	public ITUIndoorModelDegradingDist.ITUDegradingDistConfig getConfig() {
		return config;
	}

	public void setConfig(ITUIndoorModelDegradingDist.ITUDegradingDistConfig config) {
		this.config = config;
	}

	public double getMult() {
		return mult;
	}

	public void setMult(double mult) {
		this.mult = mult;
	}

	public double getOffset() {
		return offset;
	}

	public void setOffset(double offset) {
		this.offset = offset;
	}

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public void roundNumbers(int scale) {
		mult = ServerUtil.round(mult, scale);
		config.roundNumbers(scale);
	}

	@Override
	public String toString() {
		return "BruteforceResult{" +
				"config=" + config +
				", mult=" + mult +
				", offset=" + offset +
				'}';
	}
}

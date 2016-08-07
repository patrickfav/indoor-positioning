package at.ac.tuwien.inso.indoor.sensorserver.math.solver;

import at.ac.tuwien.inso.indoor.sensorserver.math.radiomodels.EEnvironmentModel;
import at.ac.tuwien.inso.indoor.sensorserver.math.radiomodels.ITUIndoorModelDegradingDist;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.EFrequencyRange;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.pathloss.BruteforceDistanceInfo;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.pathloss.BruteforceDistanceResult;
import at.ac.tuwien.inso.indoor.sensorserver.util.ServerUtil;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;

/**
 * Finds the best configuration for converting pathloss to distance
 * for given target distances.
 */
public class PathLossBruteforceSolver {
	private static Logger log = Logger.getLogger(PathLossBruteforceSolver.class);
	private static final int MAX_ACCURACY = 4;
	private List<BruteforceDistanceInfo> list;
	private EFrequencyRange range;
	private EEnvironmentModel environmentModel;

	public PathLossBruteforceSolver(List<BruteforceDistanceInfo> list, EFrequencyRange range, EEnvironmentModel environmentModel) {
		this.list = list;
		this.range = range;
		this.environmentModel = environmentModel;
	}

	public BruteforceDistanceResult calculate() {
		long benchTime = new Date().getTime();
		log.info("Start bruteforce");
		int iterator=0;
		BruteforceDistanceResult result = bruteforce(environmentModel,range,list,
				new LoopData(40,80,1),
				new LoopData(0.1,1.0,0.1),
				new LoopData(0.8,1.5,0.1),
				new LoopData(-0.3,0.5,0.1),
				Double.MAX_VALUE,iterator);

		log.info("Raw result found "+result+" try to refine");

		for (int i = 1; i < MAX_ACCURACY; i++) {
			result = bruteforce(environmentModel,range,list,
					new LoopData(result.getConfig().getBound()-(1.5/Math.pow(10,i)),result.getConfig().getBound()+(1.5/Math.pow(10,i)),0.1/Math.pow(10,i)),
					new LoopData(result.getConfig().getFac()-(0.15/Math.pow(10,i)),result.getConfig().getFac()+(0.15/Math.pow(10,i)),0.01/Math.pow(10,i)),
					new LoopData(result.getMult()-(0.15/Math.pow(10,i)),result.getMult()+(0.15/Math.pow(10,i)),0.01/Math.pow(10,i)),
					new LoopData(result.getConfig().getOffsetM()-(0.15/Math.pow(10,i)),result.getConfig().getOffsetM()+(0.15/Math.pow(10,i)),0.01/Math.pow(10,i)),
					result.getOffset(),iterator);
			log.debug("refining step "+i+" after "+(new Date().getTime()-benchTime)+"ms: "+result);
		}

		log.info("bruteforce finished with result after "+(new Date().getTime()-benchTime)+"ms and "+result.getIterations()+" iteratons "+result);

		result.roundNumbers(MAX_ACCURACY);
		return result;
	}

	private static BruteforceDistanceResult bruteforce(EEnvironmentModel envModel,EFrequencyRange range , List<BruteforceDistanceInfo> list,
	                                                   LoopData boundData,LoopData facData,LoopData multiData,LoopData offsetMData,
	                                                   double bestOffsetParam,int oldIterator) {
		ITUIndoorModelDegradingDist.ITUDegradingDistConfig bestConfig=null;
		double bestMult = 1;
		double bestOffset = bestOffsetParam;

		double offset;
		ITUIndoorModelDegradingDist.ITUDegradingDistConfig config=null;
		ITUIndoorModelDegradingDist model=null;
		int i = 0;
		double hz = EFrequencyRange.frequencyHz(range, 1);

		for (double bound = boundData.getLower(); bound < boundData.getUpper(); bound+=boundData.getIncStep()) {//bounds
			for (double fac = facData.getLower(); fac < facData.getUpper(); fac+=facData.getIncStep()) { //fac
				for (double mult = multiData.getLower(); mult < multiData.getUpper(); mult+=multiData.getIncStep()) {
					for(double configOffset = offsetMData.getLower();configOffset < offsetMData.getUpper(); configOffset+=offsetMData.getIncStep()) {
						config = new ITUIndoorModelDegradingDist.ITUDegradingDistConfig(bound,fac, configOffset);

						if(model != null) {
							model.setConfig(config);
						} else {
							model = new ITUIndoorModelDegradingDist(envModel, config);
						}

						offset = 0;

						for (BruteforceDistanceInfo bruteforceDistanceInfo : list) {
							offset += Math.abs(bruteforceDistanceInfo.getOffset(model.getDistanceInMeter(bruteforceDistanceInfo.getPathLoss(), hz, 0) * mult));
						}

						if (Math.abs(offset) < Math.abs(bestOffset)) {
							bestOffset = offset;
							bestMult = mult;
							bestConfig = config;
						}
						i++;
					}
				}
			}
		}

		return new BruteforceDistanceResult(bestConfig,ServerUtil.round(bestMult,MAX_ACCURACY),bestOffset,oldIterator+i);
	}

	private static class LoopData {
		private double lower;
		private double upper;
		private double incStep;

		private LoopData(double lower, double upper, double incStep) {
			this.lower = lower;
			this.upper = upper;
			this.incStep = incStep;
		}

		public double getLower() {
			return lower;
		}

		public double getUpper() {
			return upper;
		}

		public double getIncStep() {
			return incStep;
		}
	}
}

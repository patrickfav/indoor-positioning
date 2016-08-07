package at.ac.tuwien.inso.indoor.sensorserver.math.solver;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.pathloss.BruteforceMultInfo;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.pathloss.BruteforceMultResult;
import at.ac.tuwien.inso.indoor.sensorserver.services.scheduler.Callback;
import at.ac.tuwien.inso.indoor.sensorserver.util.ServerUtil;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;

/**
 * Finds the best multipliers so that in a distance matrix
 * the distance a->b ~ b->a
 */
public class NodeMultiNormalizer {
	private static Logger log = Logger.getLogger(NodeMultiNormalizer.class);
	private static final int MAX_ITERATIONS = 10000000;
	private static final int MAX_THREAD = 8;
	private static final double UPPER_BOUND = 1.1;

	private List<BruteforceMultInfo> inputList;
	private List<BruteforceMultResult> resultList = new CopyOnWriteArrayList<BruteforceMultResult>();
	public ExecutorService threadPool;

	public NodeMultiNormalizer(List<BruteforceMultInfo> inputList) {
		this.inputList = inputList;
	}

	public BruteforceMultResult calculate() {
		Random rnd = new Random();

		double initialValue = 1;

		final Map<String,MutableDouble> multiMap = new HashMap<String, MutableDouble>();
		for (BruteforceMultInfo bruteforceMultInfo : inputList) {
			multiMap.put(bruteforceMultInfo.getMac1(),new MutableDouble(initialValue));
			multiMap.put(bruteforceMultInfo.getMac2(),new MutableDouble(initialValue));
		}

		List<Map.Entry<String, MutableDouble>> entries = new ArrayList<Map.Entry<String, MutableDouble>>(multiMap.entrySet());
		Collections.shuffle(entries,rnd); // make it random for problems where list.size() > MAX_THREAD

		double baseOffset = 0;
		for (BruteforceMultInfo bruteforceMultInfo : inputList) {
			baseOffset += Math.abs((Math.abs(bruteforceMultInfo.getdBm1()) * multiMap.get(bruteforceMultInfo.getMac1()).getValue()) - (Math.abs(bruteforceMultInfo.getdBm2()) * multiMap.get(bruteforceMultInfo.getMac2()).getValue()));
		}

		log.info("Base offset is "+baseOffset);

		threadPool = new ThreadPoolExecutor(MAX_THREAD,MAX_THREAD,1, TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(1024));

		for(int i=0;i<Math.min(MAX_THREAD,entries.size());i++) {
			Map<String,MutableDouble> multiMapCopy =copyMultiMap(multiMap);
			threadPool.submit(new CalculatorRunnable(lockEntryAndReturnCopyOfRestOfList(multiMapCopy,entries.get(i).getKey()),multiMapCopy, inputList,new Callback<BruteforceMultResult>() {
				@Override
				public void callback(BruteforceMultResult bruteforceMultResult) {
					resultList.add(bruteforceMultResult);
				}
			}, initialValue));
		}


		log.debug("waiting for completion");
		try {
			threadPool.shutdown();
			threadPool.awaitTermination(45, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.error("Timeout in threadpool in NodeMultiNormalizer",e);
		}

		BruteforceMultResult bestResult = findBestResult(resultList);
		bestResult.setBaseOffset(baseOffset);

		log.debug("all calculations finished, best result: "+bestResult);

		threadPool.shutdown();

		return bestResult;
	}

	private Map<String, MutableDouble> copyMultiMap(Map<String, MutableDouble> multiMap) {
		Map<String, MutableDouble> newMap = new HashMap<String, MutableDouble>();
		for (Map.Entry<String, MutableDouble> entry : multiMap.entrySet()) {
			newMap.put(entry.getKey(),new MutableDouble(entry.getValue().getValue()));
		}
		return newMap;
	}

	private List<Map.Entry<String, MutableDouble>> lockEntryAndReturnCopyOfRestOfList(final Map<String, MutableDouble> original,String lockedMac) {
		List<Map.Entry<String, MutableDouble>> newList = new ArrayList<Map.Entry<String, MutableDouble>>(original.entrySet());

		for (Map.Entry<String, MutableDouble> e : newList) {
			if(e.getKey().equals(lockedMac)) {
				e.getValue().setValue(1);
				newList.remove(e);
				break;
			}
		}
		return newList;
	}

	private BruteforceMultResult findBestResult(List<BruteforceMultResult> resultList) {
		BruteforceMultResult bestResult=null;
		for (BruteforceMultResult bruteforceMultResult : resultList) {
			if(bestResult == null || bestResult.getOffset() > bruteforceMultResult.getOffset()) {
				bestResult = bruteforceMultResult;
			}
		}
		return bestResult;
	}

	private static class CalculatorRunnable implements Runnable {
		private final Callback<BruteforceMultResult> callback;
		private final List<Map.Entry<String, MutableDouble>> modifieableEntries;
		private final Map<String,MutableDouble> multiMap;
		private final List<BruteforceMultInfo> inputList;
		private final double initialValue;

		private CalculatorRunnable(List<Map.Entry<String, MutableDouble>> modifieableEntries, Map<String, MutableDouble> multiMap, List<BruteforceMultInfo> list, Callback<BruteforceMultResult> callback, double initialValue) {
			this.callback = callback;
			this.modifieableEntries = modifieableEntries;
			this.multiMap = multiMap;
			this.inputList = list;
			this.initialValue = initialValue;
		}

		private double getAppropriateIncrementStep(double initialIncStep) {
			double incStep = initialIncStep;
			double estimatedCycles = Math.pow((UPPER_BOUND-initialValue)*2/incStep,modifieableEntries.size());

			while(estimatedCycles > MAX_ITERATIONS) {
				incStep += initialIncStep;
				estimatedCycles = Math.pow((UPPER_BOUND-initialValue)*2/incStep,modifieableEntries.size());
			}
			return incStep;
		}

		@Override
		public void run() {
			final double incrementStep = getAppropriateIncrementStep(0.001);

			log.info(Thread.currentThread().getName()+": Start mult calibrating with increment steps of "+incrementStep+"  and estimated "+Math.pow((UPPER_BOUND-initialValue)*2/incrementStep,modifieableEntries.size())+" cycles");

			int iterations = 0;
			double offset;
			boolean notFinished = true;
			MutableDouble mDouble = null;
			boolean overflow;
			int listIndex;

			double bestOffset = Double.MAX_VALUE;
			Map<String,Double> bestMultis=null;

			while(notFinished) {
				listIndex = modifieableEntries.size() - 1;
				do {
					overflow = false;

					mDouble = modifieableEntries.get(listIndex).getValue();
					mDouble.setValue(getNextValue(mDouble.getValue(), initialValue, incrementStep));
					if (mDouble.getValue() >= UPPER_BOUND && listIndex > 0) {
						mDouble.setValue(initialValue);
						listIndex--;
						overflow = true;
					} else if (mDouble.getValue() >= UPPER_BOUND && listIndex == 0) {
						notFinished = false;
						log.debug(Thread.currentThread().getName()+": finshed after " + iterations + " cycles (this might be different than estimated because of double accuracy errors)");
					}
				} while (overflow);


				offset = 0;
				for (BruteforceMultInfo bruteforceMultInfo : inputList) {
					offset += Math.abs((Math.abs(bruteforceMultInfo.getdBm1()) * multiMap.get(bruteforceMultInfo.getMac1()).getValue()) - (Math.abs(bruteforceMultInfo.getdBm2()) * multiMap.get(bruteforceMultInfo.getMac2()).getValue()));
				}

				if (Math.abs(offset) < Math.abs(bestOffset)) {
					bestOffset = offset;
					bestMultis = new HashMap<String, Double>();
					for (Map.Entry<String, MutableDouble> entry : multiMap.entrySet()) {
						bestMultis.put(entry.getKey(), ServerUtil.round(entry.getValue().getValue(),4));
					}
				}
				iterations++;

				if(iterations >= MAX_ITERATIONS) {
					notFinished = false;
					log.info(Thread.currentThread().getName()+": cancel, too many cycles: "+iterations);
				}
			}

			log.info(Thread.currentThread().getName()+": best offset: "+bestOffset+" and mult "+bestMultis);

			callback.callback(new BruteforceMultResult(bestMultis,bestOffset));
		}

		/**
		 * This makes it so that it will go form initialValue out + and -, so e.g. 1.0 -> 1.1 -> 0.9 -> 1.2 -> 0.8 -> 1.3 -> 0.7 etc
		 */
		private static double getNextValue(double currentValue, double initialValue, double increment)  {
			if(currentValue == initialValue) {
				return initialValue + increment;
			} else if(currentValue > initialValue) {
				return initialValue-(currentValue-initialValue);
			} else if(currentValue < initialValue) {
				return (initialValue - currentValue) + initialValue + increment;
			} else {
				throw new IllegalStateException("This should not happend. Oh god why?");
			}
		}
	}

	private static class MutableDouble {
		private double value;

		private MutableDouble(double value) {
			this.value = value;
		}

		public double getValue() {
			return value;
		}

		public void setValue(double value) {
			this.value = value;
		}

		public void increment(double incrementBy) {
			value += incrementBy;
		}
	}
}

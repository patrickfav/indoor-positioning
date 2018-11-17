package at.ac.tuwien.inso.indoor.sensorserver.services.scheduler;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.Survey;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.Adapter;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNode;
import at.ac.tuwien.inso.indoor.sensorserver.services.scheduler.jobs.ISensorJob;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by PatrickF on 15.09.2014.
 */
public class FullSurveyWorker implements Callback<Survey>{
    protected static Logger log = Logger.getLogger(FullSurveyWorker.class);

    public List<SensorNode> nodes;
    public long delay;
    public int repeatCount;
    public ExecutorService threadPool;
    public Map<String,Survey> surveyResultMap;
    public List<SurveyCallable> workers;
    public long estimatedRuntime=0;
    public Callback<Double> progressCallback;
    public Map<SensorNode,Double> progressMap = new ConcurrentHashMap<SensorNode, Double>();

    public FullSurveyWorker(List<SensorNode> nodes, long delay, int repeatCount,Callback<Double> progressCallback) {
        this.nodes = nodes;
        this.delay = delay;
        this.repeatCount = repeatCount;
        this.progressCallback = progressCallback;

        threadPool = new ThreadPoolExecutor(8,16,30, TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(1024));
        surveyResultMap = new ConcurrentHashMap<String, Survey>();
        workers=new ArrayList<SurveyCallable>();

        long runTime=0;
        int runs =0;
        for (final SensorNode node : nodes) {
            if(node.isEnabled()) {

                progressMap.put(node, 0d);

                for (Adapter adapter : node.getAdapters()) {
                    SurveyCallable worker = new SurveyCallable(delay, repeatCount, adapter.getName(), node, this, new Callback<Double>() {
                        @Override
                        public void callback(Double aDouble) {
                            progressMap.put(node, aDouble);
                            updateProgress();
                        }
                    });
                    runTime += worker.getEstimatedFullRuntime();
                    runs++;
                    workers.add(worker);
                }
            } else {
                log.info("Skip node "+node.getNodeName()+" because it is not enabled");
            }
        }
        estimatedRuntime = Math.round(((double) runTime / (float) runs) * Math.max(1, runs - ISensorJob.MAX_CONCURRENT_THREADS)) + ((runs * (runTime / runs)) / 100) + 2000;
    }

    public Map<String,Survey> startFullSurvey() {
        log.debug("estimated runtime: "+getEstimatedFullRuntime()+"ms");

        for (SurveyCallable worker : workers) {
            threadPool.submit(worker);
        }

        log.debug("waiting for completion");
        try {
            threadPool.shutdown();
            threadPool.awaitTermination(estimatedRuntime * 3, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("Timeout in threadpool in ping job",e);
        }
        log.debug("surveys complete with "+surveyResultMap.values().size()+" surveys");

        threadPool.shutdown();

        return surveyResultMap;
    }

    public long getEstimatedFullRuntime() {
        return estimatedRuntime;
    }

    public void cancel() {
        if(!threadPool.isShutdown()) {
            log.debug("cancel");
            threadPool.shutdownNow();
        }
    }

    @Override
    public void callback(Survey survey) {
        surveyResultMap.put(survey.getNodeId()+"_"+survey.getAdapter(),survey);
    }

    private void updateProgress() {
        if(progressCallback != null) {
            double progress=0d;
            for (SensorNode sensorNode : progressMap.keySet()) {
                progress += progressMap.get(sensorNode);
            }
            progressCallback.callback(progress / (double) progressMap.keySet().size());
        }
    }
}

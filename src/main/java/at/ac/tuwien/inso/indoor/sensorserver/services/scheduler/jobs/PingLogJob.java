package at.ac.tuwien.inso.indoor.sensorserver.services.scheduler.jobs;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.MiscManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.SensorManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.JobLog;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.PingLog;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNode;
import at.ac.tuwien.inso.indoor.sensorserver.services.exceptions.SensorRequestException;
import at.ac.tuwien.inso.indoor.sensorserver.services.requests.RouterScriptPingRequest;
import at.ac.tuwien.inso.indoor.sensorserver.services.scheduler.SchedulerManager;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by PatrickF on 15.09.2014.
 */
public class PingLogJob extends ISensorJob {
    protected static Logger log = Logger.getLogger(PingLogJob.class);

    private JobLog job;
    private ExecutorService threadPool;
    private long estimatedRuntimeMs;

    public PingLogJob() {
        threadPool = new ThreadPoolExecutor(8, 16, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1024));
        estimatedRuntimeMs = 0;
    }

    @Override
    public void execute(final JobExecutionContext jobExecutionContext) throws JobExecutionException {
        jobExecutionContext.setResult(0d);
        String networkId = jobExecutionContext.getJobDetail().getJobDataMap().getString(SchedulerManager.NETWORK_ID_KEY);
        final List<SensorNode> nodes = SensorManager.getInstance().getAllNodesFromNetwork(networkId);

        job = new JobLog(jobExecutionContext.getJobDetail().getJobDataMap().getString(SchedulerManager.JOB_ID_KEY), networkId, null);

        estimatedRuntimeMs = nodes.size() * 2000;
        log.info("start full ping check");

        MiscManager.getInstance().addJobAsStarted(job, estimatedRuntimeMs, "Ping Check", "");
        for (final SensorNode node : nodes) {
            threadPool.submit(new Runnable() {
                @Override
                public void run() {
                    PingLog log = new PingLog();
                    log.setNodeId(node.getNodeId());
                    log.setJobId(job.getJobId());
                    log.setUrl(node.getFullUrl());
                    try {
                        RouterScriptPingRequest request = new RouterScriptPingRequest(node);
                        boolean success = request.startRequest();
                        log.setSuccess(success);
                    } catch (SensorRequestException e) {
                        log.setSuccess(false);
                        log.setError(true);
                    }
                    PingLogJob.log.debug("add pinglog " + log);
                    MiscManager.getInstance().addPing(log);

                    jobExecutionContext.setResult(Math.min(0.99d,((Double) jobExecutionContext.getResult()) + (1.0 / nodes.size())));
                }
            });
        }

        try {
            threadPool.shutdown();
            threadPool.awaitTermination(estimatedRuntimeMs * 3, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("Timeout in threadpool in ping job", e);
        }

        threadPool.shutdown();

        MiscManager.getInstance().endJob(job, JobLog.Status.SUCCESS);

        log.info("full ping job ended");
    }


    @Override
    public long getEstimatedFullRuntime() {
        return estimatedRuntimeMs;
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        if (!threadPool.isShutdown()) {
            log.debug("cancel");
            MiscManager.getInstance().endJob(job, JobLog.Status.CANCEL);
            threadPool.shutdownNow();
        }
    }
}

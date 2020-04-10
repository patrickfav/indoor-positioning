package at.ac.tuwien.inso.indoor.sensorserver.services.scheduler.jobs;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.MiscManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.SensorManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.JobLog;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNode;
import at.ac.tuwien.inso.indoor.sensorserver.services.analysis.AnalysisBuilder;
import at.ac.tuwien.inso.indoor.sensorserver.services.scheduler.SchedulerManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

import java.util.List;

/**
 * Created by PatrickF on 25.09.2014.
 */
public class AnalysisJob extends ISensorJob {
    protected static Logger log = LogManager.getLogger(AnalysisJob.class);

    private JobLog job;
    private long estimatedRuntimeMs = 8000;
    private Thread thread;

    @Override
    public long getEstimatedFullRuntime() {
        return estimatedRuntimeMs;
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }

    @Override
    public void execute(final JobExecutionContext jobExecutionContext) throws JobExecutionException {
        jobExecutionContext.setResult(0.0d);
        final String networkId = jobExecutionContext.getJobDetail().getJobDataMap().getString(SchedulerManager.NETWORK_ID_KEY);
        log.debug("Start new analysis job for " + networkId);


        job = new JobLog(jobExecutionContext.getJobDetail().getJobDataMap().getString(SchedulerManager.JOB_ID_KEY), networkId, null);
        MiscManager.getInstance().addJobAsStarted(job, estimatedRuntimeMs, "Network Analysis", "");
        try {
            jobExecutionContext.setResult(0.1d);
            final List<SensorNode> nodes = SensorManager.getInstance().getAllNodesFromNetwork(networkId);

            estimatedRuntimeMs = nodes.size() * 2000 + 1000;

            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        jobExecutionContext.setResult(0.2d);
                        new AnalysisBuilder().createAndPresistAnalysis(networkId);
                        jobExecutionContext.setResult(0.9d);
                        MiscManager.getInstance().endJob(job, JobLog.Status.SUCCESS);
                        log.debug("Analysis Job successful for " + networkId);
                    } catch (Exception e) {
                        log.error("Error in Job: " + e.getMessage(), e);
                        MiscManager.getInstance().endJob(job, JobLog.Status.ERROR, e.getMessage());
                    }

                }
            });
            thread.start();
        } catch (Exception e) {
            log.error("Error in Job: " + e.getMessage(), e);
            MiscManager.getInstance().endJob(job, JobLog.Status.ERROR, e.getMessage());
        }
    }
}

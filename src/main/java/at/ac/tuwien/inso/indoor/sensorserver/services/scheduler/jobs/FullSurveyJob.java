package at.ac.tuwien.inso.indoor.sensorserver.services.scheduler.jobs;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.MiscManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.SensorManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.JobLog;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.Survey;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNode;
import at.ac.tuwien.inso.indoor.sensorserver.services.scheduler.Callback;
import at.ac.tuwien.inso.indoor.sensorserver.services.scheduler.FullSurveyWorker;
import at.ac.tuwien.inso.indoor.sensorserver.services.scheduler.SchedulerManager;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

import java.util.List;
import java.util.Map;

/**
 * Created by PatrickF on 15.09.2014.
 */
public class FullSurveyJob extends ISensorJob {
    protected static Logger log = Logger.getLogger(FullSurveyJob.class);

    public static final long SURVEY_DELAY = 5000;
    public static final int SURVEY_REPEAT_COUNT = 5;

    private JobLog job;
    private long estimatedRuntimeMs;
    private FullSurveyWorker worker;

    public FullSurveyJob() {
    }

    @Override
    public void execute(final JobExecutionContext jobExecutionContext) throws JobExecutionException {
        jobExecutionContext.setResult(0.0d);
        String networkId = jobExecutionContext.getJobDetail().getJobDataMap().getString(SchedulerManager.NETWORK_ID_KEY);
        List<SensorNode> nodes = SensorManager.getInstance().getAllNodesFromNetwork(networkId);
        worker = new FullSurveyWorker(nodes, SURVEY_DELAY, SURVEY_REPEAT_COUNT, new Callback<Double>() {
            @Override
            public void callback(Double aDouble) {
                jobExecutionContext.setResult(aDouble);
            }
        });
        estimatedRuntimeMs = worker.getEstimatedFullRuntime();

        job = new JobLog(jobExecutionContext.getJobDetail().getJobDataMap().getString(SchedulerManager.JOB_ID_KEY), networkId, null);

        MiscManager.getInstance().addJobAsStarted(job, estimatedRuntimeMs, "Full Survey", "");
        log.info("start full survey (jobid: " + job.getJobId() + ")");
        Map<String, Survey> resultMap = worker.startFullSurvey();

        for (String id : resultMap.keySet()) {
            resultMap.get(id).setJobId(job.getJobId());
            MiscManager.getInstance().addSurvey(resultMap.get(id));
            log.debug("Adding survey " + resultMap.get(id));
        }

        MiscManager.getInstance().endJob(job, JobLog.Status.SUCCESS);

        log.info("full survey job ended (jobid: " + job.getJobId() + ")");
    }

    @Override
    public long getEstimatedFullRuntime() {
        return estimatedRuntimeMs;
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        MiscManager.getInstance().endJob(job, JobLog.Status.CANCEL);
        worker.cancel();
    }
}

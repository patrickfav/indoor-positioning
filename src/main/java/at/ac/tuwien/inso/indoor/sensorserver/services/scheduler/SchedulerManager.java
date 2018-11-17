package at.ac.tuwien.inso.indoor.sensorserver.services.scheduler;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNetwork;
import at.ac.tuwien.inso.indoor.sensorserver.services.scheduler.jobs.AnalysisJob;
import at.ac.tuwien.inso.indoor.sensorserver.services.scheduler.jobs.FullSurveyJob;
import at.ac.tuwien.inso.indoor.sensorserver.services.scheduler.jobs.PingLogJob;
import org.apache.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.UUID;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Created by PatrickF on 15.09.2014.
 */
public final class SchedulerManager {
    protected static Logger log = Logger.getLogger(SchedulerManager.class);

    public static final String NETWORK_ID_KEY = "progress-id";
    public static final String JOB_ID_KEY = "job-id";

    private static final String SURVEY_JOB_GROUP = "network-survey";
    private static final String PING_JOB_GROUP = "network-ping";
    private static final String ANALYSIS_JOB_GROUP = "network-analysis";

    private static SchedulerManager instance;

    public static SchedulerManager getInstance() {
        if (instance == null) {
            instance = new SchedulerManager();
        }
        return instance;
    }

    public static void reset() {
        if (instance != null) {
            instance.shutdownNow();
        }
        instance = null;
    }

    private Scheduler scheduler;
    private SchedulerFactory schedulerFactory;

    private SchedulerManager() {
        try {
            schedulerFactory = new StdSchedulerFactory();
            scheduler = schedulerFactory.getScheduler();
            scheduler.start();
        } catch (Exception e) {
            log.error("Could not instantiate quartz scheduler");
        }
    }

    /* ********************************************************************* SURVEY */

    public void addSurveyJob(SensorNetwork network) throws SchedulerException {
        scheduleSurveyJob(network, false);
    }

    public void scheduleSurveyJob(SensorNetwork network, boolean runOnlyOnceNow) {
        scheduleJob(network, runOnlyOnceNow, FullSurveyJob.class, SURVEY_JOB_GROUP, network.getCronScheduleSurvey());
    }

    public void cancelSurveyJob(String networkId) {
        cancelJob(networkId, SURVEY_JOB_GROUP);
    }

    public boolean isScheduledSurveyJob(String networkId) {
        return isScheduledJob(networkId, SURVEY_JOB_GROUP);
    }

    /* ********************************************************************* PING */

    public void addPingLogJob(SensorNetwork network) {
        schedulePingJob(network, false);
    }

    public void schedulePingJob(SensorNetwork network, boolean runOnlyOnceNow) {
        scheduleJob(network, runOnlyOnceNow, PingLogJob.class, PING_JOB_GROUP, network.getCronSchedulePing());
    }

    public void cancelPingJob(String networkId) {
        cancelJob(networkId, PING_JOB_GROUP);
    }

    public boolean isScheduledPingJob(String networkId) {
        return isScheduledJob(networkId, PING_JOB_GROUP);
    }

    /* ********************************************************************* ANALYSIS */

    public void addAnalysisLogJob(SensorNetwork network) {
        scheduleAnalysisJob(network, false);
    }

    public void scheduleAnalysisJob(SensorNetwork network, boolean runOnlyOnceNow) {
        scheduleJob(network, runOnlyOnceNow, AnalysisJob.class, ANALYSIS_JOB_GROUP, network.getCronScheduleAnalysis());
    }

    public void cancelAnalysisJob(String networkId) {
        cancelJob(networkId, ANALYSIS_JOB_GROUP);
    }

    public boolean isScheduledAnalysisJob(String networkId) {
        return isScheduledJob(networkId, ANALYSIS_JOB_GROUP);
    }

    /* ********************************************************************* PRIVATE */

    private void scheduleJob(SensorNetwork network, boolean runOnlyOnceNow, Class<? extends Job> jobClass, String group, String cronSchedule) {
        if (network == null || cronSchedule == null || cronSchedule.isEmpty()) {
            log.error("Could not add " + group + " job, cron definition is missing from network " + network);
            return;
        }

        if (!network.isCronEnabled() && !runOnlyOnceNow) {
            log.info("Cron Schedule disable in network " + network + " - ignore add job");
            return;
        }

        if (runOnlyOnceNow) {
            log.info("Add " + group + " job for network " + network.getNetworkName() + ". Run only once");

        } else {
            log.info("Add " + group + " job for network " + network.getNetworkName() + " with schedule " + cronSchedule);
        }

        try {
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put(NETWORK_ID_KEY, network.getNetworkId());
            jobDataMap.put(JOB_ID_KEY, UUID.randomUUID().toString());

            String jobName = network.getNetworkId();
            if (runOnlyOnceNow) {
                jobName = jobName + "_once_" + UUID.randomUUID().toString();
            }

            JobDetail job = newJob(jobClass).setJobData(jobDataMap)
                    .withIdentity(jobName, group)
                    .build();

            Trigger trigger;
            if (runOnlyOnceNow) {
                trigger = newTrigger().withIdentity(UUID.randomUUID().toString(), group).startNow().build();
            } else {
                trigger = newTrigger()
                        .withIdentity(network.getNetworkId(), group)
                        .withSchedule(CronScheduleBuilder.cronSchedule(cronSchedule))
                        .build();
            }

            // Tell quartz to schedule the job using our trigger
            scheduler.scheduleJob(job, trigger);
        } catch (Exception e) {
            log.error("Could not add " + group + " job for " + network, e);
        }
    }

    private void cancelJob(String networkId, String group) {
        try {
            log.info("Cancel schedule for job and group " + group + " and networkId " + networkId);
            scheduler.deleteJob(new JobKey(networkId, group));
        } catch (SchedulerException e) {
            log.error("Could not cancel job for network " + networkId, e);
        }
    }

    private boolean isScheduledJob(String networkId, String group) {
        try {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group))) {
                if (jobKey.getName().equalsIgnoreCase(networkId)) {
                    return true;
                }
            }
        } catch (SchedulerException e) {
            log.error("Could not query for jobs in scheduler", e);
        }
        return false;
    }

    public Double getProgress(String jobId) {
        Double progress = 0d;
        try {
            for (JobExecutionContext jobExecutionContext : scheduler.getCurrentlyExecutingJobs()) {
                if (jobExecutionContext.getJobDetail().getJobDataMap().getString(JOB_ID_KEY).equalsIgnoreCase(jobId)) {
                    progress = (Double) jobExecutionContext.getResult();
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Could not get job progress info", e);
        }
        return progress;
    }

    public void shutdownNow() {
        try {
            if (scheduler.isStarted()) {
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(SURVEY_JOB_GROUP))) {
                    scheduler.interrupt(jobKey);
                    scheduler.deleteJob(jobKey);
                }

                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(PING_JOB_GROUP))) {
                    scheduler.interrupt(jobKey);
                    scheduler.deleteJob(jobKey);
                }

                log.info("Shutdown scheduler");
                scheduler.shutdown(false);
            }
        } catch (SchedulerException e) {
            log.error("Could not shutdown scheduler", e);
        }
    }

}

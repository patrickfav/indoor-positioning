package at.ac.tuwien.inso.indoor.sensorserver.services.scheduler.jobs;

import org.quartz.InterruptableJob;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by PatrickF on 16.09.2014.
 */
public abstract class ISensorJob implements InterruptableJob {
    public static final int MAX_CONCURRENT_THREADS = 16;

    public ThreadPoolExecutor createThreadPool() {
        return new ThreadPoolExecutor(8, MAX_CONCURRENT_THREADS, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1024));
    }
    public abstract long getEstimatedFullRuntime();
}

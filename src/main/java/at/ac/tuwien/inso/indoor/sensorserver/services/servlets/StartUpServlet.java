package at.ac.tuwien.inso.indoor.sensorserver.services.servlets;


import at.ac.tuwien.inso.indoor.sensorserver.persistence.couchdb.DB;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.SensorManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNetwork;
import at.ac.tuwien.inso.indoor.sensorserver.services.ServerConfig;
import at.ac.tuwien.inso.indoor.sensorserver.services.scheduler.SchedulerManager;
import at.ac.tuwien.inso.indoor.sensorserver.util.ServerUtil;
import org.apache.log4j.Logger;
import org.quartz.SchedulerException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * @author PatrickF
 * @since 08.09.2014
 */
public class StartUpServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(StartUpServlet.class);

    @Override
    public void init() {
        try {
            log.info("init mock server");
            setServerConfig();
            addJobs();
        } catch (Exception e) {
            log.error("Could not init servlet", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            resetAll();
            addTestData();
            addJobs();
            resp.getWriter().println("Server has been reseted");
        } catch (Exception e) {
            log.error("Could not reset", e);
            try {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            } catch (IOException e1) {
                log.error("Could not send error", e1);
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        SchedulerManager.getInstance().shutdownNow();
    }

    private void resetAll() {
        log.info("Setup/Reset all data");
        DB.getInstance().resetCouchDB();
        ServerConfig.reset();
        setServerConfig();
        SchedulerManager.reset();
    }

    private void setServerConfig() {
        ServerConfig.getInstance().setRootPath(new File(getServletContext().getRealPath("/")).getName());
        ServerConfig.getInstance().setVersion(ServerUtil.getImplementationVersionFromManifest(getServletContext()));

        ServerConfig savedConfig = ServerConfig.getInstance().getServerConfigDao().getServerConfig();
        if(savedConfig != null) {
            ServerConfig.getInstance().set(savedConfig);
            log.info("A saved ServerConfig was found and configs reused");
        } else {
            log.info("A saved ServerConfig could not be found");
        }

        ServerConfig.getInstance().saveToDb();

        //TODO: This will only work if the client is on same machine as server (using localhost as url) - find a better solution later
        ServerConfig.getInstance().setCouchDBUrl(DB.DB_URL+"/"+DB.getInstance().getDBName());
    }

    private void addTestData() {
        //todo
    }

    private void addJobs() {
        for (SensorNetwork sensorNetwork : SensorManager.getInstance().getAllSensorNetworksNonDeleted()) {
            try {
                SchedulerManager.getInstance().addSurveyJob(sensorNetwork);
                SchedulerManager.getInstance().addPingLogJob(sensorNetwork);
                SchedulerManager.getInstance().addAnalysisLogJob(sensorNetwork);
            } catch (SchedulerException e) {
                log.error("Could not add scheduler job for network "+sensorNetwork);
            }
        }
    }

}

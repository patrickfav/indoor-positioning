package at.ac.tuwien.inso.indoor.sensorserver.services.servlets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by PatrickF on 16.09.2014.
 */
public class ContainerListener implements ServletContextListener {
    private static Logger log = LogManager.getLogger(StartUpServlet.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info(" =============== Initialize Listener");

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info(" =============== Destroy Listener");

    }
}

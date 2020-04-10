package at.ac.tuwien.inso.indoor.sensorserver.services;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.couchdb.TypeAbleCouchDBDocument;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.dao.ServerConfigDao;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.EtagManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.SignalMapConfig;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.UUID;

/**
 * @author PatrickF
 * @since 15.11.13
 * Time: 19:38
 */
public final class ServerConfig extends TypeAbleCouchDBDocument {
    @JsonIgnore
    private static Logger log = LogManager.getLogger(ServerConfig.class);

    @JsonIgnore
    private static ServerConfig instance;

    public static ServerConfig getInstance() {
        if (instance == null) {
            instance = new ServerConfig();
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    @JsonIgnore
    private ServerConfigDao serverConfigDao;

    private ServerConfig() {
        serverStartUp = new Date().toString();
    }

    private String rootPath = "";
    private String version = "undefined";
    private String serverStartUp;
    private String etagSalt = UUID.randomUUID().toString();
    private long ifModifiedSinceDate = new Date().getTime();
    private int maxAgeCacheControl = -1;
    private boolean autoReloadJobs = true;
    private String couchDBUrl = "";
    private SignalMapConfig signalMapConfig = new SignalMapConfig();

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public String getEtagSalt() {
        return etagSalt;
    }

    public void setEtagSalt(String etagSalt) {
        this.etagSalt = etagSalt;
        EtagManager.getInstance().regenerateETag(ServerConfig.class);
    }

    public long getIfModifiedSinceDate() {
        return ifModifiedSinceDate;
    }

    public void setIfModifiedSinceDate(long ifModifiedSinceDate) {
        this.ifModifiedSinceDate = ifModifiedSinceDate;
        EtagManager.getInstance().regenerateETag(ServerConfig.class);

    }

    public int getMaxAgeCacheControl() {
        return maxAgeCacheControl;
    }

    public void setMaxAgeCacheControl(int maxAgeCacheControl) {
        this.maxAgeCacheControl = maxAgeCacheControl;
        EtagManager.getInstance().regenerateETag(ServerConfig.class);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getServerStartUp() {
        return serverStartUp;
    }

    public void setServerStartUp(String serverStartUp) {
        this.serverStartUp = serverStartUp;
    }

    public boolean isAutoReloadJobs() {
        return autoReloadJobs;
    }

    public void setAutoReloadJobs(boolean autoReloadJobs) {
        this.autoReloadJobs = autoReloadJobs;
    }

    public String getCouchDBUrl() {
        return couchDBUrl;
    }

    public void setCouchDBUrl(String couchDBUrl) {
        this.couchDBUrl = couchDBUrl;
    }

    public SignalMapConfig getSignalMapConfig() {
        return signalMapConfig;
    }

    public void setSignalMapConfig(SignalMapConfig signalMapConfig) {
        this.signalMapConfig = signalMapConfig;
        EtagManager.getInstance().regenerateETag(ServerConfig.class);
    }

    public void set(ServerConfig config) {
        setEtagSalt(config.getEtagSalt());
        setIfModifiedSinceDate(config.getIfModifiedSinceDate());
        setMaxAgeCacheControl(config.getMaxAgeCacheControl());
        setAutoReloadJobs(config.isAutoReloadJobs());
        setSignalMapConfig(config.getSignalMapConfig());
    }

    @JsonIgnore
    public ServerConfigDao getServerConfigDao() {
        if (serverConfigDao == null) {
            serverConfigDao = new ServerConfigDao();
        }
        return serverConfigDao;
    }

    @JsonIgnore
    public void saveToDb() {
        getServerConfigDao().setNewServerConfig(this);
        log.debug("ServerConfig updated in DB");
    }
}

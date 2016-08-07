package at.ac.tuwien.inso.indoor.sensorserver.persistence.couchdb;


import at.ac.tuwien.inso.indoor.sensorserver.persistence.ObjectMapperManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.JobLog;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.PingLog;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.Analysis;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.Survey;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.Blacklist;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.RoomList;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNetwork;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNode;
import at.ac.tuwien.inso.indoor.sensorserver.services.ServerConfig;
import org.apache.log4j.Logger;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.UpdateConflictException;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import org.ektorp.support.DesignDocument;

/**
 * @author PatrickF
 * @since 01.12.13
 *        Time: 00:45
 */
public class DB {
    public static String DB_URL = "http://localhost:5984";
    private static Logger log = Logger.getLogger(DB.class);
    private static DB ourInstance = new DB();
    private static final String DB_MAIN = "sensordb";

    public static DB getInstance() {
        return ourInstance;
    }

    private CouchDbConnector mainDb;
    private CouchDbInstance couchDbInstance;

    private DB() {
        HttpClient httpClient = null;
        try {
            log.info("instantiate db with naming: "+addContextPrefix("<dbName>"));
            httpClient = new StdHttpClient.Builder().url(DB_URL).connectionTimeout(15000).socketTimeout(25000).build();

            couchDbInstance = new StdCouchDbInstance(httpClient);
            mainDb = new StdCouchDbConnector(addContextPrefix(DB_MAIN), couchDbInstance,new ObjectMapperManager());

            mainDb.createDatabaseIfNotExists();
            addDesignDocuments();
        } catch (Exception e) {
            log.error("Could not connect to couchDB",e);
        }
    }

    public CouchDbConnector getMainDB() {
        return mainDb;
    }

    public String getDBName() {return mainDb.getDatabaseName();}

    public void resetCouchDB() {
        log.info("reset couch db with naming: " + addContextPrefix("<dbName>"));
        couchDbInstance.deleteDatabase(addContextPrefix(DB_MAIN));

        couchDbInstance.createDatabase(addContextPrefix(DB_MAIN));
        addDesignDocuments();
    }

    private void addDesignDocuments() {
        addDesignDocument(mainDb,createDesignDocument(SensorNetwork.class,ViewLib.getByNetworkId("by_networkId", SensorNetwork.class)));
        addDesignDocument(mainDb,createDesignDocument(SensorNode.class,ViewLib.getByNetworkId("by_networkId", SensorNode.class),ViewLib.getByNodeId("by_nodeId", SensorNode.class)));
        addDesignDocument(mainDb,createDesignDocument(ServerConfig.class));
        addDesignDocument(mainDb,createDesignDocument(Survey.class,ViewLib.getByNodeId("by_nodeId", Survey.class),ViewLib.getByNodeIdSortByDateAndLimit("by_nodeIdSortByDate", Survey.class),
                ViewLib.getByNodeIdAndAdapterSortByDateAndLimit("by_nodeIdAndAdapterSortByDate", Survey.class),ViewLib.getByNetworkIdSortByDateAndLimit("by_networkIdSortByDate", Survey.class)));
        addDesignDocument(mainDb,createDesignDocument(PingLog.class,ViewLib.getByNodeId("by_nodeId", PingLog.class),ViewLib.getByNodeIdSortByDateAndLimit("by_nodeIdSortByDate", PingLog.class)));
        addDesignDocument(mainDb,createDesignDocument(JobLog.class,ViewLib.getByJobId("by_jobId", JobLog.class),
                ViewLib.getByNetworkId("by_networkId", JobLog.class),ViewLib.getByNodeId("by_nodeId", JobLog.class),ViewLib.getByNetworkIdSortByDateAndLimit("by_networkIdSortByDate",JobLog.class)));
        addDesignDocument(mainDb,createDesignDocument(Analysis.class,ViewLib.getByNetworkIdSortByDateAndLimit("by_networkId", Analysis.class),ViewLib.getByAnalysisId("by_analysisId", Analysis.class)));
        addDesignDocument(mainDb,createDesignDocument(Blacklist.class,ViewLib.getByNetworkId("by_networkId", Blacklist.class)));
        addDesignDocument(mainDb,createDesignDocument(RoomList.class,ViewLib.getByNetworkId("by_networkId", RoomList.class)));
    }
    private void addDesignDocument(CouchDbConnector connector, DesignDocument doc) {
        try {
            connector.create(doc);
        } catch (UpdateConflictException e) {
            log.warn("Ignoring document update conflict for design document");
        }

    }

    private DesignDocument createDesignDocument(Class clazz,ViewLib.ViewWrapper... views) {
        DesignDocument designDoc = new DesignDocument(DesignDocument.ID_PREFIX+clazz.getSimpleName());
        designDoc.addView("all", new DesignDocument.View("function(doc) { if (doc.dbType == '" + clazz.getSimpleName() + "' ) emit( null, doc._id )}"));
        for (ViewLib.ViewWrapper view : views) {
            designDoc.addView(view.getName(), view.getView());
        }
        return designDoc;
    }

    private String addContextPrefix(String dbName) {
        if(ServerConfig.getInstance().getRootPath() != null && !ServerConfig.getInstance().getRootPath().isEmpty()) {
            return dbName+"_"+ServerConfig.getInstance().getRootPath();
        } else {
            log.info("could not find db name prefix");
            return dbName;
        }
    }
}

package at.ac.tuwien.inso.indoor.sensorserver.persistence.couchdb;

import org.ektorp.CouchDbConnector;
import org.ektorp.DbInfo;
import org.ektorp.support.CouchDbRepositorySupport;

import java.util.List;

/**
 * Created by PatrickF on 02.02.14.
 */
public abstract class AMockRepository<T> extends CouchDbRepositorySupport<T> {
    private final CouchDbConnector connector;

    protected AMockRepository(Class<T> type, CouchDbConnector db) {
        super(type, db);
        connector = db;
    }

    protected AMockRepository(Class<T> type, CouchDbConnector db, boolean createIfNotExists) {
        super(type, db, createIfNotExists);
        connector = db;
    }

    protected AMockRepository(Class<T> type, CouchDbConnector db, String designDocName) {
        super(type, db, designDocName);
        connector = db;
    }

    protected CouchDbConnector getConnector() {
        return connector;
    }

    public DbInfo getDBInfo() {
        return connector.getDbInfo();
    }

    public static class EntityWrapper<T> {
        private List<T> list;
        private long docCount;

        public EntityWrapper(List<T> list, long docCount) {
            this.list = list;
            this.docCount = docCount;
        }

        public List<T> getList() {
            return list;
        }

        public void setList(List<T> list) {
            this.list = list;
        }

        public long getDocCount() {
            return docCount;
        }

        public void setDocCount(long docCount) {
            this.docCount = docCount;
        }
    }
}

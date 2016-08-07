package at.ac.tuwien.inso.indoor.sensorserver.persistence.dao;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.couchdb.DB;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.JobLog;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.PingLog;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.Analysis;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.Survey;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.Blacklist;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.RoomList;
import at.ac.tuwien.inso.indoor.sensorserver.util.ServerUtil;
import org.ektorp.ComplexKey;
import org.ektorp.ViewQuery;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.DesignDocument;

import java.util.Date;
import java.util.List;

/**
 * Created by PatrickF on 19.09.2014.
 */
public class MiscDao {

    public static class SurveyDao extends CouchDbRepositorySupport<Survey> {
        public SurveyDao() {
            super(Survey.class, DB.getInstance().getMainDB());
        }

        public List<Survey> findByNodeId(String nodeId) {
            return queryView("by_nodeId", String.valueOf(nodeId));
        }

        public List<Survey> findByNodeAndAdapterLimit(String nodeId, String adapter, int limit) {
            ComplexKey startKey = ComplexKey.of(nodeId,adapter);
            ComplexKey endKey = ComplexKey.of(nodeId,adapter, ComplexKey.emptyObject());

            ViewQuery query = new ViewQuery().designDocId(DesignDocument.ID_PREFIX+Survey.class.getSimpleName())
                    .limit(limit).startKey(endKey).endKey(startKey).descending(true).includeDocs(true).viewName("by_nodeIdAndAdapterSortByDate");
            return db.queryView(query, Survey.class);
        }

        public List<Survey> findByNodeLimit(String nodeId,int limit) {
            ComplexKey startKey = ComplexKey.of(nodeId);
            ComplexKey endKey = ComplexKey.of(nodeId, ComplexKey.emptyObject());

            ViewQuery query = new ViewQuery().designDocId(DesignDocument.ID_PREFIX+Survey.class.getSimpleName())
                    .limit(limit).startKey(endKey).endKey(startKey).descending(true).includeDocs(true).viewName("by_nodeIdSortByDate");
            return db.queryView(query, Survey.class);
        }

        public List<Survey> findByNetworkLimit(String networkId, int limit) {
            ComplexKey startKey = ComplexKey.of(networkId);
            ComplexKey endKey = ComplexKey.of(networkId, ComplexKey.emptyObject());

            ViewQuery query = new ViewQuery().designDocId(DesignDocument.ID_PREFIX+Survey.class.getSimpleName())
                    .limit(limit).startKey(endKey).endKey(startKey).descending(true).includeDocs(true).viewName("by_networkIdSortByDate");
            return db.queryView(query, Survey.class);
        }
    }


    public static class JobDao extends CouchDbRepositorySupport<JobLog> {
        public JobDao() {
            super(JobLog.class, DB.getInstance().getMainDB());
        }

        public List<JobLog> findByJobId(String nodeId) {
            return queryView("by_jobId", String.valueOf(nodeId));
        }
        public List<JobLog> findByNodeId(String nodeId) {
            return queryView("by_nodeId", String.valueOf(nodeId));
        }
        public List<JobLog> findByNetworkId(String networkId) {
            return queryView("by_networkId", String.valueOf(networkId));
        }


        public List<JobLog> findByNetworkIdLimit(String networkId,int limit) {
            ComplexKey startKey = ComplexKey.of(networkId);
            ComplexKey endKey = ComplexKey.of(networkId, ComplexKey.emptyObject());

            ViewQuery query = new ViewQuery().designDocId(DesignDocument.ID_PREFIX+JobLog.class.getSimpleName())
                    .limit(limit).startKey(endKey).endKey(startKey).descending(true).includeDocs(true).viewName("by_networkIdSortByDate");
            return db.queryView(query, JobLog.class);
        }
    }


    public static class PingDao extends CouchDbRepositorySupport<PingLog> {
        public PingDao() {
            super(PingLog.class, DB.getInstance().getMainDB());
        }

        public List<PingLog> findByNodeId(String nodeId) {
            return queryView("by_nodeId", String.valueOf(nodeId));
        }

        public List<PingLog> findByNodeSortByDate(String nodeId, int limit) {
            ComplexKey startKey = ComplexKey.of(nodeId);
            ComplexKey endKey = ComplexKey.of(nodeId, ComplexKey.emptyObject());

            ViewQuery query = new ViewQuery().designDocId(DesignDocument.ID_PREFIX+PingLog.class.getSimpleName())
                    .limit(limit).startKey(endKey).endKey(startKey).descending(true).includeDocs(true).viewName("by_nodeIdSortByDate");
            return db.queryView(query, PingLog.class);
        }
    }


    public static class AnalysisDao extends CouchDbRepositorySupport<Analysis> {
        public AnalysisDao() {
            super(Analysis.class, DB.getInstance().getMainDB());
        }

        public Analysis findByAnalysisId(String analysisId) {
            List<Analysis> analysisList = queryView("by_analysisId", String.valueOf(analysisId));

            if(analysisList.isEmpty()) {
                return null;
            } else {
                return analysisList.get(0);
            }
        }

//        public Analysis findPinnedAnalysis(String networkId) {
//            List<Analysis> analysisList = queryView("by_analysisId", String.valueOf(analysisId));
//
//            if(analysisList.isEmpty()) {
//                return null;
//            } else {
//                return analysisList.get(0);
//            }
//        }

        public List<Analysis> findByNetworkIdLimit(String networkId, int limit) {
            ComplexKey startKey = ComplexKey.of(networkId);
            ComplexKey endKey = ComplexKey.of(networkId, ComplexKey.emptyObject());

            ViewQuery query = new ViewQuery().designDocId(DesignDocument.ID_PREFIX+Analysis.class.getSimpleName())
                    .limit(limit).startKey(endKey).endKey(startKey).descending(true).includeDocs(true).viewName("by_networkId");
            return db.queryView(query, Analysis.class);
        }

        public List<Analysis> findByNetworkIdAndStartKeyLimit(String networkId, String startKeyDate, String startDocId,int limit) {
            ComplexKey startKey;
            ComplexKey endKey;
            ViewQuery query;
            if(startKeyDate == null || startKeyDate.isEmpty()) {
                startKey = ComplexKey.of(networkId, ComplexKey.emptyObject());
                endKey = ComplexKey.of(networkId);
                query = new ViewQuery().designDocId(DesignDocument.ID_PREFIX+Analysis.class.getSimpleName())
                        .limit(limit+1).startKey(startKey).endKey(endKey).descending(true).includeDocs(true).viewName("by_networkId");
            } else {
                startKey = ComplexKey.of(networkId, ServerUtil.createISO8601UTCDate(new Date(Long.valueOf(startKeyDate))));
                query = new ViewQuery().designDocId(DesignDocument.ID_PREFIX+Analysis.class.getSimpleName())
                        .limit(limit+1).startKey(startKey).startDocId(startDocId).descending(true).includeDocs(true).viewName("by_networkId");
            }
            return db.queryView(query, Analysis.class);
        }
    }

    public static class BlacklistDao extends CouchDbRepositorySupport<Blacklist> {
        public BlacklistDao() {
            super(Blacklist.class, DB.getInstance().getMainDB());
        }

        public Blacklist findByNetworkId(String networkId) {
            List<Blacklist> list = queryView("by_networkId", String.valueOf(networkId));

            if(list.isEmpty()) {
                return null;
            } else {
                return list.get(0);
            }
        }
    }

    public static class RoomListDao extends CouchDbRepositorySupport<RoomList> {
        public RoomListDao() {
            super(RoomList.class, DB.getInstance().getMainDB());
        }

        public RoomList findByNetworkId(String networkId) {
            List<RoomList> roomLists = queryView("by_networkId", String.valueOf(networkId));

            if(roomLists.isEmpty()) {
                return null;
            } else {
                return roomLists.get(0);
            }
        }
    }

}

package at.ac.tuwien.inso.indoor.sensorserver.persistence.manager;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.dao.MiscDao;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.JobLog;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.PingLog;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.Analysis;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.AnalysisMetaData;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.EFrequencyRange;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.Survey;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.Blacklist;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.RoomList;
import at.ac.tuwien.inso.indoor.sensorserver.services.exceptions.ResourceNotFoundException;
import at.ac.tuwien.inso.indoor.sensorserver.services.scheduler.SchedulerManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by PatrickF on 08.09.2014.
 */
public class MiscManager extends AManager {

    private static Logger log = Logger.getLogger(MiscManager.class);
    private static MiscManager instance;

    public static MiscManager getInstance() {
        if (instance == null) {
            instance = new MiscManager();
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    private MiscDao.SurveyDao surveyDao;
    private MiscDao.PingDao pingDao;
    private MiscDao.JobDao jobDao;
    private MiscDao.AnalysisDao analysisDao;
    private MiscDao.BlacklistDao blacklistDao;
    private MiscDao.RoomListDao roomListDao;

    public MiscManager() {
        surveyDao = new MiscDao.SurveyDao();
        pingDao = new MiscDao.PingDao();
        jobDao = new MiscDao.JobDao();
        analysisDao = new MiscDao.AnalysisDao();
        blacklistDao = new MiscDao.BlacklistDao();
        roomListDao = new MiscDao.RoomListDao();
    }

    /* ************************************************************ SURVEYS */

    public synchronized void addSurvey(Survey survey) {
        surveyDao.add(survey);
        EtagManager.getInstance().regenerateETag(Survey.class);
    }

    public List<Survey> getAllSurveysFromNode(String nodeId) {
        SensorManager.getInstance().getSensorNodeById(nodeId);
        return surveyDao.findByNodeId(nodeId);
    }

    public List<Survey> getAllSurveysFromNodeSorted(String nodeId, String adapter, int limit) {
        SensorManager.getInstance().getSensorNodeById(nodeId);

        List<Survey> list = surveyDao.findByNodeAndAdapterLimit(nodeId, adapter, limit);
        Collections.sort(list);
        return list;
    }

    public List<Survey> getAllSurveysFromNodeSorted(String nodeId, int limit) {
        SensorManager.getInstance().getSensorNodeById(nodeId);
        List<Survey> list = surveyDao.findByNodeLimit(nodeId, limit);
        Collections.sort(list);
        return list;
    }

    public List<Survey> getAllSurveysFromNetworkSorted(String networkId, int limit) {
        List<Survey> list = surveyDao.findByNetworkLimit(networkId, limit);
        Collections.sort(list);
        return list;
    }

    public void deleteOldSurveys(String nodeId, int keepSurveys) {
        List<Survey> surveys;

        do {
            surveys = getAllSurveysFromNodeSorted(nodeId, keepSurveys + 100);
            for (int i = keepSurveys; i < surveys.size(); i++) {
                surveyDao.remove(surveys.get(i));
            }
        } while (surveys.size() > keepSurveys);

        EtagManager.getInstance().regenerateETag(Survey.class);
    }

    /* ************************************************************ JOBS */

    public synchronized void addJobAsStarted(JobLog jobLog, long etaMs, String title, String description) {
        jobLog.setStart(new Date());
        jobLog.setStatus(JobLog.Status.RUNNING);
        jobLog.setJobTitle(title);
        jobLog.setJobDescription(description);
        jobLog.setEstimatedRuntimeMs(etaMs);
        jobDao.add(jobLog);
        EtagManager.getInstance().regenerateETag(JobLog.class);
    }

    public synchronized void updateJob(JobLog jobLog) {
        jobDao.update(jobLog);
        EtagManager.getInstance().regenerateETag(JobLog.class);
    }

    public synchronized void endJob(JobLog jobLog, JobLog.Status status) {
        jobLog.setEnd(new Date());
        jobLog.setStatus(status);
        jobLog.setProgress(1.0d);
        if (status.equals(JobLog.Status.SUCCESS)) {
            jobLog.setStatusDescription("Succesful");
        } else if (status.equals(JobLog.Status.ERROR)) {
            jobLog.setStatusDescription("Error");
        } else if (status.equals(JobLog.Status.CANCEL)) {
            jobLog.setStatusDescription("Canceld");
        }
        jobDao.update(jobLog);
        EtagManager.getInstance().regenerateETag(JobLog.class);
    }

    public synchronized void endJob(JobLog jobLog, JobLog.Status status, String statusDescription) {
        jobLog.setEnd(new Date());
        jobLog.setStatus(status);
        jobLog.setProgress(1.0d);
        jobLog.setStatusDescription(statusDescription);
        jobDao.update(jobLog);
        EtagManager.getInstance().regenerateETag(JobLog.class);
    }

    public List<JobLog> getAllJobLogsFromNode(String nodeId) {
        SensorManager.getInstance().getSensorNodeById(nodeId); //check id
        return fillWithProgress(jobDao.findByNodeId(nodeId));
    }

    public List<JobLog> getAllJobLogsFromNetwork(String networkId) {
        SensorManager.getInstance().checkIfNetworkExists(networkId); //check id
        return fillWithProgress(jobDao.findByNetworkId(networkId));
    }

    public List<JobLog> getAllJobLogsFromNetworkSorted(String networkId, int limit) {
        SensorManager.getInstance().checkIfNetworkExists(networkId); //check id
        return fillWithProgress(jobDao.findByNetworkIdLimit(networkId, limit));
    }

    private List<JobLog> fillWithProgress(List<JobLog> list) {
        boolean shouldResetCache = false;
        for (JobLog jobLog : list) {
            if (jobLog.getStatus() != null && jobLog.getStatus().equals(JobLog.Status.RUNNING)) {
                jobLog.setProgress(SchedulerManager.getInstance().getProgress(jobLog.getJobId()));
                shouldResetCache = true;
            }
        }
        if (shouldResetCache) {
            EtagManager.getInstance().regenerateETag(JobLog.class);
        }

        return list;
    }

    /* ************************************************************ PING */

    public synchronized void addPing(PingLog ping) {
        pingDao.add(ping);
        EtagManager.getInstance().regenerateETag(PingLog.class);
    }

    public List<PingLog> getAllPingLogsFromNode(String nodeId) {
        return pingDao.findByNodeId(nodeId);
    }

    public List<PingLog> getAllPingLogsFromNodeSorted(String nodeId, int limit) {
        SensorManager.getInstance().getSensorNodeById(nodeId); //check id
        return pingDao.findByNodeSortByDate(nodeId, limit);
    }

    /* ************************************************************ ANALYSIS */

    public synchronized void addAnaylsis(Analysis analysis) {
        analysisDao.add(analysis);
        EtagManager.getInstance().regenerateETag(Analysis.class);
    }

    public synchronized Analysis updateAnaylsis(Analysis analysis) {
        analysisDao.update(analysis);
        EtagManager.getInstance().regenerateETag(Analysis.class);
        return analysis;
    }

    public Analysis getByAnalysisId(String analysisId) {
        Analysis analysis = analysisDao.findByAnalysisId(analysisId);
        if (analysis == null) {
            throw new ResourceNotFoundException("Could not find Analysis with id " + analysisId);
        }
        return analysis;
    }

//    public Analysis getActiveAnalysis(String networkId) {
//        Analysis analysis = analysisDao.findByAnalysisId(analysisId);
//        if(analysis == null) {
//            List<Analysis> analysisList = analysisDao.findByNetworkIdLimit(networkId, 1);
//            if(!analysisList.isEmpty()) {
//                return  analysisList.get(0);
//            } else {
//                return null;
//            }
//        }
//        return analysis;
//    }

    public List<AnalysisMetaData> getAnalysisMetaListSortedByDate(String networkId, int limit) {
        SensorManager.getInstance().checkIfNetworkExists(networkId); //check id
        List<Analysis> analysis = analysisDao.findByNetworkIdLimit(networkId, limit);
        List<AnalysisMetaData> metaDatas = new ArrayList<AnalysisMetaData>();
        for (Analysis a : analysis) {
            metaDatas.add(new AnalysisMetaData(a));
        }

        return metaDatas;
    }

    public List<AnalysisMetaData> getAnalysisMetaListSortedByDatePagination(String networkId, String startKeyDate, String startDocId, int limit) {
        SensorManager.getInstance().checkIfNetworkExists(networkId); //check id
        List<Analysis> analysis = analysisDao.findByNetworkIdAndStartKeyLimit(networkId, startKeyDate, startDocId, limit);
        List<AnalysisMetaData> metaDatas = new ArrayList<AnalysisMetaData>();
        for (Analysis a : analysis) {
            metaDatas.add(new AnalysisMetaData(a));
        }

        return metaDatas;
    }

    public List<Analysis> getAnalysisListForNetworkId(String networkId, int limit) {
        return analysisDao.findByNetworkIdLimit(networkId, limit);
    }

    public Analysis getLatestAnalysis(String networkId) {
        List<Analysis> analysisList = getAnalysisListForNetworkId(networkId, 1);
        if (!analysisList.isEmpty()) {
            return analysisList.get(0);
        }
        return null;
    }

    public Analysis updateDistanceMultiplier(String analysisId, EFrequencyRange range, Double multiplier) {
        Analysis analysis = getByAnalysisId(analysisId);
        analysis.getDistMultiMap().put(range, multiplier);
        return updateAnaylsis(analysis);
    }

    public void deleteAnalysis(String analysisId) {
        Analysis a = getByAnalysisId(analysisId);
        analysisDao.remove(a);
        EtagManager.getInstance().regenerateETag(Analysis.class);
    }

    /* ************************************************************ BLACKLIST */

    public synchronized void addBlacklist(Blacklist blacklist) {
        blacklistDao.add(blacklist);
        EtagManager.getInstance().regenerateETag(Blacklist.class);
    }

    public synchronized void updateBlacklist(Blacklist blacklist) {
        blacklistDao.update(blacklist);
        EtagManager.getInstance().regenerateETag(Blacklist.class);
    }

    public Blacklist getBlacklistByNetworkId(String networkId) {
        SensorManager.getInstance().checkIfNetworkExists(networkId); //check id

        Blacklist blacklist = blacklistDao.findByNetworkId(networkId);

        if (blacklist == null) {
            blacklist = new Blacklist();
            blacklist.setNetworkId(networkId);
            addBlacklist(blacklist);
        }

        return blacklist;
    }

    /* ************************************************************ ROOMLIST */

    public synchronized void addRoomList(RoomList roomList) {
        roomListDao.add(roomList);
        EtagManager.getInstance().regenerateETag(RoomList.class);
    }

    public RoomList updateRoomlist(RoomList roomList) {
        roomListDao.update(roomList);
        EtagManager.getInstance().regenerateETag(RoomList.class);
        return roomList;
    }

    public RoomList getRoomlistByNetworkId(String networkId) {
        SensorManager.getInstance().checkIfNetworkExists(networkId); //check id

        RoomList roomList = roomListDao.findByNetworkId(networkId);

        if (roomList == null) {
            roomList = new RoomList();
            roomList.setNetworkId(networkId);
            addRoomList(roomList);
        }

        return roomList;
    }

    public RoomList addNewMacRoomMapping(String networkId, String macAddress, String roomId) {
        SensorManager.getInstance().checkIfNetworkExists(networkId); //check id
        RoomList roomList = getRoomlistByNetworkId(networkId);

        if (roomId == null || roomId.isEmpty()) {
            roomList.getMacToRoomIdMap().remove(macAddress);
        } else {
            boolean found = false;

            for (RoomList.Room room : roomList.getRooms()) {
                if (room.getRoomId().equals(roomId)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw new ResourceNotFoundException("RoomId " + roomId + " was not found");
            }

            roomList.getMacToRoomIdMap().put(macAddress, roomId);
        }
        return updateRoomlist(roomList);
    }

}

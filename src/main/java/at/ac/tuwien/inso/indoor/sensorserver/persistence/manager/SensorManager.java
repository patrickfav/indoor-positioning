package at.ac.tuwien.inso.indoor.sensorserver.persistence.manager;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.dao.SensorDao;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.Adapter;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNetwork;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNode;
import at.ac.tuwien.inso.indoor.sensorserver.services.ApiConst;
import at.ac.tuwien.inso.indoor.sensorserver.services.exceptions.GenericServerException;
import at.ac.tuwien.inso.indoor.sensorserver.services.exceptions.ResourceNotFoundException;
import at.ac.tuwien.inso.indoor.sensorserver.services.exceptions.SensorRequestException;
import at.ac.tuwien.inso.indoor.sensorserver.services.requests.OUILookupRequest;
import at.ac.tuwien.inso.indoor.sensorserver.services.scheduler.SchedulerManager;
import org.apache.log4j.Logger;
import org.quartz.SchedulerException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by PatrickF on 08.09.2014.
 */
public class SensorManager extends AManager {

    private static Logger log = Logger.getLogger(SensorManager.class);
    private static SensorManager instance;

    public static SensorManager getInstance() {
        if (instance == null) {
            instance = new SensorManager();
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    private SensorDao sensorDoa;

    public SensorManager() {
        sensorDoa = new SensorDao();
    }

    /* **********************************************************************************************  NETWORK */
    public void addSensorNetwork(SensorNetwork network) {
        sensorDoa.getSensorNetworkDao().add(network);
        try {
            SchedulerManager.getInstance().addSurveyJob(network);
            SchedulerManager.getInstance().addPingLogJob(network);
            SchedulerManager.getInstance().addAnalysisLogJob(network);
        } catch (SchedulerException e) {
            throw new GenericServerException("Could not add jobs to scheduler for network " + network, e);
        }
        EtagManager.getInstance().regenerateETag(SensorNetwork.class);
    }

    public void updateSensorNetwork(SensorNetwork network, boolean updateSchedule) {
        sensorDoa.getSensorNetworkDao().update(network);
        if (updateSchedule) {
            try {
                SchedulerManager.getInstance().cancelSurveyJob(network.getNetworkId());
                SchedulerManager.getInstance().cancelPingJob(network.getNetworkId());
                SchedulerManager.getInstance().cancelAnalysisJob(network.getNetworkId());

                SchedulerManager.getInstance().addSurveyJob(network);
                SchedulerManager.getInstance().addPingLogJob(network);
                SchedulerManager.getInstance().addAnalysisLogJob(network);
            } catch (SchedulerException e) {
                throw new GenericServerException("Could not update jobs to scheduler for network " + network, e);
            }
        }
        EtagManager.getInstance().regenerateETag(SensorNetwork.class);
    }

    public List<SensorNetwork> getAllSensorNetworksNonDeleted() {
        List<SensorNetwork> networks = sensorDoa.getSensorNetworkDao().getAll();
        List<SensorNetwork> nonDeleted = new ArrayList<SensorNetwork>();

        for (SensorNetwork network : networks) {
            if (!network.isDeleted()) {
                nonDeleted.add(network);
            }
        }
        Collections.sort(nonDeleted);
        return nonDeleted;
    }

    public SensorNetwork getSensorNetworkById(String networkId) {
        SensorNetwork network = sensorDoa.getSensorNetworkDao().getByNetworkId(networkId);
        if (network == null) {
            throw new ResourceNotFoundException("Could not find SensorNetwork with id " + networkId);
        }
        return network;
    }

    public void checkIfNetworkExists(String networkId) {
        SensorNetwork network = sensorDoa.getSensorNetworkDao().getByNetworkId(networkId);
        if (network == null) {
            throw new ResourceNotFoundException("Could not find SensorNetwork with id " + networkId);
        }
    }

    public void setNetworkDeleted(String networkId) {
        SensorNetwork network = getSensorNetworkById(networkId);
        network.setDeleted(true);
        network.setCronEnabled(false);
        updateSensorNetwork(network, false);

        SchedulerManager.getInstance().cancelSurveyJob(network.getNetworkId());
        SchedulerManager.getInstance().cancelPingJob(network.getNetworkId());
        SchedulerManager.getInstance().cancelAnalysisJob(network.getNetworkId());

    }

    public SensorNetwork saveBlueprintImage(String networkId, InputStream inputStream, String contentType) {
        return sensorDoa.getSensorNetworkDao().addAttachment(networkId, "blueprint_" + networkId, inputStream, contentType, true);
    }

    /* **********************************************************************************************  NODE */

    public void updateSensorNode(SensorNode node) {
        sensorDoa.getSensorNodeDao().update(node);
        EtagManager.getInstance().regenerateETag(SensorNode.class);
    }

    public List<SensorNode> getAllNodesFromNetwork(String networkId) {
        if (null == sensorDoa.getSensorNetworkDao().getByNetworkId(networkId)) {
            throw new ResourceNotFoundException("Could not find SensorNetwork with id " + networkId);
        }

        return sensorDoa.getSensorNodeDao().findByNetworkId(networkId);
    }

    public void addSensorNode(SensorNode node) {
        Iterator<Adapter> iter = node.getAdapters().iterator();
        while (iter.hasNext()) {
            if (!iter.next().getMode().equalsIgnoreCase(ApiConst.OpenWrtConst.MODE_MASTER)) {
                iter.remove();
                log.info("Ignore Adapter because not master mode.");
            }
        }

        SchedulerManager.getInstance().schedulePingJob(getSensorNetworkById(node.getNetworkId()), true);

        for (Adapter adapter : node.getAdapters()) {
            try {
                adapter.setOuiMacInfo(new OUILookupRequest(adapter.getMacAddress()).startRequest());
            } catch (SensorRequestException e) {
                log.warn("Could not get OUI Info for node " + node + " and adapter " + adapter, e);
            }
        }

        sensorDoa.getSensorNodeDao().add(node);
        EtagManager.getInstance().regenerateETag(SensorNode.class);
    }

    public SensorNode getSensorNodeById(String nodeId) {
        SensorNode node = sensorDoa.getSensorNodeDao().findByNodeId(nodeId);
        if (node == null) {
            throw new ResourceNotFoundException("Could not find SensorNode with id " + nodeId);
        }

        return node;
    }
}

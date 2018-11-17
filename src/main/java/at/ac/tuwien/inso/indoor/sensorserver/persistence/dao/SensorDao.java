package at.ac.tuwien.inso.indoor.sensorserver.persistence.dao;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.couchdb.DB;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNetwork;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNode;
import org.ektorp.AttachmentInputStream;
import org.ektorp.support.CouchDbRepositorySupport;

import java.io.InputStream;
import java.util.List;

/**
 * Created by PatrickF on 08.09.2014.
 */
public class SensorDao {
    private SensorNetworkDao sensorNetworkDao;
    private SensorNodeDao sensorNodeDao;

    public SensorDao() {
        sensorNetworkDao = new SensorNetworkDao();
        sensorNodeDao = new SensorNodeDao();
    }

    public SensorNetworkDao getSensorNetworkDao() {
        return sensorNetworkDao;
    }

    public SensorNodeDao getSensorNodeDao() {
        return sensorNodeDao;
    }

    public class SensorNetworkDao extends CouchDbRepositorySupport<SensorNetwork> {
        public SensorNetworkDao() {
            super(SensorNetwork.class, DB.getInstance().getMainDB());
        }

        public SensorNetwork getByNetworkId(String networkId) {
            List<SensorNetwork> networks = queryView("by_networkId", String.valueOf(networkId));

            if (networks.isEmpty()) {
                return null;
            } else {
                return networks.get(0);
            }
        }

        public SensorNetwork addAttachment(String networkId, String attachmentId, InputStream inputStream, String contentType, boolean deleteIfAlreadyExistsBeforeAdding) {
            SensorNetwork network = getByNetworkId(networkId);
            String rev = network.getRevision();
            if (deleteIfAlreadyExistsBeforeAdding) {
                try {
                    AttachmentInputStream attachmentInputStream = db.getAttachment(network.getId(), attachmentId);
                    attachmentInputStream.close();
                    rev = db.deleteAttachment(network.getId(), rev, attachmentId);
                } catch (Exception e) {/*do nothing; not found and thats ok*/}
            }

            AttachmentInputStream attachment = new AttachmentInputStream(attachmentId, inputStream, contentType);
            rev = db.createAttachment(network.getId(), rev, attachment);
            return getByNetworkId(networkId);
        }
    }

    public class SensorNodeDao extends CouchDbRepositorySupport<SensorNode> {
        public SensorNodeDao() {
            super(SensorNode.class, DB.getInstance().getMainDB());
        }

        public List<SensorNode> findByNetworkId(String networkId) {
            return queryView("by_networkId", String.valueOf(networkId));
        }

        public SensorNode findByNodeId(String nodeId) {
            List<SensorNode> nodes = queryView("by_nodeId", String.valueOf(nodeId));

            if (nodes.isEmpty()) {
                return null;
            } else {
                return nodes.get(0);
            }
        }
    }
}

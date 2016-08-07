package at.ac.tuwien.inso.indoor.sensorserver.persistence.dao;

import at.ac.tuwien.inso.indoor.sensorserver.services.ServerConfig;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.couchdb.DB;
import org.ektorp.support.CouchDbRepositorySupport;

import java.util.List;
import java.util.UUID;

/**
 * Created by PatrickF on 08.09.2014.
 */
public class ServerConfigDao extends CouchDbRepositorySupport<ServerConfig>  {

    public ServerConfigDao() {
        super(ServerConfig.class, DB.getInstance().getMainDB());
    }

    public ServerConfig getServerConfig() {
        List<ServerConfig> list= getAll();
        if(!list.isEmpty()) {
            return list.get(list.size()-1);
        }
        return null;
    }

    public void setNewServerConfig(ServerConfig config) {
        clearConfig();
        if(config.getId() == null) config.setId(UUID.randomUUID().toString());
        config.setRevision(null);
        add(config);

    }

    public void clearConfig() {
        for (ServerConfig serverConfig : getAll()) {
            remove(serverConfig);
        }
    }
}

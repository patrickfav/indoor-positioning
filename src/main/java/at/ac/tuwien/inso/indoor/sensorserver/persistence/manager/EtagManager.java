package at.ac.tuwien.inso.indoor.sensorserver.persistence.manager;

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

import java.util.*;

/**
 * Created by PatrickF on 03.10.2014.
 */
public class EtagManager {
    private static Logger log = Logger.getLogger(EtagManager.class);
    private static EtagManager instance;

    public static EtagManager getInstance() {
        if (instance == null) {
            instance = new EtagManager(Survey.class, PingLog.class, JobLog.class, Analysis.class,
                    Blacklist.class, RoomList.class, SensorNetwork.class, SensorNode.class, ServerConfig.class);
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    private Map<String, String> eTagMap = new HashMap<String, String>();

    public EtagManager(Class<?>... models) {
        regenerateETag(models);
    }

    public void regenerateETag(Class<?>... classes) {
        if (classes.length == 0) {
            throw new IllegalArgumentException("Must at least pass 1 class");
        }

        for (Class<?> aClass : classes) {
            eTagMap.put(aClass.getName(), UUID.randomUUID().toString());
        }
    }

    public String getQuotedETag(Class<?>... classes) {
        return "\"" + getETag(classes) + "\"";
    }

    public String getETag(Class<?>... classes) {
        if (classes.length == 0) {
            throw new IllegalArgumentException("Must at least pass 1 class");
        }

        Arrays.sort(classes, new ClassNameComp()); //sort that you get the same order every time

        StringBuilder sb = new StringBuilder();
        for (Class<?> clazz : classes) {
            if (!eTagMap.containsKey(clazz.getName())) {
                regenerateETag(clazz);
            }
            sb.append(eTagMap.get(clazz.getName()));
        }
        return sb.toString();
    }

    private class ClassNameComp implements Comparator<Class<?>> {
        @Override
        public int compare(Class<?> o1, Class<?> o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }
}

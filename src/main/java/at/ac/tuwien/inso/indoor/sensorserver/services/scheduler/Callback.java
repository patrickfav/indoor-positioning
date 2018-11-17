package at.ac.tuwien.inso.indoor.sensorserver.services.scheduler;

/**
 * Created by PatrickF on 16.09.2014.
 */
public interface Callback<T> {
    void callback(T t);
}

package at.ac.tuwien.inso.indoor.sensorserver.services;

/**
 * Created by PatrickF on 08.09.2014.
 */
public final class ApiConst {

    public static final String DEFAULT_ROUTER_SERVICES_RES = "/cgi-bin";

    public static final String ROUTER_SERVICE_IWINFO = "/iwinfo";
    public static final String ROUTER_SERVICE_WS_PING = "/ping.html";
    public static final String ROUTER_SERVICE_SCRIPT_PING = "/ping";
    public static final String ROUTER_SERVICE_REBOOT = "/reboot";

    public static final String ROUTER_ADAPTER_QUERY_PARAM = "adapter";
    public static final String ROUTER_SCAN_BOOL_QUERY_PARAM = "scan";

    public final class CacheControl {
        public static final int SENSOR_NETWORK = 10 * 1;
        public static final int PING = 10 * 1;
        public static final int JOB = 3 * 1;
        public static final int SURVEY = 10 * 1;
        public static final int ANALYSIS = 10 * 1;
        public static final int BLACKLIST = 10 * 1;
        public static final int ROOMLIST = 10 * 1;
        public static final int SERVER_CONFIG = 10 * 1;

        private CacheControl() {
        }
    }

    public final class OpenWrtConst {
        public static final String MODE_MASTER = "master";

        private OpenWrtConst() {

        }
    }

    private ApiConst() {
    }
}

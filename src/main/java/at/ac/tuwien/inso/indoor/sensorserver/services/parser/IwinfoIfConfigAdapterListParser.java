package at.ac.tuwien.inso.indoor.sensorserver.services.parser;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by PatrickF on 12.09.2014.
 */
public final class IwinfoIfConfigAdapterListParser {
    private static Logger log = Logger.getLogger(IwinfoIfConfigAdapterListParser.class);

    private IwinfoIfConfigAdapterListParser() {
    }

    public static List<String> parse(String ifConfig) {
        return parse(ifConfig, false);
    }

    public static List<String> parse(String ifConfig, boolean shouldLog) {
        List<String> adapterList = new ArrayList<String>();

        if (shouldLog) log.debug("ifconfig: " + ifConfig);

        Pattern pattern = Pattern.compile("(wl\\d|wl\\d\\.\\d|wl\\d-\\d|" +
                "wlan\\d|wlan\\d\\.\\d|wlan\\d-\\d|" +
                "ath\\d|ath\\d\\.\\d|ath\\d-\\d)\\s+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(ifConfig);

        while (matcher.find()) {
            adapterList.add(matcher.group(1).trim());
        }

        if (shouldLog) log.debug("found adapters: " + adapterList);

        return adapterList;
    }
}

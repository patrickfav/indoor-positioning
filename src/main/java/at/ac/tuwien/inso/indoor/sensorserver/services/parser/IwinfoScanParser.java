package at.ac.tuwien.inso.indoor.sensorserver.services.parser;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.WlanScanNode;
import at.ac.tuwien.inso.indoor.sensorserver.util.RadioUtil;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by PatrickF on 12.09.2014.
 */
public class IwinfoScanParser {
    private static Logger log = Logger.getLogger(IwinfoScanParser.class);

    public static List<WlanScanNode> parse(String iwinfoScanString) {
        return parse(iwinfoScanString, false);
    }

    public static List<WlanScanNode> parse(String iwinfoScanString, boolean shouldLog) {
        List<WlanScanNode> wlanScanNodeList = new ArrayList<WlanScanNode>();
        if (iwinfoScanString == null || iwinfoScanString.trim().length() == 0) {
            return wlanScanNodeList;
        }

        String[] cells = iwinfoScanString.trim().split("\\n\\n");

        if (shouldLog) log.debug("Found " + cells.length + " APs");

        for (String cell : cells) {
            try {
                wlanScanNodeList.add(parseCell(cell, shouldLog));
            } catch (IllegalStateException e) {
                log.warn("Could not parse element: " + cell);
            }
        }

        if (shouldLog) log.debug("Found APs: " + wlanScanNodeList);

        return wlanScanNodeList;
    }

    private static WlanScanNode parseCell(String cell, boolean shouldLog) {
        try {
            WlanScanNode m = new WlanScanNode();

            if (shouldLog) log.debug("Cell: " + cell);
            Pattern macAddressPattern = Pattern.compile("Address:\\s*(([0-9A-F]{2}[:-]){5}([0-9A-F]{2}))\\s*\\n", Pattern.CASE_INSENSITIVE);
            Matcher macAdressMatcher = macAddressPattern.matcher(cell);
            macAdressMatcher.find();
            m.setMacAddress(macAdressMatcher.group(1).replace("-", ":"));

            Pattern ssidPattern = Pattern.compile("ESSID:\\s*\"(.+)\"\\s*\\n|ESSID:\\s*(unknown)\\s*\\n", Pattern.CASE_INSENSITIVE);
            Matcher ssidMatcher = ssidPattern.matcher(cell);
            ssidMatcher.find();
            m.setSsid(ssidMatcher.group(1));

            Pattern channelPattern = Pattern.compile("Channel:\\s*(\\d+)\\s*\\n", Pattern.CASE_INSENSITIVE);
            Matcher channelMatcher = channelPattern.matcher(cell);
            channelMatcher.find();
            m.setChannel(Integer.valueOf(channelMatcher.group(1)));

            Pattern signalPattern = Pattern.compile("Signal:\\s*(-?\\d+)\\s*dBm", Pattern.CASE_INSENSITIVE);
            Matcher signallMatcher = signalPattern.matcher(cell);
            signallMatcher.find();
            m.setSignalStrengthDbm(Double.valueOf(signallMatcher.group(1)));

            Pattern encryptionPattern = Pattern.compile("Encryption:\\s*(.+)\\s*", Pattern.CASE_INSENSITIVE);
            Matcher encryptionMatcher = encryptionPattern.matcher(cell);
            encryptionMatcher.find();
            m.setEncryption(encryptionMatcher.group(1));

            m.setFrequencyRange(RadioUtil.guessFrequencyFromChannel(m.getChannel()));

            return m;
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not parse scan element", e);
        }
    }
}

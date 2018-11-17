package at.ac.tuwien.inso.indoor.sensorserver.services.parser;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.Adapter;
import at.ac.tuwien.inso.indoor.sensorserver.util.RadioUtil;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by PatrickF on 12.09.2014.
 */
public class IwinfoAdapterListParser {
    private static Logger log = Logger.getLogger(IwinfoAdapterListParser.class);

    public static List<Adapter> parse(String iwinfoAdapterList) {
        return parse(iwinfoAdapterList, false);
    }

    public static List<Adapter> parse(String iwinfoAdapterList, boolean shouldLog) {
        List<Adapter> adapterList = new ArrayList<Adapter>();
        if (iwinfoAdapterList == null || iwinfoAdapterList.trim().length() == 0) {
            return adapterList;
        }

        String[] adapters = iwinfoAdapterList.trim().split("\\n\\n");

        if (shouldLog) log.debug("Found " + adapters.length + " adapter");

        for (String adapter : adapters) {
            adapterList.add(parseAdapter(adapter, shouldLog));
        }

        if (shouldLog) log.debug("Found Adapters: " + adapterList);

        return adapterList;
    }

    private static Adapter parseAdapter(String cell, boolean shouldLog) {
        Adapter a = new Adapter();

        if (shouldLog) log.debug("Cell: " + cell);

        Pattern namePattern = Pattern.compile("^((wlan\\d(-\\d|\\.\\d)?)|(wl\\d(-\\d|\\.\\d)?)|(ath\\d(-\\d|\\.\\d)?)|(eth\\d(-\\d|\\.\\d)?)|(br\\d)(-\\d|\\.\\d)?)\\s+", Pattern.CASE_INSENSITIVE);
        Matcher nameMatcher = namePattern.matcher(cell);
        nameMatcher.find();
        a.setName(nameMatcher.group(1));

        Pattern macAddressPattern = Pattern.compile(" Access Point:\\s*(([0-9A-F]{2}[:-]){5}([0-9A-F]{2}))\\s*\\n", Pattern.CASE_INSENSITIVE);
        Matcher macAdressMatcher = macAddressPattern.matcher(cell);
        macAdressMatcher.find();
        a.setMacAddress(macAdressMatcher.group(1).replace("-", ":"));

        Pattern ssidPattern = Pattern.compile("ESSID:\\s*\"(.+)\"\\s*\\n|ESSID:\\s*(unknown)\\s*\\n", Pattern.CASE_INSENSITIVE);
        Matcher ssidMatcher = ssidPattern.matcher(cell);
        ssidMatcher.find();
        a.setSsid(ssidMatcher.group(1));

        Pattern modePattern = Pattern.compile("Mode:\\s*(\\w{4,12})\\s*", Pattern.CASE_INSENSITIVE);
        Matcher modeMatcher = modePattern.matcher(cell);
        modeMatcher.find();
        a.setMode(modeMatcher.group(1));

        Pattern channelPattern = Pattern.compile("Channel:\\s*(\\d+)\\s+", Pattern.CASE_INSENSITIVE);
        Matcher channelMatcher = channelPattern.matcher(cell);
        channelMatcher.find();
        a.setChannel(Integer.valueOf(channelMatcher.group(1)));

        Pattern nosePattern = Pattern.compile("Noise:\\s*(-?\\d+)\\s*dBm", Pattern.CASE_INSENSITIVE);
        Matcher noiseMatcher = nosePattern.matcher(cell);
        noiseMatcher.find();
        a.setNoiseDbm(Integer.valueOf(noiseMatcher.group(1)));

        Pattern txPowerPattern = Pattern.compile("Tx-Power:\\s*(-?\\d+)\\s*dBm", Pattern.CASE_INSENSITIVE);
        Matcher txPowerMatcher = txPowerPattern.matcher(cell);
        txPowerMatcher.find();
        a.setTxPowerDbm(Integer.valueOf(txPowerMatcher.group(1)));

        a.setFrequencyRange(RadioUtil.guessFrequencyFromChannel(a.getChannel()));
        return a;
    }
}

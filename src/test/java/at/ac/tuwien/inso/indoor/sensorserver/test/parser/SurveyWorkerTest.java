package at.ac.tuwien.inso.indoor.sensorserver.test.parser;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.EFrequencyRange;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.WlanScanNode;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNode;
import at.ac.tuwien.inso.indoor.sensorserver.services.scheduler.SurveyCallable;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by PatrickF on 14.09.2014.
 */
public class SurveyWorkerTest {
    private static Logger log = Logger.getLogger(SurveyWorkerTest.class);

    private List<WlanScanNode> wlanScanNodes;
    private Random rnd = new Random(322768354L);
    @Before
    public void setup() {
        wlanScanNodes = new ArrayList<WlanScanNode>(3);
        WlanScanNode n1= new WlanScanNode();
        n1.setSsid("UPC1373981");
        n1.setChannel(5);
        n1.setMacAddress("8C:04:FF:02:E2:1F");
        n1.setEncryption("mixed WPA/WPA2 PSK (TKIP, CCMP)");
        n1.setFrequencyRange(EFrequencyRange.WLAN_2_4Ghz);
        n1.setSignalStrengthDbm(-84d);

        wlanScanNodes.add(n1);

        WlanScanNode n2= new WlanScanNode();
        n2.setSsid("Amtmann");
        n2.setChannel(6);
        n2.setMacAddress("FC:94:E3:3F:38:8E");
        n2.setEncryption("WPA2 PSK (CCMP)");
        n2.setFrequencyRange(EFrequencyRange.WLAN_2_4Ghz);
        n2.setSignalStrengthDbm(-75d);

        wlanScanNodes.add(n2);

        WlanScanNode n3= new WlanScanNode();
        n3.setSsid("A1-f35570");
        n3.setChannel(6);
        n3.setMacAddress("74:88:8B:F3:55:75");
        n3.setEncryption("mixed WPA/WPA2 PSK (TKIP, CCMP)");
        n3.setFrequencyRange(EFrequencyRange.WLAN_2_4Ghz);
        n3.setSignalStrengthDbm(-93d);

        wlanScanNodes.add(n3);

        WlanScanNode n4= new WlanScanNode();
        n4.setSsid("Willi Wonkas Router");
        n4.setChannel(13);
        n4.setMacAddress("00:1E:69:63:B5:FF");
        n4.setEncryption("mixed WPA/WPA2 PSK (CCMP)");
        n4.setFrequencyRange(EFrequencyRange.WLAN_2_4Ghz);
        n4.setSignalStrengthDbm(-57d);

        wlanScanNodes.add(n4);
    }

    @Test
    public void testNormalSurveyShouldOk() throws Exception{
        SurveyCallable worker = new SurveyCallable(100,3,"wlan0",new SensorNode()) {
            @Override
            protected List<WlanScanNode> scan(SensorNode node, String adapterName) {
                List<WlanScanNode> measurements = new ArrayList<WlanScanNode>();

                for (WlanScanNode measurement : wlanScanNodes) {
                    WlanScanNode wlanScanNode = new WlanScanNode();
                    wlanScanNode.setSsid(measurement.getSsid());
                    wlanScanNode.setChannel(measurement.getChannel());
                    wlanScanNode.setMacAddress(measurement.getMacAddress());
                    wlanScanNode.setEncryption(measurement.getEncryption());
                    wlanScanNode.setFrequencyRange(measurement.getFrequencyRange());
                    wlanScanNode.setSignalStrengthDbm(measurement.getSignalStrengthDbm() - rnd.nextInt(10)+5);

                    measurements.add(wlanScanNode);
                }

                if(rnd.nextDouble() > 0.7d) {
                    measurements.remove(rnd.nextInt(measurements.size()));
                }

                log.debug(measurements);

                return measurements;
            }
        };

        worker.call();
    }

}

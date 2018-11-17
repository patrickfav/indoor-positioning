package at.ac.tuwien.inso.indoor.sensorserver.test.parser.examples;

/**
 * Created by PatrickF on 13.09.2014.
 */
public final class XMLExamples {
    public static final String xmlExample_iwinfo_adapter_list =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<router>\n" +
                    "<adapter-list>\n" +
                    "<![CDATA[\n" +
                    "wlan0     ESSID: \"Node xxx.xxx.2.1 - TPLink\"\n" +
                    "          Access Point: C0:4A:00:AB:E3:B6\n" +
                    "          Mode: Master  Channel: 13 (2.472 GHz)\n" +
                    "          Tx-Power: 18 dBm  Link Quality: unknown/70\n" +
                    "          Signal: unknown  Noise: -95 dBm\n" +
                    "          Bit Rate: unknown\n" +
                    "          Encryption: WPA2 PSK (CCMP)\n" +
                    "          Type: nl80211  HW Mode(s): 802.11bgn\n" +
                    "          Hardware: unknown [Generic MAC80211]\n" +
                    "          TX power offset: unknown\n" +
                    "          Frequency offset: unknown\n" +
                    "          Supports VAPs: yes  PHY name: phy0\n" +
                    "\n" +
                    "]]>\n" +
                    "</adapter-list>\n" +
                    "<date>\n" +
                    "  <![CDATA[\n" +
                    "Sat Sep 13 10:02:12 GMT 2014\n" +
                    "]]>\n" +
                    "</date>\n" +
                    "<uptime>\n" +
                    "  <![CDATA[\n" +
                    " 10:02:12 up 1 day, 14:12,  load average: 0.00, 0.02, 0.04\n" +
                    "]]>\n" +
                    "</uptime>\n" +
                    "<ifconfig>\n" +
                    "  <![CDATA[\n" +
                    "br-eth0   Link encap:Ethernet  HWaddr C0:4A:00:AB:E3:B6  \n" +
                    "          UP BROADCAST MULTICAST  MTU:1508  Metric:1\n" +
                    "          RX packets:0 errors:0 dropped:0 overruns:0 frame:0\n" +
                    "          TX packets:0 errors:0 dropped:0 overruns:0 carrier:0\n" +
                    "          collisions:0 txqueuelen:0 \n" +
                    "          RX bytes:0 (0.0 B)  TX bytes:0 (0.0 B)\n" +
                    "\n" +
                    "br-lan    Link encap:Ethernet  HWaddr C0:4A:00:AB:E3:B5  \n" +
                    "          inet addr:192.168.2.1  Bcast:192.168.2.255  Mask:255.255.255.0\n" +
                    "          inet6 addr: fe80::c24a:ff:feab:e3b5/64 Scope:Link\n" +
                    "          inet6 addr: fdeb:92c3:70e2::1/64 Scope:Global\n" +
                    "          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1\n" +
                    "          RX packets:27720 errors:0 dropped:0 overruns:0 frame:0\n" +
                    "          TX packets:29599 errors:0 dropped:0 overruns:0 carrier:0\n" +
                    "          collisions:0 txqueuelen:0 \n" +
                    "          RX bytes:2528944 (2.4 MiB)  TX bytes:5977296 (5.7 MiB)\n" +
                    "\n" +
                    "eth0      Link encap:Ethernet  HWaddr C0:4A:00:AB:E3:B6  \n" +
                    "          UP BROADCAST MULTICAST  MTU:1508  Metric:1\n" +
                    "          RX packets:0 errors:0 dropped:0 overruns:0 frame:0\n" +
                    "          TX packets:0 errors:0 dropped:0 overruns:0 carrier:0\n" +
                    "          collisions:0 txqueuelen:1000 \n" +
                    "          RX bytes:0 (0.0 B)  TX bytes:0 (0.0 B)\n" +
                    "          Interrupt:4 \n" +
                    "\n" +
                    "eth1      Link encap:Ethernet  HWaddr C0:4A:00:AB:E3:B5  \n" +
                    "          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1\n" +
                    "          RX packets:33702 errors:0 dropped:74 overruns:0 frame:0\n" +
                    "          TX packets:27635 errors:0 dropped:0 overruns:0 carrier:0\n" +
                    "          collisions:0 txqueuelen:1000 \n" +
                    "          RX bytes:3404649 (3.2 MiB)  TX bytes:5785312 (5.5 MiB)\n" +
                    "          Interrupt:5 \n" +
                    "\n" +
                    "lo        Link encap:Local Loopback  \n" +
                    "          inet addr:127.0.0.1  Mask:255.0.0.0\n" +
                    "          inet6 addr: ::1/128 Scope:Host\n" +
                    "          UP LOOPBACK RUNNING  MTU:65536  Metric:1\n" +
                    "          RX packets:1094708 errors:0 dropped:0 overruns:0 frame:0\n" +
                    "          TX packets:1094708 errors:0 dropped:0 overruns:0 carrier:0\n" +
                    "          collisions:0 txqueuelen:0 \n" +
                    "          RX bytes:74468379 (71.0 MiB)  TX bytes:74468379 (71.0 MiB)\n" +
                    "\n" +
                    "wlan0     Link encap:Ethernet  HWaddr C0:4A:00:AB:E3:B6  \n" +
                    "          UP BROADCAST MULTICAST  MTU:1500  Metric:1\n" +
                    "          RX packets:0 errors:0 dropped:0 overruns:0 frame:0\n" +
                    "          TX packets:4 errors:0 dropped:0 overruns:0 carrier:0\n" +
                    "          collisions:0 txqueuelen:1000 \n" +
                    "          RX bytes:0 (0.0 B)  TX bytes:480 (480.0 B)\n" +
                    "\n" +
                    "]]>\n" +
                    "</ifconfig>\n" +
                    "</router>\n";

    public static final String xmlExample_iwinfo_adapter_wlan0 =
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                    "<router>\n" +
                    "<adapter name=\"wlan0\">\n" +
                    "  <info>\n" +
                    "  <![CDATA[\n" +
                    "wlan0     ESSID: \"Node xxx.xxx.2.1 - TPLink\"\n" +
                    "          Access Point: C0:4A:00:AB:E3:B6\n" +
                    "          Mode: Master  Channel: 13 (2.472 GHz)\n" +
                    "          Tx-Power: 18 dBm  Link Quality: unknown/70\n" +
                    "          Signal: unknown  Noise: -95 dBm\n" +
                    "          Bit Rate: unknown\n" +
                    "          Encryption: WPA2 PSK (CCMP)\n" +
                    "          Type: nl80211  HW Mode(s): 802.11bgn\n" +
                    "          Hardware: unknown [Generic MAC80211]\n" +
                    "          TX power offset: unknown\n" +
                    "          Frequency offset: unknown\n" +
                    "          Supports VAPs: yes  PHY name: phy0\n" +
                    "  ]]>\n" +
                    "  </info>\n" +
                    "  <scan>\n" +
                    "  <![CDATA[\n" +
                    "Cell 01 - Address: 00:1E:69:64:1D:E7\n" +
                    "          ESSID: \"UPC017649\"\n" +
                    "          Mode: Master  Channel: 1\n" +
                    "          Signal: -73 dBm  Quality: 37/70\n" +
                    "          Encryption: mixed WPA/WPA2 PSK (TKIP, CCMP)\n" +
                    "\n" +
                    "Cell 02 - Address: C4:27:95:89:89:A0\n" +
                    "          ESSID: \"UPC1932821\"\n" +
                    "          Mode: Master  Channel: 1\n" +
                    "          Signal: -53 dBm  Quality: 57/70\n" +
                    "          Encryption: mixed WPA/WPA2 PSK (TKIP, CCMP)\n" +
                    "\n" +
                    "Cell 03 - Address: 00:24:D1:88:2F:7F\n" +
                    "          ESSID: \"UPC011691\"\n" +
                    "          Mode: Master  Channel: 1\n" +
                    "          Signal: -86 dBm  Quality: 24/70\n" +
                    "          Encryption: mixed WPA/WPA2 PSK (TKIP, CCMP)\n" +
                    "\n" +
                    "Cell 04 - Address: 30:39:F2:4B:42:46\n" +
                    "          ESSID: \"PBS-4B4241\"\n" +
                    "          Mode: Master  Channel: 4\n" +
                    "          Signal: -76 dBm  Quality: 34/70\n" +
                    "          Encryption: WPA PSK (TKIP)\n" +
                    "\n" +
                    "Cell 05 - Address: 8C:04:FF:02:E2:1F\n" +
                    "          ESSID: \"UPC1373981\"\n" +
                    "          Mode: Master  Channel: 5\n" +
                    "          Signal: -77 dBm  Quality: 33/70\n" +
                    "          Encryption: mixed WPA/WPA2 PSK (TKIP, CCMP)\n" +
                    "\n" +
                    "Cell 06 - Address: D8:50:E6:A8:96:58\n" +
                    "          ESSID: \"Brainwavez 2.4Ghz\"\n" +
                    "          Mode: Master  Channel: 6\n" +
                    "          Signal: -47 dBm  Quality: 63/70\n" +
                    "          Encryption: WPA2 PSK (CCMP)\n" +
                    "\n" +
                    "Cell 07 - Address: FC:94:E3:3F:38:8E\n" +
                    "          ESSID: \"Amtmann\"\n" +
                    "          Mode: Master  Channel: 6\n" +
                    "          Signal: -80 dBm  Quality: 30/70\n" +
                    "          Encryption: mixed WPA/WPA2 PSK (TKIP, CCMP)\n" +
                    "\n" +
                    "Cell 08 - Address: 00:26:24:78:D4:5F\n" +
                    "          ESSID: \"UPC0049659\"\n" +
                    "          Mode: Master  Channel: 11\n" +
                    "          Signal: -78 dBm  Quality: 32/70\n" +
                    "          Encryption: mixed WPA/WPA2 PSK (TKIP, CCMP)\n" +
                    "\n" +
                    "Cell 09 - Address: C4:27:95:C0:9B:18\n" +
                    "          ESSID: \"UPC2531653\"\n" +
                    "          Mode: Master  Channel: 11\n" +
                    "          Signal: -62 dBm  Quality: 48/70\n" +
                    "          Encryption: mixed WPA/WPA2 PSK (TKIP, CCMP)\n" +
                    "\n" +
                    "Cell 10 - Address: 00:1E:69:63:B5:FF\n" +
                    "          ESSID: \"Willi Wonkas Router\"\n" +
                    "          Mode: Master  Channel: 13\n" +
                    "          Signal: -66 dBm  Quality: 44/70\n" +
                    "          Encryption: mixed WPA/WPA2 PSK (CCMP)\n" +
                    "\n" +
                    "  ]]>\n" +
                    "  </scan>\n" +
                    "  <txpowerlist>\n" +
                    "  <![CDATA[\n" +
                    "   0 dBm (   1 mW)\n" +
                    "   1 dBm (   1 mW)\n" +
                    "   2 dBm (   1 mW)\n" +
                    "   3 dBm (   1 mW)\n" +
                    "   4 dBm (   2 mW)\n" +
                    "   5 dBm (   3 mW)\n" +
                    "   6 dBm (   3 mW)\n" +
                    "   7 dBm (   5 mW)\n" +
                    "   8 dBm (   6 mW)\n" +
                    "   9 dBm (   7 mW)\n" +
                    "  10 dBm (  10 mW)\n" +
                    "  11 dBm (  12 mW)\n" +
                    "  12 dBm (  15 mW)\n" +
                    "  13 dBm (  19 mW)\n" +
                    "  14 dBm (  25 mW)\n" +
                    "  15 dBm (  31 mW)\n" +
                    "  16 dBm (  39 mW)\n" +
                    "  17 dBm (  50 mW)\n" +
                    "* 18 dBm (  63 mW)\n" +
                    "  ]]>\n" +
                    "  </txpowerlist>\n" +
                    "  <assoclist>\n" +
                    "  <![CDATA[\n" +
                    "No station connected\n" +
                    "  ]]>\n" +
                    "  </assoclist>\n" +
                    "</adapter>\n" +
                    "</router>\n";
}

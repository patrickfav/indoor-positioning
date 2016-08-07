package at.ac.tuwien.inso.indoor.sensorserver.test.parser;

import at.ac.tuwien.inso.indoor.sensorserver.services.parser.IwinfoAdapterListParser;
import at.ac.tuwien.inso.indoor.sensorserver.services.parser.IwinfoScanParser;
import at.ac.tuwien.inso.indoor.sensorserver.test.parser.examples.AdapterListExamples;
import at.ac.tuwien.inso.indoor.sensorserver.test.parser.examples.ScanExamples;
import org.junit.Test;

/**
 * Created by PatrickF on 13.09.2014.
 */
public class IwinfoParserTest {

    @Test
    public void testParseScanShouldOk() throws Exception{
        IwinfoScanParser.parse(ScanExamples.scan_TL_WR710N_Barrier_Breaker_r42434,true);
    }

    @Test
    public void testParseAdapterListShouldOk() throws Exception{
        IwinfoAdapterListParser.parse(AdapterListExamples.adapterlist_TL_WR710N_Barrier_Breaker_r42434, true);
    }
}

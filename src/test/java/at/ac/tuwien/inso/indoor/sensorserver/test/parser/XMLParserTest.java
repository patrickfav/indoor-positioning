package at.ac.tuwien.inso.indoor.sensorserver.test.parser;

import at.ac.tuwien.inso.indoor.sensorserver.services.parser.IwinfoXmlReader;
import at.ac.tuwien.inso.indoor.sensorserver.test.parser.examples.XMLExamples;
import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by PatrickF on 13.09.2014.
 */
public class XMLParserTest {
    private static Logger log = Logger.getLogger(XMLParserTest.class);

    @Test
    public void testIwinfoWlan0AdapterParseShouldOk() throws Exception {
        IwinfoXmlReader.IwinfoAdapter adapter = IwinfoXmlReader.parseSpecificIwinfoAdapter(XMLExamples.xmlExample_iwinfo_adapter_wlan0);
        assertNotNull(adapter);
        assertNotNull(adapter.getInfo());
        assertNotNull(adapter.getScan());
        assertNotNull(adapter.getAssoclist());
        assertNotNull(adapter.getTxpowerlist());

        log.info(adapter);
    }

    @Test
    public void testIwinfoListAdapterParseShouldOk() throws Exception {
        IwinfoXmlReader.IwinfoList adapter = IwinfoXmlReader.parseIwinfoAdapterList(XMLExamples.xmlExample_iwinfo_adapter_list);
        assertNotNull(adapter);
        assertNotNull(adapter.getAdapterList());
        log.info(adapter);
    }
}

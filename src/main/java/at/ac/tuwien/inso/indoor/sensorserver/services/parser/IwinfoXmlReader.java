package at.ac.tuwien.inso.indoor.sensorserver.services.parser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

/**
 * Created by PatrickF on 12.09.2014.
 */
public class IwinfoXmlReader {

    public static IwinfoList parseIwinfoAdapterList(String xml) throws Exception {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));
            NodeList adapterNodeList = document.getDocumentElement().getChildNodes();

            final IwinfoList iwinfoAdapter = new IwinfoList();
            for (int i = 0; i < adapterNodeList.getLength(); i++) {
                if (adapterNodeList.item(i) instanceof Element) {
                    Element elem = (Element) adapterNodeList.item(i);
                    if (elem.getTagName().equalsIgnoreCase("adapter-list")) {
                        iwinfoAdapter.setAdapterList(elem.getTextContent().trim());
                    }
                    if (elem.getTagName().equalsIgnoreCase("date")) {
                        iwinfoAdapter.setDate(elem.getTextContent().trim());
                    }
                    if (elem.getTagName().equalsIgnoreCase("uptime")) {
                        iwinfoAdapter.setUptime(elem.getTextContent().trim());
                    }
                    if (elem.getTagName().equalsIgnoreCase("ifconfig")) {
                        iwinfoAdapter.setIfconfig(elem.getTextContent().trim());
                    }
                    if (elem.getTagName().equalsIgnoreCase("cpuinfo")) {
                        iwinfoAdapter.setCpuInfo(elem.getTextContent().trim());
                    }
                    if (elem.getTagName().equalsIgnoreCase("meminfo")) {
                        iwinfoAdapter.setMemInfo(elem.getTextContent().trim());
                    }
                    if (elem.getTagName().equalsIgnoreCase("version")) {
                        iwinfoAdapter.setVersion(elem.getTextContent().trim());
                    }
                }
            }

            return iwinfoAdapter;
        } catch (Exception e) {
            throw new Exception("Error while parsing adapter xml", e);
        }
    }

    public static IwinfoAdapter parseSpecificIwinfoAdapter(String xml) throws Exception {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));
            NodeList adapterNodeList = document.getDocumentElement().getChildNodes();

            final IwinfoAdapter iwinfoAdapter = new IwinfoAdapter();
            for (int i = 0; i < adapterNodeList.getLength(); i++) {
                Node adapterNode = adapterNodeList.item(i);
                if (adapterNode instanceof Element) {
                    NodeList adapterDetailsNodes = adapterNode.getChildNodes();
                    iwinfoAdapter.setName(((Element) document.adoptNode(adapterNode)).getAttribute("name"));

                    for (int j = 0; j < adapterDetailsNodes.getLength(); j++) {
                        Node detailNode = adapterDetailsNodes.item(j);
                        if (detailNode instanceof Element) {
                            Element elem = (Element) detailNode;

                            if (elem.getTagName().equalsIgnoreCase("info")) {
                                iwinfoAdapter.setInfo(elem.getTextContent().trim());
                            }
                            if (elem.getTagName().equalsIgnoreCase("scan")) {
                                iwinfoAdapter.setScan(elem.getTextContent().trim());
                            }
                            if (elem.getTagName().equalsIgnoreCase("txpowerlist")) {
                                iwinfoAdapter.setTxpowerlist(elem.getTextContent().trim());
                            }
                            if (elem.getTagName().equalsIgnoreCase("assoclist")) {
                                iwinfoAdapter.setAssoclist(elem.getTextContent().trim());
                            }
                        }
                    }
                }
            }

            return iwinfoAdapter;
        } catch (Exception e) {
            throw new Exception("Error while parsing adapter xml", e);
        }
    }

    public static class IwinfoAdapter {
        private String name;
        private String info;
        private String scan;
        private String txpowerlist;
        private String assoclist;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }

        public String getScan() {
            return scan;
        }

        public void setScan(String scan) {
            this.scan = scan;
        }

        public String getTxpowerlist() {
            return txpowerlist;
        }

        public void setTxpowerlist(String txpowerlist) {
            this.txpowerlist = txpowerlist;
        }

        public String getAssoclist() {
            return assoclist;
        }

        public void setAssoclist(String assoclist) {
            this.assoclist = assoclist;
        }

        @Override
        public String toString() {
            return "IwinfoAdapter{" +
                    "name='" + name + '\'' +
                    ", info='" + info + '\'' +
                    ", scan='" + scan + '\'' +
                    ", txpowerlist='" + txpowerlist + '\'' +
                    ", assoclist='" + assoclist + '\'' +
                    '}';
        }
    }

    public static class IwinfoList {
        private String adapterList;
        private String date;
        private String uptime;
        private String ifconfig;
        private String cpuInfo;
        private String memInfo;
        private String version;

        public String getAdapterList() {
            return adapterList;
        }

        public void setAdapterList(String adapterList) {
            this.adapterList = adapterList;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getUptime() {
            return uptime;
        }

        public void setUptime(String uptime) {
            this.uptime = uptime;
        }

        public String getIfconfig() {
            return ifconfig;
        }

        public void setIfconfig(String ifconfig) {
            this.ifconfig = ifconfig;
        }

        public String getCpuInfo() {
            return cpuInfo;
        }

        public void setCpuInfo(String cpuInfo) {
            this.cpuInfo = cpuInfo;
        }

        public String getMemInfo() {
            return memInfo;
        }

        public void setMemInfo(String memInfo) {
            this.memInfo = memInfo;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        @Override
        public String toString() {
            return "IwinfoList{" +
                    "adapterList='" + adapterList + '\'' +
                    ", date='" + date + '\'' +
                    ", uptime='" + uptime + '\'' +
                    ", ifconfig='" + ifconfig + '\'' +
                    ", cpuInfo='" + cpuInfo + '\'' +
                    ", memInfo='" + memInfo + '\'' +
                    ", version='" + version + '\'' +
                    '}';
        }
    }
}

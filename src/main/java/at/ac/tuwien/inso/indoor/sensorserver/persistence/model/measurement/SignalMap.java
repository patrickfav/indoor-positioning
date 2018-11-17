package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement;

import java.util.*;

/**
 * Created by PatrickF on 20.10.2014.
 */
public class SignalMap {
    private int tileLengthCm;
    private int lengthX;
    private int lengthY;
    private int spreadCmExtendedNodes;
    private int defaultSignalRadiusCm;

    private Map<String, Vertex> managedNodes = new HashMap<String, Vertex>();
    private Map<String, Vertex> extendedNodes = new HashMap<String, Vertex>();
    private List<Edge> mangedNodeEdges = new ArrayList<Edge>();
    private List<Edge> extendedNodeEdges = new ArrayList<Edge>();
    private FloorplanConfig floorplanConfig = new FloorplanConfig();
    private List<Log> logListExtendedNodes = new ArrayList<Log>();

    public SignalMap(int tileLengthCm, int lengthX, int lengthY, SignalMapConfig config, int defaultSignalRadiusCm) {
        this.tileLengthCm = tileLengthCm;
        this.lengthY = lengthY;
        this.lengthX = lengthX;
        this.spreadCmExtendedNodes = config.getDefaultSpreadExtendedNodesCm();
        this.defaultSignalRadiusCm = defaultSignalRadiusCm;
    }

    public SignalMap() {

    }

    public int getTileLengthCm() {
        return tileLengthCm;
    }

    public void setTileLengthCm(int tileLengthCm) {
        this.tileLengthCm = tileLengthCm;
    }

    public int getLengthX() {
        return lengthX;
    }

    public void setLengthX(int lengthX) {
        this.lengthX = lengthX;
    }

    public int getLengthY() {
        return lengthY;
    }

    public void setLengthY(int lengthY) {
        this.lengthY = lengthY;
    }

    public Map<String, Vertex> getManagedNodes() {
        return managedNodes;
    }

    public void setManagedNodes(Map<String, Vertex> managedNodes) {
        this.managedNodes = managedNodes;
    }

    public Map<String, Vertex> getExtendedNodes() {
        return extendedNodes;
    }

    public void setExtendedNodes(Map<String, Vertex> extendedNodes) {
        this.extendedNodes = extendedNodes;
    }

    public List<Edge> getMangedNodeEdges() {
        return mangedNodeEdges;
    }

    public void setMangedNodeEdges(List<Edge> mangedNodeEdges) {
        this.mangedNodeEdges = mangedNodeEdges;
    }

    public List<Edge> getExtendedNodeEdges() {
        return extendedNodeEdges;
    }

    public void setExtendedNodeEdges(List<Edge> extendedNodeEdges) {
        this.extendedNodeEdges = extendedNodeEdges;
    }

    public FloorplanConfig getFloorplanConfig() {
        return floorplanConfig;
    }

    public void setFloorplanConfig(FloorplanConfig floorplanConfig) {
        this.floorplanConfig = floorplanConfig;
    }

    public int getSpreadCmExtendedNodes() {
        return spreadCmExtendedNodes;
    }

    public void setSpreadCmExtendedNodes(int spreadCmExtendedNodes) {
        this.spreadCmExtendedNodes = spreadCmExtendedNodes;
    }

    public List<Log> getLogListExtendedNodes() {
        return logListExtendedNodes;
    }

    public void setLogListExtendedNodes(List<Log> logListExtendedNodes) {
        this.logListExtendedNodes = logListExtendedNodes;
    }

    public int getDefaultSignalRadiusCm() {
        return defaultSignalRadiusCm;
    }

    public void setDefaultSignalRadiusCm(int defaultSignalRadiusCm) {
        this.defaultSignalRadiusCm = defaultSignalRadiusCm;
    }

    public static class Vertex {
        private Point originalPos;
        private Point currentPos;
        private String mac;
        private String name;
        private PhysicalAdapter physicalAdapter;
        private int visibility;
        private int possiblePositions = 1;
        private double probability = 1d;
        private double signalStrengthFac = 1d;

        public Vertex(Point pos, String mac) {
            this.originalPos = pos;
            this.currentPos = pos;
            this.mac = mac;
        }

        public Vertex() {
        }

        public Point getOriginalPos() {
            return originalPos;
        }

        public void setOriginalPos(Point originalPos) {
            this.originalPos = originalPos;
        }

        public Point getCurrentPos() {
            return currentPos;
        }

        public void setCurrentPos(Point currentPos) {
            this.currentPos = currentPos;
        }

        public String getMac() {
            return mac;
        }

        public void setMac(String mac) {
            this.mac = mac;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public PhysicalAdapter getPhysicalAdapter() {
            return physicalAdapter;
        }

        public void setPhysicalAdapter(PhysicalAdapter physicalAdapter) {
            this.physicalAdapter = physicalAdapter;
        }

        public int getVisibility() {
            return visibility;
        }

        public void setVisibility(int visibility) {
            this.visibility = visibility;
        }

        public int getPossiblePositions() {
            return possiblePositions;
        }

        public void setPossiblePositions(int possiblePositions) {
            this.possiblePositions = possiblePositions;
        }

        public double getProbability() {
            return probability;
        }

        public void setProbability(double probability) {
            this.probability = probability;
        }

        public double getSignalStrengthFac() {
            return signalStrengthFac;
        }

        public void setSignalStrengthFac(double signalStrengthFac) {
            this.signalStrengthFac = signalStrengthFac;
        }
    }

    public static class Edge {
        private String fromMac;
        private String toMac;

        public String getFromMac() {
            return fromMac;
        }

        public void setFromMac(String fromMac) {
            this.fromMac = fromMac;
        }

        public String getToMac() {
            return toMac;
        }

        public void setToMac(String toMac) {
            this.toMac = toMac;
        }
    }

    public static class Point {
        private int x;
        private int y;

        public Point() {
        }

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }
    }

    public static class FloorplanConfig {
        private int posX = -1;
        private int posY = -1;
        private double scale = 1;
        private int rotationDegrees = 0;

        public FloorplanConfig() {
        }

        public FloorplanConfig(FloorplanConfig floorplanConfig1) {
            this.posX = floorplanConfig1.getPosX();
            this.posY = floorplanConfig1.getPosY();
            this.scale = floorplanConfig1.getScale();
            this.rotationDegrees = floorplanConfig1.getRotationDegrees();
        }

        public int getPosX() {
            return posX;
        }

        public void setPosX(int posX) {
            this.posX = posX;
        }

        public int getPosY() {
            return posY;
        }

        public void setPosY(int posY) {
            this.posY = posY;
        }

        public double getScale() {
            return scale;
        }

        public void setScale(double scale) {
            this.scale = scale;
        }

        public int getRotationDegrees() {
            return rotationDegrees;
        }

        public void setRotationDegrees(int rotationDegrees) {
            this.rotationDegrees = rotationDegrees;
        }
    }

    public static class Log implements Comparable<Log> {
        private String msg;
        private String priority;
        private Date date;

        public Log() {
        }

        public Log(String msg, String priority) {
            this.msg = msg;
            this.priority = priority;
            this.date = new Date();
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getPriority() {
            return priority;
        }

        public void setPriority(String priority) {
            this.priority = priority;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        @Override
        public int compareTo(Log o) {
            return date.compareTo(o.getDate()) * -1;
        }
    }
}

package at.ac.tuwien.inso.indoor.sensorserver.services.positioner;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.Analysis;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.EFrequencyRange;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.SignalMap;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.SignalMapConfig;
import at.ac.tuwien.inso.indoor.sensorserver.services.scheduler.Callback;
import at.ac.tuwien.inso.indoor.sensorserver.util.ServerUtil;
import cern.colt.list.IntArrayList;
import cern.colt.list.ObjectArrayList;
import cern.colt.matrix.impl.SparseObjectMatrix2D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Created by PatrickF on 26.10.2014.
 */
public class ExtendedNodeTrilaterationPositioner implements Callable<ExtendedNodeTrilaterationPositioner.ExtendedNodePositionWrapper> {
    private static Logger log = LogManager.getLogger(ExtendedNodeTrilaterationPositioner.class);


    private int tileLengthCm = 10;
    private int spreadCm;
    private int probabilitySpread;

    private Random rnd;

    private Analysis analysis;
    private SignalMap providedSignalMap;
    private EFrequencyRange range;

    private SparseObjectMatrix2D matrix;
    private NodeDistanceMatrix data;
    private String macExtendedNode;

    private SignalMapConfig config;
    private Callback<ExtendedNodePositionWrapper> callback;

    public ExtendedNodeTrilaterationPositioner(SignalMapConfig config, String macExtendedNode, EFrequencyRange range, Analysis analysis, SignalMap providedSignalMap, int spreadCm, Callback<ExtendedNodePositionWrapper> callback) {
        this.macExtendedNode = macExtendedNode;
        this.range = range;
        this.analysis = analysis;
        this.providedSignalMap = providedSignalMap;
        this.rnd = config.generateRnd();
        this.tileLengthCm = config.getTileLenghtCm();
        this.spreadCm = spreadCm;
        this.callback = callback;
        this.config = config;
    }

    @Override
    public ExtendedNodePositionWrapper call() throws Exception {
        return calculatePosition();
    }

    public ExtendedNodePositionWrapper calculatePosition() {
        ExtendedNodePositionWrapper wrapper = new ExtendedNodePositionWrapper();

        data = new NodeDistanceMatrix(analysis, range);

        Set<String> macThatSeeExtendedNode = data.getAllManagedNodeMacsWhichSeeGivenExtendedNode(macExtendedNode);

        if (data.getExtendedNodesMacList().get(macExtendedNode).isIgnored()) {
            log("info", "Ignoring " + macExtendedNode + " because it is on the blacklist", wrapper);
            return returnAndNotify(wrapper);
        }

        if (macThatSeeExtendedNode.size() < config.getMinVisibilityNeededForTrilateration()) {
            log("info", "Ignoring " + macExtendedNode + " because it is only seen by " + macThatSeeExtendedNode.size(), wrapper);
            return returnAndNotify(wrapper);
        }

        int validVisibilityCount = 0;
        for (String managedMac : macThatSeeExtendedNode) {
            if (data.getLookupMap().get(new NodeDistanceMatrix.MacCombKey(managedMac, macExtendedNode)).getManagedNode().getStatistics().getMean() > config.getMinDbmNeededToConsiderForTrilateration()) {
                validVisibilityCount++;
            }
        }

        if (validVisibilityCount < config.getMinVisibilityNeededForTrilateration()) {
            log("info", "Ignoring " + macExtendedNode + " because it is only seen by " + validVisibilityCount + " with over " + config.getMinDbmNeededToConsiderForTrilateration() + "dBm", wrapper);
            return returnAndNotify(wrapper);
        }

        matrix = new SparseObjectMatrix2D(providedSignalMap.getLengthX(), providedSignalMap.getLengthY());
        probabilitySpread = PosHelper.probabilitySpread(tileLengthCm, spreadCm);

        for (String managedMac : macThatSeeExtendedNode) {
            if (data.getLookupMap().get(new NodeDistanceMatrix.MacCombKey(managedMac, macExtendedNode)).getManagedNode().getStatistics().getMean() > config.getMinDbmNeededToConsiderForTrilateration()) {
                drawSignalCircles(providedSignalMap.getManagedNodes().get(managedMac).getCurrentPos().getX(), providedSignalMap.getManagedNodes().get(managedMac).getCurrentPos().getY(),
                        PosHelper.toTiles(PosHelper.convertMToCm(data.getNormalizedDistanceMap().get(new NodeDistanceMatrix.MacCombKey(managedMac, macExtendedNode))), tileLengthCm), managedMac);
            } else {
                log("debug", "Ignoring measurement from node " + managedMac + " RSS too low.", wrapper);
            }
        }

        NodeProbabilityDetails mostLikely = getMostLikely(macThatSeeExtendedNode);

        if (mostLikely == null) {
            log("info", "No position found with trilateration for " + macExtendedNode, wrapper);
            return returnAndNotify(wrapper);
        } else {
            log("info", "Position found with trilateration for " + macExtendedNode + ": " + mostLikely.getSelectedPoint().getX()
                    + "," + mostLikely.getSelectedPoint().getY() + " out of " + mostLikely.getAllPossiblePoints().size() + " with same probability "
                    + mostLikely.getMaxProbability(), wrapper);
        }

        SignalMap.Vertex extendedNodeVertex = new SignalMap.Vertex(new SignalMap.Point(mostLikely.getSelectedPoint().getX(), mostLikely.getSelectedPoint().getY()), macExtendedNode);
        extendedNodeVertex.setName(ServerUtil.implode(",", new ArrayList<String>(data.getExtendedNodesMacList().get(macExtendedNode).getSsidSet())));
        extendedNodeVertex.setPossiblePositions(mostLikely.getAllPossiblePoints().size());
        extendedNodeVertex.setVisibility(validVisibilityCount);
        extendedNodeVertex.setProbability(mostLikely.getMaxProbability());
        wrapper.getExtendedNodes().put(macExtendedNode, extendedNodeVertex);

        for (String macSrc : macThatSeeExtendedNode) {
            SignalMap.Edge edge = new SignalMap.Edge();
            edge.setToMac(macExtendedNode);
            edge.setFromMac(macSrc);

            boolean alreadyAdded = false;
            for (SignalMap.Edge e : wrapper.getExtendedNodeEdges()) {
                if (e.getFromMac().equals(edge.getFromMac()) && e.getToMac().equals(e.getToMac())) {
                    alreadyAdded = true;
                    break;
                }
            }

            if (!alreadyAdded) {
                wrapper.getExtendedNodeEdges().add(edge);
            }
        }

        return returnAndNotify(wrapper);
    }

    private ExtendedNodePositionWrapper returnAndNotify(ExtendedNodePositionWrapper wrapper) {
        if (callback != null) {
            callback.callback(wrapper);
        }
        return wrapper;
    }

    private NodeProbabilityDetails getMostLikely(Set<String> macThatSeeExtendedNode) {
        NodeProbabilityDetails details = new NodeProbabilityDetails();
        ObjectArrayList entries = new ObjectArrayList();

        matrix.getNonZeros(new IntArrayList(), new IntArrayList(), entries);

        double max = 0;
        for (int i = 0; i < entries.size(); i++) {
            NodeProbabilityPoint point = (NodeProbabilityPoint) entries.get(i);
            if (point.getProbabilitySum(macExtendedNode, macThatSeeExtendedNode) > max) {
                max = point.getProbabilitySum(macExtendedNode, macThatSeeExtendedNode);
            }
        }

        details.setMaxProbability(max);
        if (max > 0) {
            for (int i = 0; i < entries.size(); i++) {
                NodeProbabilityPoint point = (NodeProbabilityPoint) entries.get(i);
                if (point.getProbabilitySum(macExtendedNode, macThatSeeExtendedNode) == max) {
                    details.getAllPossiblePoints().add(point);
                }
            }

            if (!details.getAllPossiblePoints().isEmpty()) {
                details.setSelectedPoint(details.getAllPossiblePoints().get(rnd.nextInt(details.getAllPossiblePoints().size())));
                return details;
            }
        }
        return null;
    }

    private void drawSignalCircles(int xCenterTile, int yCenterTile, int radiusTiles, String macSrc) {
        MidpointAlgorithm.drawCircleToNodeProbSparseArray(xCenterTile, yCenterTile, radiusTiles, matrix, macSrc, macExtendedNode, 1.0, probabilitySpread);
    }

    public static class ExtendedNodePositionWrapper {
        private Map<String, SignalMap.Vertex> extendedNodes = new HashMap<String, SignalMap.Vertex>();
        private List<SignalMap.Edge> extendedNodeEdges = new ArrayList<SignalMap.Edge>();
        private List<SignalMap.Log> logList = new ArrayList<SignalMap.Log>();

        public Map<String, SignalMap.Vertex> getExtendedNodes() {
            return extendedNodes;
        }

        public List<SignalMap.Edge> getExtendedNodeEdges() {
            return extendedNodeEdges;
        }

        public List<SignalMap.Log> getLogList() {
            return logList;
        }
    }

    private void log(String priority, String msg, ExtendedNodePositionWrapper wrapper) {
        if (priority.equalsIgnoreCase("debug")) {
            log.debug(msg);
        } else if (priority.equalsIgnoreCase("info")) {
            log.info(msg);
        } else if (priority.equalsIgnoreCase("warn")) {
            log.warn(msg);
        }

        wrapper.getLogList().add(new SignalMap.Log(msg, priority));
    }

}

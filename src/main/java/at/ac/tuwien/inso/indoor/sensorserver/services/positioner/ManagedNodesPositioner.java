package at.ac.tuwien.inso.indoor.sensorserver.services.positioner;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.SensorManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.Analysis;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.EFrequencyRange;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.PhysicalAdapter;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.SignalMap;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.SignalMapConfig;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNetwork;
import cern.colt.list.IntArrayList;
import cern.colt.list.ObjectArrayList;
import cern.colt.matrix.impl.SparseObjectMatrix2D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by PatrickF on 10.10.2014.
 */
public class ManagedNodesPositioner {
    private static Logger log = LogManager.getLogger(ManagedNodesPositioner.class);

    private int tileLengthCm;
    private int spreadCm;

    private int lengthCm;
    private int widthCm;
    private Analysis analysis;
    private EFrequencyRange range;
    private Random rnd;
    private int probabilitySpread;

    private SparseObjectMatrix2D matrix;
    private NodeDistanceMatrix data;
    private SignalMapConfig config;

    public ManagedNodesPositioner(Analysis analysis, EFrequencyRange range, SignalMapConfig config) {
        this.rnd = config.generateRnd();
        this.analysis = analysis;
        this.widthCm = config.getCanvasDimensionManagedNodesCm();
        this.lengthCm = config.getCanvasDimensionManagedNodesCm();
        this.range = range;
        this.spreadCm = config.getDefaultSpreaManagedNodesCm();
        this.tileLengthCm = config.getTileLenghtCm();
        this.config = config;

        checkInitValues();
        initializeMatrix();
    }

    private void checkInitValues() {
        log.info("check values");
        if (tileLengthCm < 5) {
            throw new IllegalArgumentException("Tile-length must be at least 5");
        }
        if (lengthCm <= tileLengthCm) {
            throw new IllegalArgumentException("Length " + lengthCm + " must be greater than tile length of " + tileLengthCm);
        }
        if (widthCm <= tileLengthCm) {
            throw new IllegalArgumentException("Width " + widthCm + " must be greater than tile length of " + tileLengthCm);
        }
        if (!analysis.getExtendedNodeMap().containsKey(range)) {
            throw new IllegalArgumentException("Analysis does not contain any info on freq range " + range);
        }
    }

    private void initializeMatrix() {
        log.info("initialize");
        int cellsLength = PosHelper.toTiles(lengthCm, tileLengthCm);
        int cellsWidth = PosHelper.toTiles(widthCm, tileLengthCm);
        probabilitySpread = PosHelper.probabilitySpread(tileLengthCm, spreadCm);
        matrix = new SparseObjectMatrix2D(cellsLength, cellsWidth);
        data = new NodeDistanceMatrix(analysis, range);

        if (data.getManagedNodesMacMap().keySet().size() < 2) {
            throw new IllegalArgumentException("We need at least 3 active nodes. Found only " + data.getManagedNodesMacMap().keySet());
        }

        log.info("pick first node");

        Set<String> usedMacSet = new HashSet<String>();

        String macAddressFirstNode = pickNextNode(usedMacSet);
        usedMacSet.add(macAddressFirstNode);

        SensorPosition anchor = setFixedSensorPosition(lengthCm / 2, widthCm / 2, macAddressFirstNode, null);
        data.getManagedNodesFixedPosition().add(anchor);
        drawSignalCircles(anchor.getxCm(), anchor.getyCm(), macAddressFirstNode, usedMacSet);

        log.info("guess next node");
        SensorPosition firstGuess = findRandomFixedPosForFirstPick(anchor.getxCm(), anchor.getyCm(), macAddressFirstNode, usedMacSet);
        data.getManagedNodesFixedPosition().add(firstGuess);
        usedMacSet.add(firstGuess.getMacAddress());

        log.info("guess the rest of the active nodes");

        for (String unknownManagedMac : getAllMeasuredManagedMacAddressesFromSensorNode(macAddressFirstNode, usedMacSet)) {
            NodeProbabilityDetails details = getMostLikely(unknownManagedMac, usedMacSet);
            if (details != null) {
                NodeProbabilityPoint selectedPoint = details.getSelectedPoint();
                usedMacSet.add(unknownManagedMac);
                data.getManagedNodesFixedPosition().add(setFixedSensorPosition(selectedPoint.getX() * tileLengthCm, selectedPoint.getY() * tileLengthCm, unknownManagedMac, details));
                drawSignalCircles(selectedPoint.getX() * tileLengthCm, selectedPoint.getY() * tileLengthCm, unknownManagedMac, usedMacSet);
            } else {
                log.warn("details for " + unknownManagedMac + " is null, that means it could not been found with the given macs and spread, try to enhance spread in config");
            }
        }

        log.info("guess the rest of the active nodes");
    }

    public SignalMap createSignalMap() {
        int maxX = 0;
        int maxY = 0;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;

        for (SensorPosition sensorPosition : data.getManagedNodesFixedPosition()) {
            if (sensorPosition.getxTiles() > maxX) {
                maxX = sensorPosition.getxTiles();
            }
            if (sensorPosition.getxTiles() < minX) {
                minX = sensorPosition.getxTiles();
            }
            if (sensorPosition.getyTiles() > maxY) {
                maxY = sensorPosition.getyTiles();
            }
            if (sensorPosition.getyTiles() < minY) {
                minY = sensorPosition.getyTiles();
            }
        }

        int overflowTiles = (int) Math.ceil(config.getCmOverflowForCalculatedCanvas() / tileLengthCm);
        maxX += overflowTiles;
        maxY += overflowTiles;
        minX = Math.max(0, minX - overflowTiles);
        minY = Math.max(0, minY - overflowTiles);

        SensorNetwork network = SensorManager.getInstance().getSensorNetworkById(analysis.getNetworkId());
        SignalMap signalMap = new SignalMap(tileLengthCm, maxX - minX, maxY - minY, config, (int) Math.round(PosHelper.maxDistanceRadius(network.getEnvironmentModel(), network.getPathLossConfig(), config, range)));

        for (SensorPosition sensorPosition : data.getManagedNodesFixedPosition()) {
            SignalMap.Vertex v = new SignalMap.Vertex(new SignalMap.Point(sensorPosition.getxTiles() - minX, sensorPosition.getyTiles() - minY), sensorPosition.getMacAddress());

            for (PhysicalAdapter physicalAdapter : analysis.getPhysicalAdaptersMap().get(range)) {
                if (physicalAdapter.getMacAddress().equalsIgnoreCase(sensorPosition.getMacAddress())) {
                    v.setName(physicalAdapter.getNodeName());
                    v.setPhysicalAdapter(physicalAdapter);
                    break;
                }
            }

            v.setVisibility(data.getAllManagedNodeMacsWhichSeeGivenExtendedNode(sensorPosition.getMacAddress()).size());
            if (sensorPosition.getNodeProbabilityDetails() != null) {
                v.setPossiblePositions(sensorPosition.getNodeProbabilityDetails().getAllPossiblePoints().size());
                v.setProbability(sensorPosition.getNodeProbabilityDetails().getMaxProbability());
            }

            signalMap.getManagedNodes().put(v.getMac(), v);
        }

        Set<NodeDistanceMatrix.MacCombKey> usedMacs = new HashSet<NodeDistanceMatrix.MacCombKey>();
        for (NodeDistanceMatrix.MacCombKey macCombKey : data.getNormalizedDistanceMap().keySet()) {
            if (data.isManagedNodeMac(macCombKey.getSrc()) && data.isManagedNodeMac(macCombKey.getTarget()) && !usedMacs.contains(macCombKey)) {
                usedMacs.add(macCombKey);
                usedMacs.add(macCombKey.createOppositeMacCombKey());

                SignalMap.Edge edge = new SignalMap.Edge();
                edge.setFromMac(macCombKey.getSrc());
                edge.setToMac(macCombKey.getTarget());
                signalMap.getMangedNodeEdges().add(edge);
            }
        }

        return signalMap;
    }

    private NodeProbabilityDetails getMostLikely(String target, Set<String> shouldBeSeenByTheseMacs) {
        NodeProbabilityDetails nodeProbabilityDetails = new NodeProbabilityDetails();
        ObjectArrayList entries = new ObjectArrayList();

        matrix.getNonZeros(new IntArrayList(), new IntArrayList(), entries);

        double max = 0;
        for (int i = 0; i < entries.size(); i++) {
            NodeProbabilityPoint point = (NodeProbabilityPoint) entries.get(i);
            if (point.getProbabilitySum(target, shouldBeSeenByTheseMacs) > max) {
                max = point.getProbabilitySum(target, shouldBeSeenByTheseMacs);
            }
        }


        if (max > 0) {
            nodeProbabilityDetails.setMaxProbability(max);

            for (int i = 0; i < entries.size(); i++) {
                NodeProbabilityPoint point = (NodeProbabilityPoint) entries.get(i);
                if (point.getProbabilitySum(target, shouldBeSeenByTheseMacs) == max) {
                    nodeProbabilityDetails.getAllPossiblePoints().add(point);
                }
            }

            if (!nodeProbabilityDetails.getAllPossiblePoints().isEmpty()) {
                nodeProbabilityDetails.setSelectedPoint(nodeProbabilityDetails.getAllPossiblePoints().get(rnd.nextInt(nodeProbabilityDetails.getAllPossiblePoints().size())));
                return nodeProbabilityDetails;
            }
        }

        return null;
    }

    private SensorPosition setFixedSensorPosition(int xCm, int yCm, String macAddress, NodeProbabilityDetails details) {
        SensorPosition pos = new SensorPosition(xCm, yCm, macAddress, tileLengthCm);

        NodeProbabilityPoint probabilityPoint = null;
        if ((probabilityPoint = (NodeProbabilityPoint) matrix.getQuick(pos.getxTiles(), pos.getyTiles())) == null) {
            probabilityPoint = new NodeProbabilityPoint(pos.getxTiles(), pos.getyTiles());
        }
        probabilityPoint.setFixedPosMacAddress(macAddress);
        matrix.setQuick(pos.getxTiles(), pos.getyTiles(), probabilityPoint);
        pos.setNodeProbabilityDetails(details);
        return pos;
    }

    private SensorPosition findRandomFixedPosForFirstPick(int firstNodeCenterXCm, int firstNodeCenterYCm, String firstAnchorMacAddress, Set<String> ignoreMac) {
        List<String> measuredByFirstNode = new ArrayList<String>(getAllMeasuredManagedMacAddressesFromSensorNode(firstAnchorMacAddress, ignoreMac));
        String nextMacAddr = measuredByFirstNode.get(rnd.nextInt(measuredByFirstNode.size()));
        int radiusCm = PosHelper.convertMToCm(data.getNormalizedDistanceMap().get(new NodeDistanceMatrix.MacCombKey(firstAnchorMacAddress, nextMacAddr)));

        int orientation = rnd.nextInt(3);
        SensorPosition pos = null;

        if (orientation == 0) {
            pos = setFixedSensorPosition(firstNodeCenterXCm, firstNodeCenterYCm + radiusCm, nextMacAddr, null);
            drawSignalCircles(firstNodeCenterXCm, firstNodeCenterYCm + radiusCm, nextMacAddr, ignoreMac);
        } else if (orientation == 1) {
            pos = setFixedSensorPosition(firstNodeCenterXCm + radiusCm, firstNodeCenterYCm, nextMacAddr, null);
            drawSignalCircles(firstNodeCenterXCm + radiusCm, firstNodeCenterYCm, nextMacAddr, ignoreMac);
        } else if (orientation == 2) {
            pos = setFixedSensorPosition(firstNodeCenterXCm, firstNodeCenterYCm - radiusCm, nextMacAddr, null);
            drawSignalCircles(firstNodeCenterXCm, firstNodeCenterYCm - radiusCm, nextMacAddr, ignoreMac);
        } else if (orientation == 3) {
            pos = setFixedSensorPosition(firstNodeCenterXCm - radiusCm, firstNodeCenterYCm, nextMacAddr, null);
            drawSignalCircles(firstNodeCenterXCm - radiusCm, firstNodeCenterYCm, nextMacAddr, ignoreMac);
        }

        return pos;
    }

    private void drawSignalCircles(int xCenterCm, int yCenterCm, String macSrc, Set<String> ignoreMac) {
        Set<String> measuredByFirstNode;

        measuredByFirstNode = getAllMeasuredManagedMacAddressesFromSensorNode(macSrc, ignoreMac);

        for (String targetMac : measuredByFirstNode) {
            drawCircleToMatrix(xCenterCm, yCenterCm,
                    PosHelper.convertMToCm(data.getNormalizedDistanceMap().get(new NodeDistanceMatrix.MacCombKey(macSrc, targetMac))),
                    macSrc, targetMac, 1.0);
        }
    }

    private void drawCircleToMatrix(int xCmCenter, int yCmCenter, int cmRadius, String macSrc, String macTarget, Double probability) {
        int xTilesCenter = PosHelper.toTiles(xCmCenter, tileLengthCm);
        int yTilesCenter = PosHelper.toTiles(yCmCenter, tileLengthCm);
        int radiusTiles = PosHelper.toTiles(cmRadius, tileLengthCm);

        MidpointAlgorithm.drawCircleToNodeProbSparseArray(xTilesCenter, yTilesCenter, radiusTiles, matrix, macSrc, macTarget, probability, probabilitySpread);
    }

    private String pickNextNode(Set<String> ignoreMac) {
        Set<String> maxNodeMacSet = new HashSet<String>();

        int max = 0;
        for (String mac : data.getManagedNodeMacCopy(ignoreMac)) {
            if (data.getManagedNodesMacMap().get(mac).getScannedMacsManaged().size() > max) {
                max = data.getManagedNodesMacMap().get(mac).getScannedMacsManaged().size();
            }
        }

        if (max > 0) {
            for (String mac : data.getManagedNodeMacCopy(ignoreMac)) {
                if (data.getManagedNodesMacMap().get(mac).getScannedMacsManaged().size() == max) {
                    maxNodeMacSet.add(mac);
                }
            }
            if (!maxNodeMacSet.isEmpty()) {
                return new ArrayList<String>(maxNodeMacSet).get(rnd.nextInt(maxNodeMacSet.size()));
            }
        }

        return null;
    }

    private Set<String> getAllMeasuredManagedMacAddressesFromSensorNode(String sensorNodeMacAddr, Set<String> ignoreMac) {
        Set<String> macs = new HashSet<String>(data.getManagedNodesMacMap().get(sensorNodeMacAddr).getScannedMacsManaged());
        macs.removeAll(ignoreMac);
        return macs;
    }
}

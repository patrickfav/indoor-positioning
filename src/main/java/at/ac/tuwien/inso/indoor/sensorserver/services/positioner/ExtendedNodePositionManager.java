package at.ac.tuwien.inso.indoor.sensorserver.services.positioner;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.Analysis;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.EFrequencyRange;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.SignalMap;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.SignalMapConfig;
import at.ac.tuwien.inso.indoor.sensorserver.services.scheduler.Callback;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by PatrickF on 26.10.2014.
 */
public class ExtendedNodePositionManager {
    private static Logger log = Logger.getLogger(ExtendedNodePositionManager.class);
    private int calculationMatrixLengthCm;

    private int tileLengthCm;
    private int spreadCm;
    private SignalMapConfig config;

    private SignalMap signalMap;
    private NodeDistanceMatrix data;
    private Analysis analysis;
    private EFrequencyRange range;

    private ExecutorService threadPool;

    public ExtendedNodePositionManager(int spreadCm, SignalMap signalMap, Analysis analysis, EFrequencyRange range, SignalMapConfig config) {
        this.spreadCm = spreadCm;
        this.signalMap = signalMap;
        this.analysis = analysis;
        this.range = range;
        this.tileLengthCm = config.getTileLenghtCm();
        this.calculationMatrixLengthCm = config.getCanvasDimensionExtendedNodesCm();
        this.config = config;
        initialize();
    }

    private void initialize() {
        if (signalMap == null) {
            log.error("signal map is null");
        }

        data = new NodeDistanceMatrix(analysis, range);
        threadPool = new ThreadPoolExecutor(8, 8, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(512));
    }

    public SignalMap calculate() {
        log.debug("start calculating with spread " + spreadCm);

        int halfAddedMatrixTiles = PosHelper.toTiles(calculationMatrixLengthCm / 2, tileLengthCm);
        int tileLengthX = signalMap.getLengthX() + (2 * halfAddedMatrixTiles);
        int tileLengthY = signalMap.getLengthY() + (2 * halfAddedMatrixTiles);

        signalMap.setLengthX(tileLengthX);
        signalMap.setLengthY(tileLengthY);

        signalMap.getExtendedNodes().clear();
        signalMap.getExtendedNodeEdges().clear();

        for (String managedMac : data.getManagedNodesMacMap().keySet()) {
            signalMap.getManagedNodes().get(managedMac).getCurrentPos().setX(signalMap.getManagedNodes().get(managedMac).getCurrentPos().getX() + halfAddedMatrixTiles);
            signalMap.getManagedNodes().get(managedMac).getCurrentPos().setY(signalMap.getManagedNodes().get(managedMac).getCurrentPos().getY() + halfAddedMatrixTiles);
        }

        final List<ExtendedNodeTrilaterationPositioner.ExtendedNodePositionWrapper> resultList = new CopyOnWriteArrayList<ExtendedNodeTrilaterationPositioner.ExtendedNodePositionWrapper>();

        for (String extendedMac : data.getExtendedNodesMacList().keySet()) {
            threadPool.submit(new ExtendedNodeTrilaterationPositioner(config, extendedMac, range, analysis, signalMap, spreadCm, new Callback<ExtendedNodeTrilaterationPositioner.ExtendedNodePositionWrapper>() {
                @Override
                public void callback(ExtendedNodeTrilaterationPositioner.ExtendedNodePositionWrapper extendedNodePositionWrapper) {
                    resultList.add(extendedNodePositionWrapper);
                }
            }));
        }

        log.debug("waiting for completion");
        try {
            threadPool.shutdown();
            threadPool.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Timeout in threadpool in extnode trilaterte job", e);
        }

        signalMap.getLogListExtendedNodes().clear();

        for (ExtendedNodeTrilaterationPositioner.ExtendedNodePositionWrapper wrapper : resultList) {
            for (SignalMap.Vertex vertex : wrapper.getExtendedNodes().values()) {
                signalMap.getExtendedNodes().put(vertex.getMac(), vertex);
            }
            signalMap.getExtendedNodeEdges().addAll(wrapper.getExtendedNodeEdges());
            signalMap.getLogListExtendedNodes().addAll(wrapper.getLogList());
        }
        Collections.sort(signalMap.getLogListExtendedNodes());

        log.debug("calculation finished");

        return findNewDimensions(signalMap, tileLengthCm, config.getCmOverflowForCalculatedCanvas());
    }

    private static SignalMap findNewDimensions(SignalMap signalMap, int tileLengthCm, int cmOverflow) {
        int maxX = 0;
        int maxY = 0;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;

        List<SignalMap.Vertex> allNodes = new ArrayList<SignalMap.Vertex>(signalMap.getManagedNodes().values());
        allNodes.addAll(signalMap.getExtendedNodes().values());

        for (SignalMap.Vertex v : allNodes) {
            if (v.getCurrentPos().getX() > maxX) {
                maxX = v.getCurrentPos().getX();
            }
            if (v.getCurrentPos().getX() < minX) {
                minX = v.getCurrentPos().getX();
            }
            if (v.getCurrentPos().getY() > maxY) {
                maxY = v.getCurrentPos().getY();
            }
            if (v.getCurrentPos().getY() < minY) {
                minY = v.getCurrentPos().getY();
            }
        }

        int overflowTiles = (int) Math.ceil(cmOverflow / tileLengthCm);
        maxX += overflowTiles;
        maxY += overflowTiles;
        minX = Math.max(0, minX - overflowTiles);
        minY = Math.max(0, minY - overflowTiles);

        signalMap.setLengthX(maxX - minX);
        signalMap.setLengthY(maxY - minY);

        for (SignalMap.Vertex vertex : allNodes) {
            SignalMap.Point p = vertex.getCurrentPos();
            p.setX(p.getX() - minX);
            p.setY(p.getY() - minY);
            vertex.setCurrentPos(p);
            vertex.setOriginalPos(p);
        }

        return signalMap;
    }
}

package at.ac.tuwien.inso.indoor.sensorserver.services.positioner;

import at.ac.tuwien.inso.indoor.sensorserver.math.radiomodels.ITUIndoorModelDegradingDist;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.EFrequencyRange;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.SignalMap;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.SignalMapConfig;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNetwork;
import cern.colt.list.IntArrayList;
import cern.colt.list.ObjectArrayList;
import cern.colt.matrix.impl.SparseObjectMatrix2D;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by PatrickF on 10.11.2014.
 */
public class RSSMatrixCreator {
	private static Logger log = Logger.getLogger(RSSMatrixCreator.class);
	private static final boolean PRINT_DEBUG=true;


    private SignalMap signalMap;
    private SignalMapConfig config;
    private SensorNetwork sensorNetwork;
    private EFrequencyRange frequencyRange;
	private boolean shouldIncludeExtendedNodes;

    private ITUIndoorModelDegradingDist model;
    private double hz;
    private SparseObjectMatrix2D matrix;


    public RSSMatrixCreator(SignalMap signalMap, SignalMapConfig config, SensorNetwork sensorNetwork, EFrequencyRange frequencyRange, boolean shouldIncludeExtendedNodes) {
        this.signalMap = signalMap;
        this.config = config;
        this.sensorNetwork = sensorNetwork;
        this.frequencyRange = frequencyRange;
	    this.shouldIncludeExtendedNodes = shouldIncludeExtendedNodes;
        initialize();
    }

    public SparseObjectMatrix2D getMatrix() {
        return matrix;
    }

    public List<RSSPoint> getReferencePoints() {
        List<RSSPoint> refPoints = new ArrayList<RSSPoint>();

        IntArrayList arrayListX = new IntArrayList();
        IntArrayList arrayListY = new IntArrayList();
        ObjectArrayList dataMapArrayList = new ObjectArrayList();
        matrix.getNonZeros(arrayListX,arrayListY,dataMapArrayList);

        for (int i = 0; i < dataMapArrayList.size(); i++) {
            refPoints.add(new RSSPoint(arrayListX.get(i),arrayListY.get(i), (Map<String, Double>) dataMapArrayList.get(i)));
        }
        return refPoints;
    }

    private void initialize() {
        model = new ITUIndoorModelDegradingDist(sensorNetwork.getEnvironmentModel(),sensorNetwork.getPathLossConfig());
        matrix = new SparseObjectMatrix2D(signalMap.getLengthX(),signalMap.getLengthY());
        hz = EFrequencyRange.frequencyHz(frequencyRange,1);

        for (Map.Entry<String, SignalMap.Vertex> entry : signalMap.getManagedNodes().entrySet()) {
            fillSignalStrengthMatrix(entry.getValue());
            if(PRINT_DEBUG) PosHelper.printToFile("signalstrengthmatrix_"+entry.getValue().getMac().replace(":","-")+".txt",PosHelper.printSparseMatrix(matrix,entry.getValue().getMac()));
        }

	    if(shouldIncludeExtendedNodes) {
		    for (Map.Entry<String, SignalMap.Vertex> entry : signalMap.getExtendedNodes().entrySet()) {
			    fillSignalStrengthMatrix(entry.getValue());
			    if (PRINT_DEBUG)
				    PosHelper.printToFile("signalstrengthmatrix_" + entry.getValue().getMac().replace(":", "-") + ".txt", PosHelper.printSparseMatrix(matrix, entry.getValue().getMac()));
		    }
	    } else {
		    log.debug("skip extended nodes");
	    }

	    if(PRINT_DEBUG) PosHelper.printToFile("signalstrengthmatrix.txt",PosHelper.printSparseMatrix(matrix));
    }

    private void fillSignalStrengthMatrix(SignalMap.Vertex vertex) {
        int radiusTiles = PosHelper.toTiles(PosHelper.convertMToCm(model.getDistanceInMeter(config.getMaxPathLossForSignalStrengthMatrixCalculation(),hz,0) * vertex.getSignalStrengthFac()),config.getTileLenghtCm());
        int minX = Math.max(0,vertex.getCurrentPos().getX()-radiusTiles);
        int minY = Math.max(0,vertex.getCurrentPos().getY()-radiusTiles);

        int maxX = Math.min(signalMap.getLengthX(),vertex.getCurrentPos().getX()+radiusTiles);
        int maxY = Math.min(signalMap.getLengthY(), vertex.getCurrentPos().getY() + radiusTiles);

        for (int column = minX; column < maxX; column++) {
            for (int row = minY; row < maxY; row++) {
                double distFromOrigin = getDistanceTiles(Math.abs(vertex.getCurrentPos().getX()-column),Math.abs(vertex.getCurrentPos().getY()-row));
                double pathLoss = model.getPathLossDb(distFromOrigin*config.getTileLenghtCm()/100d,hz,0) * -1;
                setPathLossToMatrix(column,row,vertex.getMac(),pathLoss);
            }
        }
    }

    private void setPathLossToMatrix(int x, int y, String mac,double pathLoss) {
        Map<String,Double> pathLossMap=null;

        if(Math.abs(pathLoss) > Math.abs(config.getMaxPathLossForSignalStrengthMatrixCalculation())) {
            return;
        }

        if(Double.isNaN(pathLoss) || Math.abs(pathLoss) < Math.abs(config.getMinPathLossValueForSignalStrengthMatrix())) {
            pathLoss=config.getMinPathLossValueForSignalStrengthMatrix()*-1;
        }

        if(x >= 0 && y >= 0 && x < signalMap.getLengthX() && y < signalMap.getLengthY()) {
            Object o = matrix.getQuick(x,y);
            if(o == null) {
                pathLossMap = new HashMap<String, Double>();
                matrix.setQuick(x,y,pathLossMap);
            } else {
                pathLossMap = (Map<String,Double>) o;
            }
            pathLossMap.put(mac,pathLoss);
        }
    }

    private static double getDistanceTiles(int offsetX, int offsetY) {
        return Math.sqrt(Math.pow(offsetX,2)+Math.pow(offsetY,2));
    }

    public static class RSSPoint {
        private int x;
        private int y;
        private Map<String,Double> pathLossMap;

        public RSSPoint() {
        }

        public RSSPoint(int x, int y, Map<String, Double> pathLossMap) {
            this.x = x;
            this.y = y;
            this.pathLossMap = pathLossMap;
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

        public Map<String, Double> getPathLossMap() {
            return pathLossMap;
        }

        public void setPathLossMap(Map<String, Double> pathLossMap) {
            this.pathLossMap = pathLossMap;
        }
    }
}

package at.ac.tuwien.inso.indoor.sensorserver.services.positioner;

import at.ac.tuwien.inso.indoor.sensorserver.math.radiomodels.EEnvironmentModel;
import at.ac.tuwien.inso.indoor.sensorserver.math.radiomodels.ITUIndoorModelDegradingDist;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.EFrequencyRange;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.SignalMapConfig;
import cern.colt.matrix.impl.SparseObjectMatrix2D;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Map;

/**
 * Created by PatrickF on 10.10.2014.
 */
public class PosHelper {
    public static final String EMPTY_STRING_REPRESENTATION = " ";
	private static Logger log = Logger.getLogger(PosHelper.class);

    public static int convertMToCm(double meter) {
        return (int) Math.round(meter * 100.0);
    }


    public static int toTiles(int cm,int tileLength) {
        return (int) Math.round((double) cm / (double) tileLength);
    }

    public static int probabilitySpread(int tileLengthCm, int maxSpreadCm) {
       int spreadTiles = (int) Math.round(((double)maxSpreadCm - (double) tileLengthCm) / (double) tileLengthCm);
       return Math.max(0,spreadTiles);
    }

    public static void printMatrixToConsole(SparseObjectMatrix2D matrix2D,String srcMac) {
        cheapConsolePrint(printSparseMatrix(matrix2D, srcMac));
    }
    public static void printMatrixToConsole(SparseObjectMatrix2D matrix2D) {
        cheapConsolePrint(printSparseMatrix(matrix2D));
    }

    public static void printMatrixToFile(String filename, SparseObjectMatrix2D matrix2D,String srcMac) {
        printToFile(filename, printSparseMatrix(matrix2D, srcMac));
    }
    public static void printMatrixToFile(String filename, SparseObjectMatrix2D matrix2D) {
        printToFile(filename, printSparseMatrix(matrix2D));
    }

    public static String printSparseMatrix(SparseObjectMatrix2D matrix2D,String srcMac) {
        StringBuilder sb = new StringBuilder();
        sb.append("Representation of mac: ").append(srcMac).append("\n");
        for (int i = 0; i < matrix2D.columns(); i++) {
            for (int j = 0; j < matrix2D.rows(); j++) {
                Object obj = matrix2D.get(j,i);
                if(obj == null) {
                    sb.append(EMPTY_STRING_REPRESENTATION);
                } else {
                    if(obj instanceof NodeProbabilityPoint) {
                        sb.append(((NodeProbabilityPoint) matrix2D.getQuick(i, j)).getSingleCharStringRepresentation(srcMac));
                    } else if(obj instanceof Map) {
                        Map<String,Double> map = (Map<String,Double>) obj;
                        if(map.containsKey(srcMac)) {
                            sb.append(String.valueOf(Math.abs(map.get(srcMac))).substring(0,1));
                        } else {
                            sb.append(EMPTY_STRING_REPRESENTATION);
                        }
                    } else {
                        sb.append(obj.toString().substring(0, 1));
                    }
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public static String printSparseMatrix(SparseObjectMatrix2D matrix2D) {
        StringBuilder sb = new StringBuilder();
        sb.append("Representation of all probabilities \n");
        for (int i = 0; i < matrix2D.columns(); i++) {
            for (int j = 0; j < matrix2D.rows(); j++) {
                Object obj = matrix2D.get(j,i);
                if(obj == null) {
                    sb.append(EMPTY_STRING_REPRESENTATION);
                } else {
                    if(obj instanceof NodeProbabilityPoint) {
                        sb.append(((NodeProbabilityPoint) matrix2D.getQuick(i, j)).getSingleCharStringRepresentation());
                    } else if(obj instanceof Map) {
                        Map<String,Double> map = (Map<String,Double>) obj;
                        sb.append(map.keySet().size());
                    } else {
                        sb.append(obj.toString().substring(0,1));
                    }
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void cheapConsolePrint(String s) {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new
                    FileOutputStream(java.io.FileDescriptor.out), "ASCII"), 512);
            out.write(s);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printToFile(String filename,String content) {
        BufferedWriter writer = null;
        try {
            File file = new File("C:\\Temp\\"+filename);
            file.createNewFile();
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
        } catch (Exception e) {
            log.error("Could not print matrix to file",e);
        } finally {
            try {if(writer != null) {writer.close();}} catch (Exception e) {e.printStackTrace();}
        }
    }

    public static double maxDistanceRadius(EEnvironmentModel environmentModel,ITUIndoorModelDegradingDist.ITUDegradingDistConfig config,SignalMapConfig signalMapConfig,EFrequencyRange frequencyRange) {
        ITUIndoorModelDegradingDist model = new ITUIndoorModelDegradingDist(environmentModel,config);
        return convertMToCm(model.getDistanceInMeter(signalMapConfig.getMaxPathLossForSignalStrengthMatrixCalculation(),EFrequencyRange.frequencyHz(frequencyRange,1),0));
    }

}

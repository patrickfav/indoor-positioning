package at.ac.tuwien.inso.indoor.sensorserver.services.positioner;

import cern.colt.matrix.impl.SparseObjectMatrix2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by PatrickF on 10.10.2014.
 */
public class MidpointAlgorithm {
    public static void simpleCharArrayDrawCircle(final int centerX, final int centerY, final int radius, char[][] image, char paint) {
        int d = (5 - radius * 4)/4;
        int x = 0;
        int y = radius;
        do {
            image[centerX + x][centerY + y] =  paint;
            image[centerX + x][centerY - y] =  paint;
            image[centerX - x][centerY + y] =  paint;
            image[centerX - x][centerY - y] =  paint;
            image[centerX + y][centerY + x] =  paint;
            image[centerX + y][centerY - x] =  paint;
            image[centerX - y][centerY + x] =  paint;
            image[centerX - y][centerY - x] =  paint;
            if (d < 0) {
                d += 2 * x + 1;
            } else {
                d += 2 * (x - y) + 1;
                y--;
            }
            x++;
        } while (x <= y);

    }

    public static void drawCircleToNodeProbSparseArray(final int centerX, final int centerY, final int radius, SparseObjectMatrix2D matrix, String macSrc, String macTarget, Double baseProbability, int spreadWidth) {
        int d = 5-(radius<<2);
        int x = 0;
        int y = radius;
        do {
            setProbabilitySpread(centerX + x,centerY + y,matrix,macSrc,macTarget,baseProbability,spreadWidth);
            setProbabilitySpread(centerX + x,centerY - y,matrix,macSrc,macTarget,baseProbability,spreadWidth);
            setProbabilitySpread(centerX - x,centerY + y,matrix,macSrc,macTarget,baseProbability,spreadWidth);
            setProbabilitySpread(centerX - x,centerY - y,matrix,macSrc,macTarget,baseProbability,spreadWidth);

            setProbabilitySpread(centerX + y,centerY + x,matrix,macSrc,macTarget,baseProbability,spreadWidth);
            setProbabilitySpread(centerX + y,centerY - x,matrix,macSrc,macTarget,baseProbability,spreadWidth);
            setProbabilitySpread(centerX - y,centerY + x,matrix,macSrc,macTarget,baseProbability,spreadWidth);
            setProbabilitySpread(centerX - y,centerY - x,matrix,macSrc,macTarget,baseProbability,spreadWidth);

            if (d < 0) {
                d += 2 * x + 1;
            } else {
                d += 2 * (x - y) + 1;
                y--;
            }
            x++;
        } while (x <= y);
    }

    private static void setProbabilitySpread(final int x, final int y,SparseObjectMatrix2D matrix,String macSrc,String macTarget, Double baseProb, int spread) {
        List<Double> probabilities = new ArrayList<Double>();

        for (int i = 0; i < spread; i++) {
            probabilities.add(getCDFNormalDistribution(i,0,i==0 ? 0.4:i));
        }

        setToSparseProbMatrix(x,y,matrix,macSrc,macTarget,probabilities.get(0));

        for (int i = 1; i < spread; i++) {
            for (int j = x-i; j <x+i ; j++) {
                setToSparseProbMatrix(j,y-i,matrix,macSrc,macTarget,probabilities.get(i));
                setToSparseProbMatrix(j,y+i,matrix,macSrc,macTarget,probabilities.get(i));
            }
            for (int j = y-i; j <y+i ; j++) {
                setToSparseProbMatrix(x+i,j,matrix,macSrc,macTarget,probabilities.get(i));
                setToSparseProbMatrix(x-i,j,matrix,macSrc,macTarget,probabilities.get(i));
            }
        }
    }

    private static void setToSparseProbMatrix(final int x, final int y,SparseObjectMatrix2D matrix,String macSrc,String macTarget, Double probability) {
        if(x >= 0 && y >= 0 & x < matrix.rows() && y < matrix.columns()) {
            NodeProbabilityPoint xyPoint=null;
            if((xyPoint = (NodeProbabilityPoint) matrix.getQuick(x,y))==null) {
                xyPoint = new NodeProbabilityPoint(x,y);
            }
            xyPoint.addProbability(macSrc,macTarget,probability);

            matrix.setQuick(x,y,xyPoint);
        }
    }



    private static double getCDFNormalDistribution(double x, double mean, double stdDev) {
        double a= 1.0/(stdDev*2.506628274631);
        return a* Math.exp(-(Math.pow((x-mean),2)/(2*Math.pow(stdDev,2))));
    }
}

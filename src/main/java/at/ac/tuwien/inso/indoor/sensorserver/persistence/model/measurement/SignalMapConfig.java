package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement;

import java.util.Random;

/**
 * Created by PatrickF on 27.10.2014.
 */
public class SignalMapConfig {
    private int tileLenghtCm = 10;

    private int defaultSpreadExtendedNodesCm = 70;
    private int defaultSpreaManagedNodesCm = 120;

    private int canvasDimensionManagedNodesCm = 30*100;
    private int canvasDimensionExtendedNodesCm = 40*100;
    private int cmOverflowForCalculatedCanvas = 200;

    private int minDbmNeededToConsiderForTrilateration = -85;
    private int minVisibilityNeededForTrilateration = 3;

    private long randomSeed = 1319874l; //-1 is total random

    private int maxPathLossForSignalStrengthMatrixCalculation = 90;
    private int minPathLossValueForSignalStrengthMatrix = 30;

    public SignalMapConfig() {
    }

    /*public SignalMapConfig(SignalMapConfig original) {
        setTileLenghtCm(original.getTileLenghtCm());
        setDefaultSpreadExtendedNodesCm(original.getDefaultSpreadExtendedNodesCm());
        setDefaultSpreaManagedNodesCm(original.getDefaultSpreaManagedNodesCm());
        setCanvasDimensionExtendedNodesCm(original.getCanvasDimensionExtendedNodesCm());
        setCanvasDimensionManagedNodesCm(original.getCanvasDimensionManagedNodesCm());
        setCmOverflowForCalculatedCanvas(original.getCmOverflowForCalculatedCanvas());
        setMinDbmNeededToConsiderForTrilateration(original.getMinDbmNeededToConsiderForTrilateration());
        setMinVisibilityNeededForTrilateration(original.getMinVisibilityNeededForTrilateration());
        setRandomSeed(original.getRandomSeed());

    }*/

    public int getTileLenghtCm() {
        return tileLenghtCm;
    }

    public void setTileLenghtCm(int tileLenghtCm) {
        this.tileLenghtCm = tileLenghtCm;
    }

    public int getDefaultSpreadExtendedNodesCm() {
        return defaultSpreadExtendedNodesCm;
    }

    public void setDefaultSpreadExtendedNodesCm(int defaultSpreadExtendedNodesCm) {
        this.defaultSpreadExtendedNodesCm = defaultSpreadExtendedNodesCm;
    }

    public int getDefaultSpreaManagedNodesCm() {
        return defaultSpreaManagedNodesCm;
    }

    public void setDefaultSpreaManagedNodesCm(int defaultSpreaManagedNodesCm) {
        this.defaultSpreaManagedNodesCm = defaultSpreaManagedNodesCm;
    }

    public int getCanvasDimensionManagedNodesCm() {
        return canvasDimensionManagedNodesCm;
    }

    public void setCanvasDimensionManagedNodesCm(int canvasDimensionManagedNodesCm) {
        this.canvasDimensionManagedNodesCm = canvasDimensionManagedNodesCm;
    }

    public int getCanvasDimensionExtendedNodesCm() {
        return canvasDimensionExtendedNodesCm;
    }

    public void setCanvasDimensionExtendedNodesCm(int canvasDimensionExtendedNodesCm) {
        this.canvasDimensionExtendedNodesCm = canvasDimensionExtendedNodesCm;
    }

    public int getCmOverflowForCalculatedCanvas() {
        return cmOverflowForCalculatedCanvas;
    }

    public void setCmOverflowForCalculatedCanvas(int cmOverflowForCalculatedCanvas) {
        this.cmOverflowForCalculatedCanvas = cmOverflowForCalculatedCanvas;
    }

    public int getMinDbmNeededToConsiderForTrilateration() {
        return minDbmNeededToConsiderForTrilateration;
    }

    public void setMinDbmNeededToConsiderForTrilateration(int minDbmNeededToConsiderForTrilateration) {
        this.minDbmNeededToConsiderForTrilateration = minDbmNeededToConsiderForTrilateration;
    }

    public int getMinVisibilityNeededForTrilateration() {
        return minVisibilityNeededForTrilateration;
    }

    public void setMinVisibilityNeededForTrilateration(int minVisibilityNeededForTrilateration) {
        this.minVisibilityNeededForTrilateration = minVisibilityNeededForTrilateration;
    }

    public long getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
    }


    public int getMaxPathLossForSignalStrengthMatrixCalculation() {
        return maxPathLossForSignalStrengthMatrixCalculation;
    }

    public void setMaxPathLossForSignalStrengthMatrixCalculation(int maxPathLossForSignalStrengthMatrixCalculation) {
        this.maxPathLossForSignalStrengthMatrixCalculation = maxPathLossForSignalStrengthMatrixCalculation;
    }

    public int getMinPathLossValueForSignalStrengthMatrix() {
        return minPathLossValueForSignalStrengthMatrix;
    }

    public void setMinPathLossValueForSignalStrengthMatrix(int minPathLossValueForSignalStrengthMatrix) {
        this.minPathLossValueForSignalStrengthMatrix = minPathLossValueForSignalStrengthMatrix;
    }

    public Random generateRnd() {
        if(randomSeed == -1) {
            return new Random();
        }
        return new Random(randomSeed);
    }
}

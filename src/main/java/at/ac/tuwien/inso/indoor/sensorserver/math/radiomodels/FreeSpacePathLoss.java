package at.ac.tuwien.inso.indoor.sensorserver.math.radiomodels;

import at.ac.tuwien.inso.indoor.sensorserver.util.RadioUtil;

/**
 * In telecommunication, free-space path loss (FSPL) is the loss in signal strength of an electromagnetic wave that
 * would result from a line-of-sight path through free space (usually air), with no obstacles nearby to cause reflection
 * or diffraction. It assumes that the antenna gain is 1.0, or 0 dBi. It does not include any loss associated with
 * hardware imperfections. A discussion of these losses may be found in the article on link budget. The FSPL is rarely
 * used standalone, but rather as a part of the Friis transmission equation.
 *
 * https://en.wikipedia.org/wiki/Free-space_path_loss
 *
 * Radio and antenna engineers use the following simplified formula
 * (also known as the Friis transmission equation) for the path loss
 * between two isotropic antennas in free space
 *
 * https://en.wikipedia.org/wiki/Path_loss#Radio_engineer_formula
 */
public class FreeSpacePathLoss implements IRadioPropagationModel {
    @Override
    public double getPathLossDb(double distanceMeter, double frequencyHz,int roomsBetween) {
        double wavelength = RadioUtil.waveLengthMeter(frequencyHz);
        return 20*Math.log10((4*Math.PI*distanceMeter)/wavelength);
    }

    @Override
    public double getDistanceInMeter(double pathLossDb, double frequencyHz,int roomsBetween) {
        double wavelength = RadioUtil.waveLengthMeter(frequencyHz);
        return (wavelength*Math.exp((1/20)*pathLossDb*(Math.log(2)+Math.log(5))+1))/(4*Math.PI);
    }
}

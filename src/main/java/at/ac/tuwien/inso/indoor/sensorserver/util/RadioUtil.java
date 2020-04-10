package at.ac.tuwien.inso.indoor.sensorserver.util;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.EFrequencyRange;

/**
 * Created by PatrickF on 30.09.2014.
 */
public final class RadioUtil {
    public static final double LIGHT_SPEED_M_S = 299792458;

    private RadioUtil() {
    }

    public static int convertToMhz(double frequencyHz) {
        return (int) frequencyHz / 1000 / 1000;
    }

    /**
     * The Frequency Range from a wlan can be roughly guessed by the channel number. They
     * are not always unique but in most case they are. See https://en.wikipedia.org/wiki/List_of_WLAN_channels
     *
     * @param channel
     * @return
     */
    public static EFrequencyRange guessFrequencyFromChannel(int channel) {
        if (channel > 0 && channel < 14) {
            return EFrequencyRange.WLAN_2_4Ghz;
        }

        if (channel == 34 || channel == 36 || channel == 38 || channel == 40 || channel == 42 ||
                channel == 44 || channel == 46 || channel == 48 || channel == 52 || channel == 56 ||
                channel == 60 || channel == 64 || channel == 100 || channel == 104 || channel == 108 ||
                channel == 112 || channel == 116 || channel == 120 || channel == 124 || channel == 128 ||
                channel == 132 || channel == 136 || channel == 140 || channel == 149 || channel == 153 ||
                channel == 157 || channel == 161 || channel == 165) {
            return EFrequencyRange.WLAN_5Ghz;
        }

        return EFrequencyRange.UNKNOWN;
    }

    public static double waveLengthMeter(double frequencyHz) {
        return LIGHT_SPEED_M_S / frequencyHz;
    }
}

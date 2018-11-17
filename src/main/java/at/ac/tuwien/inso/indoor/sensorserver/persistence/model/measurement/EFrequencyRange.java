package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement;

/**
 * Created by PatrickF on 12.09.2014.
 */
public enum EFrequencyRange {
    UNKNOWN, WLAN_2_4Ghz, WLAN_5Ghz;

    public static double frequencyHz(EFrequencyRange range, int channel) {
        if (range == WLAN_2_4Ghz) {
            switch (channel) {
                case 1:
                    return 2.412E12;
                case 2:
                    return 2.417E12;
                case 3:
                    return 2.422E12;
                case 4:
                    return 2.427E12;
                case 5:
                    return 2.432E12;
                case 6:
                    return 2.437E12;
                case 7:
                    return 2.442E12;
                case 8:
                    return 2.447E12;
                case 9:
                    return 2.452E12;
                case 10:
                    return 2.457E12;
                case 11:
                    return 2.462E12;
                case 12:
                    return 2.467E12;
                case 13:
                    return 2.472E12;
                case 14:
                    return 2.484E12;
                default:
                    return 2.4E12;
            }
        } else if (range == WLAN_5Ghz) {
            switch (channel) {
                case 36:
                    return 5.18E12;
                case 40:
                    return 5.2E12;
                case 44:
                    return 5.22E12;
                case 52:
                    return 5.26E12;
                case 100:
                    return 5.5E12;
                default:
                    return 5E12;
            }
        }
        return 2E12;
    }
}

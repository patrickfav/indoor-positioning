package at.ac.tuwien.inso.indoor.sensorserver.test.math;

import at.ac.tuwien.inso.indoor.sensorserver.services.positioner.MidpointAlgorithm;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by PatrickF on 10.10.2014.
 */
public class MidpointAlgorithmTest {
    private char[][] image1;

    @Before
    public void setup() {
        image1 = new char[101][101];
        for (int i = 0; i < 101; i++) {
            for (int j = 0; j < 101; j++) {
                image1[i][j] = '.';
            }
        }
    }

    @Test
    public void testMidpointAlgorithmWithDifferentRadii() throws Exception {
        MidpointAlgorithm.simpleCharArrayDrawCircle(50, 50, 2, image1, 'a');
        MidpointAlgorithm.simpleCharArrayDrawCircle(50, 50, 5, image1, '0');
        MidpointAlgorithm.simpleCharArrayDrawCircle(50, 50, 10, image1, '1');
        MidpointAlgorithm.simpleCharArrayDrawCircle(50, 50, 20, image1, '2');
        MidpointAlgorithm.simpleCharArrayDrawCircle(50, 50, 30, image1, '3');
        MidpointAlgorithm.simpleCharArrayDrawCircle(50, 50, 40, image1, '4');
        MidpointAlgorithm.simpleCharArrayDrawCircle(50, 50, 50, image1, '5');


        printCharArray(image1);
    }

    @Test
    public void testMidpointAlgorithmWithIncrementingRadii() throws Exception {
        for (int i = 0; i < 30; i++) {
            MidpointAlgorithm.simpleCharArrayDrawCircle(50, 50, i, image1, String.valueOf(i).charAt(0));
        }
        printCharArray(image1);
    }

    public static void printCharArray(char[][] array) {
        for (char[] innerArray : array) {
            for (char paint : innerArray) {
                System.out.print(paint);
            }
            System.out.print("\n");
        }
    }
}

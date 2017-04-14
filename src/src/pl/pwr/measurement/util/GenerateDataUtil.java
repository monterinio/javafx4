package src.pl.pwr.measurement.util;

import java.util.Random;

public class GenerateDataUtil {
    private GenerateDataUtil() {        }

    public static double generateVoltage1() {
        Random rand = new Random();
        return 50*rand.nextDouble()+200;
    }

    public static double generateVoltage2() {
        Random rand = new Random();
        return 50*rand.nextDouble()+350;
    }
}

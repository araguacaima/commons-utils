package com.araguacaima.commons.utils;

import java.util.Random;

public class RandomUtils {

    public static double getRandomDoubleInRange(double aStart, double aEnd, Random aRandom) {
        if (aStart > aEnd) {
            throw new IllegalArgumentException("Start cannot exceed End.");
        }
        double range = aEnd - aStart + 1;
        double fraction = (range * aRandom.nextDouble());
        return (fraction + aStart);
    }

    public static long getRandomLongInRange(long aStart, long aEnd, Random aRandom) {
        if (aStart > aEnd) {
            throw new IllegalArgumentException("Start cannot exceed End.");
        }
        long range = aEnd - aStart + 1;
        long fraction = (long) (range * aRandom.nextDouble());
        return (fraction + aStart);
    }
}

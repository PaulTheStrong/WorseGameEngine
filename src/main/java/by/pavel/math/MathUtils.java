package by.pavel.math;

import static java.lang.Math.min;

public class MathUtils {

    public static int bound(int max, int v) {
        return Math.max(min(v, max), 0);
    }

}

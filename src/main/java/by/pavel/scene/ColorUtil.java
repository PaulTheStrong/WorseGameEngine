package by.pavel.scene;

import static by.pavel.math.MathUtils.bound;

import by.pavel.math.Vector4f;

public class ColorUtil {

    public static final int BLUE = colorOf(0, 0, 255, 255);
    public static final int GREEN = colorOf(0, 255, 0, 255);
    public static final int RED = colorOf(255, 0, 0, 255);

    public static int colorOf(int r, int g, int b, int a) {
        return ((0xff) << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
    }

    public static Vector4f rgbaVec(int rgba) {
        int r = (rgba >> 16) & 0xff;
        int g = (rgba >> 8) & 0xff;
        int b = rgba & 0xff;
        int a = (rgba >> 25) & 0xff;
        return new Vector4f(r / 255f, g / 255f, b / 255f, 1.f);
    }

    public static int colorOf(Vector4f rgba) {
        return colorOf(
            bound(255, (int) (rgba.x * 255.f)),
            bound(255, (int) (rgba.y * 255.f)),
            bound(255, (int) (rgba.z * 255.f)),
            255);
    }
}

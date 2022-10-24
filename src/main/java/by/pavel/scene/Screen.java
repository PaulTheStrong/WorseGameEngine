package by.pavel.scene;

import by.pavel.math.Matrix4f;
import by.pavel.math.Vector3i;
import by.pavel.math.Vector4f;
import by.pavel.parser.OBJData;
import lombok.Getter;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static by.pavel.math.Vector4f.normalize;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.min;

public class Screen {

    static final Vector4f lightDirection = new Vector4f(2, -1, 1);
    private static final double COS_30 = cos(Math.PI / 180.f * 30.f);

    private final int width;
    private final int height;

    @Getter
    private final Camera camera;
    private final Projection projection;
    private float[] zBuffer;

    @Getter
    private final BufferedImage bufferedImage;

    public Screen(int width, int height) {
        this.width = width;
        this.height = height;
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        zBuffer = new float[width * height];
        camera = new Camera(0.3f, new Vector4f(0, 0, 0), new Vector4f(0, 0, 1), new Vector4f(0, 1, 0));
        projection = new Projection(45, 1.33f, 0, 100);
    }

    public void clear() {
        zBuffer = new float[width * height];
        for (int i = 0; i < width * height; i++) {
            zBuffer[i] = 1.0f;
        }
        bufferedImage.getGraphics().clearRect(0, 0, width, height);
    }

    public void drawPixel(int x, int y, int color, float z) {
        if (x >= 0 & x < width && y >= 0 && y < height) {
            float normalizedZ = z / projection.far;
            float initialBuf = zBuffer[y * width + x];
            if (normalizedZ < initialBuf) {
                zBuffer[y * width + x] = normalizedZ;
                bufferedImage.setRGB(x, height - 1 - y, color);
            }
        } else {
//            throw new IndexOutOfBoundsException("Error writing to buffer with params x = " + x + ", y = " + y);
        }
    }

    public static int colorOf(int r, int g, int b, int a) {
        return ((a & 0xff) << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
    }

    private Map<Integer, Integer> lineX(int x1, int x2, int y1, int y2) {
        Map<Integer, Integer> result = new HashMap<>();
        int deltaX = abs(x2 - x1); // 2
        int deltaY = abs(y2 - y1); // 6
        int signX = x1 < x2 ? 1 : -1; // -1
        int signY = y1 < y2 ? 1 : -1; // -1

        int error = deltaX - deltaY;

        while (x1 != x2 || y1 != y2) {
            result.putIfAbsent(y1, x1);
            int error2 = error * 2; // -8
            if (error2 > -deltaY) {
                error -= deltaY;
                x1 += signX;
            }
            if (error2 < deltaX) {
                error += deltaX;
                y1 += signY;
            }
        }
        return result;
    }

    public void drawLine(int x1, int x2, int y1, int y2, int color, float z) {
        int deltaX = abs(x2 - x1); // 2
        int deltaY = abs(y2 - y1); // 6
        int signX = x1 < x2 ? 1 : -1; // -1
        int signY = y1 < y2 ? 1 : -1; // -1

        if (abs(deltaX) > width * 4 || abs(deltaY) > height * 4) {
            return;
        }

        int error = deltaX - deltaY;

        drawPixel(x1, y1, color, z);
        while (x1 != x2 || y1 != y2) {
            drawPixel(x1, y1, color, z);
            int error2 = error * 2; // -8
            if (error2 > -deltaY) {
                error -= deltaY;
                x1 += signX;
            }
            if (error2 < deltaX) {
                error += deltaX;
                y1 += signY;
            }
        }
    }

    public void drawOBJ(Model model) {
        Matrix4f modelMatr = model.getModel();
        Matrix4f prjMatrix = projection.getProjectionMatrix();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f mvp = modelMatr.multiply(viewMatrix).multiply(prjMatrix);

        List<Vector4f> vertices = model.getVertices();
        for (List<Vector3i> face : model.getFaces()) {
            int verticesPerFace = face.size();
            for (int i = 1; i < verticesPerFace - 1; i++) {
                Vector4f v1 = vertices.get(face.get(0).x);
                Vector4f v2 = vertices.get(face.get(i).x);
                Vector4f v3 = vertices.get(face.get(i + 1).x);
                Vector4f v1Mvp = mvp.multiply(v1);
                Vector4f v2Mvp = mvp.multiply(v2);
                Vector4f v3Mvp = mvp.multiply(v3);
                Vector4f v1M = modelMatr.multiply(v1);
                Vector4f v2M = modelMatr.multiply(v2);
                Vector4f v3M = modelMatr.multiply(v3);
                if (shouldDraw(v1Mvp, v2Mvp, v3Mvp)) {
                    drawTriangle(v1Mvp, v2Mvp, v3Mvp, getColorWithLight(v1M, v2M, v3M));
                }
            }
        }
    }

    private boolean shouldDraw(Vector4f v1, Vector4f v2, Vector4f v3) {
        Vector4f side1 = new Vector4f(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z, 0);
        Vector4f side2 = new Vector4f(v3.x - v1.x, v3.y - v1.y, v3.z - v1.z, 0);
        Vector4f triangleNormal = normalize(side1).cross(normalize(side2));
        Vector4f minus = camera.getTarget().minus(camera.getEye());
        minus.w = 0;
        Vector4f direction = normalize(minus);
        return triangleNormal.dot(direction) < COS_30;
    }

    public int getColorWithLight(Vector4f v1, Vector4f v2, Vector4f v3) {

        Vector4f side1 = new Vector4f(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z, 0);
        Vector4f side2 = new Vector4f(v3.x - v1.x, v3.y - v1.y, v3.z - v1.z, 0);
        Vector4f triangleNormal = normalize(side1).cross(normalize(side2));
        Vector4f light = new Vector4f(-lightDirection.x, -lightDirection.y, -lightDirection.z, 0);
        float coeff = normalize(triangleNormal).dot(normalize(light));
        coeff = (coeff + 1.f) / 2.f;
        return colorOf(0, 0, (int)(255.f * coeff), 255);

    }

    public void drawTriangle(Vector4f v1, Vector4f v2, Vector4f v3, int color) {

        if (v1.z < 0 || v2.z < 0 || v3.z < 0) {
            return;
        }

        if (!shouldDraw(v1, v2, v3)) {
            return;
        }

        int x1 = (int) (v1.x / v1.w * (float) width / 2.f) + width / 2;
        int x2 = (int) (v2.x / v2.w * (float) width / 2.f) + width / 2;
        int x3 = (int) (v3.x / v3.w * (float) width / 2.f) + width / 2;

        int y1 = ((int) (v1.y / v1.w * (float) height / 2.f) + height / 2);
        int y2 = ((int) (v2.y / v2.w * (float) height / 2.f) + height / 2);
        int y3 = ((int) (v3.y / v3.w * (float) height / 2.f) + height / 2);

        float z1 = v1.z;
        float z2 = v2.z;
        float z3 = v3.z;
        float avgZ = (z1 + z2 + z3) / 3.f;

//        color = colorOf(255, 255, 255, 255);
        if (y1 > y2) {
            int tmp = y1;
            y1 = y2;
            y2 = tmp;
            tmp = x1;
            x1 = x2;
            x2 = tmp;
        }
        if (y1 > y3) {
            int tmp = y1;
            y1 = y3;
            y3 = tmp;
            tmp = x1;
            x1 = x3;
            x3 = tmp;
        }
        if (y2 > y3) {
            int tmp = y2;
            y2 = y3;
            y3 = tmp;
            tmp = x2;
            x2 = x3;
            x3 = tmp;
        }

        // y = kx + b
        // y1 = kx1 + b => b = y1 - kx1 => b = y1 - x1 * (y2 - y1) / (x2 - x1)
        // y2 = kx2 + b => y2 = kx2 + y1 - kx1 => y2 = k(x2 - x1) + y1 => k = (y2 - y1) / (x2 - x1)
        //

        // y = 1 - staight line equation
        // (1) y = k1x + b1 => x = (y - b1) / k1 => x = (c - y1 + x1 * (y2 - y1) / (x2 - x1)
        // (2) y = k2x + b2 => b2 = 1, k2x = 0 => y = 1

        Map<Integer, Integer> line1 = lineX(x1, x2, y1, y2);
        Map<Integer, Integer> line2 = lineX(x1, x3, y1, y3);
        Map<Integer, Integer> line3 = lineX(x2, x3, y2, y3);
        for (int i = bound(height, y1); i <= bound(height, y2); i++) {
            if (line1.containsKey(i) && line2.containsKey(i)) {
                drawLine(bound(width, line1.get(i)), bound(width, line2.get(i)), i, i, color, avgZ);
            }
        }

        for (int i = bound(height, y2); i <= bound(height, y3); i++) {
            if (line2.containsKey(i) && line3.containsKey(i)) {
                drawLine(bound(width, line3.get(i)), bound(width, line2.get(i)), i, i, color, avgZ);
            }
        }

        drawLine(x1, x2, y1, y2, color, avgZ);
        drawLine(x2, x3, y2, y3, color, avgZ);
        drawLine(x3, x1, y3, y1, color, avgZ);
    }

    private int bound(int max, int v) {
        return Math.max(min(v, max), 0);
    }

}
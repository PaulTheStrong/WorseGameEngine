package by.pavel.scene;

import by.pavel.math.Matrix4f;
import by.pavel.math.Vector3f;
import by.pavel.math.Vector3i;
import by.pavel.math.Vector4f;
import by.pavel.math.VertexData;
import by.pavel.parser.OBJData;
import lombok.Getter;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static by.pavel.math.Vector3f.normalize3;
import static by.pavel.math.Vector4f.normalize;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.min;
import static java.lang.Math.max;
import static java.lang.Math.pow;
import static java.lang.Math.round;

public class Screen {

    static final Vector4f lightPosition = new Vector4f(10, 20, -20, 0);

    private static final double COS_30 = cos(Math.PI / 180.f * 30.f);
    private static final int BLUE = colorOf(0, 0, 255, 255);
    private static final int GREEN = colorOf(0, 255, 0, 255);
    private static final int RED = colorOf(255, 0, 0, 255);
    private static final float DIFFUSE_INTENCITY = 0.5f;
    private static final float SPECULAR_INTENCITY = 0.5f;
    private static final float SHINYNESS = 1f;

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

    public static Vector4f rgbaVec(int rgba) {
        int r = (rgba >> 16) & 0xff;
        int g = (rgba >> 8) & 0xff;
        int b = rgba & 0xff;
        int a = (rgba >> 25) & 0xff;
        return new Vector4f(r / 255f, g / 255f, b / 255f, a / 255f);
    }

    public static int colorOf(Vector4f rgba) {
        return colorOf(min((int) (rgba.x * 255.f), 255), min((int) (rgba.y * 255.f), 255), min((int) (rgba.z * 255.f), 255), min((int) (rgba.w * 255.f), 255));
    }


    public void drawOBJ(Model model) {
        Matrix4f modelMatr = model.getModel();

        List<Vector3f> vertices = model.getVertices();
        List<Vector3f> normals = model.getNormals();
        for (List<Vector3i> face : model.getFaces()) {
            int verticesPerFace = face.size();
            for (int i = 1; i < verticesPerFace - 1; i++) {
                Vector3f v1 = vertices.get(face.get(0).x);
                Vector3f v2 = vertices.get(face.get(i).x);
                Vector3f v3 = vertices.get(face.get(i + 1).x);

                Vector3f v1n = normals.get(face.get(0).z);
                Vector3f v2n = normals.get(face.get(i).z);
                Vector3f v3n = normals.get(face.get(i + 1).z);

                int randomColor = colorOf(255, 179, 0, 255);
                VertexData vd1 = new VertexData(v1, v1n, modelMatr, randomColor);
                VertexData vd2 = new VertexData(v2, v2n, modelMatr, randomColor);
                VertexData vd3 = new VertexData(v3, v3n, modelMatr, randomColor);
                drawTriangle(vd1, vd2, vd3);
            }
        }
    }

    private Vector4f divideByW(Vector4f v) {
        return new Vector4f(v.x / v.w, v.y / v.w, v.z, 1);
    }

    private boolean isBackface(VertexData vd1, VertexData vd2, VertexData vd3) {
        Vector4f v1 = vd1.transform.multiply(new Vector4f(vd1.position, 1));
        Vector4f v2 = vd2.transform.multiply(new Vector4f(vd2.position, 1));
        Vector4f v3 = vd3.transform.multiply(new Vector4f(vd3.position, 1));
        Vector4f side1 = new Vector4f(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z, 0);
        Vector4f side2 = new Vector4f(v3.x - v1.x, v3.y - v1.y, v3.z - v1.z, 0);
        Vector4f triangleNormal = normalize(side1.cross(side2));
        Vector4f minus = v1.minus(camera.getEye());
        minus.w = 0;
        Vector4f direction = normalize(minus);
        return direction.dot(triangleNormal) < 0;
    }

    private float edge(float x1, float x2, float y1, float y2, float px, float py) {
        return (px - x1) * (y2 - y1) - (py - y1) * (x2 - x1);
    }


    private static final float ambientCoeff = 0.1f;


    public void drawTriangle(VertexData vd3, VertexData vd2, VertexData vd1) {

        if (isBackface(vd1, vd2, vd3)) {
            return;
        }

        Matrix4f mvp = vd1.transform.multiply(camera.getViewMatrix()).multiply(projection.projection);
        Vector4f v1 = mvp.multiply(new Vector4f(vd1.position, 1.0f));
        Vector4f v2 = mvp.multiply(new Vector4f(vd2.position, 1.0f));
        Vector4f v3 = mvp.multiply(new Vector4f(vd3.position, 1.0f));

        Vector4f vm1 = vd1.transform.multiply(new Vector4f(vd1.position, 1.0f));
        Vector4f vm2 = vd2.transform.multiply(new Vector4f(vd2.position, 1.0f));
        Vector4f vm3 = vd3.transform.multiply(new Vector4f(vd3.position, 1.0f));

        Vector4f v1n = vd1.transform.multiply(new Vector4f(vd1.normal, 0));
        Vector4f v2n = vd2.transform.multiply(new Vector4f(vd2.normal, 0));
        Vector4f v3n = vd3.transform.multiply(new Vector4f(vd3.normal, 0));

        float x1 = (v1.x / v1.w * width / 2.f) + width / 2.f;
        float x2 = (v2.x / v2.w * width / 2.f) + width / 2.f;
        float x3 = (v3.x / v3.w * width / 2.f) + width / 2.f;

        float y1 = (v1.y / v1.w * height / 2.f) + height / 2.f;
        float y2 = (v2.y / v2.w * height / 2.f) + height / 2.f;
        float y3 = (v3.y / v3.w * height / 2.f) + height / 2.f;

        float z1 = v1.z;
        float z2 = v2.z;
        float z3 = v3.z;
        float avgZ = (z1 + z2 + z3) / 3.f;

        if (z1 < 0 || z2 < 0 || z3 < 0) {
            return;
        }

        float minX = min(min(x1, x2), x3);
        float maxX = max(max(x1, x2), x3);
        float minY = min(min(y1, y2), y3);
        float maxY = max(max(y1, y2), y3);
        for (float y = bound(round(minY), height); y <= bound(round(maxY), height); y += 1) {
            for (float x = bound(round(minX), width); x <= bound(round(maxX), width); x += 1) {
                float e1 = edge(x1, x2, y1, y2, x, y);
                float e2 = edge(x2, x3, y2, y3, x, y);
                float e3 = edge(x3, x1, y3, y1, x, y);
                if (e1 >= 0 && e3 >= 0 && e2 >= 0) {
                    float area = edge(x1, x2, y1, y2, x3, y3);
                    float w3 = e1 / area;
                    float w2 = e3 / area;
                    float w1 = e2 / area;
                    Vector4f pixelNormal = v1n.mul(w1).plus(v2n.mul(w2)).plus(v3n.mul(w3));
                    Vector4f pixelColor = rgbaVec(vd1.color).mul(w1).plus(rgbaVec(vd2.color).mul(w2)).plus(rgbaVec(vd3.color).mul(w3));

                    Vector4f pixelModelPosition = vm1.mul(w1).plus(vm2.mul(w2)).plus(vm3.mul(w3));

                    Vector4f ambientColor = pixelColor.mul(ambientCoeff);
                    Vector4f lightDirection = normalize(lightPosition.minus(pixelModelPosition));
                    Vector4f viewDirection = normalize(camera.getEye().minus(pixelModelPosition));

                    float diffuseCoeff = max(0, lightDirection.dot(pixelNormal));
                    Vector4f diffuseColor = pixelColor.mul(diffuseCoeff);

                    Vector4f R = pixelNormal.mul(2 * lightDirection.dot(pixelNormal)).minus(lightDirection);
                    float specularCoeff = (float) pow(viewDirection.dot(R), SHINYNESS) * SPECULAR_INTENCITY;

                    float w = pixelColor.w;
                    Vector4f finalColor = pixelColor.mul(ambientCoeff + diffuseCoeff + specularCoeff);
                    finalColor.w = w;

                    drawPixel((int) x, (int) y, colorOf(finalColor), z1 * w1 + z2 * w2 + z3 * w3);
                }
            }
        }
    }

    private int bound(int max, int v) {
        return Math.max(min(v, max), 0);
    }

}
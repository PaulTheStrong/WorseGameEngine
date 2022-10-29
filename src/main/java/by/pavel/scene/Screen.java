package by.pavel.scene;

import by.pavel.math.Matrix4f;
import by.pavel.math.Vector3f;
import by.pavel.math.Vector3i;
import by.pavel.math.Vector4f;
import by.pavel.math.VertexData;
import by.pavel.shader.PhongPixelShader;
import by.pavel.shader.PixelShader;
import lombok.Getter;

import java.awt.image.BufferedImage;
import java.util.List;

import static by.pavel.math.MathUtils.bound;
import static by.pavel.math.Vector3f.negate3;
import static by.pavel.math.Vector3f.normalize3;
import static by.pavel.math.Vector3f.reflect3;
import static by.pavel.math.Vector4f.normalize;
import static by.pavel.scene.ColorUtil.colorOf;
import static by.pavel.scene.ColorUtil.rgbaVec;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.lang.Math.max;
import static java.lang.Math.pow;
import static java.lang.Math.round;

public class Screen {

    static final Vector3f lightPosition = new Vector3f(0, 0, 0);
    static final Vector3f DIFFUSE_LIGHT_DIRECTION = new Vector3f(0, 0, 1);
    static final Vector4f LIGHT_COLOR = new Vector4f(0, 0, 1, 1);

    private static final float SPECULAR_INTENSITY = 0.3f;
    private static final float DIFFUSE_INTENSITY = 0.3f;
    private static final float AMBIENT_COEFF = 0.3f;
    private static final float SHININESS = 100;

    private final int width;
    private final int height;

    @Getter
    private final Camera camera;
    private final Projection projection;
    private final List<LightSource> lightSources;

    private float[] zBuffer;

    @Getter
    private final BufferedImage bufferedImage;

    public Screen(int width, int height, List<LightSource> lightSources) {
        this.width = width;
        this.height = height;
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        zBuffer = new float[width * height];
        camera = new Camera(0.3f, new Vector3f(0, 0, 0), new Vector3f(0, 0, 1), new Vector3f(0, 1, 0));
        projection = new Projection(45, 1.33f, 0, 100);
        this.lightSources = lightSources;
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

    public void drawPhong(Vector4f modelColor, Model model) {
        drawOBJ(modelColor, model, new PhongPixelShader(lightSources, 0.3f));
    }

    public void drawStraight(Vector4f modelColor, Model model) {
        drawOBJ(modelColor, model, Screen::straightColor);
    }

    public void drawOBJ(Vector4f modelColor, Model model, PixelShader pixelShader) {
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

                VertexData vd1 = new VertexData(v1, v1n, modelMatr, modelColor);
                VertexData vd2 = new VertexData(v2, v2n, modelMatr, modelColor);
                VertexData vd3 = new VertexData(v3, v3n, modelMatr, modelColor);
                drawTriangle(vd1, vd2, vd3, pixelShader);
            }
        }
    }

    private Vector4f divideByW(Vector4f v) {
        return new Vector4f(v.x / v.w, v.y / v.w, v.z, 1);
    }

    private boolean isBackface(Vector3f vm1, Vector3f vm2, Vector3f vm3) {
        Vector3f side1 = new Vector3f(vm2.x - vm1.x, vm2.y - vm1.y, vm2.z - vm1.z);
        Vector3f side2 = new Vector3f(vm3.x - vm1.x, vm3.y - vm1.y, vm3.z - vm1.z);
        Vector3f triangleNormal = normalize3(side1.cross(side2));
        Vector3f minus = vm1.minus(camera.getEye());
        Vector3f direction = normalize3(minus);
        return direction.dot(triangleNormal) < 0;
    }

    private float edge(float x1, float x2, float y1, float y2, float px, float py) {
        return (px - x1) * (y2 - y1) - (py - y1) * (x2 - x1);
    }



    public void drawTriangle(VertexData vd3, VertexData vd2, VertexData vd1, PixelShader pixelShader) {

        Vector3f vm1 = vd1.transform.multiply(new Vector4f(vd1.position, 1.0f)).getXYZ();
        Vector3f vm2 = vd2.transform.multiply(new Vector4f(vd2.position, 1.0f)).getXYZ();
        Vector3f vm3 = vd3.transform.multiply(new Vector4f(vd3.position, 1.0f)).getXYZ();

        if (isBackface(vm1, vm2, vm3)) {
            return;
        }

        Matrix4f mvp = vd1.transform.multiply(camera.getViewMatrix()).multiply(projection.projection);
        Vector4f v1 = mvp.multiply(new Vector4f(vd1.position, 1.0f));
        Vector4f v2 = mvp.multiply(new Vector4f(vd2.position, 1.0f));
        Vector4f v3 = mvp.multiply(new Vector4f(vd3.position, 1.0f));

        Vector3f v1n = vd1.transform.multiply(new Vector4f(vd1.normal, 0)).getXYZ();
        Vector3f v2n = vd2.transform.multiply(new Vector4f(vd2.normal, 0)).getXYZ();
        Vector3f v3n = vd3.transform.multiply(new Vector4f(vd3.normal, 0)).getXYZ();

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
        Vector3f cameraPosition = camera.getEye();
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

                    Vector3f pixelNormal = normalize3(v1n.mul(w1).plus(v2n.mul(w2)).plus(v3n.mul(w3)));
                    Vector3f pixelModelPosition = vm1.mul(w1).plus(vm2.mul(w2)).plus(vm3.mul(w3));
                    Vector4f pixelColor = vd1.color.mul(w1).plus(vd2.color.mul(w2)).plus(vd3.color.mul(w3));

                    Vector4f finalColor = pixelShader.getPixelColor(cameraPosition, pixelNormal, pixelModelPosition, pixelColor);

                    drawPixel((int) x, (int) y, colorOf(finalColor), z1 * w1 + z2 * w2 + z3 * w3);
                }
            }
        }
    }

    private static Vector4f straightColor(Vector3f cameraPosition, Vector3f pixelNormal, Vector3f pixelModelPosition, Vector4f pixelColor) {
        return pixelColor;
    }

    private static Vector4f flatShadingColor(Vector3f cameraPosition, Vector3f pixelNormal, Vector3f pixelModelPosition, Vector4f pixelColor) {
        return pixelColor.mul((1.f + pixelNormal.dot(negate3(DIFFUSE_LIGHT_DIRECTION))) / 2.f);
    }

}
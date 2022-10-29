package by.pavel.scene;

import by.pavel.math.Matrix3f;
import by.pavel.math.Matrix4f;
import by.pavel.math.Vector2f;
import by.pavel.math.Vector3f;
import by.pavel.math.Vector3i;
import by.pavel.math.Vector4f;
import by.pavel.math.VertexData;
import by.pavel.shader.PhongPixelShader;
import by.pavel.shader.PixelShader;
import lombok.Getter;
import lombok.SneakyThrows;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.List;

import static by.pavel.math.MathUtils.bound;
import static by.pavel.math.Vector3f.ONE_VECTOR_3F;
import static by.pavel.math.Vector3f.negate3;
import static by.pavel.math.Vector3f.normalize3;
import static by.pavel.scene.ColorUtil.colorOf;
import static by.pavel.scene.ColorUtil.rgbaVec;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.lang.Math.max;
import static java.lang.Math.round;

public class Screen {

    static final Vector3f lightPosition = new Vector3f(0, 0, 0);
    static final Vector3f DIFFUSE_LIGHT_DIRECTION = new Vector3f(0, 0, 1);
    static final Vector4f LIGHT_COLOR = new Vector4f(0, 0, 1, 1);

    private static final float SPECULAR_INTENSITY = 0.3f;
    private static final float DIFFUSE_INTENSITY = 0.3f;
    private static final float AMBIENT_COEFF = 0.3f;
    private static final float SHININESS = 100;
    private static final Vector2f ZERO_VECTOR2f = new Vector2f(0, 0);
    private static final int MAX_INT_MIN_1 = MAX_VALUE - 1;

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
        List<Vector2f> uvTextures = model.getUvTextures();
        Raster texture = model.getTexture();
        Raster normalMap = model.getNormalMap();
        for (List<Vector3i> face : model.getFaces()) {
            int verticesPerFace = face.size();
            for (int i = 1; i < verticesPerFace - 1; i++) {
                Vector3f v1 = vertices.get(face.get(0).x);
                Vector3f v2 = vertices.get(face.get(i).x);
                Vector3f v3 = vertices.get(face.get(i + 1).x);

                Vector3f v1n = normals.get(face.get(0).z);
                Vector3f v2n = normals.get(face.get(i).z);
                Vector3f v3n = normals.get(face.get(i + 1).z);

                Vector2f v1t = ZERO_VECTOR2f, v2t = ZERO_VECTOR2f, v3t = ZERO_VECTOR2f;

                if (texture != null) {
                    v1t = face.get(0).y == MAX_INT_MIN_1 ? ZERO_VECTOR2f : uvTextures.get(face.get(0).y);
                    v2t = face.get(i).y == MAX_INT_MIN_1 ? ZERO_VECTOR2f : uvTextures.get(face.get(i).y);
                    v3t = face.get(i + 1).y == MAX_INT_MIN_1 ? ZERO_VECTOR2f : uvTextures.get(face.get(i + 1).y);
                }
                VertexData vd1 = new VertexData(v1, v1n, modelMatr, modelColor, v1t);
                VertexData vd2 = new VertexData(v2, v2n, modelMatr, modelColor, v2t);
                VertexData vd3 = new VertexData(v3, v3n, modelMatr, modelColor, v3t);
                drawTriangle(vd1, vd2, vd3, pixelShader, texture, normalMap);
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

    @SneakyThrows
    public void drawTriangle(VertexData vd3, VertexData vd2, VertexData vd1, PixelShader pixelShader, Raster texture, Raster normalMap) {

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

        float v1tx = 0, v1ty = 0, v2tx = 0, v2ty = 0, v3tx = 0, v3ty = 0;
        Vector3f tanget = new Vector3f(), bitangent = new Vector3f();
        Vector3f worldTangent = new Vector3f();

        if (texture != null) {
            v1tx = vd1.texture.x;
            v1ty = vd1.texture.y;
            v2tx = vd2.texture.x;
            v2ty = vd2.texture.y;
            v3tx = vd3.texture.x;
            v3ty = vd3.texture.y;

            Vector3f edge1 = vm2.minus(vm1);
            Vector3f edge2 = vm3.minus(vm1);

            float dU1 = v2tx - v1tx;
            float dV1 = v2ty - v1ty;
            float dU2 = v3tx - v1tx;
            float dV2 = v3ty - v1ty;


            float f = 1.0f / (dU1 * dV1 - dU2 * dV2);

            tanget.x = f * (dV2 * edge1.x - dV1 * edge2.x);
            tanget.y = f * (dV2 * edge1.y - dV1 * edge2.y);
            tanget.z = f * (dV2 * edge1.z - dV1 * edge2.z);

            bitangent.x = f * (-dU2 * edge1.x + dU1 * edge2.x);
            bitangent.y = f * (-dU2 * edge1.y + dU1 * edge2.y);
            bitangent.z = f * (-dU2 * edge1.z + dU1 * edge2.z);

            worldTangent = normalize3(vd1.transform.multiply(new Vector4f(tanget, 1)).getXYZ());
        }

        if (z1 < 0 || z2 < 0 || z3 < 0) {
            return;
        }

        float minX = min(min(x1, x2), x3);
        float maxX = max(max(x1, x2), x3);
        float minY = min(min(y1, y2), y3);
        float maxY = max(max(y1, y2), y3);
        Vector3f cameraPosition = camera.getEye();

        int[] color = new int[4];
        int[] normalFromMap = new int[4];
        int tWidth = texture == null ? 0 : texture.getWidth();
        int tHeight = texture == null ? 0 : texture.getHeight();
        Matrix3f TBN;
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

                    int ptx = (int) ((v1tx * w1 + v2tx * w2 + v3tx * w3) * tWidth);
                    int pty = tHeight - 1 - (int) ((v1ty * w1 + v2ty * w2 + v3ty * w3) * tHeight);

                    Vector3f pixelNormal = normalize3(v1n.mul(w1).plus(v2n.mul(w2)).plus(v3n.mul(w3)));
                    if (normalMap != null) {
                        normalMap.getPixel(ptx, pty, normalFromMap);
                        Vector3f bumpMapNormal = pixelNormal = new Vector3f(normalFromMap[0], normalFromMap[1], normalFromMap[2]);
                        Vector3f pixelTangent = normalize3(worldTangent.minus(pixelNormal));
                        Vector3f pixelBitangent = tanget.cross(pixelNormal);
                        bumpMapNormal = bumpMapNormal.mul(2.0f).minus(ONE_VECTOR_3F);
                        TBN = new Matrix3f(pixelTangent, pixelBitangent, pixelNormal);
                        pixelNormal = normalize3(TBN.multiply(bumpMapNormal));
                    }

                    Vector3f pixelModelPosition = vm1.mul(w1).plus(vm2.mul(w2)).plus(vm3.mul(w3));

                    int[] pixelColorArr = texture == null ? null : texture.getPixel(ptx, pty, color);
                    Vector4f pixelColor = texture == null ?
                        vd1.color.mul(w1).plus(vd2.color.mul(w2)).plus(vd3.color.mul(w3)) :
                        rgbaVec(colorOf(pixelColorArr[0], pixelColorArr[1], pixelColorArr[2], 255));

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
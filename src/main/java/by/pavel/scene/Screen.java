package by.pavel.scene;

import by.pavel.math.Matrix4f;
import by.pavel.math.Vector2f;
import by.pavel.math.Vector2i;
import by.pavel.math.Vector3f;
import by.pavel.math.Vector3i;
import by.pavel.math.Vector4f;
import by.pavel.math.VertexData;
import by.pavel.shader.CalcPhongPixelShader;
import by.pavel.shader.SpecularMapPhongPixelShader;
import by.pavel.shader.PixelData;
import by.pavel.shader.PixelShader;
import lombok.Getter;
import lombok.SneakyThrows;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static by.pavel.math.MathUtils.bound;
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
            zBuffer[i] = Float.POSITIVE_INFINITY;
        }
        bufferedImage.getGraphics().clearRect(0, 0, width, height);
    }

    public void drawPixel(int x, int y, int color) {
        bufferedImage.setRGB(x, height - 1 - y, color);
    }

    public void drawPhong(Vector4f modelColor, Model model) {
       if (Optional.ofNullable(model.getSpecularMap()).isPresent()) {
           drawOBJ(modelColor, model, new SpecularMapPhongPixelShader(lightSources, 0.3f));
       } else {
           drawOBJ(modelColor, model, new CalcPhongPixelShader(lightSources, 0.3f));
       }
    }

    public void drawStraight(Vector4f modelColor, Model model) {
        drawOBJ(modelColor, model, Screen::straightColor);
    }

    public void drawOBJ(Vector4f modelColor, Model model, PixelShader pixelShader) {
        Matrix4f modelMatr = model.getModel();

        Raster texture = model.getTexture();
        List<Vector3f> vertices = model.getVertices();
        List<Vector3f> normals = model.getNormals();
        List<Vector2f> uvTextures = model.getUvTextures();
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
                drawTriangle(model, vd1, vd2, vd3, pixelShader);
            }
        }
        System.out.println("Finished drawing");
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
    public void drawTriangle(Model model, VertexData vd3, VertexData vd2, VertexData vd1, PixelShader pixelShader) {

        Raster texture = model.getTexture();
        Raster normalMap = model.getNormalMap();
        Raster specularMap = model.getSpecularMap();

        Matrix4f transform = vd1.transform;
        Vector3f vm1 = transform.multiply(new Vector4f(vd1.position, 1.0f)).getXYZ();
        Vector3f vm2 = vd2.transform.multiply(new Vector4f(vd2.position, 1.0f)).getXYZ();
        Vector3f vm3 = vd3.transform.multiply(new Vector4f(vd3.position, 1.0f)).getXYZ();

        if (isBackface(vm1, vm2, vm3)) {
            return;
        }

        Matrix4f mvp = transform.multiply(camera.getViewMatrix()).multiply(projection.projection);
        Vector4f v1 = mvp.multiply(new Vector4f(vd1.position, 1.0f));
        Vector4f v2 = mvp.multiply(new Vector4f(vd2.position, 1.0f));
        Vector4f v3 = mvp.multiply(new Vector4f(vd3.position, 1.0f));

        Vector3f v1n = transform.multiply(new Vector4f(vd1.normal, 0)).getXYZ();
        Vector3f v2n = vd2.transform.multiply(new Vector4f(vd2.normal, 0)).getXYZ();
        Vector3f v3n = vd3.transform.multiply(new Vector4f(vd3.normal, 0)).getXYZ();

        float z1 = v1.z;
        float z2 = v2.z;
        float z3 = v3.z;

        if (z1 < 0 || z2 < 0 || z3 < 0) {
            return;
        }

        float x1 = (v1.x / v1.w * width / 2.f) + width / 2.f;
        float x2 = (v2.x / v2.w * width / 2.f) + width / 2.f;
        float x3 = (v3.x / v3.w * width / 2.f) + width / 2.f;

        float y1 = (v1.y / v1.w * height / 2.f) + height / 2.f;
        float y2 = (v2.y / v2.w * height / 2.f) + height / 2.f;
        float y3 = (v3.y / v3.w * height / 2.f) + height / 2.f;

        float v1tx = 0, v1ty = 0, v2tx = 0, v2ty = 0, v3tx = 0, v3ty = 0;
//        Vector3f tangent = new Vector3f();
//        Vector3f bitangent = new Vector3f();
//        Matrix3f TBN = null;
        if (texture != null || normalMap != null || specularMap != null) {
            v1tx = vd1.texture.x;
            v1ty = vd1.texture.y;
            v2tx = vd2.texture.x;
            v2ty = vd2.texture.y;
            v3tx = vd3.texture.x;
            v3ty = vd3.texture.y;

//            Vector3f edge1 = vm2.minus(vm1);
//            Vector3f edge2 = vm3.minus(vm1);
//
//            float dU1 = v2tx - v1tx;
//            float dV1 = v2ty - v1ty;
//            float dU2 = v3tx - v1tx;
//            float dV2 = v3ty - v1ty;
//
//            float f = 1.0f / (dU1 * dV2 - dU2 * dV1);
//
//            tangent.x = f * (dV2 * edge1.x - dV1 * edge2.x);
//            tangent.y = f * (dV2 * edge1.y - dV1 * edge2.y);
//            tangent.z = f * (dV2 * edge1.z - dV1 * edge2.z);
//
//            bitangent.x = f * (-dU2 * edge1.x + dU1 * edge2.x);
//            bitangent.y = f * (-dU2 * edge1.y + dU1 * edge2.y);
//            bitangent.z = f * (-dU2 * edge1.z + dU1 * edge2.z);
//            TBN = Matrix3f.transpose(new Matrix3f(
//                normalize3(tangent),
//                normalize3(bitangent),
//                normalize3(tangent.cross(bitangent))
//            ));

        }

        v1tx /= z1; v1ty /= z1;
        v2tx /= z2; v2ty /= z2;
        v3tx /= z3; v3ty /= z3;

        // pre-compute 1 over z
        z1 = 1.f / z1;
        z2 = 1.f / z2;
        z3 = 1.f / z3;

        float minX = min(min(x1, x2), x3);
        float maxX = max(max(x1, x2), x3);
        float minY = min(min(y1, y2), y3);
        float maxY = max(max(y1, y2), y3);
        Vector3f cameraPosition = camera.getEye();

        int[] color = new int[4];
        int[] normal = new int[4];
        int[] specular = new int[4];
        int tWidth = texture == null ? 0 : texture.getWidth() - 1;
        int tHeight = texture == null ? 0 : texture.getHeight() - 1;
        int nWidth = normalMap == null ? 0 : normalMap.getWidth() - 1;
        int nHeight = normalMap == null ? 0 : normalMap.getHeight() - 1;

//        List<Vector3f> sorted = List.of(new Vector3f(x1, y1, z1), new Vector3f(x2, y2, z2), new Vector3f(x3, y3, z3)).stream().sorted(Comparator.comparingDouble(v -> v.y))
//            .collect(Collectors.toList());
//        x1 = sorted.get(0).x;
//        y1 = sorted.get(0).y;
//        z1 = sorted.get(0).z;
//        x2 = sorted.get(1).x;
//        y2 = sorted.get(1).y;
//        z2 = sorted.get(1).z;
//        x3 = sorted.get(2).x;
//        y3 = sorted.get(2).y;
//        z3 = sorted.get(2).z;
//
//        float slope12 = (y2 - y1) / (x2 - x1);
//        float slope23 = (y3 - y2) / (x3 - x2);
//        float slope13 = (y3 - y1) / (x3 - x1);
//
//        List<Vector2i> points = new ArrayList<>();
//
//        for (int y = (int)(y1 + 0.5f); y < (int)(y2 + 0.5f); y++) {
//            int xLeft = (int) (x1 + (y - y1) * slope13);
//            int xRight = (int) (x1 + (y - y1) * slope12);
//            if (xLeft > xRight) {
//                int temp = xLeft;
//                xLeft = xRight;
//                xRight = temp;
//            }
//            points.add(new Vector2i(bound(width, xLeft), y));
//            points.add(new Vector2i(bound(width, xRight), y));
//        }
//        for (int y = (int)(y2 + 0.5f); y < (int)(y3 + 0.5f); y++) {
//            int xLeft = (int) (x1 + (y - y1) * slope13);
//            int xRight = (int) (x1 + (y - y2) * slope23);
//            if (xLeft > xRight) {
//                int temp = xLeft;
//                xLeft = xRight;
//                xRight = temp;
//            }
//            points.add(new Vector2i(bound(width, xLeft), y));
//            points.add(new Vector2i(bound(width, xRight), y));
//        }

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

                    int idx = (int) y * width + (int) x;
                    if (x < 0 || x >= width || y < 0 || y >= height) {
                        continue;
                    }
                    float initialBuf = zBuffer[idx];
                    float z = 1 / (w1 * z1 + w2 * z2 + w3 * z3);
                    if (z < initialBuf) {
                        zBuffer[idx] = z;
                    } else {
                        continue;
                    }
                    float s = w1 * v1tx + w2 * v2tx + w3 * v3tx;
                    float t = w1 * v1ty + w2 * v2ty + w3 * v3ty;

                    // if we use perspective correct interpolation we need to
                    // multiply the result of this interpolation by z, the depth
                    // of the point on the 3D triangle that the pixel overlaps.
                    s *= z;
                    t *= z;

                    int sTexture = (int)(s * tWidth);
                    int tTexture = (int)(tHeight * (1 - t));

                    int sNormal = (int)(s * nWidth);
                    int tNormal = (int)(nHeight * (1 - t));

                    int[] pixelColorArr = texture == null ? null : texture.getPixel(sTexture, tTexture, color);
                    int[] normalArr = normalMap == null ? null : normalMap.getPixel(sNormal, tNormal, normal);
                    int[] specularArr = specularMap == null ? null : specularMap.getPixel(sNormal, tNormal, specular);

//                    Matrix3f finalTBN = TBN;
                    Supplier<Vector3f> pixelNormalSupplier = normalMap == null
                        ? () -> v1n.mul(w1).plus(v2n.mul(w2)).plus(v3n.mul(w3))
                        : () -> transform.multiply(new Vector4f(normalArr[0] * 2 - 256f, normalArr[1] * 2 - 256f, normalArr[2] * 2 - 256f, 0)).getXYZ();
                    Vector3f pixelNormal = normalize3(pixelNormalSupplier.get());
                    Vector3f pixelModelPosition = vm1.mul(w1).plus(vm2.mul(w2)).plus(vm3.mul(w3));

                    Supplier<Vector4f> colorSupplier = texture == null ?
                        () -> vd1.color.mul(w1).plus(vd2.color.mul(w2)).plus(vd3.color.mul(w3)) :
                        () -> rgbaVec(colorOf(pixelColorArr[0], pixelColorArr[1], pixelColorArr[2], 255));

                    Vector4f pixelColor = colorSupplier.get();

                    Supplier<Float> specularCoefficient = specularMap == null
                        ? () -> null
                        : () -> specularArr[0] / 255f;

                    PixelData pixelData = new PixelData(pixelNormal, pixelModelPosition, pixelColor, specularCoefficient.get());
                    Vector4f finalColor = pixelShader.getPixelColor(cameraPosition, pixelData);

                    drawPixel((int) x, (int) y, colorOf(finalColor));
                }
            }
        }
    }

    private static Vector4f straightColor(Vector3f cameraPosition, PixelData pixelData) {
        return pixelData.getPixelColor();
    }

    private static Vector4f flatShadingColor(Vector3f cameraPosition, PixelData pixelData) {
        Vector4f pixelColor = pixelData.getPixelColor();
        Vector3f pixelNormal = pixelData.getPixelNormal();
        return pixelColor.mul((1.f + pixelNormal.dot(negate3(DIFFUSE_LIGHT_DIRECTION))) / 2.f);
    }

}
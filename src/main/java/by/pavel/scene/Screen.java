package by.pavel.scene;

import by.pavel.math.Matrix4f;
import by.pavel.math.Vector3i;
import by.pavel.math.Vector4f;
import by.pavel.parser.OBJData;
import lombok.Getter;

import java.awt.image.BufferedImage;
import java.util.List;

import static java.lang.Math.abs;

public class Screen {

    private final int width;
    private final int height;

    @Getter
    private final Camera camera;
    private final Projection projection;

    @Getter
    private final BufferedImage bufferedImage;

    public Screen(int width, int height) {
        this.width = width;
        this.height = height;
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        camera = new Camera(0.3f, new Vector4f(0, 0, 0), new Vector4f(0, 0, 1), new Vector4f(0, 1, 0));
        projection = new Projection(45, 1.33f, 0, 100);
    }

    public void clear() {
        bufferedImage.getGraphics().clearRect(0, 0, width, height);
    }

    public void drawPixel(int x, int y, int color) {
        if (x >= 0 & x < width && y >= 0 && y < height) {
            bufferedImage.setRGB(x, height - 1 - y, color);
        } else {
//            throw new IndexOutOfBoundsException("Error writing to buffer with params x = " + x + ", y = " + y);
        }
    }

    public static int colorOf(int r, int g, int b, int a) {
        return ((a & 0xff) << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
    }

    public void drawLine(int x1, int x2, int y1, int y2, int color) {
        int deltaX = abs(x2 - x1); // 2
        int deltaY = abs(y2 - y1); // 6
        int signX = x1 < x2 ? 1 : -1; // -1
        int signY = y1 < y2 ? 1 : -1; // -1

        if (abs(deltaX) > width * 4 || abs(deltaY) > height * 4) {
            return;
        }

        int error = deltaX - deltaY;

        drawPixel(x1, y1, color);
        while (x1 != x2 || y1 != y2) {
            drawPixel(x1, y1, color);
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
                Vector4f v1 = mvp.multiply(vertices.get(face.get(0).x));
                Vector4f v2 = mvp.multiply(vertices.get(face.get(i).x));
                Vector4f v3 = mvp.multiply(vertices.get(face.get(i + 1).x));
                drawTriangle(v1, v2, v3);
            }
        }
    }

    public void drawTriangle(Vector4f v1, Vector4f v2, Vector4f v3) {

        if (!(v1.z > 0 && v2.z > 0 && v3.z > 0)) {
            return;
        }

        int x1 = (int) (v1.x / v1.w * (float) width / 2.f) + width / 2;
        int x2 = (int) (v2.x / v2.w * (float) width / 2.f) + width / 2;
        int x3 = (int) (v3.x / v3.w * (float) width / 2.f) + width / 2;

        int y1 = ((int) (v1.y / v1.w * (float) height / 2.f) + height / 2);
        int y2 = ((int) (v2.y / v2.w * (float) height / 2.f) + height / 2);
        int y3 = ((int) (v3.y / v3.w * (float) height / 2.f) + height / 2);

        int blue = colorOf(0, 0, 255, 255);
        drawLine(x1, x2, y1, y2, blue);
        drawLine(x2, x3, y2, y3, blue);
        drawLine(x3, x1, y3, y1, blue);
    }
}
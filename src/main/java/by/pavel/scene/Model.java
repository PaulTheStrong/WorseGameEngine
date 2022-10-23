package by.pavel.scene;

import by.pavel.math.Matrix4f;
import by.pavel.math.Vector3i;
import by.pavel.math.Vector4f;
import lombok.Data;

import java.util.List;

@Data
public class Model {
    private Matrix4f translation;
    private Matrix4f rotation;
    private Matrix4f scale;

    private final List<Vector4f> vertices;
    private final List<List<Vector3i>> faces;

    Matrix4f model;

    private void setModel() {
        model = translation.multiply(scale).multiply(rotation);
    }

    public Model(Matrix4f translation, Matrix4f rotation, Matrix4f scale, List<Vector4f> vertices, List<List<Vector3i>> faces) {
        this.translation = translation;
        this.rotation = rotation;
        this.scale = scale;
        this.vertices = vertices;
        this.faces = faces;

        setModel();
    }

    Matrix4f getModelMatrix() {
        return model;
    }

    void setRotation(Matrix4f rotation) {
        this.rotation = rotation;
        setModel();
    }

    void setTranslation(Matrix4f translation) {
        this.translation = translation;
        setModel();
    }

    public void setScale(Matrix4f scale) {
        this.scale = scale;
        setModel();
    }
}

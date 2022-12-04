package by.pavel.scene;

import by.pavel.math.Matrix4f;
import by.pavel.math.Vector2f;
import by.pavel.math.Vector3f;
import by.pavel.math.Vector3i;
import lombok.Data;

import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

@Data
public class Model {
    private Matrix4f translation;
    private Matrix4f rotation;
    private Matrix4f scale;

    private final List<Vector3f> vertices;
    private final List<Vector3f> normals;
    private final List<List<Vector3i>> faces;
    private final List<Vector2f> uvTextures;
    private final Raster texture;
    private final Raster normalMap;
    private final Raster specularMap;

    Matrix4f model;

    private void setModel() {
        model = scale.multiply(rotation).multiply(translation);
    }

    public Model(Matrix4f translation, Matrix4f rotation, Matrix4f scale, List<Vector3f> vertices, List<Vector3f> normals, List<List<Vector3i>> faces, List<Vector2f> uvTextures, String texturePath, String normalMapPath, String specularMapPath) {
        Raster textureTemp;
        this.translation = translation;
        this.rotation = rotation;
        this.scale = scale;
        this.vertices = vertices;
        this.faces = faces;
        this.normals = normals;
        this.uvTextures = uvTextures;
        try {
            textureTemp = ImageIO.read(new File(texturePath)).getRaster();
        } catch (Exception e) {
            textureTemp = null;
        }
        this.texture = textureTemp;
        try {
            textureTemp = ImageIO.read(new File(normalMapPath)).getRaster();
        } catch (Exception e) {
            textureTemp = null;
        }
        this.normalMap = textureTemp;
        try {
            textureTemp = ImageIO.read(new File(specularMapPath)).getRaster();
        } catch (Exception e) {
            textureTemp = null;
        }
        this.specularMap = textureTemp;
        setModel();
    }

    Matrix4f getModelMatrix() {
        return model;
    }

    public void setRotation(Matrix4f rotation) {
        this.rotation = rotation;
        setModel();
    }

    public void setTranslation(Matrix4f translation) {
        this.translation = translation;
        setModel();
    }

    public void setScale(Matrix4f scale) {
        this.scale = scale;
        setModel();
    }


}

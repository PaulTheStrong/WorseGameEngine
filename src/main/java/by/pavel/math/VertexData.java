package by.pavel.math;

public class VertexData {

    public Vector3f position;
    public Vector3f normal;
    public int color;

    public Matrix4f transform;

    public VertexData(Vector3f position, Vector3f normal, Matrix4f transform, int color) {
        this.position = position;
        this.normal = normal;
        this.color = color;
        this.transform = transform;
    }
}

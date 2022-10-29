package by.pavel.math;

public class Matrix3f {

    private float
        m00, m01, m02,
        m10, m11, m12,
        m20, m21, m22;

    public Matrix3f(Vector3f vec0, Vector3f vec1, Vector3f vec2) {
        m00 = vec0.x;
        m01 = vec0.y;
        m02 = vec0.z;
        m10 = vec1.x;
        m11 = vec1.y;
        m12 = vec1.z;
        m20 = vec2.x;
        m21 = vec2.y;
        m22 = vec2.z;
    }


    public Vector3f multiply(Vector3f other) {
        float x = m00 * other.x + m01 * other.y + m02 * other.z;
        float y = m10 * other.x + m11 * other.y + m12 * other.z;
        float z = m20 * other.x + m21 * other.y + m22 * other.z;

        return new Vector3f(x, y, z);
    }
}

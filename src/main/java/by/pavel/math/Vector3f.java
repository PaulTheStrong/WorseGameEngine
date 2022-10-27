package by.pavel.math;

import static java.lang.Math.sqrt;

public class Vector3f {
    public float x, y, z;

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3f plus(Vector3f other) {
        return new Vector3f(x + other.x, y + other.y, z + other.z);
    }
    public Vector3f minus(Vector3f other) {
        return new Vector3f(x - other.x, y - other.y, z - other.z);
    }

    public Vector3f mul(float scale) {
        return new Vector3f(x * scale, y * scale, z * scale);
    }

    public float dot(Vector3f other) {
        return x * other.x + y * other.y + z * other.z;
    }

    public static Vector3f normalize3(Vector3f v) {
        float length = (float)sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
        if (length == 0) return v;
        return new Vector3f(v.x / length, v.y / length, v.z / length);
    }
}

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

    public static Vector3f negate3(Vector3f v) {
        return new Vector3f(-v.x, -v.y, -v.z);
    }

    public static Vector3f reflect3(Vector3f reflectionVector, Vector3f normalVector) {
        float dot = reflectionVector.dot(normalVector);
        if (dot <= 0) {
            return new Vector3f(0, 0, 0);
        }
        return reflectionVector.minus(normalVector.mul(dot * 2));
    }

    public Vector3f cross(Vector3f other) {
        return new Vector3f(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        );
    }
}

package by.pavel.math;

import static java.lang.Math.sqrt;

public class Vector4f {

    public float x, y, z, w = 1;

    public Vector4f() {}

    public Vector4f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = 0;
    }

    public Vector4f(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public float length() {
        return (float)sqrt(x * x + y * y + z * z);
    }

    public Vector4f plus(Vector4f other) {
        Vector4f result = new Vector4f();
        result.x = x + other.x;
        result.y = y + other.y;
        result.z = z + other.z;
        result.w = w + other.w;
        return result;
    }

    public Vector4f minus(Vector4f other) {
        Vector4f result = new Vector4f();
        result.x = x - other.x;
        result.y = y - other.y;
        result.z = z - other.z;
        result.w = w - other.w;
        return result;
    }

    public float dot(Vector4f other) {
        return x * other.x + y * other.y + z * other.z + w * other.w;
    }

    public Vector4f cross(Vector4f other) {
        return new Vector4f(
                y * other.z - z * other.y,
                z * other.x - x * other.z,
                x * other.y - y * other.x,
                1);
    }

    public static Vector4f normalize(Vector4f v) {
        float length = v.length();
        return new Vector4f(v.x / length, v.y / length, v.z / length, v.w);
    }

    public Vector4f mul(float scale) {
        return new Vector4f(x * scale, y * scale, z * scale, w * scale);
    }

}

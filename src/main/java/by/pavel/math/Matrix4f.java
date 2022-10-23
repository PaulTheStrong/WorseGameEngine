package by.pavel.math;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Matrix4f {
    float m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33;

    public Matrix4f() {
    }

    public Matrix4f(Vector4f vec0, Vector4f vec1, Vector4f vec2, Vector4f vec3) {
        m00 = vec0.x;
        m01 = vec0.y;
        m02 = vec0.z;
        m03 = vec0.w;
        m10 = vec1.x;
        m11 = vec1.y;
        m12 = vec1.z;
        m13 = vec1.w;
        m20 = vec2.x;
        m21 = vec2.y;
        m22 = vec2.z;
        m23 = vec2.w;
        m30 = vec3.x;
        m31 = vec3.y;
        m32 = vec3.z;
        m33 = vec3.w;
    }

    public Matrix4f plus(Matrix4f other) {
        Matrix4f result = new Matrix4f();
        result.m00 = m00 + other.m00;
        result.m01 = m01 + other.m01;
        result.m02 = m02 + other.m02;
        result.m03 = m03 + other.m03;
        result.m10 = m10 + other.m10;
        result.m11 = m11 + other.m11;
        result.m12 = m12 + other.m12;
        result.m13 = m13 + other.m13;
        result.m20 = m20 + other.m20;
        result.m21 = m21 + other.m21;
        result.m22 = m22 + other.m22;
        result.m23 = m23 + other.m23;
        result.m30 = m30 + other.m30;
        result.m31 = m31 + other.m31;
        result.m32 = m32 + other.m32;
        result.m33 = m33 + other.m33;

        return result;
    }

    public Matrix4f minus(Matrix4f other) {
        Matrix4f result = new Matrix4f();
        result.m00 = m00 - other.m00;
        result.m01 = m01 - other.m01;
        result.m02 = m02 - other.m02;
        result.m03 = m03 - other.m03;
        result.m10 = m10 - other.m10;
        result.m11 = m11 - other.m11;
        result.m12 = m12 - other.m12;
        result.m13 = m13 - other.m13;
        result.m20 = m20 - other.m20;
        result.m21 = m21 - other.m21;
        result.m22 = m22 - other.m22;
        result.m23 = m23 - other.m23;
        result.m30 = m30 - other.m30;
        result.m31 = m31 - other.m31;
        result.m32 = m32 - other.m32;
        result.m33 = m33 - other.m33;

        return result;
    }

    public Matrix4f multiply(Matrix4f other) {
        Matrix4f result = new Matrix4f();
        result.m00 = m00 * other.m00 + m10 * other.m01 + m20 * other.m02 + m30 * other.m03;
        result.m01 = m01 * other.m00 + m11 * other.m01 + m21 * other.m02 + m31 * other.m03;
        result.m02 = m02 * other.m00 + m12 * other.m01 + m22 * other.m02 + m32 * other.m03;
        result.m03 = m03 * other.m00 + m13 * other.m01 + m23 * other.m02 + m33 * other.m03;
        result.m10 = m00 * other.m10 + m10 * other.m11 + m20 * other.m12 + m30 * other.m13;
        result.m11 = m01 * other.m10 + m11 * other.m11 + m21 * other.m12 + m31 * other.m13;
        result.m12 = m02 * other.m10 + m12 * other.m11 + m22 * other.m12 + m32 * other.m13;
        result.m13 = m03 * other.m10 + m13 * other.m11 + m23 * other.m12 + m33 * other.m13;
        result.m20 = m00 * other.m20 + m10 * other.m21 + m20 * other.m22 + m30 * other.m23;
        result.m21 = m01 * other.m20 + m11 * other.m21 + m21 * other.m22 + m31 * other.m23;
        result.m22 = m02 * other.m20 + m12 * other.m21 + m22 * other.m22 + m32 * other.m23;
        result.m23 = m03 * other.m20 + m13 * other.m21 + m23 * other.m22 + m33 * other.m23;
        result.m30 = m00 * other.m30 + m10 * other.m31 + m20 * other.m32 + m30 * other.m33;
        result.m31 = m01 * other.m30 + m11 * other.m31 + m21 * other.m32 + m31 * other.m33;
        result.m32 = m02 * other.m30 + m12 * other.m31 + m22 * other.m32 + m32 * other.m33;
        result.m33 = m03 * other.m30 + m13 * other.m31 + m23 * other.m32 + m33 * other.m33;
        return result;
    }

    public Vector4f multiply(Vector4f other) {
        float x = m00 * other.x + m01 * other.y + m02 * other.z + m03 * other.w;
        float y = m10 * other.x + m11 * other.y + m12 * other.z + m13 * other.w;
        float z = m20 * other.x + m21 * other.y + m22 * other.z + m23 * other.w;
        float w = m30 * other.x + m31 * other.y + m32 * other.z + m33 * other.w;

        return new Vector4f(x, y, z, w);
    }

    public static Matrix4f translation(Vector4f translationVec) {
        return new Matrix4f(
                new Vector4f(1, 0, 0, translationVec.x),
                new Vector4f(0, 1, 0, translationVec.y),
                new Vector4f(0, 0, 1, translationVec.z),
                new Vector4f(0, 0, 0, 1)
        );
    }

    public static Matrix4f rotation(Vector4f rotationVec) {
        return
                new Matrix4f(
                        new Vector4f(1, 0, 0, 0),
                        new Vector4f(0, (float) cos(rotationVec.x), -(float) sin(rotationVec.x), 0),
                        new Vector4f(0, (rotationVec.x), (float) cos(rotationVec.x), 0),
                        new Vector4f(0, 0, 0, 1)).multiply(
                        new Matrix4f(
                                new Vector4f((float) cos(rotationVec.y), 0, (float) sin(rotationVec.y), 0),
                                new Vector4f(0, 1, 0, 0),
                                new Vector4f(-(float) sin(rotationVec.y), 0, (float) cos(rotationVec.y), 0),
                                new Vector4f(0, 0, 0, 1))).multiply(
                        new Matrix4f(
                                new Vector4f((float) cos(rotationVec.z), -(float) sin(rotationVec.z), 0, 0),
                                new Vector4f((float) sin(rotationVec.z), (float) cos(rotationVec.z), 0, 0),
                                new Vector4f(0, 0, 1, 0),
                                new Vector4f(0, 0, 0, 1)));

    }

    public static Matrix4f scale(Vector4f scaleVec) {
        return new Matrix4f(
                new Vector4f(scaleVec.x, 0, 0, 0),
                new Vector4f(0, scaleVec.y, 0, 0),
                new Vector4f(0, 0, scaleVec.z, 0),
                new Vector4f(0, 0, 0, 1)
        );
    }

    public static Matrix4f lookAt(Vector4f position, Vector4f target, Vector4f up) {
        Vector4f direction = Vector4f.normalize(position.minus(target));
        Vector4f right = Vector4f.normalize(up.cross(direction));
        Vector4f actualUp = direction.cross(right);
        Matrix4f lookAt = new Matrix4f(
                new Vector4f(right.x, right.y, right.z, 0),
                new Vector4f(actualUp.x, actualUp.y, actualUp.z, 0),
                new Vector4f(direction.x, direction.y, direction.z, 0),
                new Vector4f(0, 0, 0, 1));
        Matrix4f translation = Matrix4f.translation(
                new Vector4f(-position.x, -position.y, -position.z, 1));
        return translation.multiply(lookAt);
    }

}

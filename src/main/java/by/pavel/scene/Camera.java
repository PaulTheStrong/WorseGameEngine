package by.pavel.scene;

import by.pavel.math.Matrix4f;
import by.pavel.math.Vector4f;
import lombok.Data;

import static by.pavel.math.Vector4f.normalize;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

@Data
public class Camera {

    private float speed;
    private float yaw = 45;
    private float pitch = 45;

    private Vector4f eye;
    private Vector4f target;
    private Vector4f up;

    private Matrix4f view;

    public void setView() {
        view = Matrix4f.lookAt(eye, eye.plus(target), up);
    }

    public void setTarget() {
        target = normalize(new Vector4f(
                (float) cos(Math.PI / 180.f * yaw) * (float) cos(Math.PI / 180.f * pitch),
                (float) sin(Math.PI / 180.f * pitch),
                (float) sin(Math.PI / 180.f * yaw) * (float) cos(Math.PI / 180.f * pitch)
        ));
        setView();
    }

    public Camera(Vector4f eye, float yaw, float pitch, Vector4f up, float speed) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.eye = eye;
        this.up = normalize(up);
        this.speed = speed;
        setView();
    }

    public Camera(float speed, Vector4f eye, Vector4f target, Vector4f up) {
        this.speed = speed;
        this.eye = eye;
        this.target = target;
        this.up = up;
        setView();
    }

    public Matrix4f getViewMatrix() {
        return view;
    }

    public Vector4f getTarget() {
        return target;
    }

    public void moveForward() {
        Vector4f delta = target.mul(speed);
        eye = delta.plus(eye);
        eye.w = 1;
        setView();
    }

    public void moveBackward() {
        Vector4f delta = target.mul(speed);
        eye = eye.minus(delta);
        eye.w = 1;
        setView();
    }

    public void moveLeft() {
        Vector4f delta = normalize(up.cross(target)).mul(speed);
        eye = eye.plus(delta);
        eye.w = 1;
        setView();
    }

    public void moveRight() {
        Vector4f delta = normalize(up.cross(target)).mul(speed);
        eye = eye.minus(delta);
        eye.w = 1;
        setView();
    }

    public void setPitch(float pitch) {
        if (pitch > 89.0)
            pitch = 89.0f;
        if (pitch < -89.0)
            pitch = -89.0f;

        this.pitch = pitch;
        setTarget();
    }

    public void setEye(Vector4f eye) {
        this.eye = eye;
        setView();
    }

    public void setTarget(Vector4f target) {
        this.target = target;
        setView();
    }

    public void setUp(Vector4f up) {
        this.up = up;
        setView();
    }
}

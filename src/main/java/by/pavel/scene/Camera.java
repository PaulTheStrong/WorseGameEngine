package by.pavel.scene;

import by.pavel.math.Matrix4f;
import by.pavel.math.Vector3f;
import by.pavel.math.Vector4f;
import lombok.Data;

import static by.pavel.math.Vector3f.normalize3;
import static by.pavel.math.Vector4f.normalize;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

@Data
public class Camera {

    private float speed;
    private float yaw = 90;
    private float pitch = 0;

    private Vector3f eye;
    private Vector3f target;
    private Vector3f up;

    private Matrix4f view;

    public void setView() {
        view = Matrix4f.lookAt(eye, eye.plus(target), up);
    }

    public void setTarget() {
        target = normalize3(new Vector3f(
                (float) cos(Math.PI / 180.f * yaw) * (float) cos(Math.PI / 180.f * pitch),
                (float) sin(Math.PI / 180.f * pitch),
                (float) sin(Math.PI / 180.f * yaw) * (float) cos(Math.PI / 180.f * pitch)
        ));
        setView();
    }

    public Camera(float speed, Vector3f eye, Vector3f target, Vector3f up) {
        this.speed = speed;
        this.eye = eye;
        this.target = target;
        this.up = up;
        setView();
    }

    public Matrix4f getViewMatrix() {
        return view;
    }

    public Vector3f getTarget() {
        return target;
    }

    public void moveForward() {
        Vector3f delta = target.mul(speed);
        eye = delta.plus(eye);
        setView();
    }

    public void moveBackward() {
        Vector3f delta = target.mul(speed);
        eye = eye.minus(delta);
        setView();
    }

    public void moveLeft() {
        Vector3f delta = normalize3(up.cross(target)).mul(speed);
        eye = eye.plus(delta);
        setView();
    }

    public void moveRight() {
        Vector3f delta = normalize3(up.cross(target)).mul(speed);
        eye = eye.minus(delta);
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

    public void setEye(Vector3f eye) {
        this.eye = eye;
        setView();
    }

    public void setTarget(Vector3f target) {
        this.target = target;
        setView();
    }

    public void setUp(Vector3f up) {
        this.up = up;
        setView();
    }
}

package by.pavel.scene.listener;

import by.pavel.scene.Camera;
import by.pavel.scene.Mouse;
import lombok.RequiredArgsConstructor;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

@RequiredArgsConstructor
public class CameraMouseListener implements MouseMotionListener, MouseListener {

    private final Camera camera;
    private int prevX = -1;
    private int prevY = -1;

    @Override
    public void mouseDragged(MouseEvent e) {
        int dx = e.getX() - prevX;
        int dy = e.getY() - prevY;
        prevX += dx;
        prevY += dy;
        camera.setPitch(camera.getPitch() - dy / 5.f);
        camera.setYaw(camera.getYaw() + dx / 5.f);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Mouse.getInstance().setX(e.getX() - 8);
        Mouse.getInstance().setY(630 - e.getY());
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        prevX = e.getX();
        prevY = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}

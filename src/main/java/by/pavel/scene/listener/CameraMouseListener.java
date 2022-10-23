package by.pavel.scene.listener;

import by.pavel.scene.Camera;
import lombok.RequiredArgsConstructor;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

@RequiredArgsConstructor
public class CameraMouseListener implements MouseMotionListener, MouseListener {

    private final Camera camera;
    private int prevX = -1;
    private int prevY = -1;
    private boolean isMoving;

    @Override
    public void mouseDragged(MouseEvent e) {
        System.out.printf("Mouse dragged%n");
        if (isMoving) {
            int dx = e.getX() - prevX;
            int dy = e.getY() - prevY;
            System.out.println(dx);
            System.out.println(dy);
            prevX += dx;
            prevY += dy;
            camera.setPitch(camera.getPitch() - dy / 5.f);
            camera.setYaw(camera.getYaw() + dx / 5.f);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        System.out.printf("Mouse moved%n");
        if (isMoving) {
            int dx = e.getX() - prevX;
            int dy = e.getY() - prevY;
            System.out.println(dx);
            System.out.println(dy);
            prevX += dx;
            prevY += dy;
            camera.setPitch(camera.getPitch() - dy / 5.f);
            camera.setYaw(camera.getYaw() + dx / 5.f);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        prevX = e.getX();
        prevY = e.getY();
        System.out.printf("Mouse pressed, x = %d, y = %d%n", prevX, prevY);
        isMoving = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        isMoving = false;
        System.out.printf("Mouse released%n");
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}

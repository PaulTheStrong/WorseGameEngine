package by.pavel.scene.listener;

import by.pavel.scene.Camera;
import lombok.RequiredArgsConstructor;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

@RequiredArgsConstructor
public class KeyboardKeyListener implements KeyListener {

    private final Camera camera;

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_W) {
            camera.moveForward();
        }
        if (keyCode == KeyEvent.VK_A) {
            camera.moveLeft();
        }
        if (keyCode == KeyEvent.VK_S) {
            camera.moveBackward();
        }
        if (keyCode == KeyEvent.VK_D) {
            camera.moveRight();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}

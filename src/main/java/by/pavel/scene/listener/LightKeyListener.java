package by.pavel.scene.listener;

import static java.lang.Math.sin;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.function.Consumer;

import by.pavel.math.Vector3f;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LightKeyListener implements KeyListener {

    private float angle = 0;
    private final Vector3f lightDirection;

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_O) {
            angle += 5;
        }
        if (keyCode == KeyEvent.VK_P) {
            angle -= 5;
        }
        lightDirection.x = (float) sin(angle * Math.PI / 180);
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}

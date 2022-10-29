package by.pavel.scene.listener;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import by.pavel.math.Matrix4f;
import by.pavel.math.Vector3f;
import by.pavel.math.Vector4f;
import by.pavel.scene.Model;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KeyboardModelListener implements KeyListener {

    private final Model model;

    private final Vector3f rotation = new Vector3f(0, 0, 0);

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_J) {
            rotation.y += 5.f * Math.PI / 180;
            model.setRotation(Matrix4f.rotation(rotation));
        }
        if (keyCode == KeyEvent.VK_L) {
            rotation.y -= 5.f * Math.PI / 180;
            model.setRotation(Matrix4f.rotation(rotation));
        }
        if (keyCode == KeyEvent.VK_I) {
            rotation.x += 5.f * Math.PI / 180;
            model.setRotation(Matrix4f.rotation(rotation));
        }
        if (keyCode == KeyEvent.VK_K) {
            rotation.x -= 5.f * Math.PI / 180;
            model.setRotation(Matrix4f.rotation(rotation));
        }
        if (keyCode == KeyEvent.VK_O) {
            rotation.z += 5.f * Math.PI / 180;
            model.setRotation(Matrix4f.rotation(rotation));
        }
        if (keyCode == KeyEvent.VK_P) {
            rotation.z -= 5.f * Math.PI / 180;
            model.setRotation(Matrix4f.rotation(rotation));
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}

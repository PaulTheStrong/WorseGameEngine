package by.pavel.scene;

import by.pavel.math.Matrix4f;
import by.pavel.math.Vector4f;
import by.pavel.parser.OBJData;
import by.pavel.parser.OBJParser;
import by.pavel.scene.listener.CameraMouseListener;
import by.pavel.scene.listener.KeyboardKeyListener;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;

public class MainWindow {

    private JFrame frame;
    private final int width, height;
    private Screen screen;

    private JFrame initFrame() {
        frame = new JFrame("WINDOW");
        frame.setVisible(true);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(width, height);
        return frame;
    }
    public MainWindow(int width, int height) {
        this.width = width;
        this.height = height;
        frame = initFrame();
        screen = new Screen(width, height);
    }

    public void start() {
        frame.createBufferStrategy(1);
        BufferStrategy bs = frame.getBufferStrategy();
        Graphics g = bs.getDrawGraphics();

        OBJParser parser = new OBJParser();
        OBJData objData = parser.parseFile("src/main/resources/suzanne.obj");

        KeyboardKeyListener keyboardKeyListener = new KeyboardKeyListener(screen.getCamera());
        frame.addKeyListener(keyboardKeyListener);
        CameraMouseListener cameraMouseListener = new CameraMouseListener(screen.getCamera());
        frame.addMouseListener(cameraMouseListener);
        frame.addMouseMotionListener(cameraMouseListener);

        Model model = new Model(
                Matrix4f.translation(new Vector4f(0, 0, 10)),
                Matrix4f.rotation(new Vector4f(0, 135, 0)),
                Matrix4f.scale(new Vector4f(1.0f, 1.0f, 1.0f)),
                objData.getVertices(),
                objData.getSurfaces());
        while (true) {
            screen.clear();
            screen.drawOBJ(model);
            g.drawImage(screen.getBufferedImage(), 0, 0, width, height, null);
            bs.show();
        }
    }

}

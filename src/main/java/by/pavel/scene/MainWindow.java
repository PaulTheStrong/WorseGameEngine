package by.pavel.scene;

import by.pavel.math.Matrix4f;
import by.pavel.math.Vector4f;
import by.pavel.parser.OBJData;
import by.pavel.parser.OBJParser;
import by.pavel.scene.listener.CameraMouseListener;
import by.pavel.scene.listener.KeyboardKeyListener;
import by.pavel.scene.listener.LightKeyListener;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.ImageObserver;

public class MainWindow extends JFrame {

    private final int width, height;
    private Screen screen;

    private Model model;

    private JPanel imagePanel;

    public MainWindow(int width, int height) {
        super("WINDOW");
        setVisible(true);
        pack();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(width, height);

        this.width = width;
        this.height = height;
        screen = new Screen(width, height);
        initModel();
        imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                screen.clear();
                screen.drawOBJ(model);
                g.drawImage(screen.getBufferedImage(), 0, 0, width, height, new ImageObserver() {
                    @Override
                    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                        paintComponent(g);
                        return true;
                    }
                });
            }
        };
        add(imagePanel);
    }

    public void start() {
        createBufferStrategy(1);
        BufferStrategy bs = getBufferStrategy();

        KeyboardKeyListener keyboardKeyListener = new KeyboardKeyListener(screen.getCamera());
        addKeyListener(keyboardKeyListener);
        CameraMouseListener cameraMouseListener = new CameraMouseListener(screen.getCamera());
        addMouseListener(cameraMouseListener);
        addMouseMotionListener(cameraMouseListener);
//        addKeyListener(new LightKeyListener(lightDirection));
        while (true) {
            repaint();
        }
    }

    private void initModel() {
        OBJParser parser = new OBJParser();
        OBJData objData = parser.parseFile("src/main/resources/cube.obj");

        model = new Model(
                Matrix4f.translation(new Vector4f(0, 0, 10)),
                Matrix4f.rotation(new Vector4f(0, 0, 0)),
                Matrix4f.scale(new Vector4f(1f, 1f, 1f)),
                objData.getVertices(),
                objData.getNormals(),
                objData.getSurfaces());
    }
}

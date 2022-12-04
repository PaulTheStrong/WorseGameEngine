package by.pavel.scene;

import static by.pavel.scene.ColorUtil.BLUE;
import static by.pavel.scene.ColorUtil.GREEN;
import static by.pavel.scene.ColorUtil.RED;
import static by.pavel.scene.ColorUtil.WHITE;
import static by.pavel.scene.ColorUtil.colorOf;
import static by.pavel.scene.ColorUtil.rgbaVec;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferStrategy;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import by.pavel.math.Matrix4f;
import by.pavel.math.Vector3f;
import by.pavel.math.Vector4f;
import by.pavel.parser.OBJData;
import by.pavel.parser.OBJParser;
import by.pavel.scene.listener.CameraMouseListener;
import by.pavel.scene.listener.KeyboardKeyListener;
import by.pavel.scene.listener.KeyboardModelListener;

public class MainWindow extends JFrame {

    private static final String MODEL_TEXTURE = "src/main/resources/models/diffuse1.png";
    private static final String MODEL_DATA = "src/main/resources/models/model1.obj";
    private static final String MODEL_NORMAL_MAP = "src/main/resources/models/normal1.png";
    private static final String MODEL_SPECULAR_MAP = "src/main/resources/models/specular1.png";

    private final int width, height;
    private Screen screen;

    private Model model;
    private Model sphere;
    private JPanel imagePanel;
    private List<LightSource> lightSources;

    public MainWindow(int width, int height) {
        super("WINDOW");
        setVisible(true);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(width, height);

        this.width = width;
        this.height = height;
        lightSources = List.of(
            new LightSource(rgbaVec(RED), new Vector3f(10, 0, 0), 0.2f, 0.3f),
            new LightSource(rgbaVec(GREEN), new Vector3f(0, 0, 0), 0.3f, 0.1f),
            new LightSource(rgbaVec(BLUE), new Vector3f(-10, 0, 0), 0.2f, 0.5f),
            new LightSource(rgbaVec(WHITE), new Vector3f(-10, 0, 0), 0.2f, 0.4f)
        );
        screen = new Screen(width, height, lightSources);
        initModel();
        imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                screen.clear();
                screen.drawPhong(rgbaVec(colorOf(52, 122, 119, 255)), model);
                lightSources.forEach(
                    lightSource -> {
                        sphere.setTranslation(Matrix4f.translation(lightSource.getPosition()));
                        screen.drawStraight(lightSource.getColor(), sphere);
                    }
                );
                g.drawImage(screen.getBufferedImage(), 0, 0, width, height,
                    (img, infoflags, x, y, width1, height1) -> {
                        paintComponent(g);
                        return true;
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
        addKeyListener(new KeyboardModelListener(model));
//        addKeyListener(new LightKeyListener(lightDirection));
        while (true) {
            repaint();
        }
    }

    private void initModel() {
        OBJParser parser = new OBJParser();
        OBJData objData = parser.parseFile(MODEL_DATA);

        model = new Model(
            Matrix4f.translation(new Vector3f(0, 0, 10)),
            Matrix4f.rotation(new Vector3f(0, 0, 0)),
            Matrix4f.scale(new Vector3f(0.1f, 0.1f, 0.1f)),
            objData.getVertices(),
            objData.getNormals(),
            objData.getSurfaces(),
            objData.getTextures(),
            MODEL_TEXTURE,
            MODEL_NORMAL_MAP,
            MODEL_SPECULAR_MAP
        );

        OBJData sphereData = parser.parseFile("src/main/resources/sphere.obj");
        sphere = new Model(
            Matrix4f.translation(new Vector3f(0, 0, 0)),
            Matrix4f.rotation(new Vector3f(0, 0, 0)),
            Matrix4f.scale(new Vector3f(0.2f, 0.2f, 0.2f)),
            sphereData.getVertices(),
            sphereData.getNormals(),
            sphereData.getSurfaces(),
            null,
            null,
            null,
            null);
    }
}

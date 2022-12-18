package by.pavel.scene;

import static by.pavel.scene.ColorUtil.WHITE;
import static by.pavel.scene.ColorUtil.colorOf;
import static by.pavel.scene.ColorUtil.rgbaVec;

import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import by.pavel.checker.Checker;
import by.pavel.checker.Checker.Rank;
import by.pavel.checker.Checker.Side;
import by.pavel.math.Matrix4f;
import by.pavel.math.Vector2i;
import by.pavel.math.Vector3f;
import by.pavel.math.Vector4f;
import by.pavel.parser.OBJData;
import by.pavel.parser.OBJParser;
import by.pavel.scene.GameState.AnimatedChecker;
import by.pavel.scene.GameState.Move;
import by.pavel.scene.listener.CameraMouseListener;
import by.pavel.scene.listener.CheckersKeyboardListener;
import by.pavel.scene.listener.KeyboardKeyListener;
import by.pavel.scene.listener.KeyboardModelListener;

public class MainWindow extends JFrame {

//    private static final String MODEL_TEXTURE = "src/main/resources/models/diffuse4.png";
//    private static final String MODEL_DATA = "src/main/resources/models/model4.obj";
//    private static final String MODEL_NORMAL_MAP = "src/main/resources/models/normal4.png";
//    private static final String MODEL_SPECULAR_MAP = "src/main/resources/models/specular4.png";

    private static final String MODEL_TEXTURE = "src/main/resources/checkers/chessboard.png";
    private static final String MODEL_DATA = "src/main/resources/checkers/chessboardg.obj";
    private static final String MODEL_NORMAL_MAP = "";
    private static final String MODEL_SPECULAR_MAP = "";
    public static final Vector3f LEFT_DOWN_CORNER = new Vector3f(1.032333f, -4.971402f, 2.993278f);
    public static final Vector3f RIGHT_UPPER_CORNER = new Vector3f(-1.010752f, -4.971262f, 5.038136f);
    public static final float BOARD_CELL_SIZE = (LEFT_DOWN_CORNER.x - RIGHT_UPPER_CORNER.x) / 8;
    public static final Vector3f CHECKER_POSITION_X_DELTA = new Vector3f(-BOARD_CELL_SIZE, 0f, 0f);
    public static final Vector3f CHECKER_POSITION_Z_DELTA = new Vector3f(0f, 0f, BOARD_CELL_SIZE);
    public static final Vector3f BASE_CHECKER_POSITION = new Vector3f(
        LEFT_DOWN_CORNER.x - BOARD_CELL_SIZE / 2f,
        LEFT_DOWN_CORNER.y + 0.20f,
        LEFT_DOWN_CORNER.z);
    private static final Vector3f ZERO_VECTOR_3F = new Vector3f(0, 0, 0);
    public static final Vector3f SQUARE_TRANSITION = new Vector3f(0, 0.01f, 0);
    private static final Matrix4f QUEEN_ROTATION = Matrix4f.rotation(new Vector3f(3.1415f, 3.1415f, 0));
    private static final Matrix4f NORMAL_ROTATION = Matrix4f.rotation(new Vector3f(0, 3.1415f, 0));

    private Screen screen;

    private Model chessboard;
    private Model sphere;
    private Model checkerModel;
    private Model squareModel;
    private final JPanel imagePanel;
    private final List<LightSource> lightSources;
    private final GameState gameState;

    public MainWindow(int width, int height) {
        super("WINDOW");
        setVisible(true);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(width, height);

        gameState = initGameState();

        initGameState();
        lightSources = List.of(
            new LightSource(rgbaVec(WHITE), new Vector3f(0, 10, 10), BOARD_CELL_SIZE, 0.4f)
        );
        screen = new Screen(width, height, lightSources);
        initModel();
        imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                screen.clear();

                boolean anyHovered = false;
                AnimatedChecker animatedChecker = gameState.getAnimatedChecker();
                for (int y = 0; y < 8; y++) {
                    for (int x = 0; x < 8; x++) {
                        Checker checker = gameState.getChecker(x, y);
                        if (checker != null) {
                            Vector3f xDelta = CHECKER_POSITION_X_DELTA.mul(x);
                            Vector3f zDelta = CHECKER_POSITION_Z_DELTA.mul(y);
                            Vector4f checkerColor = getCheckerColor(checker);
                            if (animatedChecker != null && animatedChecker.getChecker() == checker) {
                                checkerModel.setTranslation(Matrix4f.translation(animatedChecker.getCurrentPosition()));
                            } else {
                                checkerModel.setTranslation(Matrix4f.translation(BASE_CHECKER_POSITION.plus(xDelta).plus(zDelta)));
                            }
                            if (checker.getRank().equals(Rank.QUEEN)) {
                                checkerModel.setRotation(QUEEN_ROTATION);
                            } else {
                                checkerModel.setRotation(NORMAL_ROTATION);
                            }
                            screen.drawPhong(checkerColor, checkerModel);
                            if (screen.isObjectSelected()) {
                                gameState.setHoveredChecker(checker);
                                anyHovered = true;
                            }
                        }
                    }
                }
                if (animatedChecker != null && !animatedChecker.nextState()) {
                    gameState.setAnimatedChecker(null);
                }
                if (!anyHovered) {
                    gameState.setHoveredChecker(null);
                }

                lightSources.forEach(
                    lightSource -> {
                        sphere.setTranslation(Matrix4f.translation(lightSource.getPosition()));
                        screen.drawStraight(lightSource.getColor(), sphere);
                    }
                );

                screen.drawPhong(rgbaVec(colorOf(52, 122, 119, 255)), chessboard);
                Vector2i hoveredCell = null;
                if (screen.isObjectSelected()) {
                    Vector3f xyz = screen.getSelectedObjectModelCoordinates();
                    int x = (int) ((LEFT_DOWN_CORNER.x - xyz.x) / BOARD_CELL_SIZE);
                    int y = (int) ((xyz.z - LEFT_DOWN_CORNER.z) / BOARD_CELL_SIZE);
                    hoveredCell = new Vector2i(x, y);
                }
                gameState.setHoveredCell(hoveredCell);

                for (Move move : gameState.getPossibleMoves()) {
                    Vector2i destination = move.getDestination();
                    squareModel.setTranslation(Matrix4f.translation(
                        LEFT_DOWN_CORNER
                            .plus(CHECKER_POSITION_X_DELTA.mul(destination.x + 1))
                            .plus(CHECKER_POSITION_Z_DELTA.mul(destination.y))
                            .plus(SQUARE_TRANSITION))
                    );
                    screen.drawStraight(destination.equals(hoveredCell)
                            ? rgbaVec(colorOf(255, 0, 0, 255))
                            : rgbaVec(colorOf(0, 255, 0, 255))
                        , squareModel);
                }

                List<Checker> whiteBeaten = gameState.getWhiteBeaten();
                AnimatedChecker animatedBeatenChecker = gameState.getAnimatedBeatenChecker();
                for (int i = 0; i < whiteBeaten.size(); i++) {
                    Checker checker = whiteBeaten.get(i);
                    if (checker.getRank().equals(Rank.QUEEN)) {
                        checkerModel.setRotation(QUEEN_ROTATION);
                    } else {
                        checkerModel.setRotation(NORMAL_ROTATION);
                    }
                    if (animatedBeatenChecker != null && animatedBeatenChecker.getChecker() == checker) {
                        checkerModel.setTranslation(Matrix4f.translation(animatedBeatenChecker.getCurrentPosition()));
                    } else {
                        checkerModel.setTranslation(Matrix4f.translation(
                            LEFT_DOWN_CORNER
                                .plus(CHECKER_POSITION_X_DELTA.mul(-0.5f))
                                .plus(CHECKER_POSITION_Z_DELTA.mul(i))
                                .plus(SQUARE_TRANSITION))
                        );
                    }

                    screen.drawPhong(getCheckerColor(checker), checkerModel);
                }

                List<Checker> blackBeaten = gameState.getBlackBeaten();
                for (int i = 0; i < blackBeaten.size(); i++) {
                    Checker checker = blackBeaten.get(i);
                    if (checker.getRank().equals(Rank.QUEEN)) {
                        checkerModel.setRotation(QUEEN_ROTATION);
                    } else {
                        checkerModel.setRotation(NORMAL_ROTATION);
                    }
                    if (animatedBeatenChecker != null && animatedBeatenChecker.getChecker() == checker) {
                        checkerModel.setTranslation(Matrix4f.translation(animatedBeatenChecker.getCurrentPosition()));
                    } else {
                        checkerModel.setTranslation(Matrix4f.translation(
                            RIGHT_UPPER_CORNER
                                .plus(CHECKER_POSITION_X_DELTA.mul(0.5f))
                                .plus(CHECKER_POSITION_Z_DELTA.mul(-i))
                                .plus(SQUARE_TRANSITION))
                        );
                    }
                    screen.drawPhong(getCheckerColor(checker), checkerModel);
                }

                if (animatedBeatenChecker != null && !animatedBeatenChecker.nextState()) {
                    gameState.setAnimatedBeatenChecker(null);
                }

                screen.drawTargetCross();
                g.drawImage(screen.getBufferedImage(), 0, 0, width, height,
                    (img, infoflags, x, y, width1, height1) -> {
                        paintComponent(g);
                        return true;
                    });
            }
        };
        add(imagePanel);
    }

    private Vector4f getCheckerColor(Checker checker) {
        Vector4f checkerColor;
        if (gameState.getHoveredChecker() == checker && gameState.getSelectedChecker() == null || gameState.getSelectedChecker() == checker) {
            if (checker.getSide() == Side.WHITE) {
                checkerColor = rgbaVec(colorOf(0, 255, 0, 255));
            } else {
                checkerColor = rgbaVec(colorOf(255, 0, 0, 255));
            }
        } else {
            if (checker.getSide() == Side.WHITE) {
                checkerColor = rgbaVec(colorOf(200, 200, 200, 255));
            } else {
                checkerColor = rgbaVec(colorOf(50, 50, 50, 255));
            }
        }
        return checkerColor;
    }

    public void start() {
        createBufferStrategy(1);
        BufferStrategy bs = getBufferStrategy();

        KeyboardKeyListener keyboardKeyListener = new KeyboardKeyListener(screen.getCamera());
        addKeyListener(keyboardKeyListener);
        CameraMouseListener cameraMouseListener = new CameraMouseListener(screen.getCamera());
        addMouseListener(cameraMouseListener);
        addMouseMotionListener(cameraMouseListener);
        addKeyListener(new KeyboardModelListener(chessboard));
        addKeyListener(new CheckersKeyboardListener(gameState));
//        addKeyListener(new LightKeyListener(lightDirection));
        while (true) {
            repaint();
        }
    }

    private void initModel() {
        OBJParser parser = new OBJParser();
        OBJData objData = parser.parseFile(MODEL_DATA);

        chessboard = new Model(
            Matrix4f.translation(new Vector3f(0, -5f, 4f)),
            Matrix4f.rotation(ZERO_VECTOR_3F),
            Matrix4f.scale(new Vector3f(10f, 10f, 10f)),
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
            Matrix4f.translation(ZERO_VECTOR_3F),
            Matrix4f.rotation(ZERO_VECTOR_3F),
            Matrix4f.scale(new Vector3f(0.2f, 0.2f, 0.2f)),
            sphereData.getVertices(),
            sphereData.getNormals(),
            sphereData.getSurfaces(),
            null,
            null,
            null,
            null);

        OBJData checkerData = parser.parseFile("src/main/resources/models/model4.obj");
        checkerModel = new Model(
            Matrix4f.translation(ZERO_VECTOR_3F),
            NORMAL_ROTATION,
            Matrix4f.scale(new Vector3f(0.005f, 0.005f, 0.005f)),
            checkerData.getVertices(),
            checkerData.getNormals(),
            checkerData.getSurfaces(),
            null,
            null,
            null,
            null);

        OBJData squareData = parser.parseFile("src/main/resources/checkers/sqare.obj");
        squareModel = new Model(
            Matrix4f.translation(ZERO_VECTOR_3F),
            Matrix4f.rotation(ZERO_VECTOR_3F),
            Matrix4f.scale(new Vector3f(0.25f, 1.f, 0.25f)),
            squareData.getVertices(),
            squareData.getNormals(),
            squareData.getSurfaces(),
            null,
            null,
            null,
            null);
    }

    private GameState initGameState() {
        GameState gameState = new GameState();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (y % 2 == 0 && x % 2 == 1 || y % 2 == 1 && x % 2 == 0) {
                    if (y < 3) {
                        gameState.setChecker(x, y, new Checker(Side.WHITE));
                    } else if (y >= 5) {
                        gameState.setChecker(x, y, new Checker(Side.BLACK));
                    }
                }
            }
        }
        return gameState;
    }
}

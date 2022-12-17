package by.pavel.scene;

import static by.pavel.scene.ColorUtil.WHITE;
import static by.pavel.scene.ColorUtil.colorOf;
import static by.pavel.scene.ColorUtil.rgbaVec;

import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.JFrame;
import javax.swing.JPanel;

import by.pavel.checker.Checker;
import by.pavel.checker.Checker.Side;
import by.pavel.math.Matrix4f;
import by.pavel.math.Vector2i;
import by.pavel.math.Vector3f;
import by.pavel.math.Vector4f;
import by.pavel.parser.OBJData;
import by.pavel.parser.OBJParser;
import by.pavel.scene.MainWindow.GameState.AnimatedChecker;
import by.pavel.scene.MainWindow.GameState.Move;
import by.pavel.scene.listener.CameraMouseListener;
import by.pavel.scene.listener.CheckersKeyboardListener;
import by.pavel.scene.listener.KeyboardKeyListener;
import by.pavel.scene.listener.KeyboardModelListener;
import lombok.AllArgsConstructor;
import lombok.Data;

public class MainWindow extends JFrame {

//    private static final String MODEL_TEXTURE = "src/main/resources/models/diffuse4.png";
//    private static final String MODEL_DATA = "src/main/resources/models/model4.obj";
//    private static final String MODEL_NORMAL_MAP = "src/main/resources/models/normal4.png";
//    private static final String MODEL_SPECULAR_MAP = "src/main/resources/models/specular4.png";

    private static final String MODEL_TEXTURE = "src/main/resources/checkers/chessboard.png";
    private static final String MODEL_DATA = "src/main/resources/checkers/chessboardg.obj";
    private static final String MODEL_NORMAL_MAP = "";
    private static final String MODEL_SPECULAR_MAP = "";
    private static final Vector3f LEFT_DOWN_CORNER = new Vector3f(1.032333f, -4.971402f, 2.993278f);
    private static final Vector3f RIGHT_UPPER_CORNER = new Vector3f(-1.010752f, -4.971262f, 5.038136f);
    private static final float BOARD_CELL_SIZE = (LEFT_DOWN_CORNER.x - RIGHT_UPPER_CORNER.x) / 8;
    private static final Vector3f CHECKER_POSITION_X_DELTA = new Vector3f(-BOARD_CELL_SIZE, 0f, 0f);
    private static final Vector3f CHECKER_POSITION_Z_DELTA = new Vector3f(0f, 0f, BOARD_CELL_SIZE);
    private static final Vector3f BASE_CHECKER_POSITION = new Vector3f(
        LEFT_DOWN_CORNER.x - BOARD_CELL_SIZE / 2f,
        LEFT_DOWN_CORNER.y,
        LEFT_DOWN_CORNER.z + BOARD_CELL_SIZE / 2f);
    private static final Vector3f ZERO_VECTOR_3F = new Vector3f(0, 0, 0);
    private static final Vector3f SQUARE_TRANSITION = new Vector3f(0, 0.01f, 0);

    private Screen screen;

    private Model chessboard;
    private Model sphere;
    private Model checkerModel;
    private Model squareModel;
    private final JPanel imagePanel;
    private final List<LightSource> lightSources;
    private final GameState gameState;

    @Data
    public static class GameState {
        private Checker[][] board = new Checker[8][8];

        private Checker hoveredChecker;
        private Checker selectedChecker;
        private Vector2i hoveredCell;
        private Side currentSide = Side.WHITE;

        private AnimatedChecker animatedChecker;
        private AnimatedChecker animatedBeatenChecker;

        private final List<Move> possibleMoves = new ArrayList<>();
        private final List<Checker> whiteBeaten = new ArrayList<>();
        private final List<Checker> blackBeaten = new ArrayList<>();

        @Data
        public static class AnimatedChecker {
            private final Vector3f sourcePosition;
            private final Vector3f destinationPosition;
            private Vector3f currentPosition;
            private final Checker checker;
            private long animationStartTime;
            private long animationEndTime;

            public AnimatedChecker(Vector3f sourcePosition, Vector3f destinationPosition, Checker checker) {
                this.sourcePosition = sourcePosition;
                this.destinationPosition = destinationPosition;
                this.checker = checker;
                currentPosition = sourcePosition;
                animationStartTime = System.currentTimeMillis();
                animationEndTime = System.currentTimeMillis() + 500L;
            }

            public boolean nextState() {
                long now = System.currentTimeMillis();
                if (now > animationEndTime) {
                    return false;
                }

                Vector3f delta = destinationPosition.minus(sourcePosition);
                float dx = delta.x;
                float dz = delta.z;

                float timeDelta = (now - animationStartTime) / 500.f;
                float currentY = sourcePosition.y + (float) Math.sin(Math.PI * timeDelta) * 0.5f;
                float currentX = sourcePosition.x + dx * timeDelta;
                float currentZ = sourcePosition.z + dz * timeDelta;
                currentPosition = new Vector3f(currentX, currentY, currentZ);
                System.out.println(timeDelta);

                return true;
            }
        }

        public void setSelectedChecker(Checker checker) {
            if (checker == null) {
                selectedChecker = null;
                possibleMoves.clear();
            } else if (checker.getSide().equals(currentSide)) {
                selectedChecker = checker;
                calculatePossibleMoves(checker);
            }
        }

        public void makeMove(Move move) {
            Vector2i source = move.getSource();
            Vector2i destination = move.getDestination();
            MoveState moveState = move.getMoveState();
            Vector2i beatenPosition = move.getBeatenPosition();
            Checker currentChecker = board[source.y][source.x];

            animatedChecker = new AnimatedChecker(
                BASE_CHECKER_POSITION
                    .plus(CHECKER_POSITION_X_DELTA.mul(source.x))
                    .plus(CHECKER_POSITION_Z_DELTA.mul(source.y))
                    .plus(SQUARE_TRANSITION),
                BASE_CHECKER_POSITION
                    .plus(CHECKER_POSITION_X_DELTA.mul(destination.x))
                    .plus(CHECKER_POSITION_Z_DELTA.mul(destination.y))
                    .plus(SQUARE_TRANSITION),
                currentChecker
            );

            if (moveState.equals(MoveState.MOVE_BEAT)) {
                Checker beatenChecker = board[beatenPosition.y][beatenPosition.x];
                if (beatenChecker.getSide().equals(Side.WHITE)) {
                    whiteBeaten.add(beatenChecker);
                } else {
                    blackBeaten.add(beatenChecker);
                }
                animatedBeatenChecker = new AnimatedChecker(
                    BASE_CHECKER_POSITION
                        .plus(CHECKER_POSITION_X_DELTA.mul(beatenPosition.x))
                        .plus(CHECKER_POSITION_Z_DELTA.mul(beatenPosition.y))
                        .plus(SQUARE_TRANSITION),
                    beatenChecker.getSide().equals(Side.WHITE)
                    ? LEFT_DOWN_CORNER
                        .plus(CHECKER_POSITION_X_DELTA.mul(-0.5f))
                        .plus(CHECKER_POSITION_Z_DELTA.mul(whiteBeaten.size()))
                        .plus(SQUARE_TRANSITION)
                    : RIGHT_UPPER_CORNER
                        .plus(CHECKER_POSITION_X_DELTA.mul(0.5f))
                        .plus(CHECKER_POSITION_Z_DELTA.mul(-blackBeaten.size()))
                        .plus(SQUARE_TRANSITION),
                    beatenChecker
                );
                board[beatenPosition.y][beatenPosition.x] = null;
            }
            board[source.y][source.x] = null;
            board[destination.y][destination.x] = currentChecker;
            currentSide = currentSide == Side.WHITE ? Side.BLACK : Side.WHITE;
        }

        private void calculatePossibleMoves(Checker checker) {
            Vector2i currentPosition = null;
            for (int posY = 0; posY < 8; posY++) {
                for (int posX = 0; posX < 8; posX++) {
                    if (board[posY][posX] == checker) {
                        currentPosition = new Vector2i(posX, posY);
                    }
                }
            }
            if (currentPosition == null) {
                throw new RuntimeException("Checker not found on the board");
            }
            checkPossibleMoves(checker, currentPosition);
        }

        private void checkPossibleMoves(Checker checker, Vector2i currentPosition) {
            checkMove(currentPosition, new Vector2i(1, 1), checker).ifPresent(possibleMoves::add);
            checkMove(currentPosition, new Vector2i(-1, 1), checker).ifPresent(possibleMoves::add);
            checkMove(currentPosition, new Vector2i(1, -1), checker).ifPresent(possibleMoves::add);
            checkMove(currentPosition, new Vector2i(-1, -1), checker).ifPresent(possibleMoves::add);
        }

        @Data
        @AllArgsConstructor
        public static class Move {
            private final MoveState moveState;
            private final Vector2i source;
            private final Vector2i destination;
            private final Vector2i beatenPosition;
        }

        private enum MoveState {
            MOVE_OK, MOVE_BEAT
        }

        private Optional<Move> checkMove(Vector2i current, Vector2i direction, Checker checker) {
            Vector2i positionToCheck = current.plus(direction);
            if (checkBound(positionToCheck)) {
                Checker enemy = board[positionToCheck.y][positionToCheck.x];
                Side side = checker.getSide();
                if (enemy == null && (Side.WHITE.equals(side) && direction.y > 0 || Side.BLACK.equals(side) && direction.y < 0)) {
                    return Optional.of(new Move(MoveState.MOVE_OK, current, positionToCheck, null));
                } else {
                    Vector2i beatenPosition = positionToCheck;
                    positionToCheck = positionToCheck.plus(direction);
                    if (enemy != null
                        && !enemy.getSide().equals(side)
                        && checkBound(positionToCheck)
                        && board[positionToCheck.y][positionToCheck.x] == null) {
                        return Optional.of(new Move(MoveState.MOVE_BEAT, current, positionToCheck, beatenPosition));
                    }
                }
            }
            return Optional.empty();
        }

        private boolean checkBound(Vector2i v) {
            return v.x >= 0 && v.x < 8 && v.y < 8 && v.y >= 0;
        }
    }

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
                        Checker checker = gameState.board[y][x];
                        if (checker != null) {
                            Vector3f xDelta = CHECKER_POSITION_X_DELTA.mul(x);
                            Vector3f zDelta = CHECKER_POSITION_Z_DELTA.mul(y);
                            Vector4f checkerColor = getCheckerColor(checker);
                            if (animatedChecker != null && animatedChecker.getChecker() == checker) {
                                checkerModel.setTranslation(Matrix4f.translation(animatedChecker.getCurrentPosition()));
                            } else {
                                checkerModel.setTranslation(Matrix4f.translation(BASE_CHECKER_POSITION.plus(xDelta).plus(zDelta)));
                            }
                            screen.drawPhong(checkerColor, checkerModel);
                            if (screen.isObjectSelected()) {
                                gameState.hoveredChecker = checker;
                                anyHovered = true;
                            }
                        }
                    }
                }
                if (animatedChecker != null && !animatedChecker.nextState()) {
                    gameState.setAnimatedChecker(null);
                }
                if (!anyHovered) {
                    gameState.hoveredChecker = null;
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

                for (Move move : gameState.possibleMoves) {
                    Vector2i destination = move.destination;
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
                AnimatedChecker animatedBeatenChecker = gameState.animatedBeatenChecker;
                for (int i = 0; i < whiteBeaten.size(); i++) {
                    Checker checker = whiteBeaten.get(i);
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
        if (gameState.hoveredChecker == checker && gameState.selectedChecker == null || gameState.selectedChecker == checker) {
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

        OBJData checkerData = parser.parseFile("src/main/resources/checkers/checker.obj");
        checkerModel = new Model(
            Matrix4f.translation(ZERO_VECTOR_3F),
            Matrix4f.rotation(ZERO_VECTOR_3F),
            Matrix4f.scale(new Vector3f(0.075f, 0.075f, 0.075f)),
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
                        gameState.board[y][x] = new Checker(Side.WHITE);
                    } else if (y >= 5) {
                        gameState.board[y][x] = new Checker(Side.BLACK);
                    }
                }
            }
        }
        return gameState;
    }
}

package by.pavel.scene;

import static java.lang.Math.abs;

import static by.pavel.scene.MainWindow.BASE_CHECKER_POSITION;
import static by.pavel.scene.MainWindow.CHECKER_POSITION_X_DELTA;
import static by.pavel.scene.MainWindow.CHECKER_POSITION_Z_DELTA;
import static by.pavel.scene.MainWindow.LEFT_DOWN_CORNER;
import static by.pavel.scene.MainWindow.RIGHT_UPPER_CORNER;
import static by.pavel.scene.MainWindow.SQUARE_TRANSITION;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import by.pavel.checker.Checker;
import by.pavel.checker.Checker.Rank;
import by.pavel.checker.Checker.Side;
import by.pavel.math.Vector2i;
import by.pavel.math.Vector3f;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class GameState {
    private Checker[][] board = new Checker[8][8];

    private Checker hoveredChecker;
    private Checker selectedChecker;
    private Vector2i hoveredCell;
    private Side currentSide = Side.WHITE;

    private AnimatedChecker animatedChecker;
    private AnimatedChecker animatedBeatenChecker;
    private boolean hasToBeatNext = false;

    private final List<Move> possibleMoves = new ArrayList<>();
    private final List<Checker> whiteBeaten = new ArrayList<>();
    private final List<Checker> blackBeaten = new ArrayList<>();

    public Checker getChecker(int x, int y) {
        return board[y][x];
    }

    public void setChecker(int x, int y, Checker checker) {
        board[y][x] = checker;
    }

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
        if (destination.y == 7 && currentSide.equals(Side.WHITE) || destination.y == 0 && currentSide.equals(Side.BLACK)) {
            currentChecker.setRank(Rank.QUEEN);
        }
        possibleMoves.clear();

        if (moveState.equals(MoveState.MOVE_BEAT)) {
            calculatePossibleMoves(currentChecker);
            possibleMoves.removeIf(mv -> !mv.moveState.equals(MoveState.MOVE_BEAT));
            if (possibleMoves.isEmpty()) {
                currentSide = currentSide == Side.WHITE ? Side.BLACK : Side.WHITE;
                hasToBeatNext = false;
            } else {
                hasToBeatNext = true;
            }
        } else {
            currentSide = currentSide == Side.WHITE ? Side.BLACK : Side.WHITE;
        }
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
        if (checker.getRank().equals(Rank.NORMAL)) {
            checkMove(currentPosition, new Vector2i(1, 1), checker).ifPresent(possibleMoves::add);
            checkMove(currentPosition, new Vector2i(-1, 1), checker).ifPresent(possibleMoves::add);
            checkMove(currentPosition, new Vector2i(1, -1), checker).ifPresent(possibleMoves::add);
            checkMove(currentPosition, new Vector2i(-1, -1), checker).ifPresent(possibleMoves::add);
        } else {
            for (int i = 1; i < 7; i++) {
                checkMove(currentPosition, new Vector2i(i, i), checker).ifPresent(possibleMoves::add);
                checkMove(currentPosition, new Vector2i(-i, i), checker).ifPresent(possibleMoves::add);
                checkMove(currentPosition, new Vector2i(i, -i), checker).ifPresent(possibleMoves::add);
                checkMove(currentPosition, new Vector2i(-i, -i), checker).ifPresent(possibleMoves::add);
            }
        }
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
            Checker enemy = null;
            if (checker.getRank().equals(Rank.QUEEN) && board[positionToCheck.y][positionToCheck.x] == null) {
                int length = abs(direction.x);
                Vector2i normalizedDirection = new Vector2i(direction.x / length, direction.y / length);
                Vector2i enemyPos = current;
                int enemyLen = 0;
                while (!enemyPos.equals(positionToCheck) && enemy == null) {
                    enemyLen++;
                    enemyPos = enemyPos.plus(normalizedDirection);
                    if (board[enemyPos.y][enemyPos.x] != null && !board[enemyPos.y][enemyPos.x].getSide().equals(checker.getSide())) {
                        enemy = board[enemyPos.y][enemyPos.x];
                    }
                }

                if (enemy == null) {
                    boolean anyBetween = false;
                    for (int i = 1; i < length; i++) {
                        Vector2i curr = current.plus(normalizedDirection.mul(i));
                        if (board[curr.y][curr.x] != null) {
                            anyBetween = true;
                            break;
                        }
                    }
                    if (!anyBetween) {
                        return Optional.of(new Move(MoveState.MOVE_OK, current, positionToCheck, null));
                    }
                } else {
                    boolean anyBetween = false;
                    for (int i = enemyLen + 1; i < length; i++) {
                        Vector2i curr = current.plus(normalizedDirection.mul(i));
                        if (board[curr.y][curr.x] != null) {
                            anyBetween = true;
                            break;
                        }
                    }
                    if (!anyBetween) {
                        return Optional.of(new Move(MoveState.MOVE_BEAT, current, positionToCheck, enemyPos));
                    }
                }
            } else {
                enemy = board[positionToCheck.y][positionToCheck.x];
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
        }
        return Optional.empty();
    }

    private boolean checkBound(Vector2i v) {
        return v.x >= 0 && v.x < 8 && v.y < 8 && v.y >= 0;
    }
}
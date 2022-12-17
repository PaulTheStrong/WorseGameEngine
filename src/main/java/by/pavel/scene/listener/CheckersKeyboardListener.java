package by.pavel.scene.listener;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import by.pavel.checker.Checker;
import by.pavel.math.Vector2i;
import by.pavel.scene.MainWindow.GameState;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CheckersKeyboardListener implements KeyListener {

    private final GameState gameState;

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_SPACE) {
            Checker selectedChecker = gameState.getSelectedChecker();
            Checker hoveredChecker = gameState.getHoveredChecker();
            if (selectedChecker == null && hoveredChecker != null) {
                gameState.setSelectedChecker(hoveredChecker);
            } else if (selectedChecker != null) {
                Vector2i hoveredCell = gameState.getHoveredCell();
                if (hoveredCell != null) {
                    gameState.getPossibleMoves()
                        .stream()
                        .filter(move -> move.getDestination().equals(hoveredCell))
                        .findFirst()
                        .ifPresent(gameState::makeMove);
                }
                gameState.setSelectedChecker(null);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}

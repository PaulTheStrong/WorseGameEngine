package by.pavel.checker;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Checker {

    public enum Side {
        WHITE, BLACK
    }

    public enum State {
        NORMAL, QUEEN, OUT
    }

    private Side side;

}

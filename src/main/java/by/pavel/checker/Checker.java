package by.pavel.checker;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Checker {

    public enum Side {
        WHITE, BLACK
    }

    public enum Rank {
        NORMAL, QUEEN
    }

    private final Side side;
    private Rank rank = Rank.NORMAL;

}

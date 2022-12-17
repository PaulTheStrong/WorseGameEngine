package by.pavel.scene;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Mouse {

    private int x;
    private int y;

    private static final Mouse instance = new Mouse();

    private Mouse() {}

    public static Mouse getInstance() {
        return instance;
    }
}

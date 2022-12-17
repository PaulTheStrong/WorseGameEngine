package by.pavel.math;

import java.util.Objects;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Vector2i {

    public int x, y;

    public Vector2i plus(Vector2i other) {
        return new Vector2i(x + other.x, y + other.y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Vector2i vector2i = (Vector2i) o;
        return x == vector2i.x && y == vector2i.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}

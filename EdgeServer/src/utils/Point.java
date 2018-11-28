package utils;

import java.util.Objects;

// this class intends to replace pair class
public final class Point<T> {
    private T x;
    private T y;

    public Point(T x, T y) {
        this.x = x;
        this.y = y;
    }

    public T getX() {
        return x;
    }

    public T getY() {
        return y;
    }

    public boolean isSame(Point<T> another) {
        return (this.x == another.getX() && this.y == another.getY());
    }

    public String toString() {
        String result = "utils.Point: (%d, %d)";
        return String.format(result, getX(), getY());
    }

    // override equals and hashCode so that this object can be used in HashMap correctly
    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this.toString(), obj.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}

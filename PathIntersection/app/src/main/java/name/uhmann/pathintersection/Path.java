package name.uhmann.pathintersection;

/**
 * Created by Tobias on 05.02.2017.
 */

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A {@code Path} is a sequence of connected {@code Point}s. It provides means for checking if
 * it intersects another {@code Path}.
 *
 * @param <P> Type of point. Must be a subtype of {@code Point}, e.g. {@code VisualPoint}.
 */
public class Path<P extends Point> implements Iterable<P> {

    protected ArrayList<P> points = new ArrayList<>();

    /**
     * Get the point at the specified index.
     *
     * @param index Point's index in the path.
     * @return The {@code Point} object.
     */
    public P get(int index) {
        return points.get(index);
    }

    /**
     * Add a point to the end of the path.
     *
     * @param point Point to be added.
     */
    public void add(P point) {
        points.add(point);
    }

    /**
     * Remove the specified point from the path.
     *
     * @param point Point to be removed.
     */
    public void remove(P point) {
        if (length() > 1)
            points.remove(point);
    }

    /**
     * Remove the point at the specified index from the path.
     *
     * @param index Index of point that shall be removed.
     */
    public void remove(int index) {
        if (length() > 1)
            points.remove(index);
    }

    /**
     * Get the length of the {@code Path}
     *
     * @return Length of the {@code Path}
     */
    public int length() {
        return points.size();
    }

    /**
     * Get the position of the specified {@code Point} in the {@code Path}.
     *
     * @param point Point whose index position to get.
     * @return Index position of the specified point.
     */
    public int indexOf(P point) {
        return points.indexOf(point);
    }

    /**
     * Gets if this {@code Path} intersects another one. Two paths intersect if any of their lines
     * cross or touch.
     *
     * @param other The other path.
     * @return {@code true} if the paths cross or touch, {@code false} otherwise.
     */
    public boolean intersects(Path<P> other) {
        // for each line (consecutive pair of points) in this path:
        // check intersection with all lines in the other path
        for (int i = 0; i < points.size() - 1; i++)
            for (int j = 0; j < other.points.size() - 1; j++)
                if (Point.linesIntersect(points.get(i), points.get(i+1),
                        other.points.get(j), other.points.get(j+1)))
                    return true;

        // no intersection found
        return false;
    }

    /**
     * Returns an iterator over elements of type {@code P}.
     *
     * @return An Iterator.
     */
    @Override
    public Iterator<P> iterator() {
        return new Iterator<P>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return points.size() > index;
            }

            @Override
            public P next() {
                return points.get(index++);
            }
        };
    }

    /**
     * Get a string representation that is useful for debugging. The format of the string is:
     *
     * {@code [ ( 1.23 | 45.67 ), ( 41.9 | 5.01 ), ... ]}
     *
     * @return String representation
     */
    @Override
    public String toString() {
        // Use StringBuilder instead of String for performance reasons.
        // A synchronized StringBuffer is not necessary here.
        StringBuilder output = new StringBuilder("[ ");

        // Off-by-one-Problem: No comma shall be set after last point.
        // Therefore no extended for loop is used.
        for (int i = 0; i < points.size() - 1; i++)
            output.append(points.get(i).toString()).append(", ");

        output.append(points.get(points.size() - 1)).append(" ]");

        return output.toString();
    }
}

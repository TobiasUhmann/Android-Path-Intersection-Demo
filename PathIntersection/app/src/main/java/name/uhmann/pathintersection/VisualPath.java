package name.uhmann.pathintersection;

/**
 * Created by Tobias on 05.02.2017.
 */

import static name.uhmann.pathintersection.MainActivity.DARK_GREY;

/**
 * A {@code VisualPath} is a {@code Path} that can be drawn in a {@code PathSystem}.
 */
public class VisualPath extends Path<VisualPoint> implements Drawable {

    /**
     * Draws this object to the provided {@code PathSystem}.
     *
     * @param pathSystem {@code PathSystem} to be drawn to.
     */
    @Override
    public void draw(PathSystem pathSystem) {
        // draw all lines first so that the points are at the top indicating that they are touchable
        for (int i = 0; i < points.size() - 1; i++)
            pathSystem.drawLine(points.get(i), points.get(i + 1), 1, DARK_GREY);

        // draw points
        for (int i = 0; i < points.size(); i++)
            points.get(i).draw(pathSystem);
    }
}

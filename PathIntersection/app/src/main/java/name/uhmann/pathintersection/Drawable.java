package name.uhmann.pathintersection;

/**
 * Created by Tobias on 05.02.2017.
 */

/**
 * A {@code Drawable} can be drawn to a {@code PathSystem}.All elements added to a
 * {@code PathSystem} must implement this interface as their {@code draw} method is called when
 * the {@code PathSystem} is drawn.
 */
public interface Drawable {

    /**
     * Draws this object to specified {@code PathSystem}.
     *
     * @param pathSystem {@code PathSystem} to be drawn to.
     */
    void draw(PathSystem pathSystem);
}

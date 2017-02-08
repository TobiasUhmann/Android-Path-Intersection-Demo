package name.uhmann.pathintersection;

import android.graphics.Color;

import static name.uhmann.pathintersection.MainActivity.DARK_GREY;

/**
 * A {@code VisualPoint} is a {@code Point} that can be drawn in a {@code PathSystem}.
 */
public class VisualPoint extends Point implements Drawable {

    private int color;
    private boolean focused;

    /**
     * Create a VisualPoint at the specified location.
     *
     * @param x X coordinate in units.
     * @param y Y coordinate in units.
     * @param color The point's color.
     */
    public VisualPoint(double x, double y, int color) {
        super(x, y);

        this.color = color;
    }

    /**
     * Set if this point is focused.
     *
     * @param focused The point's update.
     */
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    /**
     * Draws this object to the specified {@code PathSystem}.
     *
     * @param pathSystem {@code PathSystem} to draw to.
     */
    @Override
    public void draw(PathSystem pathSystem) {
        if (focused)
            pathSystem.fillCircle(this, 4, DARK_GREY);

        pathSystem.fillCircle(this, 3, color);
    }

    /**
     * Get a string representation that is useful for debugging. The format of the string is:
     *
     * {@code { position: ( 1.23 | 45.67 ), color: rgb( 120, 59, 80 ), focused: false }}
     *
     * @return String representation
     */
    @Override
    public String toString() {
        return "{ position: " + super.toString() + ", color: rgb( " + Color.red(color) + " , " +
                Color.green(color) + " , " + Color.blue(color) + " ), focused: " + focused + " }";
    }
}

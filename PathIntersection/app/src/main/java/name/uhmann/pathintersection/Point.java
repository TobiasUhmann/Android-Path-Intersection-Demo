package name.uhmann.pathintersection;

/**
 * Created by Tobias on 05.02.2017.
 */

import android.util.Log;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * This class provides an implementation for points in the 2-dimensional (x,y) space.
 *
 * In addition, it provides the two methods {@code onSegment} and {@code orientation}
 * as helper methods for determining if two path segments (i.e. lines) intersect.
 */
public class Point {

    public double x;   // Our 2D space is continuous. It is later transformed to the discrete pixel
    public double y;   // grid when being painted.

    // Public fields without getters and setters are used as there are no constraints for x and y.
    // Also, they are accessed from a single thread so there is no problem with reading cached
    // values. This decision was also made for android.graphics.Point.

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Get if the lines (p1 - p2) and (q1 - q2) intersect.
     *
     * A visualization of the algorithm can be found here:
     * http://www.geeksforgeeks.org/check-if-two-given-line-segments-intersect/
     *
     * @param p1 first endpoint of first line (p1 - p2)
     * @param p2 second endpoint of first line (p1 - p2)
     * @param q1 first endpoint of second line (q1 - q2)
     * @param q2 second endpoint of second line (q1 - q2)
     * @return true if the lines intersect, false otherwise
     */
    public static boolean linesIntersect(Point p1, Point p2, Point q1, Point q2) {

        Orientation orientationPPQ1 = q1.orientationTo(p1, p2);
        Orientation orientationPPQ2 = q2.orientationTo(p1, p2);
        Orientation orientationQQP1 = p1.orientationTo(q1, q2);
        Orientation orientationQQP2 = p2.orientationTo(q1, q2);

        Log.d("TAG", "linesIntersect: " + orientationPPQ1 + " " + orientationPPQ2 + " " +
                orientationQQP1 + " " + orientationQQP2);

        // General case: The lines are not aligned
        // Endpoints of (q1 - q2) on opposite sides of (p1 - p2) and vice versa?
        //
        // Only checking if q1 and q2 are on opposite sides of (p1 - p2) is not sufficient!
        // (q1 - q2) could still be beyond (p1 - p2)
        if (orientationPPQ1 != orientationPPQ2 && orientationQQP1 != orientationQQP2)
            return true;

        // Special case: The lines are aligned
        // Check if (q1 - q2) is beyond (p1 - p2) or not
        // To intersect at least one line's point must lay on the other one.
        // A point lays on a line if it is colinear to the line's endpoints and lays in the
        // rectangle delimited by the line's endpoints.
        //
        // If no special case applies neither the expression evaluates to false.
        return
                (orientationPPQ1 == Orientation.COLINEAR && q1.withinRectangle(p1, p2)) ||
                (orientationPPQ2 == Orientation.COLINEAR && q2.withinRectangle(p1, p2)) ||
                (orientationQQP1 == Orientation.COLINEAR && p1.withinRectangle(q1, q2)) ||
                (orientationQQP2 == Orientation.COLINEAR && p2.withinRectangle(q1, q2));
    }

    private enum Orientation {
        CLOCKWISE,
        COUNTERCLOCKWISE,
        COLINEAR
    }

    /**
     * Get the orientation of the triangle made up of this point and the points p and q which are
     * the endpoints of a line.
     *
     * The point and the line form a triangle whose edges can be run along starting at the first
     * endpoint of the line, passing the second endpoint and the separate point and finally
     * arriving at the line's first endpoint again: p -> q -> this -> p
     *
     * This run is clockwise, counterclockwise or colinear in the special case of all three points
     * are aligned.
     *
     * A visualization of the algorithm can be found here:
     * http://www.geeksforgeeks.org/orientation-3-ordered-points/
     *
     * However, the linked website only describes the case that both of the line's endpoints are
     * to the right or left of the third point. This implementation has an additional step to work
     * properly if that is not the case.
     *
     * @param p first endpoint of line
     * @param q second endpoint of line
     * @return CLOCKWISE, COUNTERCLOCKWISE or COLINEAR
     */
    private Orientation orientationTo(Point p, Point q) {

        Orientation result;

        // slope of a line: delta y / delta x
        double m1 = (p.y - this.y) / (p.x - this.x);
        double m2 = (q.y - this.y) / (q.x - this.x);

        // if the line's endpoints are NOT on the same side the orientation needs to be inverted!
        boolean sameSide = (p.x > this.x == q.x > this.x);

        // Simple example:
        // THIS ( 0 | 0 ), P ( 1 | 1 ), Q ( 2 | 3 )
        // The slope from THIS to P is 1.
        //
        // The slope fro P to Q is steeper (it's 2). One can picture to oneself that walking along
        // THIS -> P -> Q -> THIS would be counterclockwise.
        //
        // If Q was at ( 2 | 1 ) the slope from P to Q would be 0 and therewith less than the slope
        // from THIS to P and the resulting run would be clockwise.
        //
        // Finally, if Q was at ( 2 | 2 ) the slopes would both be 1 so one would walk along one
        // line resulting in the orientation being COLINEAR.
        if (m1 > m2) {
            if (sameSide)
                result = Orientation.CLOCKWISE;
            else
                result = Orientation.COUNTERCLOCKWISE;
        } else if (m2 > m1) {
            if (sameSide)
                result = Orientation.COUNTERCLOCKWISE;
            else
                result = Orientation.CLOCKWISE;

        } else {
            result = Orientation.COLINEAR;
        }

        return result;
    }

    /**
     * Get if this point lays within the rectangle that is delimited by the opposite corner
     * points p and q. Laying on one of the rectangles edges also yields true.
     *
     * @param p First corner point of the rectangle (opposite to q).
     * @param q Second corner point of the rectangle (opposite to p).
     * @return True if this point lays within the rectangle or on one of its edges, false otherwise.
     */
    private boolean withinRectangle(Point p, Point q) {
        return
                (min(p.x, q.x) <= this.x && this.x <= max(p.x, q.x)) &&
                (min(p.y, q.y) <= this.y && this.y <= max(p.y, q.y));
    }

    /**
     * Get a string representation that is useful for debugging. The format of the string is:
     *
     * {@code ( 1.23 | 45.67 )}
     *
     * @return String representation
     */
    @Override
    public String toString() {
        return "( " + String.format("%.2f", x) + " | " + String.format("%.2f", y) + " )";
    }
}

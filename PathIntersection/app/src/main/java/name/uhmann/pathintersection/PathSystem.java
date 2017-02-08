package name.uhmann.pathintersection;

/**
 * Created by Tobias on 05.02.2017.
 */

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import static name.uhmann.pathintersection.MainActivity.LIGHT_GREY;

/**
 * A {@code PathSystem} contains multiple {@code VisualPath}s and can be drawn to {@code Canvas},
 * e.g. the one provided by a {@code SurfaceView}.
 *
 * It has it's own coordinate system that is translated to the underlying pixel system by using
 * the specified scale factor. That way the drawn graphics aren't device dependent and a zoom
 * function can be implemented. The origin of the coordinate system is not in the upper left corner
 * but in the lower left corner, like in mathematical coordinate systems, instead.
 */
public class PathSystem implements View.OnTouchListener {

    // Keep track of the selected point to notice when it changes
    private List<VisualPath> paths = new ArrayList<>();
    private VisualPath selectedPath;
    private VisualPoint selectedPoint;

    private boolean anyIntersection;

    // The SurfaceHolder is used to lock and unlock (and thereby draw) our Canvas.
    // A reference to the Canvas is hold to be accessible from the different draw methods
    private final SurfaceHolder surfaceHolder;
    private Canvas canvas;

    // scaling factors for transforming length units to pixels and vice versa
    private final double scaleFactor;
    private final int pixelWidth;
    private final int pixelHeight;

    /**
     * Create a {@code PathSystem} with the given scale factor, i.e. the multiplier for transforming
     * the coordinate system's units into pixels and vice versa.
     *
     * @param surfaceView Holds the {@code Canvas} to draw to.
     * @param scaleFactor Scale factor for transforming coordinate system units to pixels and vice
     *                    versa.
     */
    public PathSystem(SurfaceView surfaceView, double scaleFactor) {
        this.surfaceHolder = surfaceView.getHolder();

        pixelWidth = surfaceView.getWidth();
        pixelHeight = surfaceView.getHeight();

        this.scaleFactor = scaleFactor;
    }

    /**
     * Get the {@code PathSystem}'s width in units.
     *
     * @return Width in units.
     */
    public double getWidth() {
        return pixelWidth / scaleFactor;
    }

    /**
     * Get the {@code PathSystem}'s height in units.
     *
     * @return Width in units.
     */
    public double getHeight() {
        return pixelHeight / scaleFactor;
    }

    /**
     * Add a {@code VisualPath} to the {@code PathSystem}.
     *
     * @param path Path to add.
     */
    public void add(VisualPath path) {

        paths.add(path);
    }

    /**
     * Draw the {@code PathSystem} and all of it's contained elements to the screen
     */
    public void draw() {
        canvas = surfaceHolder.lockCanvas();

        drawColor(LIGHT_GREY);
        for (VisualPath path : paths)
            path.draw(this);

        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    /**
     * Dye the whole canvas with the specified color.
     *
     * @param color Color to draw.
     */
    public void drawColor(int color) {
        canvas.drawColor(color);
    }

    /**
     * Draw a line between two {@code Point}s.
     *
     * @param p First endpoint of the line.
     * @param q Second endpoint of the line.
     */
    public void drawLine(Point p, Point q, double width, int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStrokeWidth(getXPixels(width));

        canvas.drawLine(getXPixels(p.x), getYPixels(p.y), getXPixels(q.x), getYPixels(q.y), paint);
    }

    /**
     * Draw a filled circle.
     *
     * @param point Center of the circle (coordinates in units).
     * @param radius Radius (in units).
     * @param color Color of the circle.
     */
    public void fillCircle(Point point, double radius, int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStrokeWidth(getXPixels(radius));

        canvas.drawCircle(getXPixels(point.x), getYPixels(point.y), getXPixels(radius), paint);
    }

    /**
     * Convert x coordinate units to pixels using the existing scale factor.
     *
     * @param units Measure in units
     * @return Measure in pixels
     */
    private float getXPixels(double units) {
        return (float)(units * scaleFactor);
    }

    /**
     * Convert y coordinate units to pixels using the existing scale factor. Note that the
     * coordinate system's y axis values increase bottom-up.
     *
     * @param units Measure in units
     * @return Measure in pixels
     */
    private float getYPixels(double units) {
        return pixelHeight - (float)(units * scaleFactor);
    }

    /**
     * Convert x coordinate pixels to units using the existing scale factor.
     *
     * @param pixels Measure in pixels
     * @return Measure in units
     */
    private double getXUnits(float pixels) {
        return pixels / scaleFactor;
    }

    /**
     * Convert y coordinate pixels to units using the existing scale factor. Note that the
     * coordinate system's y axis values increase bottom-up.
     *
     * @param pixels Measure in pixels
     * @return Measure in units
     */
    private double getYUnits(float pixels) {
        return (pixelHeight - pixels) / scaleFactor;
    }

    /**
     * Focus the specified {@code Point}.
     *
     * @param point Currently selected {@code Point}.
     */
    public void update(VisualPath path, VisualPoint point) {
        if (selectedPoint != null)
            selectedPoint.setFocused(false);

        selectedPath = path;
        selectedPoint = point;
        if (selectedPoint != null)
            selectedPoint.setFocused(true);

        draw();
    }

    /**
     * Called when a touch event is dispatched to a view. This allows listeners to
     * get a chance to respond before the target view.
     *
     * @param v     The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about
     *              the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                handleActionDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                handleActionMove(event);
                break;
        }

        // get the following events from this gesture
        return true;
    }

    private void handleActionDown(MotionEvent event) {
        // run through all points in all paths and get the minimal distance between the touch
        // position and a point. If that distance is smaller than a threshold the identified
        // point is dragged and listeners to the PathSystem's events are notified.

        VisualPoint closestPoint = null;
        double minDistance = Double.MAX_VALUE;
        VisualPath selectedPath = null;

            for (VisualPath path : paths) {
            for (VisualPoint point : path) {
                double deltaX = event.getX() - getXPixels(point.x);
                double deltaY = event.getY() - getYPixels(point.y);
                double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

                Log.d("TAG", "handleActionDown: " + point + " " + distance);
                if (distance < minDistance) {
                    closestPoint = point;
                    minDistance = distance;
                    selectedPath = path;
                }
            }
        }

        // selectedPoint can be null if no point was selected previously
        if (selectedPoint != null) {
            selectedPoint.setFocused(false);
        }

        // chosen threshold value (40.0) resulted in a good user experience
        if (minDistance < 40.0) {
            selectedPoint = closestPoint;
            selectedPoint.setFocused(true);
            fireFocusChanged(selectedPath, selectedPoint);
        } else {
            // also notify if no point was touched
            fireFocusChanged(null, null);
            selectedPoint = null;
        }

        draw();
    }

    private void handleActionMove(MotionEvent event) {
        // if a point has been touched: move it and check if the path crosses another one

        if (selectedPoint != null) {
            // make sure the new location is within the screen
            if (0 < event.getX() && event.getX() < pixelWidth &&
                    0 < event.getY() && event.getY() < pixelHeight) {
                selectedPoint.x = getXUnits(event.getX());
                selectedPoint.y = getYUnits(event.getY());

                checkIntersection();
                draw();
            }
        }
    }

    public void checkIntersection() {
        // iterate through all path segments (pair of consecutive points) and check if the
        // intersect any path segment of any other path. If so, notify listeners to the
        // PathSystem events.

        boolean anyIntersection = false;

        outer:
        for (int i = 0; i < paths.size(); i++)
            for (int j = i + 1; j < paths.size(); j++)
                if (paths.get(i).intersects(paths.get(j))) {
                    anyIntersection = true;
                    break outer;
                }

        if (anyIntersection != this.anyIntersection) {
            fireIntersectionChanged(anyIntersection);
            this.anyIntersection = anyIntersection;
        }
    }

    /* PathSystem Events */

    /**
     * Class that implement this interface can be registered as listeners to {@code PathSystem}
     * events and thereby be notified of the intersection state and a change of the selected point
     * of a {@code PathSystem}.
     */
    public interface PathSystemListener {
        void intersectionChanged(boolean pathsIntersect);

        void focusChanged(VisualPath path, VisualPoint point);
    }

    private ArrayList<PathSystemListener> listeners = new ArrayList<>();

    /**
     * Add a listener to inform about changes to this {@code PathSystem}.
     *
     * @param listener Listener that implements the {@code PathSystemListener} interface.
     */
    public void addPathSystemListener(PathSystemListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a previously added listener to {@code PathSystem} events.
     *
     * @param listener Listener to remove.
     */
    public void removePathSystemListener(PathSystemListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify all registered listeners of a change of the path intersection state.
     *
     * @param pathsIntersect The new path intersectin state. {@code true} if any paths intersect,
     *                       {@code false} otherwise.
     */
    private void fireIntersectionChanged(boolean pathsIntersect) {
        for (PathSystemListener listener : listeners)
            listener.intersectionChanged(pathsIntersect);
    }

    /**
     * Notify all registered listeners of a change of the selected point in the {@code PathSystem}.
     *
     * @param path Path that contains the newly selected point.
     * @param point Newly selected point.
     */
    private void fireFocusChanged(VisualPath path, VisualPoint point) {
        for (PathSystemListener listener : listeners)
            listener.focusChanged(path, point);
    }
}

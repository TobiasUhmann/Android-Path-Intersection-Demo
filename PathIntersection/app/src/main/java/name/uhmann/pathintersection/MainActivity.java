package name.uhmann.pathintersection;

/**
 * Created by Tobias on 05.02.2017.
 */

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Random;

/**
 * The Application's {@code MainActivity} overrides the {@code onCreate} method and therewith
 * provides the application's entry point.
 *
 * Besides holding and initializing the {@code PathSystem} that is drawn to the upper part of the
 * activity's screen it also holds the other GUI components and implements event handlers for
 * the respective events.
 */
public class MainActivity extends AppCompatActivity implements
        SurfaceHolder.Callback, PathSystem.PathSystemListener {

    // load colors once from resources at the beginning
    // public to be accessible from PathSystem which also uses the colors for drawing.
    public static int ORANGE;
    public static int GREEN;
    public static int DARK_GREY;
    public static int LIGHT_GREY;

    /* GUI components */

    private SurfaceView surfaceView_pathSystem;

    private LinearLayout linearLayout_controlBox;
    private RadioGroup radioGroup_pathSelection;
    private SeekBar seekBar_pointIndex;
    private SeekBar seekBar_xCoordinate;
    private SeekBar seekBar_yCoordinate;
    private Button cmd_add;
    private Button cmd_remove;

    private TextView txt_pathsIntersect;

    /* App Logic */

    private PathSystem pathSystem;

    private VisualPath orangePath = new VisualPath();
    private VisualPath greenPath = new VisualPath();
    private VisualPath selectedPath;
    private VisualPoint selectedPoint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get colors from colors.xml
        ORANGE = ContextCompat.getColor(this, R.color.orange);
        GREEN = ContextCompat.getColor(this, R.color.green);
        DARK_GREY = ContextCompat.getColor(this, R.color.darkGrey);
        LIGHT_GREY = ContextCompat.getColor(this, R.color.lightGrey);

        /* initialize GUI references */

        // Add a callback handler that is called as soon as the SurfaceView is ready
        surfaceView_pathSystem = (SurfaceView)findViewById(R.id.surfaceView);
        surfaceView_pathSystem.getHolder().addCallback(this);

        linearLayout_controlBox = (LinearLayout)findViewById(R.id.linearLayout);

        // the RadioGroup's onCheckedChangeListener can't be set declaratively in XML
        radioGroup_pathSelection = (RadioGroup)findViewById(R.id.radioGroup);
        radioGroup_pathSelection.setOnCheckedChangeListener(new RadioButtonHandler());

        seekBar_pointIndex = (SeekBar)findViewById(R.id.seekBar_pointIndex);
        seekBar_pointIndex.setOnSeekBarChangeListener(new SeekBarHandler());
        seekBar_xCoordinate = (SeekBar)findViewById(R.id.seekBarX);
        seekBar_xCoordinate.setOnSeekBarChangeListener(new SeekBarHandler());
        seekBar_yCoordinate = (SeekBar)findViewById(R.id.seekBarY);
        seekBar_yCoordinate.setOnSeekBarChangeListener(new SeekBarHandler());

        cmd_add = (Button)findViewById(R.id.cmd_add);
        cmd_remove = (Button)findViewById(R.id.cmd_remove);

        txt_pathsIntersect = (TextView)findViewById(R.id.txt_pathsIntersect);

        // execution continues in surfaceCreated when the SurfaceView is loaded
    }

    /**
     * This is called immediately after the surface is first created.
     * Implementations of this should start up whatever rendering code
     * they desire.  Note that only one thread can ever draw into
     * a {@link Surface}, so you should not draw into the Surface here
     * if your normal rendering will be in another thread.
     *
     * @param holder The SurfaceHolder whose surface is being created.
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        // create a PathSystem with a width of 100 units
        pathSystem = new PathSystem(surfaceView_pathSystem,
                surfaceView_pathSystem.getWidth() / 100.0);

        // handle touch events on SurfaceView in the PathSystem and add MainActivity as a
        // receiver of PathSystem events. Thereby MainActivity is notified when a new point is
        // selected or paths intersect and can update it's other GUI elements accordingly.
        surfaceView_pathSystem.setOnTouchListener(pathSystem);
        pathSystem.addPathSystemListener(this);

        /* add two paths for this demo application */

        // create a path that is completely orange
        orangePath.add(new VisualPoint(10, 20, ORANGE));
        orangePath.add(new VisualPoint(80, 15, ORANGE));
        orangePath.add(new VisualPoint(90, 30, ORANGE));
        orangePath.add(new VisualPoint(70, 35, ORANGE));
        pathSystem.add(orangePath);

        // a second path. it crosses the orange one initially
        greenPath.add(new VisualPoint(20, 40, GREEN));
        greenPath.add(new VisualPoint(40, 10, GREEN));
        greenPath.add(new VisualPoint(75, 60, GREEN));
        pathSystem.add(greenPath);

        // no point selected at the beginning
        deactivateControlBox();
        pathSystem.update(null, null);

        pathSystem.draw();
    }

    /**
     * Focus the specified {@code Point}.
     *
     * @param point Currently selected {@code Point}.
     */
    public void update(VisualPath path, VisualPoint point) {
        selectedPath = path;
        selectedPoint = point;

        if (path == orangePath)
            activateControlBox(ORANGE, orangePath.length(), orangePath.indexOf(point),
                    point.x, point.y);
        else if (path == greenPath)
            activateControlBox(GREEN, greenPath.length(), greenPath.indexOf(point),
                    point.x, point.y);
        else
            deactivateControlBox();

    }

    /**
     * This is called immediately after any structural changes (format or
     * size) have been made to the surface.  You should at this point update
     * the imagery in the surface.  This method is always called at least
     * once, after {@link #surfaceCreated}.
     *
     * @param holder The SurfaceHolder whose surface has changed.
     * @param format The new PixelFormat of the surface.
     * @param width  The new width of the surface.
     * @param height The new height of the surface.
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // nothing to do here: our SurfaceView doesn't change it's size
    }

    /**
     * This is called immediately before a surface is being destroyed. After
     * returning from this call, you should no longer try to access this
     * surface.  If you have a rendering thread that directly accesses
     * the surface, you must ensure that thread is no longer touching the
     * Surface before returning from this function.
     *
     * @param holder The SurfaceHolder whose surface is being destroyed.
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // no resources that need to be freed here
    }

    /**
     * Called when the "Add" or "Remove" button is clicked. This handler is registered
     * declaratively in the XML layout file. Depending on the actual button clicked a point is
     * either added to the selected path or removed from it.
     *
     * @param view The Button that has been clicked.
     */
    public void onButtonClicked(View view) {
        Button clickedButton = (Button)view;

        // if the "Add" button is clicked: add a point at a random location to the selected path
        if (clickedButton.getId() == R.id.cmd_add) {
            Random random = new Random();
            double randomX = random.nextDouble() * pathSystem.getWidth();
            double randomY = random.nextDouble() * pathSystem.getHeight();
            VisualPoint newPoint;

            if (selectedPath == orangePath)
                newPoint = new VisualPoint(randomX, randomY, ORANGE);
            else
                newPoint = new VisualPoint(randomX, randomY, GREEN);

            selectedPath.add(newPoint);
            selectedPoint = newPoint;
            pathSystem.update(selectedPath, selectedPoint);

        //  if the "Remove" button is clicked: remove the selected point
        } else if (clickedButton.getId() == R.id.cmd_remove) {
            selectedPath.remove(seekBar_pointIndex.getProgress());
            deactivateControlBox();
            pathSystem.update(null, null);
        }

        pathSystem.draw();
    }

    /**
     * Handle a change of the path intersection state by updating the activity's {@code TextView}.
     *
     * @param pathsIntersect New path intersection state. True if the paths intersect, false
     *                       otherwise.
     */
    @Override
    public void intersectionChanged(boolean pathsIntersect) {
        txt_pathsIntersect.setText(pathsIntersect ?
                getText(R.string.paths_do_intersect) : getText(R.string.paths_do_not_intersect));
    }

    /**
     * Handle a change of the point update in the path system by updating the GUI elements in
     * the control box accordingly. If a new point has been touched the control boxes color scheme
     * needs to be updated as well as the {@code SeekBar}'s values. I no point was selected the
     * control box is disabled.
     *
     * @param path Path that contains the selected point or {@code null} if no point was touched.
     * @param point Point that was selected or {@code null} if none was touched.
     */
    @Override
    public void focusChanged(VisualPath path, VisualPoint point) {
        selectedPath = path;
        selectedPoint = point;

        if (selectedPath == orangePath) {
            radioGroup_pathSelection.check(R.id.radioButton_orange);
            activateControlBox(ORANGE, orangePath.length(), orangePath.indexOf(point),
                    point.x, point.y);
        } else if (selectedPath == greenPath) {
            radioGroup_pathSelection.check(R.id.radioButton_green);
            activateControlBox(GREEN, greenPath.length(), greenPath.indexOf(point),
                    point.x, point.y);
        } else {
            deactivateControlBox();
        }
    }

    /**
     * Helper method to disable the control box, change the color scheme to grey and set all
     * {@code SeekBar}s to zero.
     */
    private void deactivateControlBox() {
        seekBar_pointIndex.setEnabled(false);
        seekBar_xCoordinate.setEnabled(false);
        seekBar_yCoordinate.setEnabled(false);
        cmd_add.setEnabled(false);
        cmd_remove.setEnabled(false);

        radioGroup_pathSelection.clearCheck();
        linearLayout_controlBox.setBackground(getDrawable(R.drawable.border_grey));
        cmd_remove.setBackground(getDrawable(R.drawable.minus_icon_grey));
        cmd_add.setBackground(getDrawable(R.drawable.plus_icon_grey));
    }

    /**
     * Helper method to enable the control box, change the color scheme to the specified color
     * and set all set the {@code SeekBar}s' values so that they represent the currently
     * selected point.
     *
     * @param color Color of the point that was selected.
     * @param pathLength Number of points the selected path contains.
     * @param pointIndex Index of the selected point within the path.
     * @param x Selected point's x coordinate in units.
     * @param y Selected point's y coordinate in units.
     */
    private void activateControlBox(int color, int pathLength, int pointIndex, double x, double y) {
        seekBar_pointIndex.setEnabled(true);
        seekBar_xCoordinate.setEnabled(true);
        seekBar_yCoordinate.setEnabled(true);
        cmd_add.setEnabled(true);
        cmd_remove.setEnabled(true);

        if (color == ORANGE) {
            linearLayout_controlBox.setBackground(getDrawable(R.drawable.border_orange));
            cmd_add.setBackground(getDrawable(R.drawable.plus_icon_orange));
            cmd_remove.setBackground(getDrawable(R.drawable.minus_icon_orange));
        } else if (color == GREEN) {
            linearLayout_controlBox.setBackground(getDrawable(R.drawable.border_green));
            cmd_add.setBackground(getDrawable(R.drawable.plus_icon_green));
            cmd_remove.setBackground(getDrawable(R.drawable.minus_icon_green));
        }

        seekBar_pointIndex.setMax(pathLength - 1);
        seekBar_pointIndex.setProgress(pointIndex);
        seekBar_xCoordinate.setMax(pathSystem != null ? (int)pathSystem.getWidth() : 100);
        seekBar_xCoordinate.setProgress((int)x);
        seekBar_yCoordinate.setMax(pathSystem != null ? (int)pathSystem.getHeight() : 100);
        seekBar_yCoordinate.setProgress((int)y);
    }

    class SeekBarHandler implements SeekBar.OnSeekBarChangeListener {

        /**
         * Notification that the progress level has changed. Clients can use the fromUser parameter
         * to distinguish user-initiated changes from those that occurred programmatically.
         *
         * @param seekBar  The SeekBar whose progress has changed
         * @param progress The current progress level. This will be in the range 0..max where max
         *                 was set by {@link ProgressBar#setMax(int)}. (The default value for max is 100.)
         * @param fromUser True if the progress change was initiated by the user.
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (selectedPath == null || selectedPoint == null)
                return;

            if (seekBar.getId() == R.id.seekBar_pointIndex) {
                // if the point selection SeekBar is clicked: update the respective point
                selectedPoint = selectedPath.get(progress);
                seekBar_xCoordinate.setProgress((int)selectedPoint.x);
                seekBar_yCoordinate.setProgress((int)selectedPoint.y);

            } else if (seekBar.getId() == R.id.seekBarX) {
                // if the X SeekBar is clicked: move the selected point in X direction
                selectedPoint.x = progress;
                pathSystem.checkIntersection();
                pathSystem.draw();

            } else if (seekBar.getId() == R.id.seekBarY) {
                //  if the Y SeekBar is clicked: move the selected point in Y direction
                selectedPoint.y = progress;
                pathSystem.checkIntersection();
                pathSystem.draw();
            }

            pathSystem.update(selectedPath, selectedPoint);
        }

        /**
         * Notification that the user has started a touch gesture. Clients may want to use this
         * to disable advancing the seekbar.
         *
         * @param seekBar The SeekBar in which the touch gesture began
         */
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        /**
         * Notification that the user has finished a touch gesture. Clients may want to use this
         * to re-enable advancing the seekbar.
         *
         * @param seekBar The SeekBar in which the touch gesture began
         */
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    class RadioButtonHandler implements RadioGroup.OnCheckedChangeListener {

        /**
         * <p>Called when the checked radio button has changed. When the
         * selection is cleared, checkedId is -1.</p>
         *
         * @param group     the group in which the checked radio button has changed
         * @param checkedId the unique identifier of the newly checked radio button
         */
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            // Use onCheckedChanged instead of onRadioButtonClicked because it's fired only when the
            // radio groups selection state changes.
            switch(checkedId) {
                case R.id.radioButton_orange:
                    if (orangePath.length() > 0) {
                        selectedPath = orangePath;
                        selectedPoint = orangePath.get(0);
                        activateControlBox(ORANGE, orangePath.length(), 0,
                                orangePath.get(0).x, orangePath.get(0).y);
                        pathSystem.update(selectedPath, selectedPoint);
                    }
                    break;
                case R.id.radioButton_green:
                    if (greenPath.length() > 0) {
                        selectedPath = greenPath;
                        selectedPoint = greenPath.get(0);
                        activateControlBox(GREEN, greenPath.length(), 0,
                                greenPath.get(0).x, greenPath.get(0).y);
                        pathSystem.update(selectedPath, selectedPoint);
                    }
                    break;
            }

            pathSystem.update(selectedPath, selectedPoint);
        }
    }
}

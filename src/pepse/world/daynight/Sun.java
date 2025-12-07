package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.Transition;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.*;

/**
 * Represents the sun and its movement in the sky.
 * The sun moves in a circular trajectory to simulate the day-night cycle.
 */
public class Sun {

    private static final String SUN_TAG = "sun";
    private static final Color SUN_COLOR = Color.YELLOW; // Color of the sun
    private static final float FULL_CIRCLE_DEGREES = 360f; // Degrees in a full circular motion
    private static final float INITIAL_ANGLE = 0f; // Starting angle for the sun's movement
    private static final float SUN_RADIUS = 300f; // Radius of the sun's circular motion

    /**
     * Creates a GameObject representing the sun and manages its circular movement using a transition.
     *
     * @param windowDimensions The dimensions of the game window.
     * @param cycleLength The duration of a full day-night cycle, in seconds.
     * @param sunSize The size of the sun.
     * @return A GameObject representing the sun.
     */
    public static GameObject create(Vector2 windowDimensions, float cycleLength, Vector2 sunSize) {
        // Define the center of rotation as the center of the screen
        Vector2 cycleCenter = windowDimensions.mult(0.5f);

        // Create the Sun object
        GameObject sun = new GameObject(
                Vector2.ZERO, // Initial position (will be updated dynamically)
                sunSize,
                new OvalRenderable(SUN_COLOR)
        );

        // Use camera coordinates for rendering
        sun.setCoordinateSpace(danogl.components.CoordinateSpace.CAMERA_COORDINATES);
        sun.setTag(SUN_TAG);

        // Circular motion transition for the sun
        new Transition<>(
                sun,
                (Float angle) -> {
                    sun.setCenter(
                            cycleCenter.add(new Vector2(0, -SUN_RADIUS).rotated(angle)) // Circular path
                    );
                },
                INITIAL_ANGLE, // Initial angle
                FULL_CIRCLE_DEGREES, // Final angle (complete circle)
                Transition.LINEAR_INTERPOLATOR_FLOAT, // Linear interpolation for smooth motion
                cycleLength, // Total duration of the cycle (in seconds)
                Transition.TransitionType.TRANSITION_LOOP, // Infinite loop
                null // No callback after each cycle
        );

        return sun;
    }
}

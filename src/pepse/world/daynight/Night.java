package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import java.awt.*;

/**
 * Represents the nighttime effect in the game, transitioning between day and night.
 * This class handles creating a GameObject that overlays the screen to simulate darkness at night.
 */
public class Night {
    private static final float MIDNIGHT_OPACITY = 0.5f; // Maximum opacity during nighttime
    private static final String NIGHT_TAG = "night"; // Tag to identify the night object
    private static final float START_OPACITY = 0f; // Starting opacity for daytime

    /**
     * Creates a GameObject representing the nighttime effect, with a day-night cycle.
     *
     * @param windowDimensions The dimensions of the game window.
     * @param cycleLength      The total duration of a full day-night cycle (in seconds).
     * @return A GameObject that represents the nighttime overlay.
     */
    public static GameObject create(Vector2 windowDimensions, float cycleLength) {
        // Create a GameObject for the night overlay
        GameObject night = new GameObject(
                Vector2.ZERO,
                windowDimensions,
                new RectangleRenderable(Color.BLACK) // Black overlay to simulate night
        );

        // Set the night overlay to follow the camera's coordinate space
        night.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);

        // Tag the GameObject for easy identification
        night.setTag(NIGHT_TAG);

        // Add a transition to change the opacity of the night object over time
        new Transition<>(
                night,
                night.renderer()::setOpaqueness, // Function to adjust the opacity
                START_OPACITY, // Starting opacity (daytime)
                MIDNIGHT_OPACITY, // Maximum opacity (nighttime)
                Transition.CUBIC_INTERPOLATOR_FLOAT, // Smooth cubic interpolation
                cycleLength / 2, // Half of the cycle for a complete transition to night
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH, // Back-and-forth for day-night cycling
                null // No callback after the transition
        );

        return night;
    }
}

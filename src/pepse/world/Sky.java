package pepse.world;

import danogl.GameObject;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import danogl.components.CoordinateSpace;

import java.awt.Color;

/**
 * Represents the sky in the game. The sky is a static background object that fills
 * the entire screen with a defined color.
 */
public class Sky {
    private static final Color BASIC_SKY_COLOR = Color.decode("#80C6E5"); // Light blue sky color
    private static final String SKY_TAG = "sky"; // Tag for identifying the sky object

    /**
     * Creates a GameObject representing the sky, which fills the screen with a solid color.
     *
     * @param windowDimensions The dimensions of the game window.
     * @return A GameObject representing the sky.
     */
    public static GameObject create(Vector2 windowDimensions) {
        // Create the GameObject for the sky
        GameObject sky = new GameObject(
                Vector2.ZERO, // Position: top-left corner
                windowDimensions, // Size: matches the window dimensions
                new RectangleRenderable(BASIC_SKY_COLOR) // Appearance: rectangle with sky color
        );

        // Set the sky to be static and not move with the camera
        sky.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);

        // Assign a tag for debugging or identification
        sky.setTag(SKY_TAG);

        return sky; // Return the sky object
    }
}

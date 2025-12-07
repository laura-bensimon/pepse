package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.*;

/**
 * A class for creating a halo effect around the sun in the game.
 * The halo enhances the visual representation of the sun and dynamically follows its position.
 */
public class SunHalo {
    private static final Color HALO_COLOR = new Color(255, 255, 0, 50); // Semi-transparent yellow
    // for the halo
    private static final float HALO_SCALE = 2.0f; // Scale factor to make the halo larger than the sun

    /**
     * Creates a halo around the sun.
     *
     * @param sun The GameObject representing the sun.
     * @return A GameObject representing the sun's halo.
     */
    public static GameObject create(GameObject sun) {
        // Create an oval renderable for the halo
        OvalRenderable haloRenderable = new OvalRenderable(HALO_COLOR);

        // Create the GameObject for the halo
        GameObject sunHalo = new GameObject(
                Vector2.ZERO, // Initial position (will be updated to follow the sun)
                sun.getDimensions().mult(HALO_SCALE), // Halo dimensions (scaled larger than the sun)
                haloRenderable
        );

        // Set the halo to use camera coordinates
        sunHalo.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);

        // Add a tag to identify this object as the sun's halo
        sunHalo.setTag("sunHalo");

        // Add dynamic behavior to update the halo's position to follow the sun
        sunHalo.addComponent(deltaTime -> sunHalo.setCenter(sun.getCenter()));

        return sunHalo;
    }
}

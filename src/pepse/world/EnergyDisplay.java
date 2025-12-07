package pepse.world;

import danogl.GameObject;
import danogl.gui.rendering.Camera;
import danogl.gui.rendering.TextRenderable;
import danogl.util.Vector2;

import java.awt.*;

/**
 * A class for displaying the current energy level of the player on the screen.
 * The display dynamically updates as the player's energy changes and follows the camera's position.
 */
public class EnergyDisplay extends GameObject {
    private static final Vector2 DISPLAY_SIZE = new Vector2(100, 50); // Size of the energy display
    private static final Vector2 DISPLAY_OFFSET = new Vector2(10, 10); // Offset from the top-left
    // corner of the camera
    private static final Color TEXT_COLOR = Color.BLACK; // Color of the energy text

    private final TextRenderable textRenderable;
    private final EnergyProvider energyProvider;
    private final Camera camera;

    /**
     * Constructor for the EnergyDisplay.
     *
     * @param topLeftCorner  The initial position of the display.
     * @param energyProvider A callback to retrieve the current energy level.
     * @param camera         The camera to ensure the display follows the player's view.
     */
    public EnergyDisplay(Vector2 topLeftCorner, EnergyProvider energyProvider, Camera camera) {
        super(topLeftCorner, DISPLAY_SIZE, null);
        this.energyProvider = energyProvider;
        this.camera = camera;

        // Create a text renderable for displaying energy
        textRenderable = new TextRenderable("Energy: 100%");
        textRenderable.setColor(TEXT_COLOR);
        this.renderer().setRenderable(textRenderable);
    }

    /**
     * Updates the energy display to reflect the current energy level and follows the camera's position.
     *
     * @param deltaTime The time elapsed since the last frame.
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        // Adjust the position of the display to follow the camera
        Vector2 cameraTopLeft = camera.getTopLeftCorner();
        this.setTopLeftCorner(cameraTopLeft.add(DISPLAY_OFFSET));

        // Update the text to reflect the current energy level
        int currentEnergy = energyProvider.getCurrentEnergy();
        textRenderable.setString(currentEnergy + "%");
    }

    /**
     * Functional interface for providing the current energy level.
     */
    @FunctionalInterface
    public interface EnergyProvider {
        /**
         * Retrieves the current energy level.
         *
         * @return The current energy as an integer percentage.
         */
        int getCurrentEnergy();
    }
}

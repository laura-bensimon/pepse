package pepse.world;

import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.gui.rendering.Camera;
import danogl.util.Vector2;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the creation and placement of cloud groups in the game world.
 * Clouds are static objects added to the background layer to enhance visual aesthetics.
 */
public class Cloud {
    private static final int FIRST_CLOUD_X = 100; // X-coordinate of the first cloud
    private static final int SECOND_CLOUD_X = 400; // X-coordinate of the second cloud
    private static final float FIRST_CLOUD_Y_RATIO = 1 / 6f; // Y-position of the first cloud as a ratio
    // of window height
    private static final float SECOND_CLOUD_Y_RATIO = 1 / 4f; // Y-position of the second cloud as a ratio
    // of window height

    private static Camera camera;

    /**
     * Constructs a Cloud instance with a camera for positioning clouds.
     *
     * @param camera The camera object used for rendering purposes.
     */
    public Cloud(Camera camera) {
        this.camera = camera;
    }

    /**
     * Creates two static cloud groups at fixed positions.
     *
     * @param windowDimensions The dimensions of the window to determine cloud positions.
     * @param gameObjects The collection of game objects to which the clouds are added.
     * @return A list of CloudGroups representing the generated clouds.
     */
    public static List<CloudGroup> create(Vector2 windowDimensions, GameObjectCollection gameObjects) {
        List<CloudGroup> cloudGroups = new ArrayList<>();

        // Position for the first cloud
        Vector2 position1 = new Vector2(FIRST_CLOUD_X, windowDimensions.y() * FIRST_CLOUD_Y_RATIO);
        CloudGroup cloudGroup1 = new CloudGroup(position1, windowDimensions, gameObjects, camera);
        cloudGroups.add(cloudGroup1);
        gameObjects.addGameObject(cloudGroup1, Layer.BACKGROUND);

        // Position for the second cloud
        Vector2 position2 = new Vector2(SECOND_CLOUD_X, windowDimensions.y() * SECOND_CLOUD_Y_RATIO);
        CloudGroup cloudGroup2 = new CloudGroup(position2, windowDimensions, gameObjects, camera);
        cloudGroups.add(cloudGroup2);
        gameObjects.addGameObject(cloudGroup2, Layer.BACKGROUND);

        return cloudGroups;
    }
}

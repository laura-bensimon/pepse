package pepse.world;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.Camera;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents a group of cloud blocks that collectively form a cloud structure.
 * The cloud moves in a horizontal looping pattern and can generate rain when triggered.
 */
public class CloudGroup extends GameObject implements JumpObserver {
    private static final Color BASE_CLOUD_COLOR = new Color(255, 255, 255); // Default cloud color
    private static final int BLOCK_SIZE = 30; // Size of individual cloud blocks
    private static final float MIN_CLOUD_SPEED = 40f; // Minimum horizontal speed of clouds
    private static final float MAX_CLOUD_SPEED = 60f; // Maximum horizontal speed of clouds
    private static final int NUM_RAIN_DROPS = 6; // Number of raindrops generated in a rain group
    private static final int RAIN_SPREAD_RADIUS_X = 100; // Horizontal spread radius for raindrops
    private static final int RAIN_SPREAD_RADIUS_Y = 60; // Vertical spread radius for raindrops

    private static final List<List<Integer>> CLOUD_SHAPE = List.of(
            List.of(0, 1, 1, 0, 0, 0),
            List.of(1, 1, 1, 1, 0, 0),
            List.of(0, 1, 1, 1, 1, 0),
            List.of(0, 0, 1, 1, 0, 0)
    );

    private final GameObjectCollection gameObjects;
    private final List<GameObject> cloudBlocks;
    private Transition<Vector2> transition;
    private final Camera camera;

    /**
     * Constructs a CloudGroup object that manages a collection of cloud blocks and their movement.
     *
     * @param basePosition     The initial position of the cloud group.
     * @param windowDimensions The dimensions of the game window.
     * @param gameObjects      The collection of game objects to add the cloud blocks.
     * @param camera           The camera used for calculating transitions.
     */
    public CloudGroup(Vector2 basePosition, Vector2 windowDimensions, GameObjectCollection gameObjects,
                      Camera camera) {
        super(basePosition, calculateSize(), null);
        this.setCoordinateSpace(CoordinateSpace.WORLD_COORDINATES);
        this.gameObjects = gameObjects;
        this.cloudBlocks = createCloudBlocks(gameObjects);
        this.camera = camera;
        addTransition(windowDimensions);
    }

    /**
     * Triggered when an avatar jumps, generating a group of raindrops below the cloud.
     */
    @Override
    public void onJump() {
        Vector2 cloudPosition = this.getTopLeftCorner();
        createRainGroup(cloudPosition, gameObjects);
    }

    /**
     * Calculates the total size of the cloud group based on the predefined shape.
     *
     * @return The size of the cloud group.
     */
    private static Vector2 calculateSize() {
        int width = CLOUD_SHAPE.get(0).size() * BLOCK_SIZE;
        int height = CLOUD_SHAPE.size() * BLOCK_SIZE;
        return new Vector2(width, height);
    }

    /**
     * Creates the individual blocks of the cloud based on the predefined shape.
     *
     * @param gameObjects The collection of game objects to add the blocks.
     * @return A list of GameObjects representing the cloud blocks.
     */
    private List<GameObject> createCloudBlocks(GameObjectCollection gameObjects) {
        List<GameObject> blocks = new ArrayList<>();

        for (int row = 0; row < CLOUD_SHAPE.size(); row++) {
            for (int col = 0; col < CLOUD_SHAPE.get(row).size(); col++) {
                if (CLOUD_SHAPE.get(row).get(col) == 1) {
                    Vector2 relativePosition = new Vector2(
                            col * BLOCK_SIZE,
                            row * BLOCK_SIZE
                    );
                    Vector2 blockPosition = this.getTopLeftCorner().add(relativePosition);

                    // Use ColorSupplier to generate a color close to the base color
                    Color blockColor = ColorSupplier.approximateColor(BASE_CLOUD_COLOR);

                    GameObject block = new Block(blockPosition, new RectangleRenderable(blockColor));
                    block.setTag("CloudBlock");
                    blocks.add(block);
                    gameObjects.addGameObject(block, Layer.BACKGROUND);
                }
            }
        }

        return blocks;
    }

    /**
     * Adds a transition to move the cloud horizontally in a looping pattern.
     *
     * @param windowDimensions The dimensions of the game window for calculating positions.
     */
    private void addTransition(Vector2 windowDimensions) {
        Random random = new Random();
        float speed = MIN_CLOUD_SPEED + random.nextFloat() * (MAX_CLOUD_SPEED - MIN_CLOUD_SPEED);

        float travelTime = (windowDimensions.x() + CLOUD_SHAPE.get(0).size() * BLOCK_SIZE) / speed;

        Vector2 offScreenRight = new Vector2(
                camera.getTopLeftCorner().x() + 2 * windowDimensions.x(),
                this.getTopLeftCorner().y()
        );

        Vector2 offScreenLeft = new Vector2(
                camera.getTopLeftCorner().x() - 2 * windowDimensions.x(),
                this.getTopLeftCorner().y()
        );

        transition = new Transition<>(
                this,
                this::updatePosition,
                offScreenLeft,
                offScreenRight,
                Transition.LINEAR_INTERPOLATOR_VECTOR,
                travelTime,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null
        );
    }

    /**
     * Updates the base position of the cloud and repositions all cloud blocks accordingly.
     *
     * @param newBasePosition The new base position of the cloud.
     */
    private void updatePosition(Vector2 newBasePosition) {
        Vector2 delta = newBasePosition.subtract(this.getTopLeftCorner());
        super.setTopLeftCorner(newBasePosition);

        for (GameObject block : cloudBlocks) {
            Vector2 currentPos = block.getTopLeftCorner();
            block.setTopLeftCorner(currentPos.add(delta));
        }
    }

    /**
     * Generates a group of raindrops below the cloud with a random spread pattern.
     *
     * @param cloudPosition The position of the cloud.
     * @param gameObjects   The collection of game objects to add the raindrops.
     */
    public void createRainGroup(Vector2 cloudPosition, GameObjectCollection gameObjects) {
        Random random = new Random();

        float bottomOfCloud = cloudPosition.y() + BLOCK_SIZE * CLOUD_SHAPE.size();

        for (int i = 0; i < NUM_RAIN_DROPS; i++) {
            float xOffset = random.nextFloat() * RAIN_SPREAD_RADIUS_X - RAIN_SPREAD_RADIUS_X / 2;
            float yOffset = random.nextFloat() * RAIN_SPREAD_RADIUS_Y;

            Vector2 rainDropPosition = new Vector2(
                    cloudPosition.x() + xOffset,
                    bottomOfCloud + yOffset
            );

            RainDrop rainDrop = new RainDrop(rainDropPosition, gameObjects);
            gameObjects.addGameObject(rainDrop, Layer.BACKGROUND - 1);
        }
    }
}

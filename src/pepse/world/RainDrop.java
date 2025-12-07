package pepse.world;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.collisions.GameObjectCollection;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import java.awt.*;

/**
 * Represents a raindrop in the game. Raindrops fall under the influence of gravity,
 * fade out over time, and are removed when they become fully transparent or collide with the ground.
 */
public class RainDrop extends GameObject {
    private static final float GRAVITY = 450f; // Acceleration due to gravity applied to the raindrop
    private static final float FADE_SPEED = 0.6f; // Speed at which the raindrop becomes transparent
    private static final Vector2 DROP_SIZE = new Vector2(7, 7); // Dimensions of the raindrop
    private static final Color DROP_COLOR = new Color(20, 98, 159); // Color of the raindrop (blue)
    private static final String RAIN_DROP_TAG = "RainDrop"; // Tag to identify the raindrop
    private static final String BLOCK_TAG = "Block"; // Tag to identify terrain blocks

    private final GameObjectCollection gameObjects; // Reference to the collection for removing the raindrop

    /**
     * Constructs a new raindrop object.
     *
     * @param position     The starting position of the raindrop.
     * @param gameObjects  The collection of game objects to manage the raindrop's lifecycle.
     */
    public RainDrop(Vector2 position, GameObjectCollection gameObjects) {
        super(position, DROP_SIZE, new RectangleRenderable(DROP_COLOR));
        this.gameObjects = gameObjects;
        transform().setAccelerationY(GRAVITY); // Apply gravity to the raindrop
        setTag(RAIN_DROP_TAG); // Tag the raindrop for identification
    }

    /**
     * Updates the state of the raindrop, reducing its opacity over time and removing it
     * when it becomes fully transparent.
     *
     * @param deltaTime Time elapsed since the last frame.
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        // Gradually decrease the opacity of the raindrop
        float newOpacity = renderer().getOpaqueness() - FADE_SPEED * deltaTime;
        renderer().setOpaqueness(Math.max(0, newOpacity));

        // Remove the raindrop if it becomes completely transparent
        if (newOpacity <= 0) {
            gameObjects.removeGameObject(this);
        }
    }

    /**
     * Handles collisions between the raindrop and other game objects.
     * If the raindrop collides with a terrain block, it is removed.
     *
     * @param other     The other game object involved in the collision.
     * @param collision The collision details.
     */
    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        // Check if the collision is with a terrain block
        if (other.getTag() != null && other.getTag().equals(BLOCK_TAG)) {
            // Remove the raindrop when it hits the ground
            gameObjects.removeGameObject(this);
        }
    }
}

package pepse.world.trees;

import danogl.GameObject;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;
import pepse.world.Avatar;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Represents a fruit in the game. Fruits can be consumed by the avatar to gain energy
 * and will reappear after a defined period.
 */
public class Fruit extends GameObject {
    private static final int ENERGY_GAIN = 10; // Energy gained by the avatar when consuming the fruit
    private static final float REAPPEAR_TIME = 30f; // Time (in seconds) before the fruit reappears after
    // being eaten
    private static final Color FRUIT_COLOR = new Color(255, 0, 0); // Color of the fruit
    private static final Vector2 FRUIT_SIZE = new Vector2(15, 15); // Size of the fruit

    private boolean isEaten = false; // Indicates if the fruit has been eaten

    /**
     * Constructs a new fruit object.
     *
     * @param topLeftCorner The top-left corner position of the fruit.
     */
    public Fruit(Vector2 topLeftCorner) {
        super(topLeftCorner, FRUIT_SIZE, new OvalRenderable(FRUIT_COLOR));
        setTag("fruit"); // Tag to identify fruit objects
    }

    /**
     * Gets the amount of energy the fruit provides when consumed.
     *
     * @return The energy gain from the fruit.
     */
    public int getEnergyGain() {
        return ENERGY_GAIN;
    }

    /**
     * Handles collisions with other game objects.
     * If the collision is with the avatar, the fruit will be consumed and provide energy.
     *
     * @param other     The other game object involved in the collision.
     * @param collision The collision details.
     */
    @Override
    public void onCollisionEnter(GameObject other, danogl.collisions.Collision collision) {
        // Ignore collisions if the fruit is already eaten or the other object is not the avatar
        if (isEaten || other.getTag() == null || !other.getTag().equals("avatar")) {
            return;
        }

        if (other instanceof Avatar avatar) {
            avatar.addEnergy(ENERGY_GAIN); // Add energy to the avatar
        }

        consume(); // Consume the fruit
    }

    /**
     * Consumes the fruit, making it invisible and temporarily disabling its functionality.
     * The fruit will reappear after a specified time.
     */
    public void eat() {
        if (isEaten) {
            return; // If already eaten, do nothing
        }

        consume(); // Consume the fruit
    }

    /**
     * Restores the fruit, making it visible and functional again.
     */
    public void reappear() {
        isEaten = false;
        renderer().setOpaqueness(1); // Make the fruit visible
        setTag("fruit"); // Restore the tag to allow collisions
    }

    /**
     * Handles the consumption of the fruit by setting it as eaten,
     * making it invisible, and scheduling its reappearance.
     */
    private void consume() {
        isEaten = true;
        renderer().setOpaqueness(0); // Make the fruit invisible
        setTag(null); // Remove the tag to prevent further collisions

        // Schedule the fruit to reappear after a delay
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                reappear(); // Restore the fruit after the delay
            }
        }, (long) (REAPPEAR_TIME * 1000)); // Convert reappear time to milliseconds
    }
}

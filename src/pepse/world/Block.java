package pepse.world;

import danogl.GameObject;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import danogl.components.GameObjectPhysics;

/**
 * Represents a single block in the game world.
 * Blocks are static, immovable objects that serve as building elements
 * for the terrain and other structures in the game.
 */
public class Block extends GameObject {
    /**
     * The constant size of a block in pixels.
     */
    public static final int SIZE = 30;

    /**
     * Constructs a new Block instance.
     *
     * @param topLeftCorner The top-left corner position of the block in the game world.
     * @param renderable The renderable object used to represent the block visually.
     */
    public Block(Vector2 topLeftCorner, Renderable renderable) {
        // Call the superclass constructor to initialize the GameObject.
        super(topLeftCorner, Vector2.ONES.mult(SIZE), renderable);

        // Prevent intersections from all directions.
        physics().preventIntersectionsFromDirection(Vector2.ZERO);

        // Set the block to be immovable in the game world.
        physics().setMass(GameObjectPhysics.IMMOVABLE_MASS);
    }
}

package pepse.world.trees;

import danogl.GameObject;
import danogl.components.GameObjectPhysics;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;
import pepse.world.Block;

import java.awt.*;
import java.util.Random;

/**
 * A class representing a tree, including its trunk, leaves, and fruits.
 * The tree is generated with random leaves and fruits distributed around the trunk.
 */
public class Tree {
    private static final int LEAF_COUNT = 50; // Number of leaves per tree
    private static final int FRUIT_COUNT = 3; // Number of fruits per tree
    private static final Vector2 LEAF_SIZE = new Vector2(20, 20); // Size of leaves
    private static final Vector2 FRUIT_SIZE = new Vector2(15, 15); // Size of fruits
    private static final Color TRUNK_COLOR = new Color(100, 50, 20); // Color of the tree trunk
    private static final Color LEAF_COLOR = new Color(50, 200, 30); // Base color of leaves
    private static final Color FRUIT_COLOR = new Color(255, 0, 0); // Color of the fruits (red)
    private static final int MIN_TRUNK_HEIGHT = 2; // Minimum height of the trunk
    private static final int MAX_TRUNK_HEIGHT = 5; // Maximum height of the trunk
    private static final float TRUNK_BLOCK_WIDTH_RATIO = 0.5f; // Width ratio of the trunk block
    private static final float LEAF_MOVEMENT_ANGLE = 15f; // Maximum angle for leaf movement
    private static final float LEAF_ANIMATION_DURATION_BASE = 1f; // Base duration for leaf animation

    /**
     * Creates a tree consisting of a trunk, leaves randomly distributed around the trunk, and fruits.
     *
     * @param position    The base position of the trunk.
     * @param trunkHeight The height of the trunk.
     * @param leafSize    The size of the leaves (configurable but not directly used in this method).
     * @return A 2D array of GameObjects representing the trunk, leaves, and fruits.
     */
    public static GameObject[][] create(Vector2 position, float trunkHeight, Vector2 leafSize) {
        // Limit trunkHeight between MIN_TRUNK_HEIGHT and MAX_TRUNK_HEIGHT
        trunkHeight = Math.max(MIN_TRUNK_HEIGHT, Math.min(trunkHeight, MAX_TRUNK_HEIGHT));

        GameObject[] trunkParts = new GameObject[(int) trunkHeight];
        GameObject[] leafParts = new GameObject[LEAF_COUNT];
        GameObject[] fruitParts = new GameObject[FRUIT_COUNT];

        Random random = new Random();

        // Create the trunk
        for (int i = 0; i < trunkHeight; i++) {
            Vector2 trunkPosition = position.add(new Vector2(0, -i * Block.SIZE));
            GameObject trunkBlock = new GameObject(
                    trunkPosition,
                    new Vector2(Block.SIZE * TRUNK_BLOCK_WIDTH_RATIO, Block.SIZE),
                    new RectangleRenderable(TRUNK_COLOR)
            );
            trunkBlock.setTag("tree_trunk");
            trunkBlock.physics().preventIntersectionsFromDirection(Vector2.ZERO);
            trunkBlock.physics().setMass(GameObjectPhysics.IMMOVABLE_MASS); // Make the trunk immovable
            trunkParts[i] = trunkBlock;
        }

        // Create the leaves
        for (int i = 0; i < LEAF_COUNT; i++) {
            // Random position around the trunk
            float xOffset = -Block.SIZE * 2 + random.nextFloat() * Block.SIZE * 4; // Horizontal spread
            float yOffset = -Block.SIZE * (random.nextInt((int) trunkHeight) + 2); // Vertical spread

            Vector2 leafPosition = position.add(new Vector2(xOffset, yOffset));

            // Use ColorSupplier to generate slightly varied colors for leaves
            Color leafColor = ColorSupplier.approximateColor(LEAF_COLOR);

            GameObject leaf = new GameObject(
                    leafPosition,
                    LEAF_SIZE,
                    new RectangleRenderable(leafColor)
            );
            leaf.setTag("tree_leaf");

            // Add movement animation for leaves
            new Transition<>(
                    leaf,
                    leaf.renderer()::setRenderableAngle,
                    -LEAF_MOVEMENT_ANGLE,
                    LEAF_MOVEMENT_ANGLE,
                    Transition.LINEAR_INTERPOLATOR_FLOAT,
                    LEAF_ANIMATION_DURATION_BASE + random.nextFloat(),
                    Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                    null
            );

            leafParts[i] = leaf;
        }

        // Create the fruits
        for (int i = 0; i < FRUIT_COUNT; i++) {
            int leafIndex = random.nextInt(LEAF_COUNT);
            Vector2 leafPosition = leafParts[leafIndex].getTopLeftCorner();

            // Fruit position slightly offset from the leaf
            Vector2 fruitPosition = leafPosition.add(new Vector2(0, -LEAF_SIZE.y() / 2));
            Fruit fruit = new Fruit(fruitPosition); // Use the Fruit class
            fruitParts[i] = fruit;
        }

        return new GameObject[][]{trunkParts, leafParts, fruitParts};
    }
}

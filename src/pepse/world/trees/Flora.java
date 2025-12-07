package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.util.Vector2;
import pepse.world.Block;
import pepse.world.Terrain;

import java.util.*;

/**
 * Responsible for managing the generation and removal of trees, flowers, and other flora
 * in the game world. Handles flora generation within a specified range and ensures objects
 * outside the visible range are removed to optimize performance.
 */
public class Flora {
    private static final int BASE_TRUNK_HEIGHT = 5; // Base height of a tree trunk
    private static final int TRUNK_HEIGHT_VARIATION = 2; // Maximum variation in tree trunk height
    private static final Vector2 TREE_PART_SIZE = new Vector2(20, 20); // Size of tree parts
    // (trunk, leaves)
    private static final int LEFT_OFFSET = 100; // Left offset for the visible range
    private static final int RIGHT_OFFSET = 100; // Right offset for the visible range

    private final Terrain terrain;
    private final Random random;
    private final List<GameObject> managedObjects;
    private final GameObjectCollection gameObjects;
    private final float treeProbability;
    private final int chunkSize;
    private final Set<Integer> generatedChunks;

    /**
     * Constructs a Flora instance to manage the generation and removal of trees and related objects.
     *
     * @param terrain The terrain object used to determine ground height for tree placement.
     * @param gameObjects The collection of game objects to manage.
     * @param random A random object for generating tree placement and properties.
     * @param treeProbability The probability of a tree being generated at any given x-coordinate.
     * @param chunkSize The size of each chunk for flora generation.
     */
    public Flora(Terrain terrain, GameObjectCollection gameObjects, Random random, float treeProbability,
                 int chunkSize) {
        this.terrain = terrain;
        this.random = random;
        this.gameObjects = gameObjects;
        this.treeProbability = treeProbability;
        this.chunkSize = chunkSize;
        this.managedObjects = new ArrayList<>();
        this.generatedChunks = new HashSet<>();
    }

    /**
     * Generates trees and flowers within the specified range.
     *
     * @param minX The minimum visible x-coordinate.
     * @param maxX The maximum visible x-coordinate.
     */
    public void generateInRange(int minX, int maxX) {
        int startChunk = minX / chunkSize;
        int endChunk = maxX / chunkSize;

        for (int chunk = startChunk; chunk <= endChunk; chunk++) {
            if (generatedChunks.contains(chunk)) {
                continue;
            }

            int chunkMinX = chunk * chunkSize;
            int chunkMaxX = (chunk + 1) * chunkSize;
            for (int x = chunkMinX; x < chunkMaxX; x += Block.SIZE) {
                if (random.nextFloat() < treeProbability) {
                    float groundHeight = terrain.groundHeightAt(x);
                    Vector2 position = new Vector2(x, groundHeight - Block.SIZE);
                    float trunkHeight = BASE_TRUNK_HEIGHT + random.nextInt(TRUNK_HEIGHT_VARIATION);
                    // Random trunk height
                    GameObject[][] treeParts = Tree.create(position, trunkHeight, TREE_PART_SIZE);

                    // Add tree trunks to the game world
                    for (GameObject trunk : treeParts[0]) {
                        if (trunk != null) {
                            gameObjects.addGameObject(trunk, Layer.STATIC_OBJECTS - 1);
                            managedObjects.add(trunk);
                        }
                    }

                    // Add tree leaves to the game world
                    for (GameObject leaf : treeParts[1]) {
                        if (leaf != null) {
                            gameObjects.addGameObject(leaf, Layer.STATIC_OBJECTS + 1);
                            managedObjects.add(leaf);
                        }
                    }

                    // Add fruits to the game world
                    for (GameObject fruit : treeParts[2]) {
                        if (fruit != null) {
                            gameObjects.addGameObject(fruit, Layer.STATIC_OBJECTS + 2);
                            managedObjects.add(fruit);
                        }
                    }
                }
            }
            generatedChunks.add(chunk);
        }
    }

    /**
     * Removes objects that are outside the visible range.
     *
     * @param minX The minimum visible x-coordinate.
     * @param maxX The maximum visible x-coordinate.
     */
    private void removeOutOfRangeChunks(int minX, int maxX) {
        List<GameObject> toRemove = new ArrayList<>();
        for (GameObject obj : managedObjects) {
            float objX = obj.getCenter().x();

            // Remove only if the object is outside the visible range
            if (objX < minX || objX > maxX) {
                toRemove.add(obj);
            }
        }

        for (GameObject obj : toRemove) {
            gameObjects.removeGameObject(obj);
            managedObjects.remove(obj);
        }

        // Clean up out-of-range chunks from the generated set
        generatedChunks.removeIf(chunk -> {
            int chunkMinX = chunk * chunkSize;
            int chunkMaxX = (chunk + 1) * chunkSize;
            return chunkMaxX < minX || chunkMinX > maxX;
        });
    }

    /**
     * Updates the visible objects based on the avatar's current position.
     *
     * @param avatarX The current x-coordinate of the avatar.
     * @param windowWidth The width of the visible window.
     */
    public void update(float avatarX, float windowWidth) {
        int minX = (int) (avatarX - LEFT_OFFSET); // Left boundary of the visible range
        int maxX = (int) (avatarX + windowWidth - RIGHT_OFFSET); // Right boundary of the visible range
        removeOutOfRangeChunks(minX, maxX);
        generateInRange(minX, maxX);
    }
}

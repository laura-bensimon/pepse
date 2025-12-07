package pepse.world;

import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;
import pepse.util.NoiseGenerator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the terrain in the game. The terrain is generated using noise functions
 * to create realistic height variations and supports creating blocks in specified ranges.
 */
public class Terrain {
    private static final Color BASE_GROUND_COLOR = new Color(212, 123, 74); // Base color
    // of the terrain
    private static final int TERRAIN_DEPTH = 20; // Depth of the terrain in blocks
    private static final double NOISE_FACTOR = 200.0; // Factor for adjusting noise variation
    private static final float GROUND_HEIGHT_RATIO = 0.66f; // Ground height as 2/3 of the window height
    private static final String GROUND_TAG = "ground"; // Tag to identify ground blocks

    private final Vector2 windowDimensions;
    private final int seed;
    private final float groundHeightAtX0;
    private final NoiseGenerator noiseGenerator;

    /**
     * Constructs a terrain generator with the specified window dimensions and seed.
     *
     * @param windowDimensions The dimensions of the game window.
     * @param seed             The seed for the pseudo-random noise generator.
     */
    public Terrain(Vector2 windowDimensions, int seed) {
        this.windowDimensions = windowDimensions;
        this.seed = seed;
        this.groundHeightAtX0 = windowDimensions.y() * GROUND_HEIGHT_RATIO; // 2/3 of the window height
        this.noiseGenerator = new NoiseGenerator(seed, (int) groundHeightAtX0);
    }

    /**
     * Returns the height of the terrain at a given X coordinate, including variations from
     * the noise generator.
     *
     * @param x The X coordinate.
     * @return The height of the terrain at the specified X coordinate.
     */
    public float groundHeightAt(float x) {
        float noise = (float) noiseGenerator.noise(x, NOISE_FACTOR); // Generate noise for the X coordinate
        return groundHeightAtX0 + noise; // Combine the base height and the noise variation
    }

    /**
     * Creates a list of blocks representing the terrain within a specified X range.
     *
     * @param minX The minimum X range.
     * @param maxX The maximum X range.
     * @return A list of blocks created within the specified range.
     */
    public List<Block> createInRange(int minX, int maxX) {
        List<Block> blocks = new ArrayList<>();

        // Align minX and maxX to multiples of block size
        int alignedMinX = (int) (Math.floor(minX / (float) Block.SIZE) * Block.SIZE);
        int alignedMaxX = (int) (Math.floor(maxX / (float) Block.SIZE) * Block.SIZE);

        // Iterate through aligned X coordinates
        for (int x = alignedMinX; x <= alignedMaxX; x += Block.SIZE) {
            // Calculate the aligned height of the terrain surface
            float groundHeight = (float) Math.floor(groundHeightAt(x) / Block.SIZE) * Block.SIZE;

            // Generate blocks for the terrain depth
            for (int i = 0; i < TERRAIN_DEPTH; i++) {
                float y = groundHeight + i * Block.SIZE;
                Vector2 blockPosition = new Vector2(x, y);

                // Create a renderable with an approximate color
                Renderable blockRenderable = new RectangleRenderable(
                        ColorSupplier.approximateColor(BASE_GROUND_COLOR)
                );

                // Create the block and add it to the list
                Block block = new Block(blockPosition, blockRenderable);
                block.setTag(GROUND_TAG); // Tag the block as ground
                blocks.add(block);
            }
        }
        return blocks;
    }

    /**
     * Retrieves the seed used for generating the terrain.
     *
     * @return The seed value.
     */
    public int getSeed() {
        return this.seed;
    }
}

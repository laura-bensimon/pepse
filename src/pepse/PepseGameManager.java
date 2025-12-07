package pepse;

import danogl.GameManager;
import danogl.GameObject;
import danogl.collisions.Layer;
import danogl.gui.ImageReader;
import danogl.gui.SoundReader;
import danogl.gui.UserInputListener;
import danogl.gui.WindowController;
import danogl.gui.rendering.Camera;
import danogl.util.Vector2;
import pepse.world.*;
import pepse.world.daynight.Night;
import pepse.world.daynight.Sun;
import pepse.world.daynight.SunHalo;
import pepse.world.trees.Flora;

import java.util.*;

public class PepseGameManager extends GameManager {
    private static final int AVATAR_START_X = 100;
    private static final int AVATAR_START_Y = 450;
    private static final int INITIAL_VISIBLE_CHUNKS = 2;
    // Number of chunks initially visible in both directions
    private static final float DAY_NIGHT_CYCLE_DURATION = 30f;
    // Duration of a full day-night cycle in seconds
    private static final Vector2 SUN_SIZE = new Vector2(100, 100);
    // Dimensions of the sun
    private static final Vector2 ENERGY_DISPLAY_POSITION = new Vector2(10, 10);
    // Position of the energy display
    private static final float TREE_DENSITY = 0.1f;
    // Density of trees in the flora
    private static final int TARGET_FRAMERATE = 60;
    // Target framerate for the game

    private int chunkSize;

    private Terrain terrain;
    private Flora flora;
    private Avatar avatar;
    private Random random;
    private int lastChunk = 0;
    private Set<Integer> generatedChunks = new HashSet<>();
    private Camera camera;
    private List<CloudGroup> activeClouds = new ArrayList<>();

    /**
     * Initializes the game with necessary resources.
     *
     * @param imageReader Provides images.
     * @param soundReader Provides sounds.
     * @param inputListener Handles user input.
     * @param windowController Manages window-related actions.
     */
    @Override
    public void initializeGame(
            ImageReader imageReader,
            SoundReader soundReader,
            UserInputListener inputListener,
            WindowController windowController
    ) {
        super.initializeGame(imageReader, soundReader, inputListener, windowController);
        Vector2 windowDimensions = windowController.getWindowDimensions();

        // Add the sky to the game
        GameObject sky = Sky.create(windowDimensions);
        gameObjects().addGameObject(sky, Integer.MIN_VALUE);

        // Initialize terrain and flora with a fixed seed for consistent generation
        random = new Random(123458); // Fixed seed for predictable results
        terrain = new Terrain(windowDimensions, random.nextInt());
        chunkSize = (int) windowDimensions.x();
        flora = new Flora(terrain, gameObjects(), random, TREE_DENSITY, chunkSize);

        // Generate terrain and objects for the initial visible range
        List<Block> blocks = terrain.createInRange(-INITIAL_VISIBLE_CHUNKS * chunkSize,
                INITIAL_VISIBLE_CHUNKS * chunkSize);
        for (Block block : blocks) {
            block.setTag("Block");
            gameObjects().addGameObject(block, Layer.STATIC_OBJECTS);
        }
        gameObjects().layers().shouldLayersCollide(Layer.DEFAULT, Layer.STATIC_OBJECTS, true);
        flora.generateInRange(-INITIAL_VISIBLE_CHUNKS * chunkSize, INITIAL_VISIBLE_CHUNKS * chunkSize);

        // Create the day-night cycle
        GameObject night = Night.create(windowDimensions, DAY_NIGHT_CYCLE_DURATION);
        gameObjects().addGameObject(night, Layer.FOREGROUND);

        // Create the sun with a circular movement centered on the screen
        GameObject sun = Sun.create(windowDimensions, DAY_NIGHT_CYCLE_DURATION, SUN_SIZE);
        gameObjects().addGameObject(sun, Layer.BACKGROUND);

        // Create the sun's halo effect
        GameObject sunHalo = SunHalo.create(sun);
        gameObjects().addGameObject(sunHalo, Layer.BACKGROUND);

        // Temporary camera creation
        camera = new Camera(
                null, // No target object yet
                Vector2.ZERO,
                windowDimensions,
                windowDimensions
        );

        // Initialize and manage clouds
        Cloud cloudManager = new Cloud(camera);
        List<CloudGroup> clouds = cloudManager.create(windowDimensions, gameObjects());
        activeClouds.addAll(clouds);

        // Create the player's avatar
        avatar = new Avatar(new Vector2(AVATAR_START_X, AVATAR_START_Y), inputListener, imageReader);
        gameObjects().addGameObject(avatar);
        for (CloudGroup cloud : clouds) {
            avatar.addJumpObserver(cloud); // Register each cloud as a jump observer
        }

        // Configure the camera to follow the avatar
        camera = new Camera(
                avatar,
                windowController.getWindowDimensions().mult(0.5f).subtract(new Vector2(AVATAR_START_X,
                        AVATAR_START_Y)),
                windowDimensions,
                windowDimensions
        );
        setCamera(camera);

        // Create an energy display for the avatar
        EnergyDisplay energyDisplay = new EnergyDisplay(
                ENERGY_DISPLAY_POSITION,
                avatar::getEnergy,
                camera
        );
        gameObjects().addGameObject(energyDisplay, Integer.MAX_VALUE); // UI layer
        gameObjects().layers().shouldLayersCollide(Layer.DEFAULT, Layer.STATIC_OBJECTS + 2,
                true); // Avatar and fruits
        gameObjects().layers().shouldLayersCollide(Layer.DEFAULT, Layer.STATIC_OBJECTS - 1,
                true);

        // Set target framerate for smooth gameplay
        windowController.setTargetFramerate(TARGET_FRAMERATE);
    }

    /**
     * Updates the game state on each frame.
     *
     * @param deltaTime Time elapsed since the last frame.
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        int currentChunk = (int) (avatar.getCenter().x() / chunkSize);

        // Generate new chunks if the avatar moves into a new one
        if (currentChunk != lastChunk) {
            int minChunk = currentChunk - 1; // Chunks visible before the avatar
            int maxChunk = currentChunk + 1; // Chunks visible after the avatar

            for (int chunk = minChunk; chunk <= maxChunk; chunk++) {
                if (!generatedChunks.contains(chunk)) {
                    int minX = chunk * chunkSize;
                    int maxX = (chunk + 1) * chunkSize;

                    List<Block> blocks = terrain.createInRange(minX, maxX);
                    for (Block block : blocks) {
                        block.setTag("Block");
                        gameObjects().addGameObject(block, Layer.STATIC_OBJECTS);
                    }
                    flora.generateInRange(minX, maxX);
                    generatedChunks.add(chunk);
                }
            }
            removeOutOfRangeChunks(minChunk, maxChunk);
            lastChunk = currentChunk;
        }
    }

    /**
     * Removes objects from chunks no longer visible.
     *
     * @param minChunk The first visible chunk.
     * @param maxChunk The last visible chunk.
     */
    private void removeOutOfRangeChunks(int minChunk, int maxChunk) {
        List<GameObject> toRemove = new ArrayList<>();
        for (GameObject obj : gameObjects()) {
            float objX = obj.getCenter().x();
            int objChunk = (int) (objX / chunkSize);

            if ((obj.getTag() != null && obj.getTag().equals("Block")) &&
                    (objChunk < minChunk || objChunk > maxChunk)) {
                toRemove.add(obj);
            }
        }
        for (GameObject obj : toRemove) {
            gameObjects().removeGameObject(obj);
        }
        generatedChunks.removeIf(chunk -> chunk < minChunk || chunk > maxChunk);
    }

    /**
     * Main method to run the game.
     */
    public static void main(String[] args) {
        new PepseGameManager().run();
    }
}

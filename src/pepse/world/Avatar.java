package pepse.world;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.gui.ImageReader;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.AnimationRenderable;
import danogl.util.Vector2;
import pepse.world.trees.Fruit;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the player's avatar with animations, energy management, and movement mechanics.
 * The avatar interacts with other objects and performs actions such as running and jumping.
 */
public class Avatar extends GameObject {
    private static final float GRAVITY = 600f; // Gravity applied to the avatar
    private static final float VELOCITY_X = 200f; // Horizontal movement speed
    private static final float VELOCITY_Y = -450f; // Jump velocity
    private static final int MAX_ENERGY = 100; // Maximum energy of the avatar
    private static final float ENERGY_GAIN_IDLE = 1f; // Energy gained when idle
    private static final float ENERGY_LOSS_RUN = 0.5f; // Energy lost when running
    private static final int ENERGY_LOSS_JUMP = 10; // Energy lost when jumping
    private static final Vector2 AVATAR_SIZE = Vector2.ONES.mult(50); // Size of the avatar
    private static final float REGENERATION_DELAY = 0.5f; // Delay before energy regeneration starts
    private static final String FRUIT_TAG = "fruit"; // Tag for identifying fruit objects

    private static GameObjectCollection gameObjectCollection;
    private boolean isWaitingForEnergyRegeneration = false; // Indicates if the avatar is waiting
    // to regenerate energy
    private float timeSinceLastAction = 0; // Time since the last action
    private boolean isInAir = false; // Indicates if the avatar is in the air

    private final UserInputListener inputListener;
    private int energy;

    private AnimationRenderable idleAnimation;
    private AnimationRenderable runAnimation;
    private AnimationRenderable jumpAnimation;

    // List of observers for jump notifications
    private final List<JumpObserver> jumpObservers;

    /**
     * Constructs the Avatar with animations and energy management.
     *
     * @param topLeftCorner The initial position of the avatar.
     * @param inputListener The listener for user inputs.
     * @param imageReader   The reader for loading image resources.
     */
    public Avatar(Vector2 topLeftCorner, UserInputListener inputListener, ImageReader imageReader) {
        super(
                topLeftCorner,
                Vector2.ONES.mult(50), // avatar size
                imageReader.readImage("assets/idle_0.png", true) //base idle imge
        );
        this.inputListener = inputListener;
        this.energy = MAX_ENERGY;
        this.jumpObservers = new ArrayList<>();// Initialize the jump observers list
        // Apply gravity to the avatar
        transform().setAccelerationY(GRAVITY);
        // Prevent collisions from all directions
        physics().preventIntersectionsFromDirection(Vector2.ZERO);
        // Initialize animations
        setupAnimations(imageReader);
        renderer().setRenderable(idleAnimation);
    }


    /**
     * Sets the game object collection for managing the game objects.
     *
     * @param gameObjectCollection The collection of game objects.
     */
    public static void setGameObjectCollection(GameObjectCollection gameObjectCollection) {
        Avatar.gameObjectCollection = gameObjectCollection;
    }

    /**
     * Configures the avatar's animations (idle, run, jump).
     *
     * @param imageReader The reader for loading image resources.
     */
    private void setupAnimations(ImageReader imageReader) {
        idleAnimation = createAnimation(
                new String[]{"assets/idle_0.png", "assets/idle_1.png", "assets/idle_2.png",
                        "assets/idle_3.png"},
                imageReader,
                0.5 // Durée entre les frames (Idle)
        );
        runAnimation = createAnimation(
                new String[]{"assets/run_0.png", "assets/run_1.png", "assets/run_2.png",
                        "assets/run_3.png", "assets/run_4.png", "assets/run_5.png"},
                imageReader,
                0.2 // Durée entre les frames (Run)
        );
        jumpAnimation = createAnimation(
                new String[]{"assets/jump_0.png", "assets/jump_1.png", "assets/jump_2.png",
                        "assets/jump_3.png"},
                imageReader,
                0.3 // Durée entre les frames (Jump)
        );
    }

    /**
     * Creates an animation renderable using the given image paths.
     *
     * @param imagePaths   Array of image paths for the animation.
     * @param imageReader  The reader for loading image resources.
     * @param frameDuration The duration of each frame in the animation.
     * @return An AnimationRenderable object.
     */
    private AnimationRenderable createAnimation(String[] imagePaths, ImageReader imageReader,
                                                double frameDuration) {
        return new AnimationRenderable(imagePaths, imageReader, true, frameDuration);
    }

    /**
     * Updates the avatar's state, including movement, energy management, and animations.
     *
     * @param deltaTime The time elapsed since the last frame.
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        // Check if the avatar has landed on the ground
        if (getVelocity().y() == 0 && isInAir) {
            isInAir = false; // L'avatar a retouché le sol
        }

        // Handle energy depletion
        if (energy <= 0) {
            transform().setVelocityX(0); // Empêche les mouvements
            renderer().setRenderable(idleAnimation); // Animation Idle par défaut

            // Incrémenter le temps d'inactivité
            timeSinceLastAction += deltaTime;

            // Si le temps d'inactivité dépasse le délai, commencer la régénération
            if (timeSinceLastAction >= REGENERATION_DELAY && !isInAir) {
                regenerateEnergy();
            }

            return; // Sortir pour éviter les autres actions
        }
        // Reset idle timer since an action occurred
        timeSinceLastAction = 0;

        boolean isMoving = false;

        // Handle horizontal movement
        float xVel = handleHorizontalMovement();
        isMoving = xVel != 0;

        transform().setVelocityX(xVel);

        // Réinitialise à l'animation Idle si l'avatar ne bouge pas horizontalement
        if (!isMoving && getVelocity().x() == 0) {
            setIdleAnimation();
        }

        // Gestion du saut
        handleJump();

        // Mise à jour de l'énergie (uniquement si l'avatar n'est pas en l'air)
        if (!isInAir) {
            updateEnergy(isMoving, false);
        }
    }



    private boolean handleLowEnergy() {
        if (energy <= 0.5f) {
            transform().setVelocityX(0); // Empêche les mouvements
            renderer().setRenderable(idleAnimation); // Animation Idle par défaut
            regenerateEnergy(); // Permet de régénérer l'énergie
            return true; // Arrête le reste de la logique pour cette frame
        }
        return false;
    }

    /**
     * Handles horizontal movement based on user input.
     *
     * @return The horizontal velocity of the avatar.
     */
    private float handleHorizontalMovement() {
        float xVel = 0;

        // Vérifie si les deux touches sont appuyées (gauche et droite), aucun mouvement
        boolean leftPressed = inputListener.isKeyPressed(KeyEvent.VK_LEFT);
        boolean rightPressed = inputListener.isKeyPressed(KeyEvent.VK_RIGHT);

        if (leftPressed && rightPressed) {
            return 0; // Aucun mouvement
        }

        if (leftPressed) {
            xVel -= VELOCITY_X;
            setRunAnimation(true); // Définit l'animation Run avec inversion horizontale
        }
        if (rightPressed) {
            xVel += VELOCITY_X;
            setRunAnimation(false); // Définit l'animation Run sans inversion
        }

        return xVel;
    }


    @Override
    public String getTag() {
        return "avatar";
    }

    /**
     * Sets the Run animation based on the direction.
     * @param isFlippedHorizontally true if the animation should be flipped horizontally.
     */
    private void setRunAnimation(boolean isFlippedHorizontally) {
        renderer().setRenderable(runAnimation);
        renderer().setIsFlippedHorizontally(isFlippedHorizontally);
    }

    /**
     * Define Idle animation.
     */
    private void setIdleAnimation() {
        renderer().setRenderable(idleAnimation); // Animation Idle
    }

    /**
     * Handles the avatar's jump action.
     *
     * @return True if the avatar jumps, false otherwise.
     */
    private boolean handleJump() {
        if (inputListener.isKeyPressed(KeyEvent.VK_SPACE) && getVelocity().y() == 0 && energy >=
                ENERGY_LOSS_JUMP) {
            transform().setVelocityY(VELOCITY_Y); // Applique une vélocité verticale
            energy = Math.max(0, energy - ENERGY_LOSS_JUMP); // Réduit l'énergie pour le saut
            isInAir = true; // L'avatar est maintenant en l'air
            renderer().setRenderable(jumpAnimation); // Animation de saut
            notifyJumpObservers(); // Notifie les observateurs de saut
            return true;
        }
        return false;
    }


    /**
     * Updates the avatar's energy based on movement or idle state.
     *
     * @param isMoving  True if the avatar is moving, false otherwise.
     * @param isJumping True if the avatar is jumping, false otherwise.
     */
    private void updateEnergy(boolean isMoving, boolean isJumping) {
        if (isInAir) {
            return; // Pas de mise à jour de l'énergie lorsque l'avatar est en l'air
        }
        if (isJumping) {
            energy = Math.max(0, energy - ENERGY_LOSS_JUMP); // Réduit l'énergie pour un saut
        } else if (isMoving) {
            energy = (int) Math.max(0, energy - ENERGY_LOSS_RUN); // Réduit l'énergie pour un mouvement
            // horizontal
        } else {
            regenerateEnergy(); // Régénérer l'énergie si Idle
        }
    }


    /**
     * Regenerates the avatar's energy when idle.
     */
    private void regenerateEnergy() {
        energy = (int) Math.min(MAX_ENERGY, energy + ENERGY_GAIN_IDLE); // Augmente l'énergie
        // jusqu'à MAX_ENERGY
      //  System.out.println("Énergie régénérée : " + energy);
    }


    /**
     * Gets the avatar's current energy.
     *
     * @return The current energy of the avatar.
     */
    public int getEnergy() {
        return energy;
    }

    /**
     * Let other objects give some energy to avatar.
     */
    public void addEnergy(int amount) {
        energy = Math.min(MAX_ENERGY, energy + amount);

    }

    /**
     * Notifies all jump observers when the avatar jumps.
     */
    private void notifyJumpObservers() {
        for (JumpObserver observer : jumpObservers) {
            observer.onJump();
        }
    }
    /**
     * Adds a jump observer to the list of observers.
     *
     * @param observer The observer to add.
     */
    public void addJumpObserver(JumpObserver observer) {

        jumpObservers.add(observer);
    }

    /**
     * Handles collisions between the avatar and other game objects.
     *
     * @param other     The other game object involved in the collision.
     * @param collision The collision details.
     */
    @Override
    public void onCollisionEnter(GameObject other, danogl.collisions.Collision collision) {
        if (other.getTag() != null && other.getTag().equals("fruit")) { // Vérifie si c'est un fruit
            if (other instanceof Fruit fruit) { // Si c'est une instance de Fruit
                this.addEnergy(fruit.getEnergyGain()); // Ajouter de l'énergie à l'avatar
                fruit.eat(); // Appeler la méthode pour rendre le fruit invisible et gérer sa réapparition
            }
        }
    }

}

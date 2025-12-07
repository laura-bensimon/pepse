package pepse.world;

/**
 * An interface for jump observers.
 * Classes that implement this interface must define actions to perform when a jump occurs.
 */
public interface JumpObserver {
    /**
     * This method is called whenever a jump action is performed by the avatar.
     */
    void onJump();
}

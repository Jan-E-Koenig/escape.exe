package at.ac.tuwien.mmue_sb10;

/**
 * Shows the current state of the player. Used mainly to play the proper animation
 * @author Lukas Lidauer
 * @since 1.0
 */
public enum PlayerState {
    IDLE,
    WAKEUP,
    RUNNING,
    GRAVITY,
    JUMPING,
    START_END_JUMP,
    DYING
}

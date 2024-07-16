package at.ac.tuwien.mmue_sb10.persistence;

/**
 * Listener Interface for Users DB Actions
 * @since 1.0
 * @author Lukas Lidauer & Jan König
 */
public interface OnUserLoadedListener {
    /**
     * Callback when User has been loaded
     * @param user User that has been loaded
     * @since 1.0
     */
    void onUserLoaded(User user);
}

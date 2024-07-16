package at.ac.tuwien.mmue_sb10.persistence;

import java.util.List;

/**
 * Listener Interface for the Highscores DB actions
 * @since 1.0
 * @author Lukas Lidauer & Jan König
 */
public interface OnHighscoresLoadedListener {
    /**
     * Callback when Highscores have been loaded
     * @since 1.0
     */
    void onHighscoresLoaded(List<Highscore> highscores);
}

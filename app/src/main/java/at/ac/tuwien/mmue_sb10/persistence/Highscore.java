// source: PersistentStorageExample2021

package at.ac.tuwien.mmue_sb10.persistence;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Highscore database class
 * @since 1.0
 * @author Lukas Lidauer & Jan König
 */
@Entity(tableName = "highscores")
public class Highscore {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public int level;
    public int deaths;

    /**
     * Creates an Highscore object
     * @param name The name of the current file/player
     * @param level The level the player currently is at
     * @param deaths The total amount of deaths
     * @since 1.0
     */
    public Highscore(String name, int level, int deaths) {
        this.name = name;
        this.level = level;
        this.deaths = deaths;
    }
}

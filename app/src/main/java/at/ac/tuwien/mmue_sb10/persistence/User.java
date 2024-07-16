// source: PersistentStorageExample2021

package at.ac.tuwien.mmue_sb10.persistence;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * User class (object as handled via DB)
 * @since 1.0
 * @author Lukas Lidauer & Jan König
 */
@Entity(tableName = "users")
public class User implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public int currentLevel;
    public int deathsTotal;
    public int deathsCurrentLevel;

    /**
     * Basic Constructor for this class
     * @since 1.0
     */
    public User() {}

    /**
     * Constructor for this class. Creates name for the current user as delived by parameter and checks if Tutorial is going to be played or not
     * @param name The name of the current file/player
     * @param tutorial if true tutorial levels are going to be played
     * @since 1.0
     */
    public User(String name, boolean tutorial) {
        this.name = name;
        this.deathsCurrentLevel = 0;
        this.deathsTotal = 0;

        if (tutorial)
            this.currentLevel = -5;
        else
            this.currentLevel = 1;
    }
}
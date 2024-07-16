// source: PersistentStorageExample2021

package at.ac.tuwien.mmue_sb10.persistence;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * DAO for the user database
 * @since 1.0
 * @author Lukas Lidauer & Jan König
 */
@Dao
public interface UserDao {
    /**
     * DAO for inserting an user into the DB
     * @param user The name of the current file/player
     * @since 1.0
     */
    @Insert
    void insert(User user);

    /**
     * DAO for updating an already existing user in the DB
     * @param user The name of the to be updated file/player
     * @since 1.0
     */
    @Update
    void update(User user);

    /**
     * Returns list of users from DB
     * @since 1.0
     */
    @Query("SELECT * FROM users")
    List<User> selectAllUsers();

    /**
     * Deletes users from DB (there will be always be either 0 or 1 users present in the DB, highscores are stored seperately)
     * @since 1.0
     */
    @Query("DELETE FROM users")
    void deleteAllUsers();
}
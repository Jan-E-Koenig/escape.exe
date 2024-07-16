/**
 * Main activity of the game currently works as a main menu.
 *
 * @author Lukas Lidauer & Jan König
 */
package at.ac.tuwien.mmue_sb10;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.res.ResourcesCompat;

import java.util.List;

import at.ac.tuwien.mmue_sb10.persistence.EscapeDatabase;
import at.ac.tuwien.mmue_sb10.persistence.OnUserLoadedListener;
import at.ac.tuwien.mmue_sb10.persistence.User;
import at.ac.tuwien.mmue_sb10.util.Concurrency;

/**
 * This class handles the main menu of the game (where you can chose between New Game, Continue, Highscores and Exit)
 * @since 0.1
 * @author Lukas Lidauer & Jan König
 */
public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private final OnUserLoadedListener onUserLoadedListener = this::onUserLoaded;

    public static boolean show_thanks = false;

    private User user;
    private TextView mmenu_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mmenu_text = findViewById(R.id.mmenu_text);
        mmenu_text.setText("");
        mmenu_text.setBackgroundColor(getResources().getColor(R.color.orange));

        EscapeSoundManager.getInstance(this).initSoundPool();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EscapeSoundManager.getInstance(this).release();
    }

    @Override
    protected void onResume() {
        super.onResume();

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        if (EscapeSoundManager.getInstance(this).isMuted()) {
            findViewById(R.id.btn_mute).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.icon_mute, null));
        } else {
            findViewById(R.id.btn_mute).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.icon_sound, null));
            EscapeSoundManager.getInstance(this).initMediaPlayer(R.raw.mmenu_bgmusic, true);
        }

        EscapeSoundManager.getInstance(this).lock();

        Concurrency.executeAsync(() -> {
            User user = loadUser();
            runOnUiThread(() -> onUserLoadedListener.onUserLoaded(user));
        });
    }

    @Override
    public void onBackPressed() {
        EscapeSoundManager.getInstance(this).release();
        finishAffinity();
    }

    /**
     * When clicked starts a new game by creating a new player profile first.
     * @param v the view as used by this method
     * @since 0.1
     */
    public void onClickNewGame(View v) {
        EscapeSoundManager.getInstance(this).playSound(EscapeSoundManager.getInstance(this).snd_button);

        show_thanks = false;

        mmenu_text.setText(R.string.new_game);
        mmenu_text.setBackgroundColor(getResources().getColor(R.color.green));
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(dm);
        float aspect_rounded = Math.round((float)dm.widthPixels / dm.heightPixels * 10) / 10f;
        Bundle bundle;
        if (aspect_rounded == Math.round(16f/9 * 10) / 10f) {
            bundle = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.fade_in_activity, R.anim.enlarge_main_activity_wide).toBundle();
        } else {
            bundle = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.fade_in_activity, R.anim.enlarge_main_activity_xwide).toBundle();
        }
        Intent intent = new Intent(this, SubNewActivity.class);
        intent.putExtra("user", user);
        startActivity(intent, bundle);
    }

    /**
     * When MuteButton has been clicked mutes the game
     * @param v MuteButton View that has been clicked
     * @since 1.0
     */
    public void onClickMute(View v) {
        EscapeSoundManager.getInstance(this).unlock();
        EscapeSoundManager.getInstance(this).toggleMute(R.raw.mmenu_bgmusic);
        EscapeSoundManager.getInstance(this).lock();
        if (EscapeSoundManager.getInstance(this).isMuted()) {
            findViewById(R.id.btn_mute).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.icon_mute, null));
        } else {
            findViewById(R.id.btn_mute).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.icon_sound, null));
        }
    }

    /**
     * When clicked takes you to current players overview and allows you to start the game from there
     * @param v the view as used by this method
     * @since 0.2
     */
    public void onClickContinue(View v) {
        EscapeSoundManager.getInstance(this).playSound(EscapeSoundManager.getInstance(this).snd_button);

        show_thanks = false;

        mmenu_text.setText(R.string.continue_game);
        mmenu_text.setBackgroundColor(getResources().getColor(R.color.green));
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(dm);
        float aspect_rounded = Math.round((float)dm.widthPixels / dm.heightPixels * 10) / 10f;
        Bundle bundle;
        if (aspect_rounded == Math.round(16f/9 * 10) / 10f) {
            bundle = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.fade_in_activity, R.anim.enlarge_main_activity_wide).toBundle();
        } else {
            bundle = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.fade_in_activity, R.anim.enlarge_main_activity_xwide).toBundle();
        }
        Intent intent = new Intent(this, SubContinueActivity.class);
        intent.putExtra("user", user);
        startActivity(intent, bundle);
    }

    /**
     * When Highscores Button has been clicked goes to HighscoreActivity
     * @param v HighscoreButton View that has been clicked
     * @since 1.0
     */
    public void onClickHighscores(View v) {
        EscapeSoundManager.getInstance(this).playSound(EscapeSoundManager.getInstance(this).snd_button);

        show_thanks = false;

        mmenu_text.setText(R.string.highscores);
        mmenu_text.setBackgroundColor(getResources().getColor(R.color.green));
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(dm);
        float aspect_rounded = Math.round((float)dm.widthPixels / dm.heightPixels * 10) / 10f;
        Bundle bundle;
        if (aspect_rounded == Math.round(16f/9 * 10) / 10f) {
            bundle = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.fade_in_activity, R.anim.enlarge_main_activity_wide).toBundle();
        } else {
            bundle = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.fade_in_activity, R.anim.enlarge_main_activity_xwide).toBundle();
        }
        Intent intent = new Intent(this, HighscoreActivity.class);
        startActivity(intent, bundle);
    }

    /**
     * Closes the software.
     * @param v the view as used by this method
     * @since 0.1
     */
    public void onClickQuit(View v) {
        EscapeSoundManager.getInstance(this).playSound(EscapeSoundManager.getInstance(this).snd_button);

        show_thanks = false;

        finishAffinity();
    }

    /**
     * Callback when all users (in this case only one) have been loaded from the db
     * @param user User that has been loaded from the db
     * @since 1.0
     */
    private void onUserLoaded(User user) {
        if (user == null) {
            mmenu_text = findViewById(R.id.mmenu_text);
            if(show_thanks) {
                mmenu_text.setText(R.string.thanks);
            }
            findViewById(R.id.btn_start).setVisibility(View.GONE);
        } else {
            this.user = user;
            findViewById(R.id.btn_start).setVisibility(View.VISIBLE);
        }
    }

    /**
     * Loads the current user from the database
     * @return User or null, if it does not exist
     * @since 1.0
     */
    private User loadUser() {
        List<User> users = EscapeDatabase.getInstance(this).userDao().selectAllUsers();
        User user;
        if (users.size() > 0)
            user = users.get(0);
        else
            user = null;
        return user;
    }
}
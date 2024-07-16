package at.ac.tuwien.mmue_sb10;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;

import at.ac.tuwien.mmue_sb10.persistence.EscapeDatabase;
import at.ac.tuwien.mmue_sb10.persistence.User;
import at.ac.tuwien.mmue_sb10.util.Concurrency;

/**
 * This class handles the submenu for starting a new game by deleting the old profile and creating a new one
 * @since 1.0
 * @author Lukas Lidauer & Jan König
 */
public class SubNewActivity extends Activity {

    CheckBox checkBox;
    User user;
    String newusername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_new);
    }

    @Override
    protected void onResume() {
        super.onResume();

        user = (User)getIntent().getSerializableExtra("user");

        checkBox = findViewById(R.id.tutorialCheckbox);

        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(dm);

        checkBox.setPadding(checkBox.getPaddingLeft() + (int)(10 * dm.density),
                checkBox.getPaddingTop(),
                checkBox.getPaddingRight(),
                checkBox.getPaddingBottom());

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        if (!EscapeSoundManager.getInstance(this).isMuted()) {
            EscapeSoundManager.getInstance(this).initMediaPlayer(R.raw.mmenu_bgmusic, true);
        }

        EscapeSoundManager.getInstance(this).lock();
    }

    @Override
    protected void onRestart() {
        finish();
        super.onRestart();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        EscapeSoundManager.getInstance(this).playSound(EscapeSoundManager.getInstance(this).snd_button);
    }

    @Override
    public void finish() {
        super.finish();
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(dm);
        float aspect_rounded = Math.round((float)dm.widthPixels / dm.heightPixels * 10) / 10f;
        if (aspect_rounded == Math.round(16f/9 * 10) / 10f) {
            overridePendingTransition(R.anim.shrink_main_activity_wide, R.anim.fade_out_activity);
        } else {
            overridePendingTransition(R.anim.shrink_main_activity_xwide, R.anim.fade_out_activity);
        }
    }

    /**
     * When clicked brings you back to the main menu.
     * @param v the view as used by this method
     * @since 0.2
     */
    public void onClickBackToMain(View v) {
        EscapeSoundManager.getInstance(this).playSound(EscapeSoundManager.getInstance(this).snd_button);
        finish();
    }

    /**
     * When clicked brings you to the game
     * @param v the view as used by this method
     * @since 0.2
     */
    public void onClickStart(View v) {
        EscapeSoundManager.getInstance(this).playSound(EscapeSoundManager.getInstance(this).snd_button);

        if(user != null) {
            newusername = ((EditText)findViewById(R.id.editTextTextPersonName)).getText().toString();
            setContentView(R.layout.activity_sub_new_confirm);
        } else {
            newusername = ((EditText)findViewById(R.id.editTextTextPersonName)).getText().toString();
            Concurrency.executeAsync(() -> saveUser(new User(newusername, checkBox.isChecked())));
            startActivity(new Intent(this, GameActivity.class));
        }
    }

    /**
     * Starts a new game even if there is already a saved user in the db. Deletes the saved user and creates a new one.
     * @param v StartConfirmButton View that has been clicked
     * @since 1.0
     */
    public void onClickStartConfirm(View v) {
        EscapeSoundManager.getInstance(this).playSound(EscapeSoundManager.getInstance(this).snd_button);
        Concurrency.executeAsync(this::deleteUser);
        Concurrency.executeAsync(() -> saveUser(new User(newusername, checkBox.isChecked())));
        startActivity(new Intent(this, GameActivity.class));
    }

    /**
     * When TutorialCheckbox has been clicked. Only plays sound
     * @param v TutorialCheckbox View that has been clicked
     * @since 1.0
     */
    public void onClickCheckbox(View v) {
        EscapeSoundManager.getInstance(this).playSound(EscapeSoundManager.getInstance(this).snd_button);
    }

    /**
     * Saves a new User in the database
     * @param user
     * @since 1.0
     */
    private void saveUser(User user) {
        EscapeDatabase.getInstance(this).userDao().insert(user);
    }

    /**
     * Deletes the current User from the database
     * @since 1.0
     */
    private void deleteUser() {
        EscapeDatabase.getInstance(this).userDao().deleteAllUsers();
    }
}
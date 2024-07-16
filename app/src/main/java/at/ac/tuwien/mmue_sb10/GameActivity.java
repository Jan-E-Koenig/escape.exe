/**
 * Basic activity that calls GameView and is responsible for state of app (via onDestroy and onBackPressed)
 * @author Lukas Lidauer & Jan König
 */

package at.ac.tuwien.mmue_sb10;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import at.ac.tuwien.mmue_sb10.persistence.User;

/**
 * This class handles the game activities such as game loop, gameView etc.
 * @since 0.1
 * @author Lukas Lidauer & Jan König
 */
public class GameActivity extends Activity {

    private static final String TAG = GameActivity.class.getSimpleName();
    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        gameView.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        gameView = findViewById(R.id.gameView);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
package at.ac.tuwien.mmue_sb10;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;

/**
 * This class handles the splash Screen for when the game is finished completely.
 * @since 1.0
 * @author Lukas Lidauer
 */
public class FinishGameActivity extends Activity {

    WebView webView;
    boolean touched;
    Animation fadeAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_game);
    }

    @Override
    protected void onResume() {
        super.onResume();

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        fadeAnimation = AnimationUtils.loadAnimation(this, R.anim.endless_fade);
        findViewById(R.id.anyKeyView).startAnimation(fadeAnimation);

        webView = findViewById(R.id.danceanim_webview);
        webView.loadUrl("file:///android_asset/danceanim.html");
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                endSplashScreen();
                return false;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        endSplashScreen();
        return super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Splashscreen is being ended. Since onTouchEvent is used, a boolean value makes sure to only fire this once
     * @since 1.0
     */
    private void endSplashScreen() {
        if (touched) //only play touch event once
            return;
        touched = true;

        MainActivity.show_thanks = true;

        finish();
    }
}
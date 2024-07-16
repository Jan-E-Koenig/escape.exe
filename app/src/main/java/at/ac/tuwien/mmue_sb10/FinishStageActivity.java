package at.ac.tuwien.mmue_sb10;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.core.app.ActivityOptionsCompat;

/**
 * This class handles the splash Screen for when a stage is completed
 * @since 1.0
 * @author Lukas Lidauer
 */
public class FinishStageActivity extends Activity {

    WebView webView;
    int current_deaths;
    int next_level;
    float screen_width;
    boolean touched;

    TextView current_level_label;
    TextView deaths_level_label;
    TextView next_level_label;
    TextView label1;
    TextView label2;
    TextView label3;
    TextView label4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_stage);
    }

    @Override
    protected void onResume() {
        super.onResume();

        touched = false;

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        webView = findViewById(R.id.runanim_webview);
        webView.loadUrl("file:///android_asset/runanim.html");
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        webView.setOnTouchListener((v, event) -> {
            endSplashScreen();
            return false;
        });

        current_deaths = getIntent().getIntExtra("current_deaths", 0);
        next_level = getIntent().getIntExtra("next_level", 0);
        screen_width = getIntent().getFloatExtra("screen_width", 1920);


        current_level_label = findViewById(R.id.current_level_label);
        deaths_level_label = findViewById(R.id.deaths_level_label);
        next_level_label = findViewById(R.id.next_level_label);
        label1 = findViewById(R.id.textView5);
        label2 = findViewById(R.id.textView6);
        label3 = findViewById(R.id.textView7);
        label4 = findViewById(R.id.textView8);

        String next_name;
        if (next_level > 0)
            next_name = getResources().getString(getResources().getIdentifier("stage" + next_level, "string", getPackageName()));
        else if (next_level == 0)
            next_name = getResources().getString(getResources().getIdentifier("stage" + (next_level + 1), "string", getPackageName()));
        else
            next_name = getResources().getString(getResources().getIdentifier("stage_" + Math.abs(next_level), "string", getPackageName()));

        if (next_level > 1)
        current_level_label.setText("" + (next_level - 1));
        else if (next_level == 1)
            current_level_label.setText("Tutorial " + 5);
        else
            current_level_label.setText("Tutorial " + (next_level + 5));

        deaths_level_label.setText("" + current_deaths);

        if (next_level > 0) {
            if (next_level > HighscoreActivity.TOTAL_LEVELS) {
                next_level_label.setText("-");
            } else {
                next_level_label.setText(next_level + " - " + next_name);
            }
        } else {
            next_level_label.setText(next_name);
        }
    }

    @Override
    protected void onRestart() {
        finish();
        super.onRestart();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        endSplashScreen();
        return super.onTouchEvent(event);
    }

    /**
     * Ends the splashscreen and returns you to the GameActivity
     * @since 1.0
     */
    private void endSplashScreen() {
        if (touched) //only play touch event once
            return;

        touched = true;

        if (next_level > HighscoreActivity.TOTAL_LEVELS) {
            Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
            startActivity(new Intent(this, OutroActivity.class), bundle);
        } else {
            ObjectAnimator animation = ObjectAnimator.ofFloat(webView, "translationX", screen_width + webView.getWidth());
            animation.setDuration(2000);
            animation.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    current_level_label.setVisibility(View.INVISIBLE);
                    deaths_level_label.setVisibility(View.INVISIBLE);
                    next_level_label.setVisibility(View.INVISIBLE);
                    label1.setVisibility(View.INVISIBLE);
                    label2.setVisibility(View.INVISIBLE);
                    label3.setVisibility(View.INVISIBLE);
                    label4.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    finish();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animation.start();
        }
    }
}
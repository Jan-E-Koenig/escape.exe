package at.ac.tuwien.mmue_sb10;

import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.res.ResourcesCompat;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * This class handles the outro video with skip function
 * @since 1.0
 * @author Lukas Lidauer & Jan König
 */
public class OutroActivity extends Activity {

    CustomVideoView videoView;
    TextView skipView;
    Button muteBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outro);
    }

    @Override
    protected void onRestart() {
        finish();
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        skipView = findViewById(R.id.txt_skip_outro);
        muteBtn = findViewById(R.id.btn_mute_outro);
        videoView = findViewById(R.id.outroView);
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.outro));
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                startEndScreen();
            }
        });
        videoView.start();

        EscapeSoundManager.getInstance(this).releaseMediaPlayer();

        if (EscapeSoundManager.getInstance(this).isMuted()) {
            videoView.mute();
            muteBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.icon_mute, null));
        } else {
            videoView.unmute();
            muteBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.icon_sound, null));
        }
    }
    /**
     * When the VideoView is clicked
     * @param view VideoView that has been clicked
     * @since 1.0
     */
    public void onClickVideo(View view) {
        if (skipView.getVisibility() == View.VISIBLE) {
            skipView.setVisibility(View.GONE);
            muteBtn.setVisibility(View.GONE);
        } else {
            skipView.setVisibility(View.VISIBLE);
            muteBtn.setVisibility(View.VISIBLE);
        }
    }

    /**
     * When the MuteButton is clicked. Mutes or Unmutes the game
     * @param view MuteButton View that has been clicked
     * @since 1.0
     */
    public void onClickMute(View view) {
        EscapeSoundManager.getInstance(this).toggleMute();
        if (EscapeSoundManager.getInstance(this).isMuted()) {
            videoView.mute();
            muteBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.icon_mute, null));
        } else {
            videoView.unmute();
            muteBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.icon_sound, null));
        }
    }

    /**
     * When the SkipButton is clicked. Skips the Video and jumps straight to the MainActivity (main menu)
     * @param view SkipButton View that has been clicked
     * @since 1.0
     */
    public void onClickSkip(View view) {
        //EscapeSoundManager.getInstance(this).playSound(EscapeSoundManager.getInstance(this).snd_button);
        videoView.stopPlayback();
        videoView.suspend();
        startEndScreen();
    }

    /**
     * Starts the EndScreenActivity Splashscreen when the video is skipped or has finished playing
     * @since 1.0
     */
    private void startEndScreen() {
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
        startActivity(new Intent(this, FinishGameActivity.class), bundle);
    }
}
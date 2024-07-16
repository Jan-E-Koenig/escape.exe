package at.ac.tuwien.mmue_sb10;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * This class handles the video view via MediaPlayer
 * @since 1.0
 * @author Lukas Lidauer
 */
public class CustomVideoView extends VideoView implements MediaPlayer.OnPreparedListener {

    private MediaPlayer mediaPlayer;
    private boolean muted;

    public CustomVideoView(Context context) {
        super(context);
        this.setOnPreparedListener(this);
        muted = false;
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnPreparedListener(this);
        muted = false;
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setOnPreparedListener(this);
        muted = false;
    }

    /**
     *
     * Callback when the VideoView is initialized and ready to be used.
     * This is used to get the MediaPlayer instance behind it to mute it if needed.
     *
     * @param mp MediaPlayer used for the video
     * @since 1.0
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        this.mediaPlayer = mp;
        if(muted)
            this.mediaPlayer.setVolume(0, 0);
    }

    /**
     * Mutes the custom video view
     * @since 1.0
     */
    public void mute() {
        try {
            mediaPlayer.setVolume(0, 0);
        } catch (Exception exc) {
            muted = true;
        }
    }

    /**
     * unmutes the custom Video view
     * @since 1.0
     */
    public void unmute() {
        try {
            mediaPlayer.setVolume(1, 1);
        } catch (Exception exc) {
            muted = false;
        }
    }
}

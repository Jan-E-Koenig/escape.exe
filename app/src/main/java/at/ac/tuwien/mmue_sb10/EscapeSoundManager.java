package at.ac.tuwien.mmue_sb10;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;

/**
 * This class handles both mediaPlayer for background music and soundpool for sound effects
 * @since 1.0
 * @author Lukas Lidauer & Jan König
 */
public class EscapeSoundManager {
    private static EscapeSoundManager sInstance;

    public int snd_button;
    public int snd_death;
    public int snd_steps;
    public int snd_jump;
    public int snd_gravity_up;
    public int snd_gravity_down;

    private Context context;
    private MediaPlayer mediaPlayer;
    private MediaPlayer levelBeatPlayer;
    private SoundPool soundPool;

    private boolean locked;
    private boolean muted;
    private int loop_stream_id;

    /**
     *
     * Private Constructor for the EscapeSoundManager
     * @param context Context of the Application
     * @since 1.0
     */
    private EscapeSoundManager(Context context) {
        this.context = context.getApplicationContext();
        SharedPreferences sp = context.getSharedPreferences("escapePrefs", 0);
        muted = sp.getBoolean("muted", false);
    }

    /**
     *
     * Singleton Pattern used for the EscapeSoundManager because there can only be one
     *
     * @param context Context of the Application to construct the EscapeSoundManager if there is no instance yet
     * @since 1.0
     */
    public static EscapeSoundManager getInstance(Context context) {
        if (null == sInstance) {
            synchronized (EscapeSoundManager.class) {
                sInstance = new EscapeSoundManager(context);
            }
        }
        return sInstance;
    }

    /**
     * Locks the SoundManager. No new MediaPlayer or SoundPool can be initialized while it is locked
     * @since 1.0
     */
    public void lock() {
        this.locked = true;
    }


    /**
     * Unlocks the SoundManager. New MediaPlayers or SoundPools can be initialized while its unlocked
     * @since 1.0
     */
    public void unlock() {
        this.locked = false;
    }


    /**
     * Checks if the SoundManager is muted and returns true if so
     * @since 1.0
     */
    public boolean isMuted() {
        return muted;
    }

    /**
     * Mutes or Unmutes the SoundManager. Also saves the status in the preferences
     * @since 1.0
     */
    public void toggleMute() {
        muted = !muted;
        if (muted)
            release();

        SharedPreferences sp = context.getSharedPreferences("escapePrefs", 0);
        sp.edit().putBoolean("muted", muted).apply();
    }

    /**
     * Mutes or Unmutes the SoundManager. Also saves the status in the preferences
     * @param music_resource Resource of the music to be played if it is unmuted
     * @since 1.0
     */
    public void toggleMute(int music_resource) {
        muted = !muted;
        if (muted) {
            release();
        } else {
            initMediaPlayer(music_resource, true);
            initSoundPool();
        }
        SharedPreferences sp = context.getSharedPreferences("escapePrefs", 0);
        sp.edit().putBoolean("muted", muted).apply();
    }

    /**
     * Pause the MediaPlayer instance without releasing it
     * @since 1.0
     */
    public void pauseMediaPlayer() {
        if (muted)
            return;
        mediaPlayer.pause();
    }

    /**
     * Resumes the MediaPlayer instance from where it has been paused
     * @since 1.0
     */
    public void resumeMediaPlayer() {
        if (muted)
            return;

        mediaPlayer.start();
    }

    /**
     * Releases all MediaPlayer and SoundPool Resources
     * @since 1.0
     */
    public void release() {
        releaseMediaPlayer();
        releaseSoundPool();
    }

    /**
     * Releases the MediaPlayer instance
     * @since 1.0
     */
    public void releaseMediaPlayer() {
        try {
            mediaPlayer.release();
            levelBeatPlayer.release();
        } catch (NullPointerException | IllegalStateException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Releases the SoundPool instance
     * @since 1.0
     */
    public void releaseSoundPool() {
        try {
            soundPool.release();
        } catch (NullPointerException | IllegalStateException exc) {
            exc.printStackTrace();
        }
    }


    /**
     * Releases and then initializes a new MediaPlayer instance. Does nothing if the SoundManager is locked or muted
     * @param music_resource Resource of the music to be played after initialization
     * @param loop           Indicates if the track should be looped
     * @since 1.0
     */
    public void initMediaPlayer(int music_resource, boolean loop) {
        if (muted || locked)
            return;

        try {
            releaseMediaPlayer();
            mediaPlayer = MediaPlayer.create(context, music_resource);
            mediaPlayer.setVolume(1, 1);
            mediaPlayer.setLooping(loop);
            mediaPlayer.start();

            levelBeatPlayer = MediaPlayer.create(context, R.raw.level_beat_music);
            levelBeatPlayer.setVolume(1, 1);
        } catch (NullPointerException | IllegalStateException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Releases and then initializes a new SoundPool instance with all sounds in the app. Does nothing if the SoundManager is locked or muted
     * @since 1.0
     */
    public void initSoundPool() {
        if (muted || locked)
            return;

        try {
            releaseSoundPool();
            soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
            snd_button = soundPool.load(context, R.raw.button_click_ogg, 3);
            snd_death = soundPool.load(context, R.raw.death_sound_ogg, 2);
            snd_jump = soundPool.load(context, R.raw.jump_ogg, 1);
            snd_gravity_up = soundPool.load(context, R.raw.gravity_to_invert_ogg, 2);
            snd_gravity_down = soundPool.load(context, R.raw.gravity_to_normal_ogg, 2);
            snd_steps = soundPool.load(context, R.raw.steps_ogg, 1);

            loop_stream_id = -1;

        } catch (NullPointerException | IllegalStateException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Plays specific sound file when level is finished
     * @since 1.0
     */
    public void playLevelBeatMusic() {
        if (muted)
            return;

        levelBeatPlayer.start();
    }

    /**
     * Plays a sound. Does nothing if the SoundManager is muted
     * @param sound_id of the sound to be played
     * @since 1.0
     */
    public void playSound(int sound_id) {
        if (muted)
            return;

        soundPool.play(sound_id, 1, 1, 0, 0, 1);
    }

    /**
     * Plays a sound on loop. Does nothing if the SoundManager is muted or if one other sound is already being played on loop
     * @param sound_id
     * @since 1.0
     */
    public void playSoundLoop(int sound_id) {
        if (muted || loop_stream_id != -1)
            return;

        loop_stream_id = soundPool.play(sound_id, 1, 1, 0, -1, 1);
    }

    /**
     * Fades out currently looping sound effect slowly
     * @param current_fade_out_time currently progressed time since the fadeout started in ms
     * @param fade_out_time         total time for the fadeout process in ms
     * @param max_fadeout           maximum fadeout factor. 0.5 means it will fade the sound to 50% volume
     * @since 1.0
     */
    public void fadeSoundLoop(float current_fade_out_time, int fade_out_time, float max_fadeout) {
        if (muted)
            return;

        float fade = Math.max(1 - (current_fade_out_time / fade_out_time), max_fadeout);

        soundPool.setVolume(loop_stream_id, fade, fade);
    }

    /**
     * Stops a sound that is being played on loop. Does nothing if the SoundManager is muted
     * @since 1.0
     */
    public void stopSoundLoop() {
        if (muted)
            return;

        soundPool.stop(loop_stream_id);
        loop_stream_id = -1;
    }
}

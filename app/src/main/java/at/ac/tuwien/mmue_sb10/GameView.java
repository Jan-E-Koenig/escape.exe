package at.ac.tuwien.mmue_sb10;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import java.util.List;

import at.ac.tuwien.mmue_sb10.persistence.EscapeDatabase;
import at.ac.tuwien.mmue_sb10.persistence.OnUserLoadedListener;
import at.ac.tuwien.mmue_sb10.persistence.User;
import at.ac.tuwien.mmue_sb10.util.Concurrency;

/**
 * The class GameView handles the visual representation of the applications state.
 * Therefore GameState and GameThread must be delivered to this class.
 * This class takes into account the screen size, FPS and density of the device it's running on.
 * @since 0.1
 * @author Lukas Lidauer & Jan König
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = GameView.class.getSimpleName();

    private GameState state;
    private GameThread thread;
    /**
     * The FPS of the device this program is running on
     */
    private float fps;
    /**
     * The pixel density of the device this program is running on
     */
    private float density;
    /**
     * The screen width of the device this program is running on
     */
    private int screenWidth;
    /**
     * The screen height of the device this program is running on
     */
    private int screenHeigth;

    private final OnUserLoadedListener onUserLoadedListener = this::onUserLoaded;

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        setFocusable(true);
    }

    public GameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getHolder().addCallback(this);
        setFocusable(true);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Display dsp = ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        this.fps = dsp.getRefreshRate();
        DisplayMetrics dm = new DisplayMetrics();
        dsp.getRealMetrics(dm);
        this.density = dm.density;
        Rect r = new Rect();
        getGlobalVisibleRect(r);
        this.screenWidth = r.width();
        this.screenHeigth = r.height();

        this.state = new GameState(getContext(), this.density, this.screenWidth, this.screenHeigth);
        this.thread = new GameThread(state, holder, getContext());

        Concurrency.executeAsync(() -> {
            User user = loadUser();
            ((Activity)getContext()).runOnUiThread(() -> onUserLoadedListener.onUserLoaded(user));
        });
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        endgame();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            this.state.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    /**
     * Is called when the user finished loading from SQLite DB
     * @param user User that has been loaded
     * @since 1.0
     */
    private void onUserLoaded(User user) {
        if(user.currentLevel > HighscoreActivity.TOTAL_LEVELS) {

            Concurrency.executeAsync(this::deleteUser);

            ((Activity) getContext()).finish();
            return;
        }
        this.state.setUser(user);
        startgame();
    }

    /**
     * Starts the game and while doing so sets density, FPS and screen ration.
     * This method also handles which level will be loaded but currently is static since no database is yet implemented
     * After loading a level it sets the main thread to running.
     * @since 0.1
     */
    public void startgame() {
        this.thread.setRunning(true);
        this.thread.start();
    }

    /**
     * Pauses the game - method needs to be implemented in the future, currently not working
     * @since 0.1
     */
    public void onBackPressed() {
        if(this.thread != null) {
            this.state.onBackPressed();
        }
    }

    /**
     * Declares the game not running anymore (by setting the corresponding thread to not running)
     * @since 0.1
     */
    public void endgame() {
        this.thread.setRunning(false);
        this.thread = null;
    }

    /**
     * Loads the current user from the database
     * @return User or null, if it does not exist
     * @since 1.0
     */
    private User loadUser() {
        List<User> users = EscapeDatabase.getInstance(getContext()).userDao().selectAllUsers();
        User user;
        if(users.size() > 0)
            user = users.get(0);
        else
            user = null;
        return user;
    }

    /**
     * Deletes the current User from the database
     * @since 1.0
     */
    private void deleteUser() {
        EscapeDatabase.getInstance(getContext()).userDao().deleteAllUsers();
    }
}

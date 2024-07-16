/**
 * Processes the game state therefore GameState must be delivered to this class.
 * This class handles frametime to ensure that the app is running smoothly on all devices
 *
 * @author Lukas Lidauer
 */

package at.ac.tuwien.mmue_sb10;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;

import androidx.core.app.ActivityOptionsCompat;

/**
 * This class mainly handles the render loop
 * @since 0.1
 * @author Lukas Lidauer & Jan König
 */
public class GameThread extends Thread {

    private static final String TAG = GameThread.class.getSimpleName();

    private GameState state;
    private SurfaceHolder holder;
    private Context context;

    private Canvas canvas;

    /**
     * Creates a new GameThread instance
     *
     * @param state  GameState instance that will be updated and rendered
     * @param holder SurfaceHolder of the SurfaceView
     * @since 0.1
     */
    public GameThread(GameState state, SurfaceHolder holder, Context context) {
        this.state = state;
        this.holder = holder;
        this.context = context;
    }

    /**
     * Sets the thread to be running or not. Thread will stop if this is set to false.
     *
     * @param active boolean that sets the thread to running
     * @since 0.1
     */
    public void setRunning(boolean active) {
        if (this.state != null)
            this.state.running = active;
    }

    /**
     * Renderloop that renders the gamestate onto the screen
     *
     * @since 0.1
     */
    @Override
    public void run() {
        long currentFrameTime, deltaFrameTime, lastFrameTime = System.currentTimeMillis();
        try {
            while (this.state.running) {
                currentFrameTime = System.currentTimeMillis();
                deltaFrameTime = currentFrameTime - lastFrameTime;

                state.update(deltaFrameTime);

                try {
                    canvas = holder.lockCanvas();
                    synchronized (holder) {
                        state.draw(canvas, deltaFrameTime);
                    }
                } finally {
                    if (canvas != null) {
                        holder.unlockCanvasAndPost(canvas);
                    }
                }

                lastFrameTime = currentFrameTime;
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

        if (!this.state.finished)
            end();
    }

    /**
     * Finishes the GameActivity when the GameState is not running anymore
     * @since 1.0
     */
    private void end() {
        EscapeSoundManager.getInstance(this.context).releaseMediaPlayer();
        ((Activity) context).finish();
    }
}

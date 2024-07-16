/**
 * Handles the current state of the application.
 * This class saves information regarding the position of the player on a 2D grid, player velocity, player acceleration, gravity (up or down), is player dead and more.
 *
 * @author Lukas Lidauer & Jan König
 */
package at.ac.tuwien.mmue_sb10;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;


import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;

import at.ac.tuwien.mmue_sb10.persistence.EscapeDatabase;
import at.ac.tuwien.mmue_sb10.persistence.Highscore;
import at.ac.tuwien.mmue_sb10.persistence.User;
import at.ac.tuwien.mmue_sb10.util.Concurrency;

/**
 * This class handles the current state of the game such as player position, velocity, current level, current player etc. This class also computes frame by frame updates and the like
 * @since 0.1
 * @author Lukas Lidauer & Jan König
 */
public class GameState {
    public static final boolean SKIP_FINISH_SPLASH_SCREEN = false;

    private static final int PLAYER_WIDTH = 18; //player width in pixels
    private static final int PLAYER_HEIGTH = 24; //player heigth in pixel (24 is maximum because of collision)
    private static final float FRAME_TIME = 83f; //player animation. 83f is default for 12fps

    /*
     * PLAYER: POSITION, VELOCITY, ACCELERATION
     */
    private float player_pos_x;
    private float player_pos_y;
    private float player_velocity_x;
    private float player_boost_x;
    private float player_velocity_y;
    private float player_acceleration_y;

    private float player_move_y;
    private float player_move_x;

    /*
     * PLAYER: STATE
     */
    private byte gravity; //gravity can either be regular or inverted (or top or bottom)
    private boolean player_inAir; //player is in air?
    private boolean player_onBoost; //player touches booster?
    private boolean player_inInverter; //player touches inverter?
    private boolean player_onJumper; //player touches jumper?
    private boolean player_first_gravity_inAir; //player is allowed to do only one gravity change in the air until he hits the ground again. This variable keeps track of that.
    private boolean player_dead; //player died
    private boolean player_no_input; //game doesnt accept input for player until stage is finished. starts screen fade out
    private float current_fade_out_time; //current timer to fade out

    private PlayerState player_state; //current state of the player. used for animations
    private PlayerState player_last_state; //last state of player. used for animations
    private Bitmap[] player_frames; //all frames of the player animations
    private float player_anim_time; //time counter used for animations
    private int player_current_frame; //current frame of the player to be drawn
    private Matrix player_draw_matrix; //transformation of player
    private float player_draw_scale; //factor to scale the player bitmap

    /*
     * CURRENT STAGE
     */
    private Stage stage; //current stage
    public boolean finished; //stage is finished
    private boolean started; //stage is started
    public boolean running; //game is running

    /*
     * PAUSE MENU
     */
    public boolean paused; //game is paused
    private RectF continue_touch_zone; //rectangle of the continue button
    private RectF exit_touch_zone; //rectangle of the exit button
    private RectF mute_pause_touch_zone; //rectangle of the mute button

    private RectF controls_zone; //rectangle of the controls zone

    /*
     * MISC
     */
    private Context context; //context of the app
    private User user; //current savefile
    private int current_deaths; //only used for splash screen. (passed as intent)
    private boolean update_user; //indicates wheter the user needs to be updated
    private float screenWidth; //screen width of the smartphone in px
    private float screenHeight; //screen heigth of the smartphone in px

    /*
     * DRAW
     */
    private float density; //density of the smartphone screen
    private float trans_x = 0; //draw-translation on x axis
    private float trans_y = 0; //draw-translation on y axis
    private float trans_x_unscaled = 0; //draw-translation on x axis unscaled
    private float trans_y_unscaled = 0; //draw-translation on y axis unscaled
    private Rect draw_src; //source rectangle for the region of the map to draw
    private RectF draw_tar; //target rectangle on the screen (full screen)
    private float start_circle_radius; //interpolates between 0 and 1
    private Bitmap start_circle_bmp; //bitmap for the expanding circle at the start
    private Canvas start_circle_canvas; //canvas to draw on start_circle_bmp
    private boolean player_invisible; //draw player or not
    private Bitmap death_counter_icon; //icon for the death counter
    private Bitmap icon_mute; //icon for the mute button
    private Bitmap icon_sound; //icon for the unmute button
    private Bitmap icon_pause; //icon for the pause button
    private Bitmap icon_control_jump; //icon showing the jump region
    private Bitmap icon_control_gravity; //icon showing the gravity region

    /*
     * PAINT
     */
    private Paint text_paint; //paint for text
    private Paint trans_paint; //paint for transparency
    private Paint button_paint; //paint for buttons
    private Paint button_text_paint; //paint for text on buttons
    private Paint death_counter_paint; //paint for the death counter
    private Paint pause_paint; //paint for drawing the pause message
    private Paint controls_header_paint; //paint for drawing the header of the controls
    private Paint controls_text_paint; //paint for drawing the controls explanation
    private Paint mapPaint; //used for efficient drawing map

    /*
     * STRINGS
     */
    private String you_died_retry; //message to display when player died
    private String finished_next_level; //message to display when level is finished

    /*
     * COLLISION
     */
    private RectF player_collision_px; //contains player corner coordinates in px after next step
    private Rect player_collision_tiles; //contains player corner coordinates in tiles after next step
    private int[] collision_corners; //0=TopLeft, 1=TopRight, 2=BottomRight, 3=BottomLeft
    private float col_time_x; //collision time on x-axis
    private float col_time_y; //collision time on y-axis

    /**
     * Creates a new GameState instance
     *
     * @param context      Context of the App to get resources
     * @param density      Pixel density of the screen
     * @param screenWidth  Width of the screen in pixel
     * @param screenHeight Heigth of the screen in pixel
     * @since 0.1
     */
    public GameState(Context context, float density, float screenWidth, float screenHeight) {
        this.context = context;
        this.stage = new Stage(context, density);
        this.density = density;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        this.player_collision_px = new RectF();
        this.player_collision_tiles = new Rect();
        this.collision_corners = new int[4];

        loadPlayerFrames();
        loadDeathCounter();
        loadControlIcons();

        Typeface font_joystix = ResourcesCompat.getFont(this.context, R.font.joystix_monospace);

        this.text_paint = new Paint();
        this.text_paint.setColor(Color.GREEN);
        this.text_paint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.text_paint.setTypeface(font_joystix);
        this.text_paint.setTextAlign(Paint.Align.CENTER);
        this.text_paint.setTextSize(20 * this.density);

        this.pause_paint = new Paint();
        this.pause_paint.setColor(Color.GREEN);
        this.pause_paint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.pause_paint.setTypeface(font_joystix);
        this.pause_paint.setTextAlign(Paint.Align.CENTER);
        this.pause_paint.setTextSize(24 * this.density);

        this.controls_header_paint = new Paint();
        this.controls_header_paint.setColor(Color.WHITE);
        this.controls_header_paint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.controls_header_paint.setTypeface(font_joystix);
        this.controls_header_paint.setTextAlign(Paint.Align.CENTER);
        this.controls_header_paint.setTextSize(26 * this.density);

        this.controls_text_paint = new Paint();
        this.controls_text_paint.setColor(Color.WHITE);
        this.controls_text_paint.setTypeface(font_joystix);
        this.controls_text_paint.setTextAlign(Paint.Align.CENTER);
        this.controls_text_paint.setTextSize(14 * this.density);

        this.trans_paint = new Paint();
        this.trans_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        this.mapPaint = new Paint();
        this.mapPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        this.button_paint = new Paint();
        this.button_paint.setColor(this.context.getResources().getColor(R.color.android_gray));

        this.button_text_paint = new Paint();
        this.button_text_paint.setColor(this.context.getResources().getColor(R.color.white));
        this.button_text_paint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.button_text_paint.setTypeface(font_joystix);
        this.button_text_paint.setTextAlign(Paint.Align.CENTER);
        this.button_text_paint.setTextSize(18 * this.density);

        this.death_counter_paint = new Paint();
        this.death_counter_paint.setColor(Color.BLACK);
        this.death_counter_paint.setTypeface(font_joystix);
        this.death_counter_paint.setTextSize(this.death_counter_icon.getHeight() * 0.65f);

        this.you_died_retry = context.getResources().getString(R.string.player_died);
        this.finished_next_level = context.getResources().getString(R.string.splashscreen_executedrun);

        this.start_circle_bmp = Bitmap.createBitmap((int) (this.screenWidth), (int) this.screenHeight, Bitmap.Config.ARGB_8888);
        this.start_circle_canvas = new Canvas(this.start_circle_bmp);

        this.draw_src = new Rect();
        this.draw_tar = new RectF();

        this.mute_pause_touch_zone = new RectF(16 * this.density, 16 * this.density, 66 * this.density, 66 * this.density);
        this.controls_zone = new RectF(this.screenWidth - 0.3f * this.screenWidth, 0, this.screenWidth, this.screenHeight);
        this.continue_touch_zone = new RectF((this.screenWidth - this.controls_zone.width()) * 0.25f, this.screenHeight / 2, (this.screenWidth - this.controls_zone.width()) * 0.75f, this.screenHeight / 2 + 40 * this.density);
        this.exit_touch_zone = new RectF((this.screenWidth - this.controls_zone.width()) * 0.25f, this.screenHeight / 2 + 80 * this.density, (this.screenWidth - this.controls_zone.width()) * 0.75f, this.screenHeight / 2 + 120 * this.density);
        loadMuteIcons();
        loadPauseIcon();

        this.player_state = PlayerState.IDLE;
        this.player_anim_time = 0;
        this.player_draw_matrix = new Matrix();
        this.player_draw_scale = (float) PLAYER_WIDTH / this.player_frames[0].getWidth();

        this.running = false;
        EscapeSoundManager.getInstance(this.context).unlock();
    }

    /**
     * Loads the player frames from the sprite sheet into the player_frames array
     * @since 1.0
     */
    private void loadPlayerFrames() {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inScaled = false;
        Bitmap player_sheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.hero_sheet, o);
        int h = player_sheet.getWidth() / 13;
        int v = player_sheet.getHeight() / 17;
        this.player_frames = new Bitmap[h * v];
        int framenumber = 0;
        for (int y = 0; y < v; y++) {
            for (int x = 0; x < h; x++) {
                this.player_frames[framenumber] = Bitmap.createBitmap(player_sheet, x * 13, y * 17, 13, 17);
                framenumber++;
            }
        }
    }

    /**
     * Loads the death counter sprite from the resources
     * @since 1.0
     */
    private void loadDeathCounter() {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inScaled = false;
        Bitmap temp_death = BitmapFactory.decodeResource(context.getResources(), R.drawable.life_counter, o);
        this.death_counter_icon = Bitmap.createScaledBitmap(
                temp_death, (int) (temp_death.getWidth() * 0.5f * this.density), (int) (temp_death.getHeight() * 0.5f * this.density), true
        );
    }

    /**
     * Loads the mute and unmute icons from the resources
     * @since 1.0
     */
    private void loadMuteIcons() {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inScaled = false;
        Bitmap temp_mute = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_mute, o);
        this.icon_mute = Bitmap.createScaledBitmap(
                temp_mute, (int) (this.mute_pause_touch_zone.width()), (int) (mute_pause_touch_zone.height()), true
        );
        temp_mute = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_sound, o);
        this.icon_sound = Bitmap.createScaledBitmap(
                temp_mute, (int) (this.mute_pause_touch_zone.width()), (int) (mute_pause_touch_zone.height()), true
        );
    }

    /**
     * Loads the pause icon from the resources
     * @since 1.0
     */
    private void loadPauseIcon() {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inScaled = false;
        Bitmap temp_pause = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_pause, o);
        this.icon_pause = Bitmap.createScaledBitmap(
                temp_pause, (int) (this.mute_pause_touch_zone.width()), (int) (mute_pause_touch_zone.height()), true
        );
    }

    /**
     * Loads the controls icons from the resources
     * @since 1.0
     */
    private void loadControlIcons() {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inScaled = false;
        Bitmap temp_jumpgravity = BitmapFactory.decodeResource(context.getResources(), R.drawable.phone_gravity, o);
        this.icon_control_jump = Bitmap.createScaledBitmap(
                temp_jumpgravity, (int) (temp_jumpgravity.getWidth() * 1.75f * this.density), (int) (temp_jumpgravity.getHeight() * 1.75f * this.density), false
        );
        temp_jumpgravity = BitmapFactory.decodeResource(context.getResources(), R.drawable.phone_jump, o);
        this.icon_control_gravity = Bitmap.createScaledBitmap(
                temp_jumpgravity, (int) (temp_jumpgravity.getWidth() * 1.75f * this.density), (int) (temp_jumpgravity.getHeight() * 1.75f * this.density), false
        );
    }

    /**
     * Updates the state of the game depending on the deltaFrameTime. Handles collision detection, gravity, movement, ...
     *
     * @param deltaFrameTime The passed time since the last updated frame.
     * @since 0.1
     */
    public void update(long deltaFrameTime) {
        if (this.player_dead || this.finished || !this.started) {
            //Game over. Proceed to next stage or retry
            return;
        } else if (this.start_circle_radius < 1) {
            //Black circle at start of level is expanding. After 1 second the screen is fully visible
            this.start_circle_radius += (float) deltaFrameTime / 1000;
            return;
        } else if (this.paused) {
            return;
        }

        this.player_velocity_y += ((float) deltaFrameTime / 1000) * this.player_acceleration_y * this.gravity;
        this.player_move_y = Math.min(this.player_velocity_y * ((float) deltaFrameTime / 1000), 12);
        this.player_move_x = Math.min( this.player_velocity_x * this.player_boost_x * ((float) deltaFrameTime / 1000), 23);

        //Player position after this deltatime-step
        this.player_collision_px.set(this.player_pos_x + this.player_move_x, this.player_pos_y + this.player_move_y, this.player_pos_x + this.player_move_x + PLAYER_WIDTH, this.player_pos_y + this.player_move_y + PLAYER_HEIGTH);
        this.player_collision_tiles.set((int) (this.player_collision_px.left / 24), (int) (this.player_collision_px.top / 24), (int) (this.player_collision_px.right / 24), (int) (this.player_collision_px.bottom / 24));

        if (this.player_collision_tiles.left >= 0 && this.player_collision_tiles.top >= 0 && this.player_collision_tiles.right < this.stage.stage_collision.length && this.player_collision_tiles.bottom < this.stage.stage_collision[0].length) {
            //Player is inside bounds => CHECK COLLISION!
            this.collision_corners[0] = this.stage.stage_collision[this.player_collision_tiles.left][this.player_collision_tiles.top]; //TopLeft
            this.collision_corners[1] = this.stage.stage_collision[this.player_collision_tiles.right][this.player_collision_tiles.top]; //TopRight
            this.collision_corners[2] = this.stage.stage_collision[this.player_collision_tiles.right][this.player_collision_tiles.bottom]; //BottomRight
            this.collision_corners[3] = this.stage.stage_collision[this.player_collision_tiles.left][this.player_collision_tiles.bottom]; //BottomLeft
            if (this.collision_corners[0] != 0 || this.collision_corners[1] != 0 || this.collision_corners[2] != 0 || this.collision_corners[3] != 0) {
                //At least one of the player corners collides with a tile with behavior (solid, die, ...)
                if ((this.collision_corners[0] == 1 && this.collision_corners[1] == 1) || (this.collision_corners[2] == 1 && this.collision_corners[3] == 1)) {
                    //Y Solid Collision => Position adjustment
                    adjustPositionY();
                    //X Collision can still happen
                    checkCollisionX();
                    if (this.player_onBoost)
                        this.player_onBoost = false;
                    if (this.player_onJumper)
                        this.player_onJumper = false;
                } else if ((this.collision_corners[0] == 4 && this.collision_corners[1] == 4) || (this.collision_corners[2] == 4 && this.collision_corners[3] == 4)) {
                    adjustPositionY();
                    checkCollisionX();
                    boostPlayerRight();
                } else if ((this.collision_corners[0] == 5 && this.collision_corners[1] == 5) || (this.collision_corners[2] == 5 && this.collision_corners[3] == 5)) {
                    adjustPositionY();
                    checkCollisionX();
                    boostPlayerLeft();
                } else if ((this.collision_corners[0] == 8 && this.collision_corners[1] == 8) || (this.collision_corners[2] == 8 && this.collision_corners[3] == 8)) {
                    adjustPositionY();
                    if (!this.player_onJumper)
                        this.player_onJumper = true;
                } else if ((collision_corners[0] != 0 && collision_corners[3] != 0) || (collision_corners[1] != 0 && collision_corners[2] != 0)) {
                    //X Collision
                    checkCollisionX();
                } else {
                    //Only one corner collided, can be either X or Y first
                    calcCollisionTimeX();
                    calcCollisionTimeY();
                    if (this.col_time_y < 0 && this.col_time_x > 0) {
                        //no valid collision on Y, collision on X
                        killPlayer();
                        this.player_pos_y = this.player_collision_px.top;
                    } else {
                        //Y before X => Y Solid Collosion => Position adjustment
                        adjustPositionY();
                        if (collision_corners[0] == 4 || collision_corners[3] == 4 || collision_corners[1] == 4 || collision_corners[2] == 4) {
                            boostPlayerRight();
                        } else if (collision_corners[0] == 5 || collision_corners[3] == 5 || collision_corners[1] == 5 || collision_corners[2] == 5) {
                            boostPlayerLeft();
                        } else if (collision_corners[0] == 8 || collision_corners[3] == 8 || collision_corners[1] == 8 || collision_corners[2] == 8) {
                            if (!this.player_onJumper)
                                this.player_onJumper = true;
                        } else {
                            if (this.player_onBoost)
                                this.player_onBoost = false;
                            if (this.player_onJumper)
                                this.player_onJumper = false;
                        }
                    }
                }

                if (collision_corners[0] == 3 || collision_corners[1] == 3 || collision_corners[2] == 3 || collision_corners[3] == 3) {
                    //X Inverter Collision
                    if (!this.player_inInverter) {
                        this.player_velocity_x *= -1;
                        this.player_inInverter = true;
                    }
                    this.player_pos_y = this.player_collision_px.top;
                } else if (collision_corners[0] == 6 || collision_corners[1] == 6 || collision_corners[2] == 6 || collision_corners[3] == 6) {
                    //X Finish Collision
                    finishStage();
                    this.player_pos_y = this.player_collision_px.top;
                } else if (collision_corners[0] == 7 || collision_corners[1] == 7 || collision_corners[2] == 7 || collision_corners[3] == 7) {
                    //X Collision with no-input tile
                    //happens before finish line for running out of screen effect
                    setNoPlayerInput();
                    this.player_pos_y = this.player_collision_px.top;
                } else if (collision_corners[0] == 2 || collision_corners[1] == 2 || collision_corners[2] == 2 || collision_corners[3] == 2) {
                    //X Death Collision (spikes)
                    killPlayer();
                    this.player_pos_y = this.player_collision_px.top;
                } else {
                    if (this.player_inInverter)
                        this.player_inInverter = false;
                }

                if (this.player_inAir)
                    this.player_inAir = false;
                if (this.player_first_gravity_inAir)
                    this.player_first_gravity_inAir = false;
            } else {
                //None of the player corners collides with anything
                this.player_pos_y = this.player_collision_px.top;
                if (!this.player_inAir)
                    this.player_inAir = true;
                if (this.player_onBoost)
                    this.player_onBoost = false;
                if (this.player_onJumper)
                    this.player_onJumper = false;

                EscapeSoundManager.getInstance(this.context).stopSoundLoop();
            }
            this.player_pos_x = this.player_collision_px.left;
        } else {
            //Player is out of bounds => DIE!
            killPlayer();
            this.player_pos_x = this.player_collision_px.left;
            this.player_pos_y = this.player_collision_px.top;
        }
    }

    /**
     * Boosts the player speed by a factor of 1.5 if going right, otherwise slows down by factor of 0.66
     * Only works once per boost platform
     *
     * @since 0.1
     */
    private void boostPlayerRight() {
        if (this.player_velocity_x > 0 && !this.player_onBoost) {
            this.player_boost_x *= 1.5;
            this.player_onBoost = true;
        } else if (this.player_velocity_x < 0 && !this.player_onBoost) {
            this.player_boost_x *= (2f / 3);
            this.player_onBoost = true;
        }
    }

    /**
     * Boosts the player speed by a factor of 1.5 if going left, otherwise slows down by factor of 0.66
     * Only works once per boost platform
     *
     * @since 0.1
     */
    private void boostPlayerLeft() {
        if (this.player_velocity_x < 0 && !this.player_onBoost) {
            this.player_boost_x *= 1.5;
            this.player_onBoost = true;
        } else if (this.player_velocity_x > 0 && !this.player_onBoost) {
            this.player_boost_x *= (2f / 3);
            this.player_onBoost = true;
        }
    }

    /**
     * When player object collides with tiles on Y axis (basically when it is walking on the ground), adjust Y position to be exactly
     *
     * @since 0.1
     */
    private void adjustPositionY() {
        if (this.player_velocity_y > 0)
            this.player_pos_y = this.player_collision_px.bottom - this.player_collision_px.bottom % 24 - PLAYER_HEIGTH;
        else
            this.player_pos_y = this.player_collision_px.top + (24 - this.player_collision_px.top % 24);

        this.player_velocity_y = 0;

        this.player_last_state = this.player_state;
        this.player_state = PlayerState.RUNNING;

        EscapeSoundManager.getInstance(this.context).playSoundLoop(EscapeSoundManager.getInstance(this.context).snd_steps);
    }

    /**
     * When player object collides with a wall horizontally, player dies
     *
     * @since 0.1
     */
    private void checkCollisionX() {
        if ((this.collision_corners[0] == 1 && this.collision_corners[3] == 1) || (this.collision_corners[1] == 1 && this.collision_corners[2] == 1)) {
            killPlayer();
            this.player_pos_x = this.player_collision_px.left;
            this.player_pos_y = this.player_collision_px.top;
        }
    }

    /**
     * Calculates the exact time it took the player object to collide with the tile object on x axis
     * Player object might overlap the collided object, this calculates exact time it takes to collide without overlap
     *
     * @since 0.1
     */
    private void calcCollisionTimeX() {
        if (this.player_velocity_x < 0)
            this.col_time_x = (this.player_collision_tiles.right * 24 - this.player_pos_x) / (this.player_velocity_x * this.player_boost_x);
        else
            this.col_time_x = (this.player_collision_tiles.left * 24 + (24 - PLAYER_WIDTH) - this.player_pos_x) / (this.player_velocity_x * this.player_boost_x); //TODO: (24 - PLAYER_WIDTH) only works with PLAYER_WIDTH < 24
    }

    /**
     * Calculates the exact time it took the player object to collide with the tile object on y axis
     * Player object might overlap the collided object, this calculates exact time it takes to collide without overlap
     *
     * @since 0.1
     */
    private void calcCollisionTimeY() {
        if (this.player_velocity_y < 0)
            this.col_time_y = (this.player_collision_tiles.bottom * 24 - this.player_pos_y) / this.player_velocity_y;
        else
            this.col_time_y = (this.player_collision_tiles.top * 24 - this.player_pos_y) / this.player_velocity_y;
    }

    /**
     * Draws the current state of the game onto c
     *
     * @param c              The Canvas that is drawn onto
     * @param deltaFrameTime The passed time since the last frame
     * @since 0.1
     */
    public void draw(Canvas c, float deltaFrameTime) {
        if (!this.player_no_input) {
            translateX(c);
            translateY(c);
        }

        drawMap(c);

        if (!this.player_invisible)
            drawPlayer(c, deltaFrameTime);

        drawHUD(c, deltaFrameTime);
    }

    /**
     * Translates the X drawing area to fit the current player position
     *
     * @param c Canvas that needs to be translated (needed for width and height)
     * @since 1.0
     */
    private void translateX(Canvas c) {
        if (this.player_velocity_x > 0) {
            this.trans_x = this.player_pos_x * this.stage.stage_scale - 96 * this.stage.stage_scale;
        } else {
            this.trans_x = this.player_pos_x * this.stage.stage_scale - (c.getWidth() - 120 * this.stage.stage_scale);
        }
        if (this.trans_x < 0) this.trans_x = 0;
        else if (this.trans_x > this.stage.stage_foreground.getWidth() * this.stage.stage_scale - c.getWidth())
            this.trans_x = this.stage.stage_foreground.getWidth() * this.stage.stage_scale - c.getWidth();

        this.trans_x_unscaled = this.trans_x / this.stage.stage_scale;
    }

    /**
     * Translates the Y drawing area to fit the current player position
     *
     * @param c Canvas that needs to be translated (needed for width and height)
     * @since 1.0
     */
    private void translateY(Canvas c) {
        if (this.player_pos_y * this.stage.stage_scale + PLAYER_HEIGTH * this.stage.stage_scale > this.trans_y + c.getHeight() - (48 + PLAYER_HEIGTH) * this.stage.stage_scale)
            this.trans_y = this.player_pos_y * this.stage.stage_scale + PLAYER_HEIGTH * this.stage.stage_scale - c.getHeight() + (48 + PLAYER_HEIGTH) * this.stage.stage_scale;
        else if (this.player_pos_y * this.stage.stage_scale < this.trans_y + 48 * this.stage.stage_scale)
            this.trans_y = this.player_pos_y * this.stage.stage_scale - 48 * this.stage.stage_scale;
        if (this.trans_y < 0) this.trans_y = 0;
        else if (this.trans_y > this.stage.stage_foreground.getHeight() * this.stage.stage_scale - c.getHeight())
            this.trans_y = this.stage.stage_foreground.getHeight() * this.stage.stage_scale - c.getHeight();

        this.trans_y_unscaled = this.trans_y / this.stage.stage_scale;
    }

    /**
     * Draws the level including background
     * Uses old way of drawing by scaling the whole bitmap (deprecated)
     *
     * @param c Canvas to draw the level onto
     * @since 0.1
     */
    /*private void drawMap(Canvas c) {
        this.draw_src.set(
                (int) (this.trans_x_unscaled),
                (int) (this.trans_y_unscaled),
                (int) (c.getWidth() / this.stage.stage_scale + this.trans_x_unscaled),
                (int) (c.getHeight() / this.stage.stage_scale + this.trans_y_unscaled)
        );

        this.draw_tar.set(
                0,
                0,
                c.getWidth(),
                c.getHeight()
        );

        //c.drawBitmap(this.stage.stage_foreground, -this.trans_x, -this.trans_y, null);
        c.drawBitmap(
                this.stage.stage_foreground,
                this.draw_src,
                this.draw_tar,
                null
        );
    }*/

    /**
     * Draws the level including background
     * Uses new way of drawing by scaling the canvas and drawing the unscaled bitmap onto it
     * @param c Canvas to draw the level onto
     * @since 1.0
     */
    private void drawMap(Canvas c) {
        /*this.draw_src.set(
                (int) (this.trans_x_unscaled),
                (int) (this.trans_y_unscaled),
                (int) (c.getWidth() / this.stage.stage_scale + this.trans_x_unscaled),
                (int) (c.getHeight() / this.stage.stage_scale + this.trans_y_unscaled)
        );

        this.draw_tar.set(
                0,
                0,
                c.getWidth() / this.stage.stage_scale,
                c.getHeight() / this.stage.stage_scale
        );*/

        c.scale(this.stage.stage_scale, this.stage.stage_scale);
        c.drawBitmap(this.stage.stage_foreground, -this.trans_x_unscaled, -this.trans_y_unscaled, null);
        //c.drawBitmap(this.stage.stage_foreground, this.draw_src, this.draw_tar, null);
        c.scale(1 / this.stage.stage_scale, 1 / this.stage.stage_scale);
    }

    /**
     * Draws the level including background
     * Uses a new way of drawing by iterating and drawing tile by tile
     * Unused because of background drawing
     * @param c Canvas to draw the level onto
     * @since 1.0
     */
    /*private void drawMap(Canvas c) {
        int screentransx = (int)(trans_x % this.stage.tile_size_scaled);
        int screentransy = (int)(trans_y % this.stage.tile_size_scaled);
        int startx = (int)(this.trans_x / this.stage.tile_size_scaled);
        int endx = startx + (int)(this.screenWidth / this.stage.tile_size_scaled) + 1;
        int starty = (int)(this.trans_y / this.stage.tile_size_scaled);
        int endy = starty + (int)(this.screenHeight / this.stage.tile_size_scaled) + 1;

        for(int y = starty; y < endy; y++) {
            for(int x = startx; x < endx; x++) {
                if(this.stage.stage_tiles[x][y] != -1) {
                    c.drawBitmap(
                            this.stage.tiles_textures[this.stage.stage_tiles[x][y]],
                            (x - startx) * (int) (this.stage.tile_size_scaled) - screentransx,
                            (y - starty) * (int) (this.stage.tile_size_scaled) - screentransy,
                            mapPaint);
                } else {
                    c.drawBitmap(
                            this.stage.stage_background,
                            null,
                            new Rect((x - startx) * (int) (this.stage.tile_size_scaled) - screentransx, (y - starty) * (int) (this.stage.tile_size_scaled) - screentransy, (x - startx) * (int) (this.stage.tile_size_scaled) - screentransx + (int)this.stage.tile_size_scaled, (y - starty) * (int) (this.stage.tile_size_scaled) - screentransy + (int)this.stage.tile_size_scaled),
                            mapPaint
                    );
                }
            }
        }
    }*/

    /**
     * Draws the current player frame fully transformed
     *
     * @param c              Canvas to draw the player frame onto
     * @param deltaFrameTime The passed time since the last frame
     * @since 1.0
     */
    private void drawPlayer(Canvas c, float deltaFrameTime) {
        this.player_draw_matrix.reset();
        if (this.player_velocity_x > 0) {
            this.player_draw_matrix.setTranslate(this.player_pos_x * this.stage.stage_scale - this.trans_x, this.player_pos_y * this.stage.stage_scale - this.trans_y);
            this.player_draw_matrix.preScale(this.player_draw_scale * this.stage.stage_scale, this.player_draw_scale * this.stage.stage_scale);
        } else {
            this.player_draw_matrix.setTranslate((this.player_pos_x + PLAYER_WIDTH) * this.stage.stage_scale - this.trans_x, this.player_pos_y * this.stage.stage_scale - this.trans_y);
            this.player_draw_matrix.preScale(-this.player_draw_scale * this.stage.stage_scale, this.player_draw_scale * this.stage.stage_scale);
        }

        if (this.player_last_state == PlayerState.JUMPING && this.player_state == PlayerState.RUNNING) {
            //LANDING
            this.player_state = PlayerState.START_END_JUMP;
            this.player_anim_time = 0;
        }

        this.player_anim_time = (this.player_anim_time + deltaFrameTime) % 1000;
        switch (this.player_state) {
            case IDLE:
                this.player_current_frame = (int) ((this.player_anim_time / FRAME_TIME) % 11) + 8;
                break;
            case WAKEUP:
                this.player_current_frame = (int) ((this.player_anim_time / FRAME_TIME) % 8) + 20;
                break;
            case RUNNING:
                this.player_current_frame = (int) ((this.player_anim_time / FRAME_TIME) % 6) + 42;
                if (this.gravity < 0) {
                    this.player_draw_matrix.postTranslate(0, PLAYER_HEIGTH * this.stage.stage_scale);
                    this.player_draw_matrix.preScale(1, -1);
                }
                break;
            case JUMPING:
                if (this.player_velocity_y < 0 && this.gravity > 0 || this.player_velocity_y > 0 && this.gravity < 0) {
                    //JUMP UP
                    this.player_current_frame = (int) (this.player_anim_time / FRAME_TIME) % 3 + 39;
                } else if (this.player_velocity_y < 0 && this.gravity < 0 || this.player_velocity_y > 0 && this.gravity > 0) {
                    //JUMP DOWN
                    this.player_current_frame = (int) (this.player_anim_time / FRAME_TIME) % 2 + 34;
                }
                if (this.gravity < 0) {
                    this.player_draw_matrix.postTranslate(0, 24 * this.stage.stage_scale);
                    this.player_draw_matrix.preScale(1, -1);
                }
                break;
            case START_END_JUMP:
                if (this.player_anim_time > FRAME_TIME * 2) {
                    if (this.player_last_state == PlayerState.JUMPING) {
                        this.player_last_state = this.player_state;
                        this.player_state = PlayerState.RUNNING;
                    } else if (this.player_last_state == PlayerState.RUNNING) {
                        this.player_last_state = this.player_state;
                        this.player_state = PlayerState.JUMPING;
                    }
                    this.player_anim_time = 0;
                }
                this.player_current_frame = (int) ((this.player_anim_time) / FRAME_TIME) % 3 + 36;
                if (this.gravity < 0) {
                    this.player_draw_matrix.postTranslate(0, PLAYER_HEIGTH * this.stage.stage_scale);
                    this.player_draw_matrix.preScale(1, -1);
                }
                break;
            case GRAVITY:
                if (this.gravity < 0) {
                    this.player_current_frame = (int) (this.player_anim_time / FRAME_TIME) % 3 + 31;
                } else {
                    this.player_current_frame = (int) (this.player_anim_time / FRAME_TIME) % 3 + 28;
                }
                break;
            case DYING:
                if (this.player_anim_time > FRAME_TIME * 7) {
                    this.player_invisible = true;
                } else {
                    this.player_current_frame = (int) ((this.player_anim_time / FRAME_TIME) % 8);
                }
                if (this.gravity < 0) {
                    this.player_draw_matrix.postTranslate(0, PLAYER_HEIGTH * this.stage.stage_scale);
                    this.player_draw_matrix.preScale(1, -1);
                }
                break;
        }

        c.drawBitmap(this.player_frames[this.player_current_frame], player_draw_matrix, null);
    }

    /**
     * Draws the HUD on the canvas
     * HUD includes pausescreen, deathscreen, finishedscreen, fadeins, fadeouts, deathcounter, ...
     *
     * @param c              Canvas to draw the HUD onto
     * @param deltaFrameTime Passed time since the last frame
     * @since 1.0
     */
    private void drawHUD(Canvas c, float deltaFrameTime) {
        drawDeathCounter(c);

        if (this.paused) {
            drawFadeout(c, deltaFrameTime, 200, 128);
            drawPauseScreen(c);
        } else {
            c.drawBitmap(this.icon_pause, this.mute_pause_touch_zone.left, this.mute_pause_touch_zone.top, null);
        }

        if (this.player_dead) {
            //Player is dead. Draw retry message
            drawFadeout(c, deltaFrameTime, 1000, 255, 300);
            c.drawText(this.you_died_retry, this.screenWidth / 2, this.screenHeight / 2, this.text_paint);
        } else if (this.finished) {
            drawFadeout(c, deltaFrameTime, 2500, 255);
            c.drawText(finished_next_level, this.screenWidth / 2, this.screenHeight / 2, this.text_paint);
        } else if (this.start_circle_radius < 1) {
            //Stage has started. Draw expanding circle first second
            this.start_circle_canvas.drawCircle((this.player_pos_x + PLAYER_WIDTH / 2f - this.trans_x_unscaled) * this.stage.stage_scale, (this.player_pos_y + PLAYER_HEIGTH / 2f - this.trans_y_unscaled) * this.stage.stage_scale, this.start_circle_radius * this.screenWidth, trans_paint);
            c.drawBitmap(start_circle_bmp, 0, 0, null);
        } else if (this.player_no_input) {
            drawFadeout(c, deltaFrameTime, 2500, 255);
            EscapeSoundManager.getInstance(this.context).fadeSoundLoop(this.current_fade_out_time, 3500, 0f);
        }
    }

    /**
     * Draws the death counter onthe canvas
     *
     * @param c Canvas to draw the death counter onto
     * @since 1.0
     */
    private void drawDeathCounter(Canvas c) {
        c.drawBitmap(this.death_counter_icon, 16 * this.density, c.getHeight() - this.death_counter_icon.getHeight() - 16 * this.density, null);
        if(this.user.deathsCurrentLevel < 1000)
            c.drawText("" + this.user.deathsCurrentLevel, 54 * this.density, c.getHeight() - 19 * this.density - this.death_counter_icon.getHeight() / 2f - this.death_counter_paint.ascent() / 2, this.death_counter_paint);
        else
            c.drawText("oof", 54 * this.density, c.getHeight() - 19 * this.density - this.death_counter_icon.getHeight() / 2f - this.death_counter_paint.ascent() / 2, this.death_counter_paint);
    }

    /**
     * Draws the pause screen on the canvas
     *
     * @param c Canvas to draw the pause screen onto
     * @since 1.0
     */
    private void drawPauseScreen(Canvas c) {
        c.drawText(context.getResources().getText(R.string.pause_game).toString(), (this.screenWidth - this.controls_zone.width()) / 2, this.screenHeight / 2 - 40 * this.density, this.pause_paint);
        c.drawRect(this.continue_touch_zone, this.button_paint);
        c.drawRect(this.exit_touch_zone, this.button_paint);
        c.drawText(context.getResources().getText(R.string.continue_game).toString(), this.continue_touch_zone.centerX(), this.continue_touch_zone.centerY() - this.button_text_paint.ascent() / 2 - 2 * this.density, this.button_text_paint);
        c.drawText(context.getResources().getText(R.string.backtomain).toString(), this.exit_touch_zone.centerX(), this.exit_touch_zone.centerY() - this.button_text_paint.ascent() / 2 - 2 * this.density, this.button_text_paint);

        c.drawRect(this.controls_zone, this.button_paint);
        c.drawText(this.context.getResources().getText(R.string.controls).toString(), this.controls_zone.centerX(), this.screenHeight * 0.2f, this.controls_header_paint);
        drawMultiLineText(c, this.context.getResources().getText(R.string.control_gravity).toString(), this.controls_zone.centerX(), this.controls_zone.centerY() - 0.05f * this.screenHeight + this.button_text_paint.getTextSize(), this.controls_text_paint);
        drawMultiLineText(c, this.context.getResources().getText(R.string.control_jump).toString(), this.controls_zone.centerX(), this.controls_zone.centerY() + this.icon_control_jump.getHeight() + 0.2f * this.screenHeight + this.button_text_paint.getTextSize(), this.controls_text_paint);

        c.drawBitmap(this.icon_control_gravity, this.controls_zone.centerX() - this.icon_control_gravity.getWidth() / 2f, this.controls_zone.centerY() - 0.05f * this.screenHeight - this.icon_control_gravity.getHeight(), null);
        c.drawBitmap(this.icon_control_jump, this.controls_zone.centerX() - this.icon_control_jump.getWidth() / 2f, this.controls_zone.centerY() + 0.2f * this.screenHeight, null);

        if (EscapeSoundManager.getInstance(this.context).isMuted()) {
            c.drawBitmap(this.icon_mute, this.mute_pause_touch_zone.left, this.mute_pause_touch_zone.top, null);
        } else {
            c.drawBitmap(this.icon_sound, this.mute_pause_touch_zone.left, this.mute_pause_touch_zone.top, null);
        }
    }

    /**
     * Helper method to draw a string with line-breaks onto the canvas with proper styling
     * @param c Canvas to draw the text onto
     * @param text Text to draw
     * @param x Position X on the canvas to draw
     * @param y Position Y on the canvas to draw
     * @param paint Paint to use for drawing the text
     * @since 1.0
     */
    private void drawMultiLineText(Canvas c, String text, float x, float y, Paint paint) {
        String[] lines = text.split("\n");
        for(int i = 0; i < lines.length; i++, y += paint.getTextSize() + 2 * this.density) {
            c.drawText(lines[i], x, y, paint);
        }
    }

    /**
     * Draws the fadeout animation on the canvas
     *
     * @param c              Canvas to draw the fadeout animation onto
     * @param deltaFrameTime The passed time since the last frame
     * @param fade_out_time  Time for the screen to fully turn black
     * @param max_alpha      Maximum alpha for the fadeout effect
     * @since 1.0
     */
    private void drawFadeout(Canvas c, float deltaFrameTime, int fade_out_time, int max_alpha) {
        this.current_fade_out_time += deltaFrameTime;
        c.drawARGB(Math.min((int) ((this.current_fade_out_time / fade_out_time) * max_alpha), max_alpha), 0, 0, 0);
    }

    /**
     * Draws the fadeout animation on the canvas
     *
     * @param c              Canvas to draw the fadeout animation onto
     * @param deltaFrameTime The passed time since the last frame
     * @param fade_out_time  Time in ms for the screen to fully turn black
     * @param max_alpha      Maximum alpha for the fadeout effect
     * @param wait_time      Time in ms to wait before starting the fadeout effect
     * @since 1.0
     */
    private void drawFadeout(Canvas c, float deltaFrameTime, int fade_out_time, int max_alpha, int wait_time) {
        this.current_fade_out_time += deltaFrameTime;
        if (this.current_fade_out_time > wait_time) {
            c.drawARGB(Math.min((int) (((this.current_fade_out_time - wait_time) / fade_out_time) * max_alpha), max_alpha), 0, 0, 0);
        }
    }

    /**
     * Sets the stage to finished
     * @since 1.0
     */
    private void finishStage() {
        this.finished = true;
        this.player_last_state = this.player_state;
        this.player_state = PlayerState.DYING; //Same animation as dying is played
        this.player_anim_time = 0;

        if (this.update_user) {
            EscapeSoundManager.getInstance(this.context).stopSoundLoop();

            if(this.user.currentLevel > 0) {
                Highscore highscore = new Highscore(this.user.name, this.user.currentLevel, this.user.deathsCurrentLevel);
                Concurrency.executeAsync(() -> insertHighscore(highscore));

                this.current_deaths = this.user.deathsCurrentLevel;

                this.user.currentLevel++;
                this.user.deathsCurrentLevel = 0;
                Concurrency.executeAsync(() -> updateUser(this.user));

                if (this.user.currentLevel > HighscoreActivity.TOTAL_LEVELS) {
                    Highscore finalscore = new Highscore(this.user.name, 0, this.user.deathsTotal);
                    Concurrency.executeAsync(() -> insertHighscore(finalscore));
                }
            } else {
                this.current_deaths = this.user.deathsCurrentLevel;
                this.user.currentLevel++;
                if(this.user.currentLevel == 0)
                    this.user.currentLevel++; //two times to skip level 0
                this.user.deathsCurrentLevel = 0;
                this.user.deathsTotal = 0; //tutorial deaths dont count
                Concurrency.executeAsync(() -> updateUser(this.user));
            }
        }
        this.update_user = false;
    }

    /**
     * Prepares finishing a stage by not allowing any more input
     * @since 1.0
     */
    private void setNoPlayerInput() {
        if (!this.player_no_input) {
            EscapeSoundManager.getInstance(this.context).pauseMediaPlayer();
            EscapeSoundManager.getInstance(this.context).playLevelBeatMusic();
            this.player_no_input = true;
            this.gravity = 1;
            this.player_boost_x = 1.0f;
            if (this.player_velocity_y < 0)
                this.player_velocity_y = 0;
        }
    }

    /**
     * Sets the player to dead and applies the dying animation. Can be called multiple times
     * @since 1.0
     */
    private void killPlayer() {
        if (!this.player_dead)
            this.player_dead = true;

        this.player_last_state = this.player_state;
        this.player_state = PlayerState.DYING;
        this.player_anim_time = 0;

        if (this.update_user) {
            EscapeSoundManager.getInstance(this.context).pauseMediaPlayer();
            EscapeSoundManager.getInstance(this.context).stopSoundLoop();
            EscapeSoundManager.getInstance(this.context).playSound(EscapeSoundManager.getInstance(this.context).snd_death);
            this.user.deathsCurrentLevel++;
            this.user.deathsTotal++;
            Concurrency.executeAsync(() -> updateUser(this.user));
            this.update_user = false;
        }
    }

    /**
     * Inverts the gravity of the game to face upside down. Also marks the player to be in air
     * Only works if player is not in air when method call happens
     *
     * @since 0.1
     */
    private void invertGravity() {
        if (!this.player_inAir || !this.player_first_gravity_inAir) {
            this.gravity *= -1;
            this.player_inAir = true;
            this.player_first_gravity_inAir = true;

            this.player_last_state = this.player_state;
            this.player_state = PlayerState.GRAVITY;
            this.player_anim_time = 0;

            if (this.gravity < 0)
                EscapeSoundManager.getInstance(this.context).playSound(EscapeSoundManager.getInstance(this.context).snd_gravity_up);
            else
                EscapeSoundManager.getInstance(this.context).playSound(EscapeSoundManager.getInstance(this.context).snd_gravity_down);
        }
    }

    /**
     * Sets the vertical velocity of the player to make a small jump. Also marks the player to be in air
     * Only works if player is not in air when method call happens
     *
     * @since 0.1
     */
    private void jump() {
        if (!this.player_inAir) {
            if(this.player_onJumper)
                this.player_velocity_y = -360 * gravity;
            else
                this.player_velocity_y = -240 * gravity;
            this.player_inAir = true;

            this.player_last_state = this.player_state;
            this.player_state = PlayerState.START_END_JUMP;
            this.player_anim_time = 0;

            EscapeSoundManager.getInstance(this.context).playSound(EscapeSoundManager.getInstance(this.context).snd_jump);
        }
    }

    /**
     * Manipulates the state of the game depending on incoming MotionEvents
     *
     * @param event Incoming MotionEvent
     * @since 0.1
     */
    public void onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (this.paused) {
                if (this.continue_touch_zone.contains(event.getX(), event.getY())) {
                    this.paused = false;
                    this.current_fade_out_time = 0;
                    EscapeSoundManager.getInstance(this.context).playSound(EscapeSoundManager.getInstance(this.context).snd_button);
                } else if (this.exit_touch_zone.contains(event.getX(), event.getY())) {
                    this.running = false;
                    EscapeSoundManager.getInstance(this.context).playSound(EscapeSoundManager.getInstance(this.context).snd_button);
                } else if (this.mute_pause_touch_zone.contains(event.getX(), event.getY())) {
                    EscapeSoundManager.getInstance(this.context).toggleMute(this.stage.current_music_id);
                    EscapeSoundManager.getInstance(this.context).playSound(EscapeSoundManager.getInstance(this.context).snd_button);
                }
            } else if (this.mute_pause_touch_zone.contains(event.getX(), event.getY()) && !this.player_no_input && !this.player_dead && !this.finished && this.started) {
                this.paused = true;
                EscapeSoundManager.getInstance(this.context).stopSoundLoop();
                EscapeSoundManager.getInstance(this.context).playSound(EscapeSoundManager.getInstance(this.context).snd_button);
            } else if (this.player_dead) {
                retry();
            } else if (this.finished) {
                if (SKIP_FINISH_SPLASH_SCREEN) {
                    load(this.user.currentLevel);
                } else {
                    this.running = false;
                    Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this.context, android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                    Intent intent = new Intent(this.context, FinishStageActivity.class);
                    intent.putExtra("current_deaths", this.current_deaths);
                    intent.putExtra("next_level", this.user.currentLevel); //user has already been updated with next level
                    intent.putExtra("screen_width", this.screenWidth);
                    this.context.startActivity(intent, bundle);
                }
            } else if (!this.started) {
                this.started = true;
                this.player_last_state = this.player_state;
                this.player_state = PlayerState.WAKEUP;
                this.player_anim_time = 0;
                EscapeSoundManager.getInstance(this.context).playSound(EscapeSoundManager.getInstance(this.context).snd_button);
            } else if (!this.player_no_input) {
                if (event.getX() < this.screenWidth / 2) {
                    invertGravity();
                } else {
                    jump();
                }
            }
        }
    }

    /**
     * Is forwarded from activity. Called when the "back" button is pressed on the device
     * @since 1.0
     */
    public void onBackPressed() {
        if(this.player_no_input)
            return;

        if (!this.paused && this.started && !this.player_dead) {
            this.paused = true;
            EscapeSoundManager.getInstance(this.context).stopSoundLoop();
            EscapeSoundManager.getInstance(this.context).playSound(EscapeSoundManager.getInstance(this.context).snd_button);
        } else {
            this.user.deathsCurrentLevel++;
            this.user.deathsTotal++;
            Concurrency.executeAsync(() -> updateUser(this.user));
            this.running = false;
            EscapeSoundManager.getInstance(this.context).playSound(EscapeSoundManager.getInstance(this.context).snd_button);
        }
    }

    /**
     * Loads the GameState and the a stage for the first use only
     *
     * @param level The ID of the stage to be loaded. Starts with 0
     * @since 0.1
     */
    public void load(int level) {
        this.started = false;
        this.paused = false;
        this.finished = false;

        this.stage.load(level);

        EscapeSoundManager.getInstance(this.context).releaseMediaPlayer();
        EscapeSoundManager.getInstance(this.context).initMediaPlayer(this.stage.current_music_id, true);

        this.player_pos_x = stage.player_start_x * 24;
        this.player_pos_y = stage.player_start_y * 24 + 24 - PLAYER_HEIGTH;
        this.player_velocity_x = stage.player_velocity_x;
        this.player_boost_x = 1.0f;
        this.player_velocity_y = 0;
        this.player_acceleration_y = 450;
        this.player_dead = false;
        this.player_inAir = true;
        this.player_onBoost = false;
        this.player_onJumper = false;
        this.player_first_gravity_inAir = false;
        this.gravity = 1;

        this.start_circle_radius = 0.1f;
        this.start_circle_canvas.drawColor(Color.BLACK);
        this.start_circle_canvas.drawText(this.stage.stage_name, this.screenWidth / 2, this.screenHeight / 2, this.text_paint);

        this.player_last_state = this.player_state;
        this.player_state = PlayerState.IDLE;
        this.player_anim_time = 0;
        this.player_invisible = false;
        this.player_no_input = false;
        this.current_fade_out_time = 0;

        this.update_user = true;
    }

    /**
     * Resets all changed values since the start of the level. Restarts the stage
     *
     * @since 0.1
     */
    private void retry() {
        this.trans_x = 0;
        this.trans_y = 0;
        this.player_pos_x = stage.player_start_x * 24;
        this.player_pos_y = stage.player_start_y * 24;
        this.player_velocity_x = stage.player_velocity_x;
        this.player_boost_x = 1.0f;
        this.player_velocity_y = 0;
        this.player_dead = false;
        this.player_inAir = true;
        this.player_onBoost = false;
        this.player_onJumper = false;
        this.player_first_gravity_inAir = false;
        this.gravity = 1;

        this.player_last_state = this.player_state;
        this.player_state = PlayerState.WAKEUP;
        this.player_anim_time = 0;
        this.player_invisible = false;
        this.current_fade_out_time = 0;

        this.paused = false;
        this.finished = false;

        this.update_user = true;

        EscapeSoundManager.getInstance(this.context).resumeMediaPlayer();
    }

    /**
     * Sets the user of the GameState. This is used as save file
     * After the user is set, it will laod the current level of the User
     * @param user User to be used as save file for this GameState instance
     * @since 1.0
     */
    public void setUser(User user) {
        this.user = user;
        load(this.user.currentLevel);
    }

    /**
     * Updates the User in the database with new values like deaths count or level id
     * @param user User to be updated
     * @since 1.0
     */
    private void updateUser(User user) {
        EscapeDatabase.getInstance(context).userDao().update(user);
    }

    /**
     * Inserts a new Highscore into the database
     * @param highscore Highscore to be inserted
     * @since 1.0
     */
    private void insertHighscore(Highscore highscore) {
        EscapeDatabase.getInstance(context).highscoreDao().insert(highscore);
    }
}

/**
 * Handles the current stage of the game and stores all the data like name of the stage, level of the stage and so on.
 * @author Lukas Lidauer
 */

package at.ac.tuwien.mmue_sb10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This class handles stages (levels) and their variables
 * @since 0.1
 * @author Lukas Lidauer
 */
public class Stage {
    public String stage_name; //name of the stage
    public int player_start_x, player_start_y; //starting position of player
    public float player_velocity_x; //how far player moves forward
    public float stage_scale; //scaling of the stage. scale * density = stage_scale
    public int[][] stage_collision; //array of tile behavior
    //public int[][] stage_tiles; //array of the tile numbers
    public Bitmap stage_foreground; //stage tiles put together (scaled)
    private Bitmap stage_background; //stage background (scaled)
    public int current_music_id; //resource id of the bg music

    public Bitmap[] tiles_textures; //all tiles of the tileset in 24x24 format
    private int[] tiles_collision; //all tile behaviors of the tileset. uses same id as tiles_textures
    private int stage_width_tiles; //width in tiles
    private int stage_heigth_tiles; //heigth in tiles
    private float density; //density of the display

    private Paint background_paint;
    private Context context; //context of the app to get resources

    /**
     * Initializes the stage and loads the tileset from resources
     * @param context Context of the app to get resources
     * @param density Density of the display to scale graphics
     */
    public Stage(Context context, float density) {
        this.context = context;
        this.density = density;
        this.background_paint = new Paint();
        this.background_paint.setAntiAlias(true);
        loadTileset();
    }

    /**
     * Loads the tileset from the resources and splits it in 24x24 tiles
     */
    private void loadTileset() {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inScaled = false;
        Bitmap tileset = BitmapFactory.decodeResource(context.getResources(), R.drawable.tileset24, o);
        tiles_collision = context.getResources().getIntArray(R.array.collision);
        int h = tileset.getWidth() / 24;
        int v = tileset.getHeight() / 24;
        tiles_textures = new Bitmap[h * v];
        //this.stage_scale = this.density * 1.3f;
        int tilenumber = 0;
        for (int x = 0; x < h; x++) {
            for (int y = 0; y < v; y++) {
                tiles_textures[tilenumber] = Bitmap.createBitmap(tileset, x * 24, y * 24, 24, 24);
                //tiles_textures[tilenumber] = Bitmap.createScaledBitmap(Bitmap.createBitmap(tileset, x * 24, y * 24, 24, 24), (int)(24 * this.stage_scale), (int)(24 * this.stage_scale), false);
                tilenumber++;
            }
        }
    }

    /**
     * Loads stage from the assets folder
     * @param level ID of the stage to load
     */
    public void load(int level) {
        Canvas canvas = null; //draws on terrain bitmap
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("stage" + level + ".txt")));
            String line;
            while ((line = reader.readLine()) != null) {
                switch (line) {
                    case "#info":
                        if(level > 0)
                            this.stage_name = context.getResources().getString(context.getResources().getIdentifier("stage" + level, "string", context.getPackageName()));
                        else
                            this.stage_name = context.getResources().getString(context.getResources().getIdentifier("stage_" + Math.abs(level), "string", context.getPackageName()));
                        line = reader.readLine();
                        this.stage_scale = this.density * Float.parseFloat(line.split("=")[1]);
                        line = reader.readLine();
                        this.current_music_id = context.getResources().getIdentifier(line.split("=")[1], "raw", context.getPackageName());
                        line = reader.readLine();
                        BitmapFactory.Options o = new BitmapFactory.Options();
                        o.inScaled = false;
                        this.stage_background = BitmapFactory.decodeResource(context.getResources(), context.getResources().getIdentifier(line.split("=")[1], "drawable", context.getPackageName()), o);
                        this.stage_background = Bitmap.createScaledBitmap(this.stage_background, (int)(this.stage_background.getWidth() * this.stage_scale), (int)(this.stage_background.getHeight() * this.stage_scale), false);
                        this.background_paint.setShader(new BitmapShader(this.stage_background, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
                        break;
                    case "#player":
                        line = reader.readLine();
                        this.player_start_x = Integer.parseInt(line.split("=")[1]);
                        line = reader.readLine();
                        this.player_start_y = Integer.parseInt(line.split("=")[1]);
                        line = reader.readLine();
                        this.player_velocity_x = Float.parseFloat(line.split("=")[1]);
                        break;
                    case "#size":
                        line = reader.readLine();
                        this.stage_width_tiles = Integer.parseInt(line.split("=")[1]);
                        line = reader.readLine();
                        this.stage_heigth_tiles = Integer.parseInt(line.split("=")[1]);
                        //this.stage_tiles = new int[this.stage_width_tiles][this.stage_heigth_tiles];
                        this.stage_foreground = Bitmap.createBitmap(stage_width_tiles * 24, stage_heigth_tiles * 24, Bitmap.Config.ARGB_8888);
                        canvas = new Canvas(this.stage_foreground);
                        this.stage_collision = new int[stage_width_tiles][stage_heigth_tiles];
                        break;
                    case "#tiles":
                        canvas.drawRect(0, 0, this.stage_foreground.getWidth(), this.stage_foreground.getHeight(), this.background_paint);
                        for (int y = 0; y < this.stage_heigth_tiles; y++) {
                            line = reader.readLine();
                            String[] tilesInLine = line.split(" ");
                            for (int x = 0; x < tilesInLine.length; x++) {
                                if (!tilesInLine[x].equals("--")) {
                                    int tile_id = Integer.parseInt(tilesInLine[x]);
                                    canvas.drawBitmap(tiles_textures[tile_id], null, new RectF(x * 24, y * 24, x * 24 + 24, y * 24 + 24), null);
                                    stage_collision[x][y] = tiles_collision[tile_id];
                                    //this.stage_tiles[x][y] = tile_id;
                                } /*else {
                                    this.stage_tiles[x][y] = -1;
                                }*/
                            }
                        }
                        break;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package edu.msu.chuppthe.steampunked;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.io.Serializable;

public class Pipe {

    public static Pipe createStartingPipe(Context context, Player player) {
        StartingPipe startingPipe = new StartingPipe(context);

        player.setStartingPipe(startingPipe);
        return startingPipe;
    }

    public static Pipe createEndingPipe(Context context, Player player) {
        Pipe endingPipe = new Pipe(true, false, false, false);
        endingPipe.setId(context, R.drawable.gauge);
        endingPipe.setMovable(false);

        endingPipe.setPlayer(player);
        return endingPipe;
    }

    public static Pipe createCapPipe(Context context, Player player) {
        Pipe capPipe = new Pipe(false, false, true, false);
        capPipe.setId(context, R.drawable.cap);

        capPipe.setPlayer(player);
        return capPipe;
    }

    public static Pipe createTeePipe(Context context, Player player) {
        Pipe teePipe = new Pipe(true, true, true, false);
        teePipe.setId(context, R.drawable.tee);

        teePipe.setPlayer(player);
        return teePipe;
    }

    public static Pipe createNinetyPipe(Context context, Player player) {
        Pipe ninetyPipe = new Pipe(false, true, true, false);
        ninetyPipe.setId(context, R.drawable.ninety);

        ninetyPipe.setPlayer(player);
        return ninetyPipe;
    }

    public static Pipe createStraightPipe(Context context, Player player) {
        Pipe straightPipe = new Pipe(true, false, true, false);
        straightPipe.setId(context, R.drawable.straight);

        straightPipe.setPlayer(player);
        return straightPipe;
    }

    protected class Parameters implements Serializable {
        /**
         * X location in the playing area (index into array)
         */
        protected int x = -1;

        /**
         * Y location in the playing area (index into array)
         */
        protected int y = -1;

        /**
         * X position
         */
        protected float xPos = 0;

        /**
         * Y position
         */
        protected float yPos = 0;

        /**
         * Base X position
         */
        protected float xBase = 0;

        /**
         * Base Y position
         */
        protected float yBase = 0;

        /**
         * Base scale
         */
        protected float scaleBase = 1f;

        /**
         * Pipe's rotation angle
         */
        protected float rotation = 3;

        /**
         * Can the piece be moved
         */
        protected boolean isMovable = true;

        /**
         * ID for the pipe image
         */
        protected int id;
    }

    /**
     * Playing area this pipe is a member of
     */
    private PlayingArea playingArea = null;

    /**
     * The player that owns the pipe
     */
    private Player player;

    /**
     * Array that indicates which sides of this pipe
     * has flanges. The order is north, east, south, west.
     * <p/>
     * As an example, a T that has a horizontal pipe
     * with the T open to the bottom would be:
     * <p/>
     * false, true, true, true
     */
    private boolean[] connect = {false, false, false, false};

    /**
     * Depth-first visited visited
     */
    private boolean visited = false;

    /**
     * Image for the pipe
     */
    protected Bitmap pipeImage = null;

    /**
     * Paint for the outline
     */
    protected Paint outlinePaint;

    /**
     * The current parameters
     */
    protected Parameters params = new Parameters();

    private float debugLeft;
    private float debugTop;
    private float debugRight;
    private float debugBottom;

    /**
     * Constructor
     *
     * @param north True if can connect north
     * @param east  True if can connect east
     * @param south True if can connect south
     * @param west  True if can connect west
     */
    public Pipe(boolean north, boolean east, boolean south, boolean west) {
        connect[0] = north;
        connect[1] = east;
        connect[2] = south;
        connect[3] = west;

        this.outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.outlinePaint.setColor(Color.BLACK);
        this.outlinePaint.setStyle(Paint.Style.STROKE);
    }

    /**
     * Search to see if there are any downstream of this pipe
     * <p/>
     * This does a simple depth-first search to find any connections
     * that are not, in turn, connected to another pipe. It also
     * set the visited flag in all pipes it does visit, so you can
     * tell if a pipe is reachable from this pipe by checking that flag.
     *
     * @return True if no leaks in the pipe
     */
    public boolean search() {
        visited = true;

        for (int d = 0; d < 4; d++) {
            /*
             * If no connection this direction, ignore
             */
            if (!connect[d]) {
                continue;
            }

            Pipe n = neighbor(d);
            if (n == null) {
                // We leak
                // We have a connection with nothing on the other side
                return false;
            }

            // What is the matching location on
            // the other pipe. For example, if
            // we are looking in direction 1 (east),
            // the other pipe must have a connection
            // in direction 3 (west)
            int dp = (d + 2) % 4;
            if (!n.connect[dp]) {
                // We have a bad connection, the other side is not
                // a flange to connect to
                return false;
            }

            if (!n.visited) {
                // Is there a leak in that direction
                if (!n.search()) {
                    // We found a leak downstream of this pipe
                    return false;
                }
            }
        }

        // Yah, no leaks
        return true;
    }

    /**
     * Find the neighbor of this pipe
     *
     * @param d Index (north=0, east=1, south=2, west=3)
     * @return Pipe object or null if no neighbor
     */
    private Pipe neighbor(int d) {
        return playingArea.neighbor(d, params.x, params.y);
    }

    /**
     * Draw the piece to the canvas
     *
     * @param canvas canvas to draw to
     */
    public void draw(Canvas canvas) {
        canvas.save();
        canvas.translate(params.xBase + params.xPos, params.yBase + params.yPos);
        canvas.scale(params.scaleBase, params.scaleBase);
        canvas.rotate(params.rotation * 90f);

        canvas.drawBitmap(pipeImage, 0, 0, null);
        this.outlinePaint.setColor(Color.BLACK);
        canvas.drawRect(0, 0, this.getImageSize(), this.getImageSize(), this.outlinePaint);
        canvas.restore();

        if (!params.isMovable) return;

        canvas.save();

        this.outlinePaint.setColor(Color.RED);
        canvas.drawRect(debugLeft, debugTop, debugRight, debugBottom, this.outlinePaint);
        canvas.restore();
    }

    public void move(float dx, float dy) {
        if (params.isMovable) {
            params.xPos += dx;
            params.yPos += dy;
        }
    }

    /**
     * Rotate the image around the point x1, y1
     *
     * @param dAngle Angle to rotate in degrees
     */
    public void rotate(float dAngle) {
        params.rotation += (dAngle / 90f);
        capRotation();

        float x1 = this.getPositionX();
        float y1 = this.getPositionY();

        if (params.rotation >= 3f) {
            x1 -= (this.getImageSize() * params.scaleBase / 2f);
            y1 -= (this.getImageSize() * params.scaleBase / 2f);
        } else if (params.rotation >= 2f) {
            x1 -= (this.getImageSize() * params.scaleBase / 2f);
            y1 += (this.getImageSize() * params.scaleBase / 2f);
        } else if (params.rotation >= 1f) {
            x1 += (this.getImageSize() * params.scaleBase / 2f);
            y1 += (this.getImageSize() * params.scaleBase / 2f);
        } else {
            x1 += (this.getImageSize() * params.scaleBase / 2f);
            y1 -= (this.getImageSize() * params.scaleBase / 2f);
        }

        double rAngle = Math.toRadians(dAngle);
        float ca = (float) Math.cos(rAngle);
        float sa = (float) Math.sin(rAngle);
        float xp = (this.getPositionX() - x1) * ca - (this.getPositionY() - y1) * sa + x1;
        float yp = (this.getPositionX() - x1) * sa + (this.getPositionY() - y1) * ca + y1;

        params.xBase = xp;
        params.yBase = yp;
        this.resetMovement();
    }

    public void setBasePosition(float x, float y, float scale) {
        params.xBase = x;
        params.yBase = y;
        params.scaleBase = scale;
    }

    public boolean hit(float testX, float testY) {
        float pX = params.xBase + params.xPos;
        float pY = params.yBase + params.yPos;
        float pSize = this.getImageSize() * params.scaleBase;

        float left = pX - 0.5f * pSize;
        float right = pX + pSize + 0.5f * pSize;
        float top = pY - 0.5f * pSize;
        float bottom = pY + pSize + 0.5f * pSize;

        if (4f > Math.round(params.rotation)) {
            if (Math.round(params.rotation) >= 3f) {
                top -= pSize;
                bottom -= pSize;
            } else if (Math.round(params.rotation) >= 2f) {
                left -= pSize;
                right -= pSize;
                top -= pSize;
                bottom -= pSize;
            } else if (Math.round(params.rotation) >= 1f) {
                left -= pSize;
                right -= pSize;
            }
        }

        debugLeft = left;
        debugRight = right;
        debugTop = top;
        debugBottom = bottom;

        return (left < testX) && (testX < right)
                && (top < testY) && (testY < bottom) && params.isMovable;
    }

    /**
     * Get the playing area
     *
     * @return Playing area object
     */
    public PlayingArea getPlayingArea() {
        return playingArea;
    }

    /**
     * Set the playing area and location for this pipe
     *
     * @param playingArea Playing area we are a member of
     * @param x           X index
     * @param y           Y index
     */
    public void setPosition(PlayingArea playingArea, int x, int y) {
        this.playingArea = playingArea;
        params.x = x;
        params.y = y;
    }

    /**
     * Set the playing area and location for this pipe
     *
     * @param context view context
     * @param id      id of the image
     */
    protected void setId(Context context, int id) {
        params.id = id;
        this.pipeImage = BitmapFactory.decodeResource(context.getResources(), id);
    }

    /**
     * Has this pipe been visited by a search?
     *
     * @return True if yes
     */
    public boolean beenVisited() {
        return this.visited;
    }

    /**
     * Set the visited flag for this pipe
     *
     * @param visited Value to set
     */
    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    /**
     * Get the smaller side of the pipe image
     *
     * @return smaller side of pipe image
     */
    public float getImageSize() {
        int pWidth = this.pipeImage.getWidth();
        int pHeight = this.pipeImage.getHeight();
        return (float) (pWidth < pHeight ? pWidth : pHeight);
    }

    /**
     * Set the movable flag for this pipe
     *
     * @param isMovable value to set
     */
    public void setMovable(boolean isMovable) {
        params.isMovable = isMovable;
    }

    /**
     * Get the X location of the pipe
     *
     * @return x location
     */
    public float getPositionX() {
        return params.xBase + params.xPos;
    }

    /**
     * Get the Y location of the pipe
     *
     * @return y location
     */
    public float getPositionY() {
        return params.yBase + params.yPos;
    }

    /**
     * Get the scale of the pipe
     *
     * @return scale
     */
    public float getScale() {
        return params.scaleBase;
    }

    public float getRotation() {
        return params.rotation;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean canConnect(int d) {
        d = (d - Math.round(params.rotation) + 4) % 4;
        return connect[d];
    }

    /**
     * Set the x and y position to 0
     */
    public void resetMovement() {
        params.xPos = 0;
        params.yPos = 0;
    }

    public void snapRotation() {
        params.rotation = Math.round(params.rotation);
    }

    private void capRotation() {
        if (params.rotation < 0) {
            params.rotation += 4;
        }
        params.rotation %= 4;
    }
}

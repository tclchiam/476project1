package edu.msu.chuppthe.steampunked.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import edu.msu.chuppthe.steampunked.game.Pipe;
import edu.msu.chuppthe.steampunked.game.PipeArea;
import edu.msu.chuppthe.steampunked.game.Player;
import edu.msu.chuppthe.steampunked.ui.SelectionAreaView;

public class SelectionArea extends PipeArea {

    private final static String PIPE_IDS = "SelectionPipe.ids";
    private final static String PIPE_IMAGE_IDS = "SelectionPipe.image.ids";
    private final static String PLAYER_IDS = "SelectionPipe.player.ids";

    /**
     * Paint for the selection area
     */
    private Paint selectionAreaPaint;

    /**
     * This variable is set to a piece we are dragging. If
     * we are not dragging, the variable is null.
     */
    private Pipe dragging = null;

    /**
     * Most recent relative X touch when dragging
     */
    private float lastRelX;

    /**
     * Most recent relative Y touch when dragging
     */
    private float lastRelY;

    /**
     * List of pipes in the selection view
     */
    private Map<Player, List<Pipe>> pipeMap;

    /**
     * Random generator
     */
    private Random random = new Random();

    /**
     * Width of the view
     */
    private float cWidth;

    /**
     * Height of the view
     */
    private float cHeight;

    private Context context;

    private boolean passMovement = false;

    public SelectionArea(Context context) {
        this.context = context;
        this.selectionAreaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.selectionAreaPaint.setColor(0xffadf99d);

        this.pipeMap = new HashMap<>();
    }

    /**
     * Handle touch events in the selection area
     *
     * @param view   view context of the touch
     * @param event  the touch event
     * @param player player that made the touch
     * @return if the touch was successful
     */
    public boolean onTouchEvent(SelectionAreaView view, MotionEvent event, Player player) {
        float relX = event.getX();
        float relY = event.getY();

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:
                return onTouched(relX, relY, player);

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                return onReleased();

            case MotionEvent.ACTION_MOVE:
                if (passMovement) {
                    return view.passTouch(event);
                }
                return translatePipe(view, relX, relY, player);

        }
        return false;
    }

    /**
     * Handle drawing the selection area
     *
     * @param canvas context to draw to
     * @param player player to draw for
     */
    public void draw(Canvas canvas, Player player) {
        float gridSize = 6;

        this.cWidth = canvas.getWidth();
        this.cHeight = canvas.getHeight();
        float cSize = cWidth > cHeight ? cWidth : cHeight;

        float horizontal = cWidth > cHeight ? cWidth : 0;
        float vertical = cWidth > cHeight ? 0 : cHeight;

        canvas.drawRect(0, 0, cWidth, cHeight, this.selectionAreaPaint);

        List<Pipe> pipes = this.pipeMap.get(player);
        if (pipes == null) {
            return;
        }

        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);

            float pSize = pipe.getImageSize();
            float scale = cSize / (gridSize * pSize);
            float facX = (i) / (gridSize - 1f);
            float facY = (i + 1f) / (gridSize - 1f);

            float dx = horizontal * facX;
            float dy = vertical * facY;

            if (cWidth > cHeight) {
                dx += Math.abs(cWidth - pSize) / 32;
                dy = 7 * Math.abs(cHeight - dy) / 8;
            } else {
                dx = Math.abs(cWidth - pSize);
                dy -= Math.abs(cWidth - pSize) / 8;
            }

            pipe.setBasePosition(dx, dy, scale);
            pipe.draw(canvas);
        }
    }

    /**
     * Generate new random pipes
     */
    public void generatePipes(Player player) {
        if (this.pipeMap.get(player) == null) {
            this.pipeMap.put(player, new ArrayList<Pipe>());
        }

        List<Pipe> pipes = this.pipeMap.get(player);
        while (pipes.size() < 5) {
            switch (random.nextInt(6)) {
                case 0:
                    pipes.add(Pipe.createCapPipe(context, player));
                    break;
                case 1:
                case 2:
                    pipes.add(Pipe.createTeePipe(context, player));
                    break;
                case 3:
                    pipes.add(Pipe.createStraightPipe(context, player));
                    break;
                case 4:
                case 5:
                    pipes.add(Pipe.createNinetyPipe(context, player));
                    break;
                default:
                    break;
            }
        }

        setAllPipesMovable(player, true);
    }

    public void saveToBundle(Bundle bundle) {
        ArrayList<String> pipeIds = new ArrayList<>();
        ArrayList<Integer> imageIds = new ArrayList<>();
        ArrayList<String> playerIds = new ArrayList<>();

        Set<Map.Entry<Player, List<Pipe>>> entrySet = this.pipeMap.entrySet();
        for (Map.Entry<Player, List<Pipe>> entry : entrySet) {
            for (Pipe pipe : entry.getValue()) {
                pipe.saveToBundle(bundle, pipeIds, imageIds, playerIds);
            }
        }

        // Store the arrays in the bundle
        bundle.putStringArray(PIPE_IDS, pipeIds.toArray(new String[pipeIds.size()]));
        bundle.putIntArray(PIPE_IMAGE_IDS, toIntArray(imageIds));
        bundle.putStringArray(PLAYER_IDS, playerIds.toArray(new String[playerIds.size()]));
    }

    public void getFromBundle(Bundle bundle, Player playerOne, Player playerTwo) {
        this.pipeMap.clear();

        String[] pipeIds = bundle.getStringArray(PIPE_IDS);
        int[] imageIds = bundle.getIntArray(PIPE_IMAGE_IDS);
        String[] playerIds = bundle.getStringArray(PLAYER_IDS);

        if (pipeIds == null || imageIds == null || playerIds == null) {
            return;
        }

        for (int index = 0; index < pipeIds.length; index++) {
            Player player;

            if (playerIds[index].equals(playerOne.getName())) {
                player = playerOne;
            } else {
                player = playerTwo;
            }

            Pipe pipe = createPipe(context, imageIds[index], player);
            pipe.getFromBundle(pipeIds[index], bundle);

            if (this.pipeMap.get(player) == null) {
                this.pipeMap.put(player, new ArrayList<Pipe>());
            }
            this.pipeMap.get(player).add(pipe);
        }
    }

    /**
     * Handle a touch message. This is when we get an initial touch
     *
     * @param x      x location for the touch, relative to the puzzle - 0 to 1 over the puzzle
     * @param y      y location for the touch, relative to the puzzle - 0 to 1 over the puzzle
     * @param player player that made the touch
     * @return true if the touch is handled
     */
    private boolean onTouched(float x, float y, Player player) {
        List<Pipe> pipes = this.pipeMap.get(player);
        if (pipes == null) {
            return false;
        }

        // Check each piece to see if it has been hit
        for (int p = pipes.size() - 1; p >= 0; p--) {
            if (pipes.get(p).hit(x, y)) {
                // We hit a piece!
                dragging = pipes.get(p);
                lastRelX = x;
                lastRelY = y;

                return true;
            }
        }
        return false;
    }

    /**
     * @return if the release was successful
     */
    private boolean onReleased() {
        passMovement = false;
        if (dragging != null) {
            dragging = null;
            return true;
        }
        return false;
    }

    /**
     * Move the pipe, check to see if its out of the view
     *
     * @param view   view context
     * @param relX   x position relative to the touch
     * @param relY   y position relative to the touch
     * @param player player who is dragging the piece
     * @return if the translation was successful
     */
    private boolean translatePipe(SelectionAreaView view, float relX, float relY, Player player) {
        if (dragging == null) {
            return false;
        }

        float dx = relX - lastRelX;
        float dy = relY - lastRelY;

        float x = dragging.getPositionX() + dx;
        float y = dragging.getPositionY() + dy;
        float pSize = dragging.getImageSize() * dragging.getScale();


        float left = this.cWidth > cHeight ? x : x + pSize / 2;
        float top = this.cWidth > cHeight ? y - pSize / 2 : y - pSize;
        float bounds = this.cWidth > cHeight ? top : left;
        float otherBounds = this.cWidth < cHeight ? top : left;

        List<Pipe> pipes = this.pipeMap.get(player);

        if ((otherBounds > 0)
                && (x + pSize < cWidth) && (y < cHeight)) {
            this.dragging.move(dx, dy);
        }

        if ((bounds <= 0) && (pipes.indexOf(this.dragging) != -1)) {
            view.notifyPieceSelected(this.dragging, this.cWidth > cHeight);
            pipes.remove(this.dragging);

            setAllPipesMovable(player, false);
            passMovement = true;
        }

        lastRelX = relX;
        lastRelY = relY;

        view.invalidate();
        return true;
    }

    /**
     * @param player    Player who owns the pieces to modify
     * @param isMovable if the pipes should be movable
     */
    private void setAllPipesMovable(Player player, boolean isMovable) {
        List<Pipe> pipes = this.pipeMap.get(player);
        if (pipes == null) return;

        for (Pipe pipe : pipes) {
            pipe.setMovable(isMovable);
            pipe.resetMovement();
        }
    }
}

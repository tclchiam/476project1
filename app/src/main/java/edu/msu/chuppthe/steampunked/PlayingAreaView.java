package edu.msu.chuppthe.steampunked;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class PlayingAreaView extends View {

    /**
     * The play area
     */
    private PlayingArea playingArea = new PlayingArea(0, 0);

    /**
     * Paint for the outline
     */
    private Paint outlinePaint;

    public PlayingAreaView(Context context) {
        super(context);
        init(null, 0);
    }

    public PlayingAreaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public PlayingAreaView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        this.outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.outlinePaint.setColor(Color.BLACK);
        this.outlinePaint.setStyle(Paint.Style.STROKE);
    }

    /**
     * Initialize
     *
     * @param gridSize  size of the playing grid. 5, 10, or 20 square
     * @param playerOne player one name
     * @param playerTwo player two name
     */
    public void setupPlayArea(int gridSize, String playerOne, String playerTwo) {
        int totalGridSize = 5 * gridSize;
        int startingX = 0;
        int endingX = totalGridSize - 1;
        int player1Y = gridSize;
        int player2Y = 3 * gridSize;

        this.playingArea = new PlayingArea(totalGridSize, totalGridSize);

        Pipe player1StartPipe = Pipe.createStartingPipe(getContext(), playerOne);
        Pipe player2StartPipe = Pipe.createStartingPipe(getContext(), playerTwo);
        this.playingArea.add(player1StartPipe, startingX, player1Y);
        this.playingArea.add(player2StartPipe, startingX, player2Y);

        Pipe player1EndPipe = Pipe.createEndingPipe(getContext());
        Pipe player2EndPipe = Pipe.createEndingPipe(getContext());
        this.playingArea.add(player1EndPipe, endingX, player1Y + 1);
        this.playingArea.add(player2EndPipe, endingX, player2Y + 1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        this.playingArea.draw(canvas);

        // Draw the outline
        int cWidth = canvas.getWidth();
        int cHeight = canvas.getHeight();
        canvas.drawRect(0, 0, cWidth, cHeight, this.outlinePaint);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return this.playingArea.onTouchEvent(this, event);
    }

    /**
     * Tell the playing area to install the selection and invalidate the view
     */
    public void installSelection() {
        this.playingArea.installSelection();
        invalidate();
    }

    /**
     * Tell the playing area to discard the selection and invalidate the view
     */
    public void discardSelection() {
        this.playingArea.discardSelection();
        invalidate();
    }

    /**
     * Add the selected pipe from the SelectionArea to the PlayingArea
     *
     * @param pipe pipe that has been selected
     */
    public void notifyPieceSelected(Pipe pipe) {
        this.playingArea.setSelected(pipe);
        invalidate();
    }
}

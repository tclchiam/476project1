package edu.msu.chuppthe.steampunked.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import edu.msu.chuppthe.steampunked.game.Pipe;
import edu.msu.chuppthe.steampunked.game.Player;
import edu.msu.chuppthe.steampunked.game.SelectionArea;

public class SelectionAreaView extends View {

    /**
     * The selection area
     */
    private SelectionArea selectionArea;

    /**
     * Paint for the outline
     */
    private Paint outlinePaint;

    public SelectionAreaView(Context context) {
        super(context);
        init(null, 0);
    }

    public SelectionAreaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SelectionAreaView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        this.selectionArea = new SelectionArea(getContext());

        this.outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.outlinePaint.setColor(Color.BLACK);
        this.outlinePaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        this.selectionArea.draw(canvas, getActivePlayer());

        // Draw the outline
        int cWidth = canvas.getWidth();
        int cHeight = canvas.getHeight();
        canvas.drawRect(0, 0, cWidth, cHeight, this.outlinePaint);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return this.selectionArea.onTouchEvent(this, event, getActivePlayer());
    }

    public void startTurn(Player player) {
        this.selectionArea.generatePipes(player);
        invalidate();
    }

    public void notifyPieceSelected(Pipe pipe, boolean isPortrait) {
        GameLiveActivity activity = (GameLiveActivity) getContext();
        activity.onPieceSelected(pipe, isPortrait);
    }

    public boolean passTouch(MotionEvent event) {
        GameLiveActivity activity = (GameLiveActivity) getContext();
        return activity.passTouch(event);
    }

    private Player getActivePlayer() {
        GameLiveActivity activity = (GameLiveActivity) getContext();
        return activity.getActivePlayer();
    }

    public void saveToBundle(Bundle bundle) {
        this.selectionArea.saveToBundle(bundle);
    }

    public void getFromBundle(Bundle bundle, Player playerOne, Player playerTwo) {
        this.selectionArea.getFromBundle(bundle, playerOne, playerTwo);
    }
}

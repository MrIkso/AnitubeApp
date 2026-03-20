package com.mrikso.anitube.app.utils;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * DotsIndicatorDecoration on main screen.
 * Dots that will be shown in size,color and in circular shape at center of screen.
 */
public class DotsIndicatorDecoration extends RecyclerView.ItemDecoration {

    private final int indicatorHeight;
    private final int indicatorItemPadding;
    private final int radius;
    private final Paint inactivePaint = new Paint();
    private final Paint activePaint = new Paint();

    public DotsIndicatorDecoration(int radius, int padding, int indicatorHeight, @ColorInt int colorInactive, @ColorInt int colorActive) {
        float strokeWidth = Resources.getSystem().getDisplayMetrics().density * 1;
        this.radius = radius;
        inactivePaint.setStrokeCap(Paint.Cap.ROUND);
        inactivePaint.setStrokeWidth(strokeWidth);
        inactivePaint.setStyle(Paint.Style.FILL);
        inactivePaint.setAntiAlias(true);
        inactivePaint.setColor(colorInactive);

        activePaint.setStrokeCap(Paint.Cap.ROUND);
        activePaint.setStrokeWidth(strokeWidth);
        activePaint.setStyle(Paint.Style.FILL);
        activePaint.setAntiAlias(true);
        activePaint.setColor(colorActive);

        this.indicatorItemPadding = padding;
        this.indicatorHeight = indicatorHeight;
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

        final RecyclerView.Adapter adapter = parent.getAdapter();
        if (adapter == null)
            return;

        int totalItemCount = adapter.getItemCount();
        if (totalItemCount == 0)
            return;

        int realItemCount = (totalItemCount > 1) ? totalItemCount / 3 : totalItemCount;

        float totalLength = this.radius * 2 * realItemCount;
        float paddingBetweenItems = Math.max(0, realItemCount - 1) * indicatorItemPadding;
        float indicatorTotalWidth = totalLength + paddingBetweenItems;
        float indicatorStartX = (parent.getWidth() - indicatorTotalWidth) / 2.0f;

        float indicatorPosY = parent.getHeight() - indicatorHeight / 1.0f;

        drawInactiveDots(c, indicatorStartX, indicatorPosY, realItemCount);

        int activePosition;
        if (parent.getLayoutManager() instanceof LinearLayoutManager) {
            activePosition = ((LinearLayoutManager) parent.getLayoutManager()).findFirstVisibleItemPosition();
        } else {
            return;
        }

        if (activePosition == RecyclerView.NO_POSITION)
            return;

        int highlightPosition = activePosition % realItemCount;

        drawActiveDot(c, indicatorStartX, indicatorPosY, highlightPosition);
    }

    private void drawInactiveDots(Canvas c, float indicatorStartX, float indicatorPosY, int itemCount) {
        final float itemWidth = this.radius * 2 + indicatorItemPadding;
        float start = indicatorStartX + radius;
        for (int i = 0; i < itemCount; i++) {
            c.drawCircle(start, indicatorPosY, radius, inactivePaint);
            start += itemWidth;
        }
    }

    private void drawActiveDot(Canvas c, float indicatorStartX, float indicatorPosY, int highlightPosition) {
        final float itemWidth = this.radius * 2 + indicatorItemPadding;
        float highlightStart = indicatorStartX + radius + (itemWidth * highlightPosition);
        c.drawCircle(highlightStart, indicatorPosY, radius, activePaint);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.bottom = indicatorHeight;
    }
}
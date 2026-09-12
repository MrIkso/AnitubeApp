package com.mrikso.anitube.app.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

public class AutoScrollHelper {
    private final RecyclerView recyclerView;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final int interval;
    private boolean isPaused = false;

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!isPaused) {
                scrollNext();
            }
            handler.postDelayed(this, interval);
        }
    };

    public AutoScrollHelper(RecyclerView recyclerView, int interval) {
        this.recyclerView = recyclerView;
        this.interval = interval;

        if (recyclerView.getOnFlingListener() == null) {
            new PagerSnapHelper().attachToRecyclerView(recyclerView);
        }

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                isPaused = (newState != RecyclerView.SCROLL_STATE_IDLE);
            }
        });
    }

    private void scrollNext() {
        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
        if (adapter == null || adapter.getItemCount() <= 1)
            return;

        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null) {
            int currentPos = layoutManager.findFirstCompletelyVisibleItemPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                LinearSmoothScroller scroller = getLinearSmoothScroller(currentPos);
                layoutManager.startSmoothScroll(scroller);
            }
        }
    }

    @NonNull
    private LinearSmoothScroller getLinearSmoothScroller(int currentPos) {
        int targetPos = currentPos + 1;

        // Slow smooth scroller
        LinearSmoothScroller scroller = new LinearSmoothScroller(recyclerView.getContext()) {
            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                // Adjusted speed: faster than before, but still smooth.
                return 150f / displayMetrics.densityDpi;
            }
        };
        scroller.setTargetPosition(targetPos);
        return scroller;
    }

    public void startAutoScroll() {
        stopAutoScroll();
        handler.postDelayed(runnable, interval);
    }

    public void stopAutoScroll() {
        handler.removeCallbacks(runnable);
    }
}
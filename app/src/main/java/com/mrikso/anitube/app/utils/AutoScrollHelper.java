package com.mrikso.anitube.app.utils;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.anitube.app.adapters.AnimeCarouselAdapter;

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

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    checkAndResetPosition();
                }
            }
        });
    }

    private void checkAndResetPosition() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();

        if (layoutManager != null && adapter != null) {
            int actualCount = ((AnimeCarouselAdapter) adapter).getCurrentList().size();
            if (actualCount <= 1)
                return;

            int currentPos = layoutManager.findFirstCompletelyVisibleItemPosition();

            if (currentPos < actualCount) {
                int targetPos = currentPos + actualCount;
                recyclerView.scrollToPosition(targetPos);
            } else if (currentPos >= actualCount * 2) {
                int targetPos = currentPos - actualCount;
                recyclerView.scrollToPosition(targetPos);
            }
        }
    }

    private void scrollNext() {
        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
        if (adapter == null || adapter.getItemCount() <= 1)
            return;

        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null) {
            int currentPos = layoutManager.findFirstCompletelyVisibleItemPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                int targetPos = (currentPos + 1) % adapter.getItemCount();
                recyclerView.smoothScrollToPosition(targetPos);
            }
        }
    }

    public void startAutoScroll() {
        stopAutoScroll();
        handler.postDelayed(runnable, interval);
    }

    public void stopAutoScroll() {
        handler.removeCallbacks(runnable);
    }
}
package com.mrikso.anitube.app.utils;

import android.os.Handler;

import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

public class AutoScrollHelper {

    private final RecyclerView recyclerView;
    private final Handler handler = new Handler();
    private final int interval;
    private int position = 0;
    private Runnable runnable;

    public AutoScrollHelper(RecyclerView recyclerView, int interval) {
        this.recyclerView = recyclerView;
        this.interval = interval;

        // Attach PagerSnapHelper to RecyclerView
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
        recyclerView.smoothScrollBy(5, 0);
    }

    public void startAutoScroll() {
        if (runnable == null) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    RecyclerView.Adapter adapter = recyclerView.getAdapter();
                    if (adapter != null) {
                        if (position == adapter.getItemCount() - 1) {
                            recyclerView.post(() -> {
                                position = 0;
                                recyclerView.smoothScrollToPosition(position);
                                recyclerView.smoothScrollBy(5, 0);
                            });
                        } else {
                            position++;
                            recyclerView.smoothScrollToPosition(position);
                        }


                    /*if (adapter != null) {
                        if (position >= adapter.getItemCount()) {
                            position = 0;
                        }
                        recyclerView.smoothScrollToPosition(position);
                        position++;*/
                        handler.postDelayed(this, interval);
                    }
                }
            };
        }
        handler.postDelayed(runnable, interval);
    }

    public void stopAutoScroll() {
        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}


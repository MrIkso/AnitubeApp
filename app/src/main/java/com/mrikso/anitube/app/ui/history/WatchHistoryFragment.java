package com.mrikso.anitube.app.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mrikso.anitube.app.R;

public class WatchHistoryFragment extends Fragment {

    public WatchHistoryFragment() {}

    public static WatchHistoryFragment newInstance() {
        WatchHistoryFragment fragment = new WatchHistoryFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
        View view =
                inflater.inflate(
                        R.layout.fragment_watch_history, viewGroup, false /* attachToRoot */);
        TextView textView = (TextView) view.findViewById(R.id.tab_number_textview);

        textView.setText("history");
        return view;
    }
}

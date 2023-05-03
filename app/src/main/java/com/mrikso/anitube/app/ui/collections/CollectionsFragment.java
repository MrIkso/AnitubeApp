package com.mrikso.anitube.app.ui.collections;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mrikso.anitube.app.R;

public class CollectionsFragment extends Fragment {

    public CollectionsFragment() {}

    public static CollectionsFragment newInstance() {
        CollectionsFragment fragment = new CollectionsFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
        View view =
                inflater.inflate(
                        R.layout.fragment_collections, viewGroup, false /* attachToRoot */);
        
        return view;
    }
}

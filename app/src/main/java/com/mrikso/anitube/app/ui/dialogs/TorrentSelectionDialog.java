package com.mrikso.anitube.app.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.adapters.TorrentListAdapter;
import com.mrikso.anitube.app.interfaces.OnTorrentClickListener;
import com.mrikso.anitube.app.model.TorrentModel;
import com.mrikso.anitube.app.ui.dialogs.base.BaseBottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TorrentSelectionDialog extends BaseBottomSheetDialogFragment {
    protected static final String ARG_PARAMS = "params";
    public static final String TAG = "TorrentSelectionDialog";

    private List arrayList;

    private OnTorrentClickListener listener;

    public static TorrentSelectionDialog newInstance(ArrayList<TorrentModel> items) {
        TorrentSelectionDialog fragment = new TorrentSelectionDialog();

        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAMS, items);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args == null) return;

        arrayList = (ArrayList) Objects.requireNonNull(args.getSerializable(ARG_PARAMS), "params must not be null");
    }

    @Nullable
    @Override
    protected View onCreateContentView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = new RecyclerView(requireContext());
        recyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        return recyclerView;
    }

    @Override
    protected void onContentViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onContentViewCreated(view, savedInstanceState);

        setTitle(R.string.download_torrent);
        getPositiveButton().setVisibility(View.GONE);
        getNegativeButton().setOnClickListener(v -> dismiss());

        RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        TorrentListAdapter adapter = new TorrentListAdapter(listener);

        recyclerView.setAdapter(adapter);
        adapter.submitList(arrayList);
        // revealBottomSheet();
    }

    public void setListener(OnTorrentClickListener listener) {
        this.listener = listener;
    }
}

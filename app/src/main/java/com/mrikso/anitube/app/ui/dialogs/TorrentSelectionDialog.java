package com.mrikso.anitube.app.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.adapters.TorrentListAdapter;
import com.mrikso.anitube.app.interfaces.OnTorrentClickListener;
import com.mrikso.anitube.app.ui.dialogs.base.BaseBottomSheetDialogFragment;
import com.mrikso.anitube.app.viewmodel.TorrentSelectionViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TorrentSelectionDialog extends BaseBottomSheetDialogFragment {
    protected static final String ARG_PARAMS = "pageUrl";
    public static final String TAG = "TorrentSelectionDialog";
    private TorrentSelectionViewModel viewModel;
    private TorrentListAdapter adapter;
    private ProgressBar progressBar;
    private TextView errorTextView;
    private RecyclerView recyclerView;

    private OnTorrentClickListener listener;

    public static TorrentSelectionDialog newInstance(String pageUrl) {
        TorrentSelectionDialog fragment = new TorrentSelectionDialog();

        Bundle args = new Bundle();
        args.putString(ARG_PARAMS, pageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TorrentSelectionViewModel.class);
        Bundle args = getArguments();
        if (args == null) return;

        String torrentPage = args.getString(ARG_PARAMS);
        viewModel.loadTorrents(torrentPage);
    }

    @Nullable
    @Override
    protected View onCreateContentView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_torrent_selection, container, false);

        progressBar = view.findViewById(R.id.progress_bar);
        errorTextView = view.findViewById(R.id.error_text_view);
        recyclerView = view.findViewById(R.id.recycler_view_torrents);

        progressBar.setVisibility(View.VISIBLE);
        errorTextView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        recyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        initObservers();

        return view;
    }

    private void initObservers() {
        viewModel.getTorrentList().observe(getViewLifecycleOwner(), result -> {
            adapter.submitList(result);
        });
        viewModel.getLoadSate().observe(getViewLifecycleOwner(), loadState -> {
            if (progressBar == null || errorTextView == null || recyclerView == null) {
                return;
            }

            switch (loadState) {
                case DONE:
                    progressBar.setVisibility(View.GONE);
                    errorTextView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    break;
                case LOADING:
                    progressBar.setVisibility(View.VISIBLE);
                    errorTextView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    break;
                case ERROR:
                    progressBar.setVisibility(View.GONE);
                    errorTextView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    break;
            }
        });
    }

    @Override
    protected void onContentViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onContentViewCreated(view, savedInstanceState);

        setTitle(R.string.download_torrent);
        getPositiveButton().setVisibility(View.GONE);
        getNegativeButton().setOnClickListener(v -> dismiss());

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TorrentListAdapter(listener);
        recyclerView.setAdapter(adapter);
    }

    public void setListener(OnTorrentClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null && viewModel.getLoadSate().getValue() != null) {
            viewModel.loadSate.postValue(viewModel.getLoadSate().getValue());
        }
    }

}

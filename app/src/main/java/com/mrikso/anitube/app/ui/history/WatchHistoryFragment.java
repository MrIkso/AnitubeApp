package com.mrikso.anitube.app.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.adapters.WatchHistoryAdapter;
import com.mrikso.anitube.app.databinding.FragmentWatchHistoryBinding;
import com.mrikso.anitube.app.ui.library.LibraryFragmentDirections;
import com.mrikso.anitube.app.utils.DialogUtils;
import com.mrikso.anitube.app.utils.GlideLoadUtils;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WatchHistoryFragment extends Fragment {

    private FragmentWatchHistoryBinding binding;
    private WatchHistoryFragmentViewModel viewModel;

    private WatchHistoryAdapter historyAdapter;

    public static WatchHistoryFragment newInstance() {
        WatchHistoryFragment fragment = new WatchHistoryFragment();
        return fragment;
    }

    public WatchHistoryFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(WatchHistoryFragmentViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWatchHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        initObservers();
        initViews();
    }

    private void initViews() {
        historyAdapter = new WatchHistoryAdapter(GlideLoadUtils.getGlide(requireContext()));
        historyAdapter.setOnItemClickListener((link) -> {
            openDetailsFragment(link);
        });

        historyAdapter.setLongItemClickListener(id -> {
            DialogUtils.showConfirmation(
                    requireContext(),
                    R.string.dialog_confirm_title,
                    R.string.dialog_confirm_delete_from_history,
                    () -> viewModel.deleteItem(id));
        });

        //        episodesAdapter.setOnLongItemClickListener(
        //                (episode, url) -> {
        //                    currentEpisode = episode;
        //                    MenuBottomSheet bottomSheet =
        //                            new MenuBottomSheet.Builder()
        //                                    .setMenuRes(R.menu.episode_menu)
        //                                    .seHiddenItems(
        //                                            new
        // ArrayList<>(List.of(R.id.action_set_watch_status)))
        //                                    .closeAfterSelect(true)
        //                                    .build();
        //                    bottomSheet.setOnSelectMenuItemListener((i, id) -> handleMenuClick(id,
        // url));
        //                    bottomSheet.show(this);
        //                });
        binding.historyList.setAdapter(historyAdapter);
    }

    private void initObservers() {

        viewModel.getLoadState().observe(getViewLifecycleOwner(), result -> {
            switch (result) {
                case ERROR:
                    binding.content.setVisibility(View.GONE);
                    binding.loadStateLayout.progressBar.setVisibility(View.GONE);
                    binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
                    break;
                case LOADING:
                    binding.content.setVisibility(View.GONE);
                    binding.loadStateLayout.progressBar.setVisibility(View.VISIBLE);
                    binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
                    break;
                case DONE:
                    binding.loadStateLayout.progressBar.setVisibility(View.GONE);
                    binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
                    break;
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                binding.content.setVisibility(View.GONE);
                binding.loadStateLayout.progressBar.setVisibility(View.GONE);
                binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
                binding.loadStateLayout.errorMessage.setText(result);
            }
        });

        viewModel.getHistory().observe(getViewLifecycleOwner(), result -> {
            if (result.isEmpty()) {
                showNoDataState();
            } else {
                historyAdapter.submitList(result);
                binding.content.setVisibility(View.VISIBLE);
                binding.loadStateLayout.progressBar.setVisibility(View.GONE);
                binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        historyAdapter = null;
    }

    private void showNoDataState() {
        binding.loadStateLayout.ivIcon.setImageResource(R.drawable.image_no_data);
        binding.loadStateLayout.errorMessageTitle.setText(R.string.state_no_data);
        binding.loadStateLayout.errorMessage.setText(R.string.state_no_data_history_desc);

        binding.content.setVisibility(View.GONE);
        binding.loadStateLayout.progressBar.setVisibility(View.GONE);
        binding.loadStateLayout.buttonLl.setVisibility(View.GONE);
        binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
    }

    private void openDetailsFragment(final String link) {
        LibraryFragmentDirections.ActionNavLibraryToNavDetails action =
                LibraryFragmentDirections.actionNavLibraryToNavDetails(link);
        Navigation.findNavController(requireView()).navigate(action);
    }
}

package com.mrikso.anitube.app.ui.watch;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.mrikso.anitube.app.adapters.EpisodesAdapter;
import com.mrikso.anitube.app.databinding.ChipGroupItemBinding;
import com.mrikso.anitube.app.databinding.FragmentWatchBinding;
import com.mrikso.anitube.app.model.DubStatusModel;
import com.mrikso.anitube.app.model.EpisodeModel;
import com.mrikso.anitube.app.model.PlayerModel;
import com.mrikso.anitube.app.model.VoicerModel;

import dagger.hilt.android.AndroidEntryPoint;

import java.util.List;
import java.util.stream.Collectors;

@AndroidEntryPoint
public class WatchAnimeFragment extends Fragment {

    private FragmentWatchBinding binding;
    private WatchAnimeFragmentViewModel viewModel;
    private EpisodesAdapter episodesAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(WatchAnimeFragmentViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentWatchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        initObservers();
        initViews();
		loadPlaylist();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        episodesAdapter = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void initObservers() {

        viewModel
                .getLoadState()
                .observe(
                        getViewLifecycleOwner(),
                        result -> {
                            switch (result) {
                                case ERROR:
                                    binding.loadStateLayout.progressBar.setVisibility(View.GONE);
                                    binding.nestedScrollView.setVisibility(View.GONE);
                                    binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
                                    break;
                                case LOADING:
                                    binding.nestedScrollView.setVisibility(View.GONE);
                                    binding.loadStateLayout.progressBar.setVisibility(View.VISIBLE);
                                    binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
                                    break;
                                case DONE:
                                    binding.loadStateLayout.progressBar.setVisibility(View.GONE);
                                    binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
                                    break;
                            }
                        });

        viewModel
                .getVoicersPlayList()
                .observe(
                        getViewLifecycleOwner(),
                        result -> {
                            if (result != null && !result.isEmpty()) {
                                showPlaylist(result);
                            } else {

                            }
                        });

        viewModel
                .getDubStatusPlayList()
                .observe(
                        getViewLifecycleOwner(),
                        result -> {
                            if (result != null && !result.isEmpty()) {
                                showDubPlaylist(result);
                            } else {

                            }
                        });

        viewModel
                .getPlayersPlayList()
                .observe(
                        getViewLifecycleOwner(),
                        result -> {
                            if (result != null && !result.isEmpty()) {
                                showPlayers(result);
                            } else {

                            }
                        });

        viewModel
                .getErrorMessage()
                .observe(
                        getViewLifecycleOwner(),
                        result -> {
                            if (result != null) {
                                binding.loadStateLayout.errorMessageTv.setText(result);
                            }
                        });

        viewModel
                .getDirectVideoUrl()
                .observe(
                        getViewLifecycleOwner(),
                        result -> {
                            if (result != null) {
                                openPlayerActivity(result.second);
                            }
                        });
    }

    private void showDubPlaylist(final List<DubStatusModel> dubs) {
        if (dubs != null && !dubs.isEmpty()) {
            ChipGroup chipGroup = binding.dubsChipGroup;
            chipGroup.setVisibility(View.VISIBLE);
            chipGroup.removeAllViews();
            chipGroup.setSingleSelection(true);
            for (DubStatusModel dub : dubs) {
                Chip chip =
                        ChipGroupItemBinding.inflate(getLayoutInflater(), chipGroup, false)
                                .getRoot();
                chip.setText(dub.getName());
                chip.setCheckable(true);
                chip.setClickable(true);
                chip.setOnClickListener(v -> showPlaylist(dub.getVoicers()));
                chipGroup.addView(chip);
            }

            chipGroup.check(chipGroup.getChildAt(0).getId());
            showPlaylist(dubs.get(0).getVoicers());
        }
    }

    private void showPlaylist(final List<VoicerModel> result) {
        binding.dubTextTil.setVisibility(View.VISIBLE);
        binding.nestedScrollView.setVisibility(View.VISIBLE);
        List<String> translationList =
                result.stream().map(VoicerModel::getName).collect(Collectors.toList());

        ArrayAdapter<String> translationAdaptrer =
                new ArrayAdapter<>(
                        requireContext(), android.R.layout.simple_list_item_1, translationList);

        AutoCompleteTextView translationsAutocomplete = binding.translations;
        translationsAutocomplete.setAdapter(translationAdaptrer);
        translationsAutocomplete.setOnItemClickListener(
                (parent, view, position, id) -> {
                    List<PlayerModel> players = result.get(position).getPlayers();
                    List<EpisodeModel> episodes = result.get(position).getEpisodes();
                    showPlayersOrEpisoder(players, episodes);
                });

        translationsAutocomplete.setText(translationList.get(0), false);
        showPlayersOrEpisoder(result.get(0).getPlayers(), result.get(0).getEpisodes());
    }

    private void showPlayersOrEpisoder(List<PlayerModel> players, List<EpisodeModel> episodes) {
        if (players != null && !players.isEmpty()) {
            showPlayers(players);
        }
        if (episodes != null && !episodes.isEmpty()) {
            showEpisodes(episodes);
        }
    }

    private void showPlayers(List<PlayerModel> players) {
        if (players != null && !players.isEmpty()) {
            ChipGroup chipGroup = binding.playersChipGroup;
            chipGroup.setVisibility(View.VISIBLE);
            chipGroup.removeAllViews();
            chipGroup.setSingleSelection(true);
            for (PlayerModel player : players) {
                Chip chip =
                        ChipGroupItemBinding.inflate(getLayoutInflater(), chipGroup, false)
                                .getRoot();
                chip.setText(player.getName());
                chip.setCheckable(true);
                chip.setClickable(true);
                chip.setOnClickListener(v -> showEpisodes(player.getEpisodes()));
                chipGroup.addView(chip);
            }

            chipGroup.check(chipGroup.getChildAt(0).getId());
            showEpisodes(players.get(0).getEpisodes());
        }
    }

    private void showEpisodes(List<EpisodeModel> episodes) {
        binding.llSort.setVisibility(View.VISIBLE);

        requireActivity()
                .runOnUiThread(
                        () -> {
                            episodesAdapter.setResults(episodes);
                        });
    }

    private void initViews() {
        binding.ibBack.setOnClickListener(
                v -> {
                    Navigation.findNavController(requireView()).popBackStack();
                });

        binding.loadStateLayout.repeat.setOnClickListener(v -> loadPlaylist());

        episodesAdapter = new EpisodesAdapter();
        episodesAdapter.setOnItemClickListener(url -> startPlay(url));
        binding.rvEpisodes.setAdapter(episodesAdapter);
    }
	
	private void reloadPlaylist(){
		WatchAnimeFragmentArgs arg = WatchAnimeFragmentArgs.fromBundle(getArguments());
        binding.tvAnimeTitle.setText(arg.getTitle());
        viewModel.loadData(arg.getId(), arg.getUrl());
	}

    private void loadPlaylist() {
        WatchAnimeFragmentArgs arg = WatchAnimeFragmentArgs.fromBundle(getArguments());
        binding.tvAnimeTitle.setText(arg.getTitle());
        viewModel.loadPlaylist(arg.getId(), arg.getUrl());
    }

    private void startPlay(String url) {
        viewModel.startPlay(url);
    }

    private void openPlayerActivity(String url) {
        Toast.makeText(requireContext(), url, Toast.LENGTH_LONG).show();
    }
}

package com.mrikso.anitube.app.ui.watch;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.chip.Chip;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.adapters.EpisodesAdapter;
import com.mrikso.anitube.app.databinding.FragmentWatchBinding;
import com.mrikso.anitube.app.databinding.ItemChipBinding;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.VideoLinksModel;
import com.mrikso.anitube.app.parser.video.model.EpisodeModel;
import com.mrikso.anitube.app.parser.video.model.PlayerModel;
import com.mrikso.anitube.app.ui.dialogs.UnsupportedVideoSourceDialog;
import com.mrikso.anitube.app.utils.DialogUtils;
import com.mrikso.anitube.app.view.TreeViewGroup;
import com.mrikso.anitube.app.viewmodel.SharedViewModel;
import com.mrikso.treeview.TreeItem;

import dagger.hilt.android.AndroidEntryPoint;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

@AndroidEntryPoint
public class WatchAnimeFragment extends Fragment implements TreeViewGroup.OnTreeRestoreListener {
    private final CompositeDisposable disposables = new CompositeDisposable();

    private FragmentWatchBinding binding;
    private WatchAnimeFragmentViewModel viewModel;
    private SharedViewModel sharedViewModel;
    private EpisodesAdapter episodesAdapter;
    private int currentEpisode;
    private AlertDialog progressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(WatchAnimeFragmentViewModel.class);
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        loadPlaylist();
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
                                    binding.nestedScrollView.setVisibility(View.GONE);
                                    binding.loadStateLayout.progressBar.setVisibility(View.GONE);
                                    binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
                                    break;
                                case LOADING:
                                    binding.nestedScrollView.setVisibility(View.GONE);
                                    binding.loadStateLayout.progressBar.setVisibility(View.VISIBLE);
                                    binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
                                    break;
                                case DONE:
                                    binding.nestedScrollView.setVisibility(View.VISIBLE);
                                    binding.loadStateLayout.progressBar.setVisibility(View.GONE);
                                    binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
                                    break;
                            }
                        });
        /*
                viewModel
                        .getVoicersPlayList()
                        .observe(
                                getViewLifecycleOwner(),
                                result -> {
                                    if (result != null && !result.isEmpty()) {
                                        showPlaylist(result);
                                    } else {
                                        showNoDataScreen();
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
                                        showNoDataScreen();
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
                                        showNoDataScreen();
                                    }
                                });
        */
        viewModel
                .getPlaylistTree()
                .observe(
                        getViewLifecycleOwner(),
                        result -> {
                            if (result != null) {
                                showTreeData(result);
                            }
                        });
        viewModel
                .getErrorMessage()
                .observe(
                        getViewLifecycleOwner(),
                        result -> {
                            if (result != null) {
                                binding.nestedScrollView.setVisibility(View.GONE);
                                binding.loadStateLayout.progressBar.setVisibility(View.GONE);
                                binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
                                binding.loadStateLayout.errorMessageTv.setText(result);
                            }
                        });
    }

    private void showTreeData(final TreeItem<PlayerModel> root) {
        // VideoSourceTreeAdapter adapter = new VideoSourceTreeAdapter();

        binding.treeView.setRoot(root, true);

        //  binding.treeView.setLayoutManager(
        //           new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,
        // false));
        //  binding.treeView.setAdapter(adapter);
        /*
                ChipGroup chipGroup =
                        ItemChipGroupBinding.inflate(getLayoutInflater(), root, false).getRoot();
                chipGroup.removeAllViews();
                chipGroup.setSingleSelection(true);
                for (TreeNode<PlayerModel> model : result.getChildren()) {
                    chipGroup.addView(createChip(model.getData().getName(), chipGroup, v -> {showTreeData(model);}));
                    if(model.getData().getEpisodes()!= null && !model.getData().getEpisodes().isEmpty()){
                        showEpisodes(model.getData().getEpisodes());
                    }
        //            if (!model.getChildren().isEmpty()) {
        //                ChipGroup chipGroup1 =
        //                        ItemChipGroupBinding.inflate(getLayoutInflater(), root, false)
        //                                .getRoot();
        //                chipGroup1.removeAllViews();
        //                chipGroup1.setSingleSelection(true);
        //                for (TreeNode<PlayerModel> children : model.getChildren()) {
        //                    chipGroup1.addView(createChip(children.getData().getName(), chipGroup1, v -> {showEpisodes(children.getData().getEpisodes());}));
        //                }
        //
        //				root.addView(chipGroup1);
        //            }
                }
                root.addView(chipGroup);
                */
    }

    private Chip createChip(String name, ViewGroup group, View.OnClickListener listener) {
        Chip chip = ItemChipBinding.inflate(getLayoutInflater(), group, false).getRoot();
        chip.setText(name);
        chip.setClickable(true);
        chip.setOnClickListener(listener);
        return chip;
    }
    /*
        private void createChips() {
            if (players != null && !players.isEmpty()) {
                ChipGroup chipGroup =
                        ChipGroupItemBinding.inflate(getLayoutInflater(), requireView(), false)
                                .getRoot();
                chipGroup.removeAllViews();
                chipGroup.setSingleSelection(true);
                for (PlayerModel player : players) {
                    Chip chip =
                            ItemChipBinding.inflate(getLayoutInflater(), chipGroup, false).getRoot();
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
    */
    private void initViews() {
        WatchAnimeFragmentArgs arg = WatchAnimeFragmentArgs.fromBundle(getArguments());
        binding.tvAnimeTitle.setText(arg.getTitle());
        binding.ibBack.setOnClickListener(
                v -> {
                    Navigation.findNavController(requireView()).popBackStack();
                });

        binding.loadStateLayout.repeat.setOnClickListener(v -> loadPlaylist());

        episodesAdapter = new EpisodesAdapter();
        episodesAdapter.setOnItemClickListener(
                (episode, url) -> {
                    currentEpisode = episode;
                    startPlay(url);
                });
        binding.rvEpisodes.setAdapter(episodesAdapter);

        binding.treeView.setTreeItemClickListener(
                item -> {
                    if (!item.isExpandable()) {
                        showEpisodes(item.getValue().getEpisodes());
                    }
                });

        binding.treeView.setTreeRestoreListener(this);
    }

    /*
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
               ChipGroup chipGroup = ChipGroupItemBinding.inflate(getLayoutInflater(), requireView(), false)
                                   .getRoot();
               chipGroup.removeAllViews();
               chipGroup.setSingleSelection(true);
               for (PlayerModel player : players) {
                   Chip chip =
                           ItemChipBinding.inflate(getLayoutInflater(), chipGroup, false)
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
    */
    // todo потрібно переробити
    private void showEpisodes(List<EpisodeModel> episodes) {
        binding.llSort.setVisibility(View.VISIBLE);

        episodesAdapter.submitList(episodes);
    }

    private void showNoDataScreen() {}

    private void reloadPlaylist() {
        WatchAnimeFragmentArgs arg = WatchAnimeFragmentArgs.fromBundle(getArguments());
        viewModel.reloadPlaylist(arg.getIsHavePlaylistsAjax(), arg.getId(), arg.getUrl());
    }

    private void loadPlaylist() {
        WatchAnimeFragmentArgs arg = WatchAnimeFragmentArgs.fromBundle(getArguments());
        viewModel.loadPlaylist(arg.getIsHavePlaylistsAjax(), arg.getId(), arg.getUrl());
    }

    private void startPlay(String url) {

        Disposable disposable =
                sharedViewModel
                        .loadData(url)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(
                                v -> {
                                    progressDialog =
                                            DialogUtils.getProDialog(
                                                    requireContext(), R.string.loading_episode);
                                })
                        .subscribeWith(
                                new DisposableSingleObserver<Pair<LoadState, VideoLinksModel>>() {
                                    @Override
                                    public void onSuccess(Pair<LoadState, VideoLinksModel> result) {
                                        DialogUtils.cancelDialog(progressDialog);
                                        if (result.first == LoadState.DONE) {
                                            openPlayerActivity(result.second);
                                        } else {
                                            UnsupportedVideoSourceDialog.show(
                                                    requireContext(), result.second.getIfRameUrl());
                                        }
                                    }

                                    @Override
                                    public void onError(Throwable throwable) {
                                        DialogUtils.cancelDialog(progressDialog);
                                        throwable.printStackTrace();
                                    }
                                });
        disposables.add(disposable);
    }

    private void openPlayerActivity(VideoLinksModel model) {
        List<EpisodeModel> listEpisodes = episodesAdapter.getCurrentList();

        HashMap<Integer, EpisodeModel> map = new HashMap<>();
        IntStream.range(0, listEpisodes.size()).forEach(i -> map.put(i, listEpisodes.get(i)));

        WatchAnimeFragmentArgs arg = WatchAnimeFragmentArgs.fromBundle(getArguments());
        WatchAnimeFragmentDirections.ActionNavWatchToNavPlayerActivity action =
                WatchAnimeFragmentDirections.actionNavWatchToNavPlayerActivity(
                        arg.getTitle(),
                        listEpisodes.get(currentEpisode).getName(),
                        currentEpisode + 1,
                        model,
                        map);
        Navigation.findNavController(requireView()).navigate(action);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.dispose();
    }

    @Override
    public void onRestored(TreeItem<PlayerModel> item) {
        Log.i("WatchAnimeFragment", "onRestore callled");
        if (!item.isExpandable()
                && item.getValue().getEpisodes() != null
                && !item.getValue().getEpisodes().isEmpty())
            showEpisodes(item.getValue().getEpisodes());
    }
}

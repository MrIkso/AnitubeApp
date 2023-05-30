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
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.blankj.utilcode.util.ClipboardUtils;
import com.google.android.material.chip.Chip;
import com.mazenrashed.bottomsheetmenulib.MenuBottomSheet;
import com.mrikso.anitube.app.App;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.adapters.EpisodesAdapter;
import com.mrikso.anitube.app.databinding.FragmentWatchBinding;
import com.mrikso.anitube.app.databinding.ItemChipBinding;
import com.mrikso.anitube.app.model.BaseAnimeModel;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.VideoLinksModel;
import com.mrikso.anitube.app.parser.video.model.EpisodeModel;
import com.mrikso.anitube.app.parser.video.model.PlayerModel;
import com.mrikso.anitube.app.ui.dialogs.UnsupportedVideoSourceDialog;
import com.mrikso.anitube.app.utils.DialogUtils;
import com.mrikso.anitube.app.utils.IntentUtils;
import com.mrikso.anitube.app.utils.PreferencesHelper;
import com.mrikso.anitube.app.view.TreeViewGroup;
import com.mrikso.anitube.app.viewmodel.ListRepository;
import com.mrikso.anitube.app.viewmodel.SharedViewModel;
import com.mrikso.treeview.TreeItem;

import dagger.hilt.android.AndroidEntryPoint;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

@AndroidEntryPoint
public class WatchAnimeFragment extends Fragment
        implements EpisodesAdapter.OnItemClickListener, TreeViewGroup.OnTreeRestoreListener {
    private final CompositeDisposable disposables = new CompositeDisposable();

    private FragmentWatchBinding binding;
    private WatchAnimeFragmentViewModel viewModel;
    private SharedViewModel sharedViewModel;
    private EpisodesAdapter episodesAdapter;
    private int currentEpisode;
    private boolean isReverse;
    private String episodePath;
    private AlertDialog progressDialog;
    private BaseAnimeModel animeModel;
    PreferencesHelper helper;
    private ListRepository listRepo;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(WatchAnimeFragmentViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        listRepo = ListRepository.getInstance();
        loadPlaylist();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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

        viewModel.getLoadState().observe(getViewLifecycleOwner(), result -> {
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
        viewModel.getPlaylistTree().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                showTreeData(result);
            }
        });
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                binding.nestedScrollView.setVisibility(View.GONE);
                binding.loadStateLayout.progressBar.setVisibility(View.GONE);
                binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
                binding.loadStateLayout.errorMessage.setText(result);
            }
        });

        disposables.add(listRepo.getData()
                .subscribeOn(Schedulers.io())
                .subscribe(
                        result -> {
                            if (result != null) {
                                episodesAdapter.setData(result);
                                if (isReverse) {
                                    episodesAdapter.reverseList();
                                }
                            }
                        }, // OnNext
                        Throwable::printStackTrace, // OnError
                        () -> {} // OnCompleted
                        ));
    }

    private void showTreeData(final TreeItem<PlayerModel> root) {
        binding.treeView.setRoot(root, true);
    }

    private Chip createChip(String name, ViewGroup group, View.OnClickListener listener) {
        Chip chip = ItemChipBinding.inflate(getLayoutInflater(), group, false).getRoot();
        chip.setText(name);
        chip.setClickable(true);
        chip.setOnClickListener(listener);
        return chip;
    }

    private void initViews() {
        WatchAnimeFragmentArgs arg = WatchAnimeFragmentArgs.fromBundle(getArguments());
        helper = App.getApplication().getPreferenceHelper();
        isReverse = helper.isReverseEpisodeList();

        showReverseButton(isReverse);
        animeModel = new BaseAnimeModel(arg.getId(), arg.getTitle(), arg.getPosterUrl(), arg.getUrl());
        binding.tvAnimeTitle.setText(arg.getTitle());
        binding.ibBack.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).popBackStack();
        });

        binding.loadStateLayout.repeat.setOnClickListener(v -> loadPlaylist());

        episodesAdapter = new EpisodesAdapter();
        episodesAdapter.setOnItemClickListener(this);
        binding.rvEpisodes.setAdapter(episodesAdapter);

        binding.treeView.setTreeItemClickListener(item -> {
            if (!item.isExpandable()) {
                showEpisodes(item);
            }
        });

        binding.treeView.setTreeRestoreListener(this);

        binding.ibSort.setOnClickListener(v -> {
            isReverse = !isReverse;
            helper.setReverseEpisodeList(isReverse);
            showReverseButton(isReverse);
            episodesAdapter.reverseList();
        });
    }

    private void handleMenuClick(int resId, String url) {
        switch (resId) {
            case R.id.action_share:
                IntentUtils.shareLink(requireContext(), url);
                break;
            case R.id.action_copy_url:
                ClipboardUtils.copyText(url);
                break;
            case R.id.action_open_in_browser:
                IntentUtils.openInBrowser(requireContext(), url);
                break;
            case R.id.action_set_watch_status:
                sharedViewModel.addOrUpdateWatchedEpisode(currentEpisode, true, animeModel);
                break;
            case R.id.action_set_unwatch_status:
                sharedViewModel.addOrUpdateWatchedEpisode(currentEpisode, false, animeModel);
                break;
        }
    }

    //    private void addOrUpdateWatchedEpisode() {
    //        EpisodeModel episodeModel = episodesAdapter.getCurrentList().get(currentEpisode);
    //        episodeModel.setIsWatched(!episodeModel.isWatched());
    //        episodesAdapter.updateItem(currentEpisode, episodeModel);
    //        viewModel.addOrUpdateWatchedEpisode(animeModel, episodeModel);
    //    }
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
    private void showEpisodes(TreeItem<PlayerModel> item) {
        binding.titleLayout.setVisibility(View.VISIBLE);
        List<EpisodeModel> episodes = item.getValue().getEpisodes();
        String episodeHint = getResources().getQuantityString(R.plurals.episode_hint, episodes.size(), episodes.size());
        episodePath = binding.treeView.getPath(item);
        binding.summary.setText(String.format("%s, %s", episodeHint, episodePath));

        listRepo.setEpisodes(episodes);
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

        Disposable disposable = sharedViewModel
                .loadData(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(v -> {
                    progressDialog = DialogUtils.getProDialog(requireContext(), R.string.loading_episode);
                })
                .subscribeWith(new DisposableSingleObserver<Pair<LoadState, VideoLinksModel>>() {
                    @Override
                    public void onSuccess(Pair<LoadState, VideoLinksModel> result) {
                        DialogUtils.cancelDialog(progressDialog);
                        if (result.first == LoadState.DONE) {
                            openPlayerActivity(result.second);
                        } else {
                            UnsupportedVideoSourceDialog.show(requireContext(), result.second.getIfRameUrl());
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
                        map,
                        animeModel,
                        episodePath);
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
                && !item.getValue().getEpisodes().isEmpty()) showEpisodes(item);
    }

    private void showReverseButton(boolean isReverse) {
        binding.sort.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), isReverse ? R.drawable.ic_down : R.drawable.ic_up));
    }

    @Override
    public void onEpisodeItemSelected(int episodeNumber, String url) {
        if (isReverse) {
            currentEpisode = episodesAdapter.getOriginalPosition(episodeNumber);
        } else {
            currentEpisode = episodeNumber;
        }
        startPlay(url);
    }

    @Override
    public void onEpisodeItemLongClicked(int episodeNumber, String url) {
        if (isReverse) {
            currentEpisode = episodesAdapter.getOriginalPosition(episodeNumber);
        } else {
            currentEpisode = episodeNumber;
        }

        MenuBottomSheet bottomSheet = new MenuBottomSheet.Builder()
                .setMenuRes(R.menu.episode_menu)
                .seHiddenItems(
                        episodesAdapter.getCurrentList().get(currentEpisode).isWatched()
                                ? new ArrayList<>(List.of(R.id.action_set_watch_status))
                                : new ArrayList<>(List.of(R.id.action_set_unwatch_status)))
                .closeAfterSelect(true)
                .build();
        bottomSheet.setOnSelectMenuItemListener((i, id) -> handleMenuClick(id, url));
        bottomSheet.show(this);
    }
}

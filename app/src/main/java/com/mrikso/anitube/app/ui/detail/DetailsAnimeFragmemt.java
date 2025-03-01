package com.mrikso.anitube.app.ui.detail;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.blankj.utilcode.util.EncodeUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.common.base.Strings;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.adapters.BaseAnimeAdapter;
import com.mrikso.anitube.app.adapters.FranchisesAdapter;
import com.mrikso.anitube.app.adapters.ScreenshotsAdapter;
import com.mrikso.anitube.app.databinding.FragmentDetailsAnimeBinding;
import com.mrikso.anitube.app.databinding.ItemChipBinding;
import com.mrikso.anitube.app.databinding.ItemDetailsInfoRowBinding;
import com.mrikso.anitube.app.databinding.LayoutReleaseActionBinding;
import com.mrikso.anitube.app.interfaces.OnTorrentClickListener;
import com.mrikso.anitube.app.model.AnimeDetailsModel;
import com.mrikso.anitube.app.model.AnimeMobileDetailsModel;
import com.mrikso.anitube.app.model.BaseAnimeModel;
import com.mrikso.anitube.app.model.DubbersTeam;
import com.mrikso.anitube.app.model.FranchiseModel;
import com.mrikso.anitube.app.model.ScreenshotModel;
import com.mrikso.anitube.app.model.SimpleModel;
import com.mrikso.anitube.app.model.WatchAnimeStatusModel;
import com.mrikso.anitube.app.ui.dialogs.ChangeAnimeStatusDialog;
import com.mrikso.anitube.app.ui.dialogs.TorrentSelectionDialog;
import com.mrikso.anitube.app.utils.DialogUtils;
import com.mrikso.anitube.app.utils.DownloadUtils;
import com.mrikso.anitube.app.utils.IntentUtils;
import com.mrikso.anitube.app.utils.ParserUtils;
import com.mrikso.anitube.app.utils.PreferencesHelper;
import com.mrikso.anitube.app.utils.StringUtils;
import com.mrikso.anitube.app.utils.ViewExtKt;
import com.mrikso.anitube.app.utils.ViewUtils;

import org.jsoup.internal.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DetailsAnimeFragmemt extends Fragment
        implements BaseAnimeAdapter.OnItemClickListener, ScreenshotsAdapter.OnItemClickListener {
    public static final String TAG = "DetailsAnimeFragmemt";

    private DetailsAnimeFragmemtViewModel viewModel;
    private FragmentDetailsAnimeBinding binding;
    private ScreenshotsAdapter screenshotsAdapter;
    private BaseAnimeAdapter similarAnimeAdapter;
    private FranchisesAdapter franchisesAdapter;
    private List<ScreenshotModel> screenshotsList = null;
    private int mode = 0;
    private int animeId;
    private int tableRowIndex = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DetailsAnimeFragmemtViewModel.class);
        loadPage();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailsAnimeBinding.inflate(inflater, container, false);
        /*
            Window window = getActivity().getWindow();
               window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.setStatusBarColor(Color.TRANSPARENT);
               window.setNavigationBarColor(Color.TRANSPARENT);
               window.setNavigationBarContrastEnforced(false);
               window.setDecorFitsSystemWindows(false);
               window.getDecorView().setSystemUiVisibility(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
               binding.getRoot().setFitsSystemWindows(false);
            */
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
        screenshotsAdapter = null;
        similarAnimeAdapter = null;
        screenshotsList = null;
        franchisesAdapter = null;
        tableRowIndex = 0;
    }

    private void initObservers() {
        viewModel.getLoadState().observe(getViewLifecycleOwner(), result -> {
            switch (result) {
                case ERROR:
                    binding.loadStateLayout.progressBar.setVisibility(View.GONE);
                    binding.clContent.setVisibility(View.GONE);
                    binding.fabWatch.setVisibility(View.GONE);
                    binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
                    break;
                case LOADING:
                    binding.clContent.setVisibility(View.GONE);
                    binding.fabWatch.setVisibility(View.GONE);
                    binding.loadStateLayout.progressBar.setVisibility(View.VISIBLE);
                    binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
                    break;
                case DONE:
                    binding.loadStateLayout.progressBar.setVisibility(View.GONE);
                    binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
                    break;
                case NO_NETWORK:
                    binding.clContent.setVisibility(View.GONE);
                    binding.loadStateLayout.progressBar.setVisibility(View.GONE);
                    binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
                    binding.loadStateLayout.errorMessage.setText(R.string.message_error_no_internet);
                    break;
            }
        });

        viewModel.getDetails().observe(getViewLifecycleOwner(), results -> {
            if (results != null) {
                showDetails(results);
            } else {
                binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getMobileDetails().observe(getViewLifecycleOwner(), results -> {
            if (results != null) {
                showMobileDetails(results);
            } else {
               // binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initViews() {
        binding.clContent.setVisibility(View.GONE);
        binding.fabBack.setOnClickListener(
                v -> Navigation.findNavController(requireView()).popBackStack());
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            reloadPage();
            binding.swipeRefreshLayout.setRefreshing(false);
        });
        binding.loadStateLayout.repeat.setOnClickListener(v -> reloadPage());

        screenshotsAdapter = new ScreenshotsAdapter();
        screenshotsAdapter.setOnItemClickListener(this);
        binding.layoutScreenshots.recyclerViewScreenshots.setAdapter(screenshotsAdapter);

        franchisesAdapter = new FranchisesAdapter(this::openDetailsFragment);
        binding.layoutFranchises.recyclerViewFranchies.setAdapter(franchisesAdapter);

        similarAnimeAdapter = new BaseAnimeAdapter();
        similarAnimeAdapter.setOnItemClickListener(this);
        binding.layoutSimilar.recyclerViewSimilar.setAdapter(similarAnimeAdapter);

        binding.btnComments.setOnClickListener(v -> {
            openCommentsFragment(animeId);
        });
    }

    private void showDetails(AnimeDetailsModel animeDetails) {
        animeId = animeDetails.getAnimeId();
        binding.clContent.setVisibility(View.VISIBLE);
        binding.fabWatch.setVisibility(View.VISIBLE);
        binding.title.setText(animeDetails.getTitle());
        String titleEng = animeDetails.getOriginalTitle();

        if (!Strings.isNullOrEmpty(titleEng)) {
            binding.titleEng.setVisibility(View.VISIBLE);
            binding.titleEng.setText(titleEng);
        }

        String posterUrl = ParserUtils.normaliseImageUrl(animeDetails.getPosterUrl());
        ViewUtils.loadImage(binding.poster, posterUrl);
        ViewUtils.loadBluredImage(binding.posterBg, posterUrl);

        String rating = animeDetails.getRating();
        if (!Strings.isNullOrEmpty(rating)) {
            binding.llScore.setVisibility(View.VISIBLE);
            binding.rbScore.setRating(Float.parseFloat(rating) / 2f);
            if (rating.endsWith(".0")) {
                rating = StringUtils.removeChars(rating, 2);
            }
            binding.tvScore.setText(String.format("%s/10", rating));
        }

        showReleaseActions(animeDetails);

        showInfoGroup(animeDetails);

        String description = animeDetails.getDescription();
        if (!Strings.isNullOrEmpty(description)) {
            binding.layoutDescription.llDescription.setVisibility(View.VISIBLE);
            var expandableTextView = binding.layoutDescription.tvDescription;
            expandableTextView.setVisibility(View.VISIBLE);
            expandableTextView.setContent(EncodeUtils.htmlDecode(description));
        }

        ScreenshotModel trailer = animeDetails.getTrailerModel();
        if (trailer != null) {
            binding.layoutTrailer.llVideo.setVisibility(View.VISIBLE);

            ViewUtils.loadImage(binding.layoutTrailer.itemTrailer.trailerPreview, trailer.getPreviewUrl());
            binding.layoutTrailer.itemTrailer.getRoot().setOnClickListener(v -> {
                IntentUtils.openInBrowser(requireContext(), trailer.getFullUrl());
            });
        }

        screenshotsList = animeDetails.getScreenshotsModel();
        if (screenshotsList != null && !screenshotsList.isEmpty()) {
            binding.layoutScreenshots.llSreenshots.setVisibility(View.VISIBLE);
            requireActivity().runOnUiThread(() -> {
                screenshotsAdapter.setResults(screenshotsList);
            });
        }

        List<BaseAnimeModel> similarAnimeList = animeDetails.getSimilarAnimeList();
        if (similarAnimeList != null && !similarAnimeList.isEmpty()) {
            binding.layoutSimilar.llSimilar.setVisibility(View.VISIBLE);
            similarAnimeAdapter.submitList(similarAnimeList);

        }

        List<FranchiseModel> franchiseAnimeList = animeDetails.getFranchiseList();
        if (franchiseAnimeList != null && !franchiseAnimeList.isEmpty() && franchiseAnimeList.size() > 1) {
            binding.layoutFranchises.llFranchise.setVisibility(View.VISIBLE);
            franchisesAdapter.submitList(franchiseAnimeList);

        }

        binding.fabWatch.setOnClickListener(v -> {
            openWatchFragment(
                    animeDetails.getAnimeId(),
                    animeDetails.getTitle(),
                    animeDetails.getPosterUrl(),
                    animeDetails.getAnimeUrl(),
                    animeDetails.isHavePlaylistsAjax());
        });
    }

    private void loadPage() {

        DetailsAnimeFragmemtArgs args = DetailsAnimeFragmemtArgs.fromBundle(getArguments());
        String url = args.getUrl();
        viewModel.loadData(url);
    }

    private void reloadPage() {
        String url = DetailsAnimeFragmemtArgs.fromBundle(getArguments()).getUrl();
        viewModel.loadAnime(url);
    }

    private void showReleaseActions(AnimeDetailsModel animeDetails) {
        LayoutReleaseActionBinding actionBinding = binding.layoutReleaseAction;
        if (PreferencesHelper.getInstance().isLogin()) {
            actionBinding.bookmarkContainer.setVisibility(View.VISIBLE);
            actionBinding.statusContainer.setVisibility(View.VISIBLE);

            showBookmarkStatus(animeDetails.isFavorites());

            actionBinding.bookmarkContainer.setOnClickListener(v -> {
                boolean isAdd = !animeDetails.isFavorites();
                if (!isAdd) {
                    DialogUtils.showConfirmation(
                            requireContext(),
                            R.string.dialog_confirm_title,
                            R.string.dialog_confirm_delete_from_bookmarks,
                            () -> addOrRemoveFromFavorites(animeDetails.getAnimeId(), isAdd));
                } else {
                    addOrRemoveFromFavorites(animeDetails.getAnimeId(), isAdd);
                }
            });

            if (animeDetails.getWatchStatusModdel() != null) {
                mode = animeDetails.getWatchStatusModdel().getViewStatus().getStatusCode();
                showReleaseWatchStatus(animeDetails.getWatchStatusModdel());
            }

            actionBinding.statusContainer.setOnClickListener(v -> {
                ChangeAnimeStatusDialog dialog = ChangeAnimeStatusDialog.newInstance(mode);
                dialog.setListener(newMode -> {
                    mode = newMode;
                    viewModel.changeAnimeStatus(animeDetails.getAnimeId(), newMode);
                    WatchAnimeStatusModel newModel = ParserUtils.getWatchModel(newMode);
                    showReleaseWatchStatus(newModel);
                });
                dialog.show(getParentFragmentManager(), ChangeAnimeStatusDialog.TAG);
            });
        }
        if (animeDetails.getTorrensList() != null
                && !animeDetails.getTorrensList().isEmpty()) {
            actionBinding.downloadTorrentContainer.setVisibility(View.VISIBLE);
            actionBinding.downloadTorrentContainer.setOnClickListener(v -> {
                TorrentSelectionDialog dialog =
                        TorrentSelectionDialog.newInstance((ArrayList) animeDetails.getTorrensList());
                dialog.setListener(new OnTorrentClickListener() {
                    @Override
                    public void onDownloadTorrent(String url) {
                        DownloadUtils.downloadFile(requireActivity(), url);
                    }

                    @Override
                    public void onDownloadByMagnet(String url) {
                        IntentUtils.openInBrowser(requireContext(), url);
                    }
                });
                dialog.show(getParentFragmentManager(), TorrentSelectionDialog.TAG);
            });
        }
        actionBinding.shareContainer.setOnClickListener(v -> {
            IntentUtils.shareLink(requireContext(), animeDetails.getAnimeUrl());
        });
    }

    private void showInfoGroup(AnimeDetailsModel animeDetails) {
        SimpleModel yearRelease = animeDetails.getReleaseYear();
        if (yearRelease != null) {
            addTableRow(R.string.year, createClickableTextView(yearRelease));
        }

        String director = animeDetails.getDirector();
        if (!StringUtil.isBlank(director)) {
            addTableRow(R.string.director, director);
        }

        String studio = animeDetails.getStudio();
        if (!StringUtil.isBlank(studio)) {
            addTableRow(R.string.studio, studio);
        }

        String episodes = animeDetails.getEpisodes();
        if (!StringUtil.isBlank(episodes)) {
            addTableRow(R.string.series, episodes);
        }

        List<SimpleModel> translators = animeDetails.getTranslators();
        if (translators != null && !translators.isEmpty()) {
            String translatorsString =
                    translators.stream().map(e -> e.getText()).collect(Collectors.joining(", "));
            addTableRow(R.string.translation, translatorsString);
        }

        SimpleModel animeSeason = animeDetails.getAnimeSeason();
        if (animeSeason != null) {
            addTableRow(R.string.anime_season, createClickableTextView(animeSeason));
        }

        String lastUpdateTime = animeDetails.getLastUpdateTime();
        if (!StringUtil.isBlank(lastUpdateTime)) {
            addTableRow(R.string.anime_last_update_status, lastUpdateTime);
        }

        showVoicers(animeDetails.getVoicers());
        showDubbers(animeDetails.getDubbersTeamList());
        showGenresGroup(animeDetails.getGenres());
    }

    private void showVoicers(List<SimpleModel> voicers) {
        if (voicers != null && !voicers.isEmpty()) {
            String dubbersString = voicers.stream().map(SimpleModel::getText).collect(Collectors.joining(", ")).trim();
            addTableRow(R.string.dubbers, dubbersString);
        }
    }

    private void showDubbers(List<DubbersTeam> dubbers) {
        if (dubbers != null && !dubbers.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (DubbersTeam dubberTeam : dubbers) {
                sb.append(dubberTeam.getDubberTeam().getText());
                sb.append(": ");
                String dubbersString =
                        dubberTeam.getDubbers().stream().map(SimpleModel::getText).collect(Collectors.joining(", "));
                sb.append(dubbersString);
                sb.append("\n");
            }
            addTableRow(R.string.dubbers, sb.toString().trim());
        }
    }

    private void showMobileDetails(AnimeMobileDetailsModel detailsModel){
        SimpleModel typeAnime = detailsModel.getAnimeType();
        if (typeAnime != null) {
            addTableRow(R.string.type, createClickableTextView(typeAnime));
        }

        String animeStatus = detailsModel.getAnimeUpdateStatus();
        if (!StringUtil.isBlank(animeStatus)) {
            addTableRow(R.string.anime_status, animeStatus);
        }
    }

    private void addTableRow(@StringRes int title, @Nullable CharSequence value) {
        TableLayout table = binding.layoutInfo.tableLayout;
        var row = ItemDetailsInfoRowBinding.inflate(getLayoutInflater(), table, false);
        row.tvTitle.setText(title);
        row.tvValue.setText(value);
        row.tvValue.setClickable(true);
        row.tvValue.setMovementMethod(LinkMovementMethod.getInstance());
        table.addView(row.getRoot(), tableRowIndex);
        tableRowIndex++;
    }

    private void showGenresGroup(List<SimpleModel> genres) {
        if (genres != null && !genres.isEmpty()) {
            binding.layoutInfo.genresChipGroup.setVisibility(View.VISIBLE);
            ChipGroup chipGroup = binding.layoutInfo.genresChipGroup;
            chipGroup.removeAllViews();
            for (SimpleModel simple : genres) {
                Chip chip = ItemChipBinding.inflate(getLayoutInflater(), chipGroup, false)
                        .getRoot();
                chip.setText(simple.getText());
                chip.setClickable(true);
                chip.setOnClickListener(v -> openSearchFragment(simple.getText(), simple.getUrl()));
                chipGroup.addView(chip);
            }
        }
    }

    @Override
    public void onBaseItemSelected(String link) {
        openDetailsFragment(link);
    }

    private void openDetailsFragment(final String url) {
        DetailsAnimeFragmemtDirections.ActinNavDetailsAnimeInfoToNavDetailsAnimeInfo action =
                DetailsAnimeFragmemtDirections.actinNavDetailsAnimeInfoToNavDetailsAnimeInfo(url);
        Navigation.findNavController(requireView()).navigate(action);
    }

    private void openWatchFragment(int id, String title, String posterUrl, String url, boolean isHavePlaylistsAjax) {
        DetailsAnimeFragmemtDirections.ActinNavDetailsAnimeInfoToNavWatch action =
                DetailsAnimeFragmemtDirections.actinNavDetailsAnimeInfoToNavWatch(
                        id, title, posterUrl, url, isHavePlaylistsAjax);
        Navigation.findNavController(requireView()).navigate(action);
    }

    private void openSearchFragment(String title, String url) {
        DetailsAnimeFragmemtDirections.ActionNavDetailsAnimeToNavSearchResult action =
                DetailsAnimeFragmemtDirections.actionNavDetailsAnimeToNavSearchResult(title, url);
        Navigation.findNavController(requireView()).navigate(action);
    }

    private void openCommentsFragment(int animeId) {
        DetailsAnimeFragmemtDirections.ActinNavDetailsAnimeInfoNavComments action =
                DetailsAnimeFragmemtDirections.actinNavDetailsAnimeInfoNavComments(animeId);
        Navigation.findNavController(requireView()).navigate(action);
    }

    @Override
    public void onScreenshotItemSelected(int position) {
        DetailsAnimeFragmemtDirections.ActionNavDetailsAnimeToNavScreenshots action =
                DetailsAnimeFragmemtDirections.actionNavDetailsAnimeToNavScreenshots(
                        position, new ArrayList<>(screenshotsList));
        Navigation.findNavController(requireView()).navigate(action);
    }

    private void createClickableTextView(TextView tv, String message, String url) {
        tv.setText(message);
        List<Pair<String, View.OnClickListener>> links = new ArrayList<>();
        links.add(new Pair<>(message, (v) -> {
            openSearchFragment(message, url);
        }));

        ViewUtils.makeLinks(tv, links);
    }

    private SpannableString createClickableTextView(SimpleModel simpleModel) {
        return createClickableTextView(simpleModel.getText(), simpleModel.getUrl());
    }

    private SpannableString createClickableTextView(String message, String url) {
        return ViewExtKt.makeLinks(message, message, (v) -> {
            openSearchFragment(message, url);
        });
    }

    private void addOrRemoveFromFavorites(int animeId, boolean isAdd) {
        showBookmarkStatus(isAdd);
        viewModel.addOrRemoveFromFavorites(animeId, isAdd);
    }

    private void showBookmarkStatus(boolean isFavorite) {
        binding.layoutReleaseAction.bookmarkStatusText.setText(
                isFavorite ? R.string.release_action_remove_from_bookmarks : R.string.release_action_add_to_bookmarks);

        binding.layoutReleaseAction.bookmark.setImageDrawable(
                isFavorite
                        ? ContextCompat.getDrawable(requireContext(), R.drawable.ic_bookmark_added)
                        : ContextCompat.getDrawable(requireContext(), R.drawable.ic_bookmark_border));
    }

    private void showReleaseWatchStatus(WatchAnimeStatusModel watchStatus) {
        if (watchStatus != null) {
            binding.layoutReleaseAction.status.setImageDrawable(
                    ViewUtils.changeIconColor(requireContext(), R.drawable.ic_done, watchStatus.getColor()));

            binding.layoutReleaseAction.watchStatusText.setText(watchStatus.getStatus());
        } else {
            binding.layoutReleaseAction.status.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_add));

            binding.layoutReleaseAction.watchStatusText.setText(R.string.release_action_add_to_list);
        }
    }
}

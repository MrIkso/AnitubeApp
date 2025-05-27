package com.mrikso.anitube.app.ui.detail;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.EncodeUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.common.base.Strings;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.adapters.ActionsAdapter;
import com.mrikso.anitube.app.adapters.BaseAnimeAdapter;
import com.mrikso.anitube.app.adapters.FranchisesAdapter;
import com.mrikso.anitube.app.adapters.ScreenshotsAdapter;
import com.mrikso.anitube.app.databinding.FragmentDetailsAnimeBinding;
import com.mrikso.anitube.app.databinding.ItemChipBinding;
import com.mrikso.anitube.app.databinding.ItemDetailsInfoRowBinding;
import com.mrikso.anitube.app.interfaces.OnTorrentClickListener;
import com.mrikso.anitube.app.model.ActionItem;
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
public class DetailsAnimeFragment extends Fragment
        implements BaseAnimeAdapter.OnItemClickListener, ScreenshotsAdapter.OnItemClickListener,
        ActionsAdapter.OnItemClickListener {
    public static final String TAG = "DetailsAnimeFragment";

    private DetailsAnimeFragmentViewModel viewModel;
    private FragmentDetailsAnimeBinding binding;
    private ScreenshotsAdapter screenshotsAdapter;
    private BaseAnimeAdapter similarAnimeAdapter;
    private FranchisesAdapter franchisesAdapter;
    private List<ScreenshotModel> screenshotsList = null;
    private int currentWatchMode = 0;
    private int animeId;
    private String animeUrl;
    private AnimeDetailsModel currentAnimeDetails;
    private int tableRowIndex = 0;
    private ActionsAdapter actionsAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DetailsAnimeFragmentViewModel.class);
        loadPage();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailsAnimeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        //setupWindowInsetsListener();
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

        actionsAdapter = new ActionsAdapter(this);

        binding.layoutReleaseAction.rvActions.setLayoutManager(new LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL, false));
        binding.layoutReleaseAction.rvActions.setAdapter(actionsAdapter);
    }

    private void showDetails(AnimeDetailsModel animeDetails) {
        currentAnimeDetails = animeDetails;
        animeId = animeDetails.getAnimeId();
        animeUrl = animeDetails.getAnimeUrl();
        binding.clContent.setVisibility(View.VISIBLE);
        binding.fabWatch.setVisibility(View.VISIBLE);
        binding.title.setText(animeDetails.getTitle());
        String titleEng = animeDetails.getOriginalTitle();

        if (!Strings.isNullOrEmpty(titleEng)) {
            binding.titleEng.setVisibility(View.VISIBLE);
            binding.titleEng.setText(titleEng);
        }

        if (!PreferencesHelper.getInstance().isLoadAnimeAdditionalInfo()) {
            loadPosterImage(animeDetails.getPosterUrl());
        }

        String rating = animeDetails.getRating();
        if (!Strings.isNullOrEmpty(rating)) {
            binding.llScore.setVisibility(View.VISIBLE);
            binding.rbScore.setRating(Float.parseFloat(rating) / 2f);
            if (rating.endsWith(".0")) {
                rating = StringUtils.removeChars(rating, 2);
            }
            binding.tvScore.setText(String.format("%s/10", rating));
        }

        updateReleaseActions(animeDetails);

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
                screenshotsAdapter.submitList(screenshotsList);
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

    private void loadPosterImage(String posterUrl) {
        String posterUrlNormal = ParserUtils.normaliseImageUrl(posterUrl);
        ViewUtils.loadImage(binding.poster, posterUrlNormal);
        ViewUtils.loadBluredImage(binding.posterBg, posterUrlNormal);
    }

    private void loadPage() {
        DetailsAnimeFragmentArgs args = DetailsAnimeFragmentArgs.fromBundle(getArguments());
        String url = args.getUrl();
        viewModel.loadData(url);
    }

    private void reloadPage() {
        clearDetailsTable();
        String url = DetailsAnimeFragmentArgs.fromBundle(getArguments()).getUrl();
        viewModel.loadAnime(url);
    }

    private void clearDetailsTable() {
        tableRowIndex = 0;
        binding.layoutInfo.tableLayout.removeAllViews();
    }

    private void updateReleaseActions(AnimeDetailsModel animeDetails) {
        List<ActionItem> newActions = new ArrayList<>();

        if (PreferencesHelper.getInstance().isLogin()) {
            ActionItem statusItem = new ActionItem(
                    ActionItem.ID.STATUS,
                    R.drawable.ic_add,
                    R.string.release_action_add_to_list,
                    true
            );

            if (animeDetails.getWatchStatusModdel() != null) {
                WatchAnimeStatusModel watchStatus = animeDetails.getWatchStatusModdel();
                currentWatchMode = watchStatus.getViewStatus().getStatusCode();

                statusItem.setCurrentIconResId(R.drawable.ic_done);
                statusItem.setCurrentIconColor(watchStatus.getColor());
                statusItem.setCurrentDisplayText(watchStatus.getStatus());
            } else {
                statusItem.setCurrentIconResId(R.drawable.ic_add);
                statusItem.setCurrentIconColor(0);
                statusItem.setCurrentDisplayText(null);
            }
            newActions.add(statusItem);

            ActionItem bookmarkItem = new ActionItem(
                    ActionItem.ID.BOOKMARK,
                    animeDetails.isFavorites() ? R.drawable.ic_bookmark_added : R.drawable.ic_bookmark_border,
                    animeDetails.isFavorites() ? R.string.release_action_remove_from_bookmarks : R.string.release_action_add_to_bookmarks,
                    true
            );

            newActions.add(bookmarkItem);
        }

        newActions.add(new ActionItem(
                ActionItem.ID.SHARE,
                R.drawable.ic_share,
                R.string.action_share,
                true
        ));

        if (animeDetails.isHasTorrent()) {
            newActions.add(new ActionItem(
                    ActionItem.ID.TORRENT,
                    R.drawable.ic_torrent_file,
                    R.string.download_torrent,
                    true
            ));
        }

        newActions.add(new ActionItem(
                ActionItem.ID.COMMENT,
                R.drawable.ic_comment,
                R.string.comments,
                true
        ));

        actionsAdapter.submitList(newActions);
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
                    translators.stream().map(SimpleModel::getText).collect(Collectors.joining(", "));
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

        showVoices(animeDetails.getVoicers());
        showDubbers(animeDetails.getDubbersTeamList());
        showGenresGroup(animeDetails.getGenres());
    }

    private void showVoices(List<SimpleModel> voices) {
        if (voices != null && !voices.isEmpty()) {
            String dubbersString = voices.stream().map(SimpleModel::getText).collect(Collectors.joining(", ")).trim();
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

    private void showMobileDetails(AnimeMobileDetailsModel detailsModel) {
        if (detailsModel == null) {
            return;
        }
        SimpleModel typeAnime = detailsModel.getAnimeType();
        if (typeAnime != null) {
            addTableRow(R.string.type, createClickableTextView(typeAnime));
        }

        String animeStatus = detailsModel.getAnimeUpdateStatus();
        if (!StringUtil.isBlank(animeStatus)) {
            addTableRow(R.string.anime_status, animeStatus);
        }
        String poster = detailsModel.getPosterUrl();
        if (!StringUtil.isBlank(poster)) {
            loadPosterImage(poster);
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
        DetailsAnimeFragmentDirections.ActinNavDetailsAnimeInfoToNavDetailsAnimeInfo action =
                DetailsAnimeFragmentDirections.actinNavDetailsAnimeInfoToNavDetailsAnimeInfo(url);
        Navigation.findNavController(requireView()).navigate(action);
    }

    private void openWatchFragment(int id, String title, String posterUrl, String url, boolean isHavePlaylistsAjax) {
        DetailsAnimeFragmentDirections.ActinNavDetailsAnimeInfoToNavWatch action =
                DetailsAnimeFragmentDirections.actinNavDetailsAnimeInfoToNavWatch(
                        id, title, posterUrl, url, isHavePlaylistsAjax);
        Navigation.findNavController(requireView()).navigate(action);
    }

    private void openSearchFragment(String title, String url) {
        DetailsAnimeFragmentDirections.ActionNavDetailsAnimeToNavSearchResult action =
                DetailsAnimeFragmentDirections.actionNavDetailsAnimeToNavSearchResult(title, url);
        Navigation.findNavController(requireView()).navigate(action);
    }

    private void openCommentsFragment(int animeId) {
        DetailsAnimeFragmentDirections.ActinNavDetailsAnimeInfoNavComments action =
                DetailsAnimeFragmentDirections.actinNavDetailsAnimeInfoNavComments(animeId);
        Navigation.findNavController(requireView()).navigate(action);
    }

    @Override
    public void onScreenshotItemSelected(int position) {
        DetailsAnimeFragmentDirections.ActionNavDetailsAnimeToNavScreenshots action =
                DetailsAnimeFragmentDirections.actionNavDetailsAnimeToNavScreenshots(
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

    private void handleBookmarkClick() {
        boolean isAdding = !currentAnimeDetails.isFavorites();

        if (!isAdding) {
            DialogUtils.showConfirmation(
                    requireContext(),
                    R.string.dialog_confirm_title,
                    R.string.dialog_confirm_delete_from_bookmarks,
                    () -> addOrRemoveFromFavorites(animeId, false)
            );
        } else {
            addOrRemoveFromFavorites(animeId, true);
        }
    }

    private void addOrRemoveFromFavorites(int animeId, boolean isAdding) {
        clearDetailsTable();
        viewModel.addOrRemoveFromFavorites(animeId, isAdding);
        updateReleaseActions(currentAnimeDetails);
        Toast.makeText(requireContext(), isAdding ? "Додано до закладок!" : "Видалено з закладок!", Toast.LENGTH_SHORT).show();
    }

    private void handleChangeStatusClick() {
        ChangeAnimeStatusDialog dialog = ChangeAnimeStatusDialog.newInstance(currentWatchMode);
        dialog.setListener(newMode -> {
            currentWatchMode = newMode;
            viewModel.changeAnimeStatus(currentAnimeDetails.getAnimeId(), newMode);

            WatchAnimeStatusModel newModel = ParserUtils.getWatchModel(newMode);
            currentAnimeDetails.setWatchStatusModdel(newModel);
            updateReleaseActions(currentAnimeDetails);
        });
        dialog.show(getParentFragmentManager(), ChangeAnimeStatusDialog.TAG);
    }

    private void handleTorrentClick() {
        TorrentSelectionDialog dialog =
                TorrentSelectionDialog.newInstance(currentAnimeDetails.getTorrentPageUrl());
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
    }

    @Override
    public void onItemClick(ActionItem item) {
        switch (item.getId()) {
            case BOOKMARK:
                handleBookmarkClick();
                break;
            case STATUS:
                handleChangeStatusClick();
                break;
            case SHARE:
                IntentUtils.shareLink(requireContext(), animeUrl);
                break;
            case TORRENT:
                handleTorrentClick();
                break;
            case COMMENT:
                openCommentsFragment(animeId);
                break;
        }
    }
}

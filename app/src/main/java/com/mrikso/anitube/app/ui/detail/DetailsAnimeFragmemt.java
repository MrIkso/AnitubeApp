package com.mrikso.anitube.app.ui.detail;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ctetin.expandabletextviewlibrary.ExpandableTextView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.mrikso.anitube.app.adapters.BaseAnimeAdapter;
import com.mrikso.anitube.app.adapters.ScreenshotsAdapter;
import com.mrikso.anitube.app.databinding.ChipGroupItemBinding;
import com.mrikso.anitube.app.databinding.FragmentDetailsAnimeBinding;
import com.mrikso.anitube.app.model.AnimeDetailsModel;
import com.mrikso.anitube.app.model.BaseAnimeModel;
import com.mrikso.anitube.app.model.DubbersTeam;
import com.mrikso.anitube.app.model.ScreenshotModel;
import com.mrikso.anitube.app.model.SimpleModel;
import com.mrikso.anitube.app.network.ApiClient;

import dagger.hilt.android.AndroidEntryPoint;

import org.jsoup.internal.StringUtil;

import java.util.List;
import java.util.stream.Collectors;

@AndroidEntryPoint
public class DetailsAnimeFragmemt extends Fragment
        implements BaseAnimeAdapter.OnItemClickListener, ScreenshotsAdapter.OnItemClickListener {
    public static final String TAG = "DetailsAnimeFragmemt";

    private DetailsAnimeFragmemtViewModel viewModel;
    private FragmentDetailsAnimeBinding binding;
    private ScreenshotsAdapter screenshotsAdapter;
    private BaseAnimeAdapter similarAnimeAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DetailsAnimeFragmemtViewModel.class);
        //if (savedInstanceState != null) {

            loadPage();
      //  }
		
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailsAnimeBinding.inflate(inflater, container, false);
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
                            }
                        });

        viewModel
                .getDetails()
                .observe(
                        getViewLifecycleOwner(),
                        results -> {
                            if (results != null) {
                                showDetails(results);
                            } else {
                                binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
                            }
                        });
    }

    private void initViews() {
        binding.clContent.setVisibility(View.GONE);
        binding.fabBack.setOnClickListener(
                v -> Navigation.findNavController(requireView()).popBackStack());
        binding.swipeRefreshLayout.setOnRefreshListener(
                () -> {
                    reloadPage();
                    binding.swipeRefreshLayout.setRefreshing(false);
                });
        binding.loadStateLayout.repeat.setOnClickListener(v -> reloadPage());

        screenshotsAdapter = new ScreenshotsAdapter();
        screenshotsAdapter.setOnItemClickListener(this);
        binding.layoutScreenshots.recyclerViewScreenshots.setAdapter(screenshotsAdapter);
        similarAnimeAdapter = new BaseAnimeAdapter();
        similarAnimeAdapter.setOnItemClickListener(this);
        binding.layoutSimilar.recyclerViewSimilar.setAdapter(similarAnimeAdapter);
    }

    private void showDetails(AnimeDetailsModel animeDetails) {
        binding.clContent.setVisibility(View.VISIBLE);
        binding.fabWatch.setVisibility(View.VISIBLE);
        binding.title.setText(animeDetails.getTitle());
        String titleEng = animeDetails.getOriginalTitle();

        if (!StringUtil.isBlank(titleEng)) {
            binding.titleEng.setVisibility(View.VISIBLE);
            binding.titleEng.setText(titleEng);
        }

        String posterUrl = ApiClient.BASE_URL + animeDetails.getPosterUrl();
        loadImage(binding.poster, posterUrl);
        loadImage(binding.posterBg, posterUrl);

        String rating = animeDetails.getRating();
        if (!StringUtil.isBlank(rating)) {
            binding.llScore.setVisibility(View.VISIBLE);
            binding.tvScore.setText(rating);
            binding.rbScore.setRating(Float.parseFloat(rating));
        }

        showInfoGroup(animeDetails);

        String description = animeDetails.getDescription();
        if (!StringUtil.isBlank(description)) {
            binding.layoutDescription.llDescription.setVisibility(View.VISIBLE);
            ExpandableTextView expandableTextView = binding.layoutDescription.tvDescription;
            expandableTextView.setContent(description);
            expandableTextView.setNeedExpend(true);
        }

        List<ScreenshotModel> screenshotsList = animeDetails.getScreenshotsModel();
        if (screenshotsList != null && !screenshotsList.isEmpty()) {
            binding.layoutScreenshots.llSreenshots.setVisibility(View.VISIBLE);
            requireActivity()
                    .runOnUiThread(
                            () -> {
                                screenshotsAdapter.setResults(screenshotsList);
                            });
        }

        ScreenshotModel trailer = animeDetails.getTrailerModel();
        if (trailer != null) {
            binding.layoutTrailer.llVideo.setVisibility(View.VISIBLE);

            loadImage(binding.layoutTrailer.itemTrailer.sivScreenshot, trailer.getPreviewUrl());
            binding.layoutTrailer.itemTrailer.sivScreenshot.setOnClickListener(
                    v -> {
                        openInBrowser(requireContext(), trailer.getFullUrl());
                    });
        }

        List<BaseAnimeModel> similarAnimeList = animeDetails.getSimilarAnimeList();
        if (similarAnimeList != null && !similarAnimeList.isEmpty()) {
            binding.layoutSimilar.llSimilar.setVisibility(View.VISIBLE);
            requireActivity()
                    .runOnUiThread(
                            () -> {
                                similarAnimeAdapter.setResults(similarAnimeList);
                            });
        }

        binding.fabWatch.setOnClickListener(
                v -> {
                    //  String url = animeDetails.getAnimeUrl();
                    //  String[] splitUrl = url.split("/");
                    // int id = animeDetails.getId();
                    openWatchFragment(
                            animeDetails.getTitle(),
                            animeDetails.getId(),
                            animeDetails.getAnimeUrl());
                });
    }

    private void loadPage() {
        String url = DetailsAnimeFragmemtArgs.fromBundle(getArguments()).getUrl();
        viewModel.loadData(url);
    }
	
	private void reloadPage() {
        String url = DetailsAnimeFragmemtArgs.fromBundle(getArguments()).getUrl();
        viewModel.loadAnime(url);
    }

    private void showInfoGroup(AnimeDetailsModel animeDetails) {
        SimpleModel yearRelease = animeDetails.getReleaseYear();
        if (yearRelease != null) {
            binding.layoutInfo.yearLayout.setVisibility(View.VISIBLE);
            TextView year = binding.layoutInfo.tvYear;
            createClickableTextView(year, yearRelease.getText(), yearRelease.getUrl());
        }

        String director = animeDetails.getDirector();
        if (!StringUtil.isBlank(director)) {
            binding.layoutInfo.directorLayout.setVisibility(View.VISIBLE);
            binding.layoutInfo.tvDirector.setText(director);
        }

        String studio = animeDetails.getStudio();
        if (!StringUtil.isBlank(studio)) {
            binding.layoutInfo.studioLayout.setVisibility(View.VISIBLE);
            binding.layoutInfo.tvStudio.setText(studio);
        }
        String episodes = animeDetails.getEpisodes();
        if (!StringUtil.isBlank(episodes)) {
            binding.layoutInfo.episodesLayout.setVisibility(View.VISIBLE);
            binding.layoutInfo.tvEpisodes.setText(episodes);
        }
        List<SimpleModel> translators = animeDetails.getTranslators();
        if (translators != null && !translators.isEmpty()) {
            binding.layoutInfo.translationLayout.setVisibility(View.VISIBLE);
            String translatorsString =
                    translators.stream().map(e -> e.getText()).collect(Collectors.joining(", "));
            binding.layoutInfo.tvTransators.setText(translatorsString);
        }
        showVoicers(animeDetails.getVoicers());
        showDubbers(animeDetails.getDubbersTeamList());

        showGenresGroup(animeDetails.getGenres());
    }

    private void showVoicers(List<SimpleModel> voicers) {
        if (voicers != null && !voicers.isEmpty()) {
            binding.layoutInfo.dubbersLayout.setVisibility(View.VISIBLE);
            String dubbersString =
                    voicers.stream().map(e -> e.getText()).collect(Collectors.joining(", "));
            binding.layoutInfo.tvDubbers.setText(dubbersString);
        }
    }

    private void showDubbers(List<DubbersTeam> dubbers) {
        if (dubbers != null && !dubbers.isEmpty()) {
            binding.layoutInfo.dubbersLayout.setVisibility(View.VISIBLE);
            StringBuilder sb = new StringBuilder();
            for (DubbersTeam dubberTeam : dubbers) {
                sb.append(dubberTeam.getDubberTeam().getText());
                sb.append(": ");
                String dubbersString =
                        dubberTeam.getDubbers().stream()
                                .map(e -> e.getText())
                                .collect(Collectors.joining(", "));
                sb.append(dubbersString);
                sb.append("\n");
            }
            binding.layoutInfo.tvDubbers.setText(sb.toString().trim());
        }
    }

    private void showGenresGroup(List<SimpleModel> genres) {
        if (genres != null && !genres.isEmpty()) {
            binding.layoutInfo.genresChipGroup.setVisibility(View.VISIBLE);
            ChipGroup chipGroup = binding.layoutInfo.genresChipGroup;
            chipGroup.removeAllViews();
            for (SimpleModel simple : genres) {
                Chip chip =
                        ChipGroupItemBinding.inflate(getLayoutInflater(), chipGroup, false)
                                .getRoot();
                chip.setText(simple.getText());
                chip.setClickable(true);
                chip.setOnClickListener(v -> openSearchFragment(simple.getUrl()));
                chipGroup.addView(chip);
            }
        }
    }

    private void loadImage(AppCompatImageView view, String url) {
        Glide.with(view.getContext()).load(url).diskCacheStrategy(DiskCacheStrategy.ALL).into(view);
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

    private void openWatchFragment(String title, int id, String url) {
        DetailsAnimeFragmemtDirections.ActinNavDetailsAnimeInfoToNavWatch action =
                DetailsAnimeFragmemtDirections.actinNavDetailsAnimeInfoToNavWatch(title, id, url);
        Navigation.findNavController(requireView()).navigate(action);
    }

    private void openSearchFragment(String link) {
        openInBrowser(requireContext(), link);
    }

    @Override
    public void onScreenshotItemSelected(String url) {}

    private void openInBrowser(Context context, String link) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // browserIntent.addCategory(Intent.CATEGORY_APP_BROWSER);
        try {
            ContextCompat.startActivity(context, browserIntent, null);
            return;
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void createClickableTextView(TextView tv, String message, String url) {
        String clickableMessage = String.format("<a href=\"%s\">%s</a>", url, message);
        tv.setText(HtmlCompat.fromHtml(clickableMessage, HtmlCompat.FROM_HTML_MODE_COMPACT));
        tv.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
}

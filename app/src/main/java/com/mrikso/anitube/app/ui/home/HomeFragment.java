package com.mrikso.anitube.app.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.adapters.ActionListAdapter;
import com.mrikso.anitube.app.adapters.AnimeCarouselAdapter;
import com.mrikso.anitube.app.adapters.BaseAnimeAdapter;
import com.mrikso.anitube.app.adapters.CollectionsAdapter;
import com.mrikso.anitube.app.adapters.ReleaseAnimeAdapter;
import com.mrikso.anitube.app.databinding.FragmentHomeBinding;
import com.mrikso.anitube.app.model.UserModel;
import com.mrikso.anitube.app.network.ApiClient;
import com.mrikso.anitube.app.utils.AutoScrollHelper;
import com.mrikso.anitube.app.utils.DotsIndicatorDecoration;
import com.mrikso.anitube.app.utils.ParserUtils;
import com.mrikso.anitube.app.utils.PreferencesHelper;
import com.mrikso.anitube.app.utils.ViewUtils;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment
        implements AnimeCarouselAdapter.OnItemClickListener,
        BaseAnimeAdapter.OnItemClickListener,
        ReleaseAnimeAdapter.OnItemClickListener {
    private final String TAG = "HomeFragment";
    private HomeFragmentViewModel viewModel;
    private FragmentHomeBinding binding;
    private AnimeCarouselAdapter carouselAdapter;
    private BaseAnimeAdapter bestAnimeAdapter;
    private ReleaseAnimeAdapter releaseAnimeAdapter;
    private CollectionsAdapter collectionsAdapter;
    private ActionListAdapter actionListAdapter;
    private String profileLink;

    private AutoScrollHelper autoScrollHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //    Log.i(TAG, "onCreate");
        viewModel = new ViewModelProvider(requireActivity()).get(HomeFragmentViewModel.class);
        viewModel.loadHome();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        initViews();
        initAnimeList();
        initListeners();
        initObservers();
        //  Log.i(TAG, "onViewCreated");
    }

    private void initViews() {
        carouselAdapter = new AnimeCarouselAdapter();
        carouselAdapter.setOnItemClickListener(this);
        bestAnimeAdapter = new BaseAnimeAdapter();
        bestAnimeAdapter.setOnItemClickListener(this);
        releaseAnimeAdapter = new ReleaseAnimeAdapter();
        releaseAnimeAdapter.setOnItemClickListener(this);
        collectionsAdapter = new CollectionsAdapter();
        collectionsAdapter.setOnItemClickListener(collection -> {
            HomeFragmentDirections.ActionNavHomeToNavCollectionDetail action =
                    HomeFragmentDirections.actionNavHomeToNavCollectionDetail(collection);
            Navigation.findNavController(requireView()).navigate(action);
        });

        /*  private Timer timer;
    private TimerTask timerTask;
    private int position;*/
        LinearLayoutManager carouselLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        RecyclerView mNowShowingRecyclerView = binding.interestingLayout.carouselRecyclerView;
        mNowShowingRecyclerView.setLayoutManager(carouselLayoutManager);
        mNowShowingRecyclerView.setAdapter(carouselAdapter);

        final int radius = getResources().getDimensionPixelSize(R.dimen.dots_radius);
        final int dotsHeight = getResources().getDimensionPixelSize(R.dimen.dots_height);
        final int color = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorPrimary, null);
        final int colorInactive = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorAccent, null);
        mNowShowingRecyclerView.addItemDecoration(new DotsIndicatorDecoration(radius, radius * 2, dotsHeight, colorInactive, color));


        /*SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mNowShowingRecyclerView);
        mNowShowingRecyclerView.smoothScrollBy(5, 0);
*/

        binding.bestAnimeLayout.bestAnimeRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.bestAnimeLayout.bestAnimeRecyclerView.setAdapter(bestAnimeAdapter);

        binding.newCollectionLayout.collectionsRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.newCollectionLayout.collectionsRecyclerView.setAdapter(collectionsAdapter);

        binding.newAnimeLayout.newAnimeRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        binding.newAnimeLayout.newAnimeRecyclerView.setAdapter(releaseAnimeAdapter);

        // Initialize AutoScrollHelper with a 3-second interval
        autoScrollHelper = new AutoScrollHelper(mNowShowingRecyclerView, 3000);
        autoScrollHelper.startAutoScroll();

        /*mNowShowingRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == 1) {
                    stopAutoScrollCarousel();
                } else if (newState == 0) {
                    position = carouselLayoutManager.findFirstCompletelyVisibleItemPosition();
                    runAutoScrollingCarousel();
                }
            }
        });*/

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.reloadHome();
            binding.swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void initListeners() {
        binding.loadStateLayout.repeat.setOnClickListener(v -> viewModel.reloadHome());
        binding.layoutToolbar.searchBtn.setOnClickListener(v -> openSearchFragment());
        binding.layoutToolbar.searchCardView.setOnClickListener(v -> openSearchFragment());
        binding.layoutToolbar.profileAvatar.setOnClickListener(v -> openProfileFragment());
        binding.newCollectionLayout.viewAllCollection.setOnClickListener(
                v -> Navigation.findNavController(requireView()).navigate(R.id.nav_collections));
    }

    private void initAnimeList() {
        actionListAdapter = new ActionListAdapter();
        actionListAdapter.setOnItemClickListener(this::onActionItemClicked);
        binding.layoutHomeAnimeList.animeListRv.setAdapter(actionListAdapter);

        GridLayoutManager manager = new GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false);
        binding.layoutHomeAnimeList.animeListRv.setLayoutManager(manager);

        //  binding.layoutHomeAnimeList.animeListRv.setLayoutManager(
        //      new AutoFitGridLayoutManager(requireContext(), 2));

    }

    private void onActionItemClicked(int mode) {
        switch (mode) {
            case ActionMode.ACTION_MODE_YEARS:
                openSimpleFragment(true);
                break;
            case ActionMode.ACTION_MODE_GENRES:
                openSimpleFragment(false);
                break;
            case ActionMode.ACTION_MODE_RANDOM_ANIME:
                openDetailsFragment(ApiClient.RANDOM_ANIME_URl);
                break;
        }
    }

    private void initObservers() {
        viewModel.getLoadState().observe(getViewLifecycleOwner(), result -> {
            switch (result) {
                case ERROR:
                    binding.homeContent.setVisibility(View.GONE);
                    binding.loadStateLayout.progressBar.setVisibility(View.GONE);
                    binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
                    break;
                case LOADING:
                    binding.homeContent.setVisibility(View.GONE);
                    binding.loadStateLayout.progressBar.setVisibility(View.VISIBLE);
                    binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
                    break;
                case DONE:
                    binding.loadStateLayout.progressBar.setVisibility(View.GONE);
                    binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
                    binding.homeContent.setVisibility(View.VISIBLE);
                    break;
                case NO_NETWORK:
                    binding.homeContent.setVisibility(View.GONE);
                    binding.loadStateLayout.progressBar.setVisibility(View.GONE);
                    binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
                    binding.loadStateLayout.errorMessage.setText(R.string.error_load);
                    break;
            }
        });

        viewModel.getInteresingAnime().observe(getViewLifecycleOwner(), results -> {
            if (results != null && !results.isEmpty()) {
                carouselAdapter.submitList(results);
            }
        });

        viewModel.getBestAnime().observe(getViewLifecycleOwner(), results -> {
            if (results != null && !results.isEmpty()) {
                bestAnimeAdapter.submitList(results);
            }
        });
        viewModel.getNewAnime().observe(getViewLifecycleOwner(), results -> {
            if (results != null && !results.isEmpty()) {
                releaseAnimeAdapter.submitList(results);
            }
        });
        viewModel.getNewACollections().observe(getViewLifecycleOwner(), results -> {
            if (results != null && !results.isEmpty()) {
                collectionsAdapter.submitList(results);
            }
        });

        viewModel.getActionList().observe(getViewLifecycleOwner(), results -> {
            if (results != null && !results.isEmpty()) {
                actionListAdapter.submitList(results);
            }
        });
        viewModel.getUserData().observe(getViewLifecycleOwner(), results -> {
            {
                if (results != null) {
                    setUserData(results);
                }
            }
        });
    }

/*
    private void stopAutoScrollCarousel() {
        if (timer != null && timerTask != null) {
            timerTask.cancel();
            timer.cancel();
            timer = null;
            timerTask = null;
            position = carouselLayoutManager.findFirstCompletelyVisibleItemPosition();
        }
    }

    private void runAutoScrollingCarousel() {
        if (carouselAdapter != null) {
            if (timer == null && timerTask == null) {
                timer = new Timer();
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        if (position == carouselAdapter.getItemCount() - 1) {
                            mNowShowingRecyclerView.post(new Runnable() {
                                @Override
                                public void run() {
                                    position = 0;
                                    mNowShowingRecyclerView.smoothScrollToPosition(position);
                                    mNowShowingRecyclerView.smoothScrollBy(5, 0);
                                }
                            });
                        } else {
                            position++;
                            mNowShowingRecyclerView.smoothScrollToPosition(position);
                        }
                    }
                };
                timer.schedule(timerTask, 4000, 4000);
            }
        }
    }
*/

    @Override
    public void onBaseItemSelected(String link) {
        openDetailsFragment(link);
    }

    @Override
    public void onCarouselItemSelected(String link) {
        openDetailsFragment(link);
    }

    @Override
    public void onReleaseItemSelected(String link) {
        openDetailsFragment(link);
    }

    private void openDetailsFragment(String url) {
        HomeFragmentDirections.ActionNavHomeToNavDetailsAnimeInfo action =
                HomeFragmentDirections.actionNavHomeToNavDetailsAnimeInfo(url);
        Navigation.findNavController(requireView()).navigate(action);
    }

    private void openSearchFragment() {
        Navigation.findNavController(requireView()).navigate(R.id.nav_search);
    }

    private void openProfileFragment() {
        if (PreferencesHelper.getInstance().isLogin()) {
            HomeFragmentDirections.ActionNavHomeToNavProfile action =
                    HomeFragmentDirections.actionNavHomeToNavProfile(profileLink);
            Navigation.findNavController(requireView()).navigate(action);
        } else {
            Navigation.findNavController(requireView()).navigate(R.id.nav_login);
        }
    }

    private void openSimpleFragment(boolean isCallendar) {
        HomeFragmentDirections.ActionNavHomeToNavSimpleList action =
                HomeFragmentDirections.actionNavHomeToNavSimpleList(isCallendar);
        Navigation.findNavController(requireView()).navigate(action);
    }

    private void setUserData(UserModel data) {
        profileLink = data.getUserUrl();
        ViewUtils.loadAvatar(binding.layoutToolbar.profileAvatar, ParserUtils.normaliseImageUrl(data.getUserAvatar()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //stopAutoScrollCarousel();
        if (autoScrollHelper != null) {
            autoScrollHelper.stopAutoScroll();
        }
        binding = null;
        releaseAnimeAdapter = null;
        carouselAdapter = null;
        bestAnimeAdapter = null;
        actionListAdapter = null;
    }
}

package com.mrikso.anitube.app.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.mrikso.anitube.app.adapters.AnimeCarouselAdapter;
import com.mrikso.anitube.app.adapters.BaseAnimeAdapter;
import com.mrikso.anitube.app.adapters.ReleaseAnimeAdapter;
import com.mrikso.anitube.app.databinding.FragmentHomeBinding;

import dagger.hilt.android.AndroidEntryPoint;

import java.util.Timer;
import java.util.TimerTask;

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
    private Timer timer;
    private TimerTask timerTask;
    private int position;
    private LinearLayoutManager carouselLayoutManager;

    private RecyclerView mNowShowingRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        viewModel = new ViewModelProvider(this).get(HomeFragmentViewModel.class);
        viewModel.loadHome();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        initViews();
        Log.i(TAG, "onViewCreated");

        viewModel
                .getLoadState()
                .observe(
                        getViewLifecycleOwner(),
                        result -> {
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
                            }
                        });

        viewModel
                .getInteresingAnime()
                .observe(
                        getViewLifecycleOwner(),
                        results -> {
                            if (results != null && !results.isEmpty()) {
                                requireActivity()
                                        .runOnUiThread(
                                                () -> {
                                                    carouselAdapter.setResults(results);
                                                });
                            }
                        });

        viewModel
                .getBestAnime()
                .observe(
                        getViewLifecycleOwner(),
                        results -> {
                            if (results != null && !results.isEmpty()) {
                                requireActivity()
                                        .runOnUiThread(
                                                () -> {
                                                    bestAnimeAdapter.setResults(results);
                                                });
                            }
                        });
        viewModel
                .getNewAnime()
                .observe(
                        getViewLifecycleOwner(),
                        results -> {
                            if (results != null && !results.isEmpty()) {
                                requireActivity()
                                        .runOnUiThread(
                                                () -> {
                                                    releaseAnimeAdapter.setResults(results);
                                                });
                            }
                        });
    }

    private void initViews() {
        carouselAdapter = new AnimeCarouselAdapter();
        carouselAdapter.setOnItemClickListener(this);
        bestAnimeAdapter = new BaseAnimeAdapter();
        bestAnimeAdapter.setOnItemClickListener(this);
        releaseAnimeAdapter = new ReleaseAnimeAdapter();
        releaseAnimeAdapter.setOnItemClickListener(this);

        carouselLayoutManager =
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        mNowShowingRecyclerView = binding.interestingLayout.carouselRecyclerView;
        mNowShowingRecyclerView.setLayoutManager(carouselLayoutManager);
        mNowShowingRecyclerView.setAdapter(carouselAdapter);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mNowShowingRecyclerView);
        mNowShowingRecyclerView.smoothScrollBy(5, 0);

        binding.bestAnimeLayout.bestAnimeRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.bestAnimeLayout.bestAnimeRecyclerView.setAdapter(bestAnimeAdapter);

        binding.newAnimeLayout.newAnimeRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        binding.newAnimeLayout.newAnimeRecyclerView.setAdapter(releaseAnimeAdapter);

        mNowShowingRecyclerView.setOnScrollListener(
                new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(
                            @NonNull RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);

                        if (newState == 1) {
                            stopAutoScrollCarousel();
                        } else if (newState == 0) {
                            position =
                                    carouselLayoutManager.findFirstCompletelyVisibleItemPosition();
                            runAutoScrollingCarousel();
                        }
                    }
                });

        binding.swipeRefreshLayout.setOnRefreshListener(
                () -> {
                    viewModel.loadHome();
                    binding.swipeRefreshLayout.setRefreshing(false);
                });

        binding.loadStateLayout.repeat.setOnClickListener(v -> viewModel.loadHome());
    }

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
        if (timer == null && timerTask == null) {
            timer = new Timer();
            timerTask =
                    new TimerTask() {
                        @Override
                        public void run() {
                            if (position == carouselAdapter.getItemCount() - 1) {
                                mNowShowingRecyclerView.post(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                position = 0;
                                                mNowShowingRecyclerView.smoothScrollToPosition(
                                                        position);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        stopAutoScrollCarousel();
        releaseAnimeAdapter = null;
        carouselAdapter = null;
        bestAnimeAdapter = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel = null;
    }
}

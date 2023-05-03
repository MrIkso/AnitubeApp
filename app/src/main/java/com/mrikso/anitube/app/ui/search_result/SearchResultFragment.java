package com.mrikso.anitube.app.ui.search_result;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.paging.LoadState;
import androidx.paging.PagingData;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.mrikso.anitube.app.adapters.AnimePagingAdapter;
import com.mrikso.anitube.app.adapters.MoviesLoadStateAdapter;
import com.mrikso.anitube.app.comparator.AnimeReleaseComparator;
import com.mrikso.anitube.app.databinding.FragmentSearchResultBinding;
import com.mrikso.anitube.app.model.AnimeReleaseModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SearchResultFragment extends Fragment {

    private FragmentSearchResultBinding binding;
    private SearchResultViewModel viewModel;
    private AnimePagingAdapter animePpagingAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SearchResultViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchResultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        initViews();
        initializeListeners();
        observeEvents();
        loadData();
    }

    private void observeEvents() {
        viewModel
                .getAnimePagingData()
                .observe(
                        getViewLifecycleOwner(),
                        results -> {
                            if (binding != null) {
                                if (results != null) {
                                    showAnimeList(results);
                                } else {
                                    binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
                                }
                            }
                        });
    }

    private void initializeListeners() {
        binding.back.setOnClickListener(
                v -> Navigation.findNavController(requireView()).popBackStack());
    }

    private void initViews() {
        binding.recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        animePpagingAdapter =
                new AnimePagingAdapter(new AnimeReleaseComparator(), getGlide(requireContext()));
        animePpagingAdapter.setOnItemClickListener(
                link -> {
                    openDetailsFragment(link);
                });
        animePpagingAdapter.addLoadStateListener(
                combinedLoadStates -> {
                    LoadState refreshLoadState = combinedLoadStates.getRefresh();
                    LoadState appendLoadState = combinedLoadStates.getAppend();
                    if (refreshLoadState instanceof LoadState.Loading) {
                        binding.content.setVisibility(View.GONE);
                        binding.loadStateLayout.progressBar.setVisibility(View.VISIBLE);
                        binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
                    }
                    if (refreshLoadState instanceof LoadState.NotLoading) {
                        if (refreshLoadState.getEndOfPaginationReached()
                                && animePpagingAdapter.getItemCount() < 1) {
                            showNoDataState();
                        } else {
                            binding.content.setVisibility(View.VISIBLE);
                        }
                        binding.loadStateLayout.progressBar.setVisibility(View.GONE);
                        binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
                    } else if (refreshLoadState instanceof LoadState.Error) {
                        binding.loadStateLayout.progressBar.setVisibility(View.GONE);
                        binding.content.setVisibility(View.GONE);
                        binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
                        binding.loadStateLayout.repeat.setOnClickListener(
                                v -> animePpagingAdapter.retry());
                        LoadState.Error loadStateError = (LoadState.Error) refreshLoadState;
                        binding.loadStateLayout.errorMessageTv.setText(
                                loadStateError.getError().getLocalizedMessage());
                    }
                    if (!(refreshLoadState instanceof LoadState.Loading)
                            && appendLoadState instanceof LoadState.NotLoading) {
                        if (appendLoadState.getEndOfPaginationReached()
                                && animePpagingAdapter.getItemCount() < 1) {
                            showNoDataState();
                        }
                    }
                    return null;
                });

        binding.recyclerView.setAdapter(
                animePpagingAdapter.withLoadStateFooter(
                        new MoviesLoadStateAdapter(
                                v -> {
                                    animePpagingAdapter.retry();
                                })));
    }

    private void openDetailsFragment(final String link) {
        SearchResultFragmentDirections.ActionNavSearchResultToNavDetails action =
                SearchResultFragmentDirections.actionNavSearchResultToNavDetails(link);
        Navigation.findNavController(requireView()).navigate(action);
    }

    private void showNoDataState() {
        // TODO: Implement this method
        binding.content.setVisibility(View.GONE);
    }

    private void initObservers() {
        viewModel
                .getAnimePagingData()
                .observe(
                        getViewLifecycleOwner(),
                        results -> {
                            if (binding != null) {
                                if (results != null) {
                                    showAnimeList(results);
                                } else {
                                    binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
                                }
                            }
                        });
    }

    private void showAnimeList(final PagingData<AnimeReleaseModel> results) {
        animePpagingAdapter.submitData(getLifecycle(), results);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        animePpagingAdapter = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel = null;
    }

    public RequestManager getGlide(Context context) {
        return Glide.with(context)
                .applyDefaultRequestOptions(
                        new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL));
    }

    private void loadData() {
        SearchResultFragmentArgs arg = SearchResultFragmentArgs.fromBundle(getArguments());
        binding.tvSearchTitle.setText(arg.getTitle());
        viewModel.searchByLink(arg.getUrl());
    }
}

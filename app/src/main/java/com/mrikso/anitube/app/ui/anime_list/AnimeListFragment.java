package com.mrikso.anitube.app.ui.anime_list;

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

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.mrikso.anitube.app.adapters.MoviesAdapter;
import com.mrikso.anitube.app.adapters.MoviesLoadStateAdapter;
import com.mrikso.anitube.app.comparator.MovieComparator;
import com.mrikso.anitube.app.databinding.FragmentAnimeListBinding;
import com.mrikso.anitube.app.model.AnimeReleaseModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AnimeListFragment extends Fragment {

    private FragmentAnimeListBinding binding;
    private AnimeListFragmentViewModel viewModel;
    private MoviesAdapter moviesAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /// setRetainInstance(true);
        viewModel = new ViewModelProvider(requireActivity()).get(AnimeListFragmentViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentAnimeListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        initViews();
        initObservers();
    }

    private void initViews() {
        moviesAdapter = new MoviesAdapter(new MovieComparator(), getGlide(requireContext()));
        moviesAdapter.setOnItemClickListener(
                link -> {
                    AnimeListFragmentDirections.ActionNavAnimeListToNavDetailsAnimeInfo action =
                            AnimeListFragmentDirections.actionNavAnimeListToNavDetailsAnimeInfo(
                                    link);
                    Navigation.findNavController(requireView()).navigate(action);
                });
        moviesAdapter.addLoadStateListener(
                combinedLoadStates -> {
                    LoadState refreshLoadState = combinedLoadStates.getRefresh();
                    LoadState appendLoadState = combinedLoadStates.getAppend();
                    if (refreshLoadState instanceof LoadState.Loading) {
                        binding.animeListContent.setVisibility(View.GONE);
                        binding.loadStateLayout.progressBar.setVisibility(View.VISIBLE);
                        binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
                    }
                    if (refreshLoadState instanceof LoadState.NotLoading) {
                        if (refreshLoadState.getEndOfPaginationReached()
                                && moviesAdapter.getItemCount() < 1) {
                            showNoDataState();
                        } else {
                            binding.animeListContent.setVisibility(View.VISIBLE);
                            binding.loadStateLayout.progressBar.setVisibility(View.GONE);
                            binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
                        }
                    } else if (refreshLoadState instanceof LoadState.Error) {
                        binding.loadStateLayout.progressBar.setVisibility(View.GONE);
                        binding.animeListContent.setVisibility(View.GONE);
                        binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
                        binding.loadStateLayout.repeat.setOnClickListener(
                                v -> moviesAdapter.retry());
                    }
                    if (!(refreshLoadState instanceof LoadState.Loading)
                            && appendLoadState instanceof LoadState.NotLoading) {
                        if (appendLoadState.getEndOfPaginationReached()
                                && moviesAdapter.getItemCount() < 1) {
                            showNoDataState();
                        }
                    }
                    return null;
                });
        binding.animeListRecyclerView.setAdapter(
                moviesAdapter.withLoadStateFooter(
                        new MoviesLoadStateAdapter(
                                v -> {
                                    moviesAdapter.retry();
                                })));

        binding.swipeRefreshLayout.setOnRefreshListener(
                () -> {
                    moviesAdapter.refresh();
                    binding.swipeRefreshLayout.setRefreshing(false);
                });
    }

    private void showNoDataState() {
        // TODO: Implement this method
        binding.animeListContent.setVisibility(View.GONE);
    }

    private void initObservers() {
        viewModel.animePagingDataFlowable.
                // .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                subscribe (
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
        moviesAdapter.submitData(getLifecycle(), results);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        moviesAdapter = null;
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

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onAttach(Context arg0) {
        super.onAttach(arg0);
    }
}

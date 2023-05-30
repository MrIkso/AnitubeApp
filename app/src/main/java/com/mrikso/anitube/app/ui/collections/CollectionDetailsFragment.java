package com.mrikso.anitube.app.ui.collections;

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
import com.google.android.material.appbar.AppBarLayout;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.adapters.AnimePagingAdapter;
import com.mrikso.anitube.app.adapters.MoviesLoadStateAdapter;
import com.mrikso.anitube.app.comparator.AnimeReleaseComparator;
import com.mrikso.anitube.app.databinding.FragmentCollectionDetailsBinding;
import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.model.CollectionModel;
import com.mrikso.anitube.app.network.ApiClient;
import com.mrikso.anitube.app.ui.search_result.SearchResultViewModel;
import com.mrikso.anitube.app.utils.IntentUtils;
import com.mrikso.anitube.app.utils.ParserUtils;
import com.mrikso.anitube.app.utils.ViewUtils;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CollectionDetailsFragment extends Fragment {

    private FragmentCollectionDetailsBinding binding;
    private SearchResultViewModel viewModel;
    private AnimePagingAdapter animePpagingAdapter;
    private CollectionModel collection;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SearchResultViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCollectionDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        CollectionDetailsFragmentArgs arg = CollectionDetailsFragmentArgs.fromBundle(getArguments());
        collection = arg.getCollection();
        initViews();
        initializeListeners();
        observeEvents();
        loadData();
    }

    private void observeEvents() {
        viewModel.getAnimePagingData().observe(getViewLifecycleOwner(), results -> {
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
        binding.toolbar.setNavigationOnClickListener(
                v -> Navigation.findNavController(requireView()).popBackStack());

        binding.toolbar.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.action_share:
                    IntentUtils.shareLink(requireContext(), ApiClient.BASE_URL + collection.getCollectionUrl());
                    return true;
            }
            return true;
        });
    }

    private void initViews() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_back);
        binding.toolbar.inflateMenu(R.menu.collection_detail);

        binding.collectionsRv.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        animePpagingAdapter = new AnimePagingAdapter(new AnimeReleaseComparator(), getGlide(requireContext()));
        animePpagingAdapter.setOnItemClickListener(link -> {
            openDetailsFragment(link);
        });
        animePpagingAdapter.addLoadStateListener(combinedLoadStates -> {
            LoadState refreshLoadState = combinedLoadStates.getRefresh();
            LoadState appendLoadState = combinedLoadStates.getAppend();
            if (refreshLoadState instanceof LoadState.Loading) {
                binding.content.setVisibility(View.GONE);
                binding.loadStateLayout.progressBar.setVisibility(View.VISIBLE);
                binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
            }
            if (refreshLoadState instanceof LoadState.NotLoading) {
                if (refreshLoadState.getEndOfPaginationReached() && animePpagingAdapter.getItemCount() < 1) {
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
                binding.loadStateLayout.repeat.setOnClickListener(v -> animePpagingAdapter.retry());
                LoadState.Error loadStateError = (LoadState.Error) refreshLoadState;
                binding.loadStateLayout.errorMessage.setText(
                        loadStateError.getError().getLocalizedMessage());
            }
            if (!(refreshLoadState instanceof LoadState.Loading) && appendLoadState instanceof LoadState.NotLoading) {
                if (appendLoadState.getEndOfPaginationReached() && animePpagingAdapter.getItemCount() < 1) {
                    showNoDataState();
                }
            }
            return null;
        });

        binding.collectionsRv.setAdapter(animePpagingAdapter.withLoadStateFooter(new MoviesLoadStateAdapter(v -> {
            animePpagingAdapter.retry();
        })));
        setCollapsingToolbarTitle();
    }

    private void setCollapsingToolbarTitle() {
        // Set onOffsetChangedListener to determine when CollapsingToolbar is collapsed
        binding.appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    // Show title when a CollapsingToolbarLayout is fully collapse
                    binding.collapsingToolbarLayout.setTitle(collection.getNameCollection());
                    isShow = true;
                } else if (isShow) {
                    // Otherwise hide the title
                    binding.collapsingToolbarLayout.setTitle("");
                    isShow = false;
                }
            }
        });
    }

    private void openDetailsFragment(final String link) {
        CollectionDetailsFragmentDirections.ActionNavCollectionDetailToNavDetailsAnimeInfo action =
                CollectionDetailsFragmentDirections.actionNavCollectionDetailToNavDetailsAnimeInfo(link);
        Navigation.findNavController(requireView()).navigate(action);
    }

    private void showNoDataState() {
        // TODO: Implement this method
        binding.content.setVisibility(View.GONE);
    }

    private void initObservers() {
        viewModel.getAnimePagingData().observe(getViewLifecycleOwner(), results -> {
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
                .applyDefaultRequestOptions(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL));
    }

    private void loadData() {

        binding.title.setText(collection.getNameCollection());
        binding.title.setSelected(true);
        binding.countAnime.setText(getString(R.string.collection_anine_count, collection.getCountAnime()));
        ViewUtils.loadImage(binding.img, ParserUtils.normaliseImageUrl(collection.getPosterUrl()));

        viewModel.searchByLink(ApiClient.BASE_URL + collection.getCollectionUrl() + "/");
    }
}

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

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.adapters.CollectionsPagingAdapter;
import com.mrikso.anitube.app.adapters.MoviesLoadStateAdapter;
import com.mrikso.anitube.app.databinding.FragmentCollectionsBinding;
import com.mrikso.anitube.app.model.CollectionModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CollectionsFragment extends Fragment {

    private FragmentCollectionsBinding binding;
    private CollectionsFragmentViewModel viewModel;
    private CollectionsPagingAdapter collectionPagingAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CollectionsFragmentViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCollectionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        initViews();
        initObservers();
    }

    private void initViews() {
        collectionPagingAdapter = new CollectionsPagingAdapter(getGlide(requireContext()));
        collectionPagingAdapter.setOnItemClickListener(collection -> {
            openCollectionDetailsFragment(collection);
        });
        collectionPagingAdapter.addLoadStateListener(combinedLoadStates -> {
            LoadState refreshLoadState = combinedLoadStates.getRefresh();
            LoadState appendLoadState = combinedLoadStates.getAppend();
            if (refreshLoadState instanceof LoadState.Loading) {
                binding.container.setVisibility(View.GONE);
                binding.loadStateLayout.progressBar.setVisibility(View.VISIBLE);
                binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
            }
            if (refreshLoadState instanceof LoadState.NotLoading) {
                if (refreshLoadState.getEndOfPaginationReached() && collectionPagingAdapter.getItemCount() < 1) {
                    showNoDataState();
                } else {
                    binding.container.setVisibility(View.VISIBLE);
                    binding.loadStateLayout.progressBar.setVisibility(View.GONE);
                    binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
                }
            } else if (refreshLoadState instanceof LoadState.Error) {
                binding.loadStateLayout.progressBar.setVisibility(View.GONE);
                binding.container.setVisibility(View.GONE);
                binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
                binding.loadStateLayout.repeat.setOnClickListener(v -> collectionPagingAdapter.retry());
                LoadState.Error loadStateError = (LoadState.Error) refreshLoadState;
                binding.loadStateLayout.errorMessage.setText(
                        loadStateError.getError().getLocalizedMessage());
            }
            if (!(refreshLoadState instanceof LoadState.Loading) && appendLoadState instanceof LoadState.NotLoading) {
                if (appendLoadState.getEndOfPaginationReached() && collectionPagingAdapter.getItemCount() < 1) {
                    showNoDataState();
                }
            }
            return null;
        });
        binding.collectionsRv.setAdapter(collectionPagingAdapter.withLoadStateFooter(new MoviesLoadStateAdapter(v -> {
            collectionPagingAdapter.retry();
        })));

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            collectionPagingAdapter.refresh();
            binding.swipeRefreshLayout.setRefreshing(false);
        });

        binding.layoutToolbar.searchBtn.setOnClickListener(v -> openSearchFragment());
        binding.layoutToolbar.searchCardView.setOnClickListener(v -> openSearchFragment());
        binding.layoutToolbar.profileAvatar.setOnClickListener(v -> openProfileFragment());
    }

    private void openCollectionDetailsFragment(final CollectionModel collection) {
        CollectionsFragmentDirections.ActionNavCollectionsToNavCollectionDetail action =
                CollectionsFragmentDirections.actionNavCollectionsToNavCollectionDetail(collection);
        Navigation.findNavController(requireView()).navigate(action);
    }

    private void showNoDataState() {
        // TODO: Implement tApiClient.link
        binding.container.setVisibility(View.GONE);
    }

    private void initObservers() {
        viewModel.getCollectionPagingData().observe(getViewLifecycleOwner(), results -> {
            if (binding != null) {
                if (results != null) {
                    showCollectionsList(results);
                } else {
                    binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void showCollectionsList(final PagingData<CollectionModel> results) {
        collectionPagingAdapter.submitData(getLifecycle(), results);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        collectionPagingAdapter = null;
    }

    public RequestManager getGlide(Context context) {
        return Glide.with(context)
                .applyDefaultRequestOptions(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL));
    }

    private void openSearchFragment() {
        Navigation.findNavController(requireView()).navigate(R.id.nav_search);
    }

    private void openProfileFragment() {
        //        if (PreferencesHelper.getInstance().isLogin()) {
        //            AnimeListFragmentDirections.ActionNavAnimeListToNavProfile action =
        //
        // AnimeListFragmentDirections.actionNavAnimeListToNavProfile(profileLink);
        //            Navigation.findNavController(requireView()).navigate(action);
        //        } else {
        //            Navigation.findNavController(requireView()).navigate(R.id.nav_login);
        //        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

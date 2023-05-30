package com.mrikso.anitube.app.ui.anime_list;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
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
import com.mrikso.anitube.app.adapters.AnimePagingAdapter;
import com.mrikso.anitube.app.adapters.MoviesLoadStateAdapter;
import com.mrikso.anitube.app.comparator.AnimeReleaseComparator;
import com.mrikso.anitube.app.databinding.FragmentAnimeListBinding;
import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.utils.ParserUtils;
import com.mrikso.anitube.app.utils.PreferencesHelper;
import com.mrikso.anitube.app.utils.ViewUtils;

import dagger.hilt.android.AndroidEntryPoint;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

@AndroidEntryPoint
public class AnimeListFragment extends Fragment {
    private final CompositeDisposable disposables = new CompositeDisposable();

    private FragmentAnimeListBinding binding;
    private AnimeListFragmentViewModel viewModel;
    private AnimePagingAdapter animePpagingAdapter;
    private String profileLink;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AnimeListFragmentViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
        animePpagingAdapter = new AnimePagingAdapter(new AnimeReleaseComparator(), getGlide(requireContext()));
        animePpagingAdapter.setOnItemClickListener(link -> {
            openDetailsFragment(link);
        });
        animePpagingAdapter.addLoadStateListener(combinedLoadStates -> {
            LoadState refreshLoadState = combinedLoadStates.getRefresh();
            LoadState appendLoadState = combinedLoadStates.getAppend();
            if (refreshLoadState instanceof LoadState.Loading) {
                binding.container.setVisibility(View.GONE);
                binding.loadStateLayout.progressBar.setVisibility(View.VISIBLE);
                binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
            }
            if (refreshLoadState instanceof LoadState.NotLoading) {
                if (refreshLoadState.getEndOfPaginationReached() && animePpagingAdapter.getItemCount() < 1) {
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
        binding.animeListRecyclerView.setAdapter(
                animePpagingAdapter.withLoadStateFooter(new MoviesLoadStateAdapter(v -> {
                    animePpagingAdapter.retry();
                })));

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            animePpagingAdapter.refresh();
            binding.swipeRefreshLayout.setRefreshing(false);
        });

        binding.layoutToolbar.searchBtn.setOnClickListener(v -> openSearchFragment());
        binding.layoutToolbar.searchCardView.setOnClickListener(v -> openSearchFragment());
        binding.layoutToolbar.profileAvatar.setOnClickListener(v -> openProfileFragment());
    }

    private void openDetailsFragment(final String link) {
        AnimeListFragmentDirections.ActionNavAnimeListToNavDetailsAnimeInfo action =
                AnimeListFragmentDirections.actionNavAnimeListToNavDetailsAnimeInfo(link);
        Navigation.findNavController(requireView()).navigate(action);
    }

    private void showNoDataState() {
        // TODO: Implement this method
        binding.container.setVisibility(View.GONE);
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

        viewModel.getUserData().observe(getViewLifecycleOwner(), results -> {
            if (results != null && results.first != null && results.second != null) {
                setUserData(results);
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

    public RequestManager getGlide(Context context) {
        return Glide.with(context)
                .applyDefaultRequestOptions(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL));
    }

    private void openSearchFragment() {
        Navigation.findNavController(requireView()).navigate(R.id.nav_search);
    }

    private void setUserData(Pair<String, String> data) {
        profileLink = data.second;
        ViewUtils.loadAvatar(binding.layoutToolbar.profileAvatar, ParserUtils.normaliseImageUrl(data.first));
    }

    private void openProfileFragment() {
        if (PreferencesHelper.getInstance().isLogin()) {
            AnimeListFragmentDirections.ActionNavAnimeListToNavProfile action =
                    AnimeListFragmentDirections.actionNavAnimeListToNavProfile(profileLink);
            Navigation.findNavController(requireView()).navigate(action);
        } else {
            Navigation.findNavController(requireView()).navigate(R.id.nav_login);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.dispose();
    }
}

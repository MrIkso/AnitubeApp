package com.mrikso.anitube.app.ui.anime_list;

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

import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.adapters.AnimePagingAdapter;
import com.mrikso.anitube.app.adapters.MoviesLoadStateAdapter;
import com.mrikso.anitube.app.comparator.AnimeReleaseComparator;
import com.mrikso.anitube.app.databinding.FragmentAnimeListBinding;
import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.model.UserModel;
import com.mrikso.anitube.app.utils.GlideLoadUtils;
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
        animePpagingAdapter = new AnimePagingAdapter(new AnimeReleaseComparator(), GlideLoadUtils.getGlide(requireContext()));
        animePpagingAdapter.setOnItemClickListener(link -> {
            openDetailsFragment(link);
        });
        animePpagingAdapter.addLoadStateListener(combinedLoadStates -> {
            LoadState refreshLoadState = combinedLoadStates.getRefresh();
            LoadState appendLoadState = combinedLoadStates.getAppend();
            if (refreshLoadState instanceof LoadState.Loading) {
                showLoadingState();
            }
            if (refreshLoadState instanceof LoadState.NotLoading) {
                if (refreshLoadState.getEndOfPaginationReached() && animePpagingAdapter.getItemCount() < 1) {
                    showNoDataState();
                } else {
                    showReadyState();
                }
            } else if (refreshLoadState instanceof LoadState.Error) {
                showErrorState((LoadState.Error) refreshLoadState);
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

    private void showReadyState() {
        if (binding != null) {
            binding.container.setVisibility(View.VISIBLE);
            binding.loadStateLayout.progressBar.setVisibility(View.GONE);
            binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
        }
    }

    private void showLoadingState() {
        if (binding != null) {
            binding.container.setVisibility(View.GONE);
            binding.loadStateLayout.progressBar.setVisibility(View.VISIBLE);
            binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
        }
    }

    private void showErrorState(LoadState.Error refreshLoadState) {
        if (binding != null) {
            binding.loadStateLayout.progressBar.setVisibility(View.GONE);
            binding.container.setVisibility(View.GONE);
            binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
            binding.loadStateLayout.repeat.setOnClickListener(v -> animePpagingAdapter.retry());
            binding.loadStateLayout.errorMessage.setText(
                    refreshLoadState.getError().getLocalizedMessage());
        }
    }

    private void openDetailsFragment(final String link) {
        AnimeListFragmentDirections.ActionNavAnimeListToNavDetailsAnimeInfo action =
                AnimeListFragmentDirections.actionNavAnimeListToNavDetailsAnimeInfo(link);
        Navigation.findNavController(requireView()).navigate(action);
    }

    private void showNoDataState() {
        if (binding != null) {
            binding.loadStateLayout.ivIcon.setImageResource(R.drawable.image_no_data);
            binding.loadStateLayout.errorMessageTitle.setText(R.string.state_no_data);
            binding.loadStateLayout.errorMessage.setText(R.string.state_no_data_anime_list_desc);

            binding.container.setVisibility(View.GONE);
            binding.loadStateLayout.progressBar.setVisibility(View.GONE);
            binding.loadStateLayout.buttonLl.setVisibility(View.GONE);
            binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
        }
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
            if (results != null) {
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

    private void openSearchFragment() {
        Navigation.findNavController(requireView()).navigate(R.id.nav_search);
    }

    private void setUserData(UserModel data) {
        profileLink = data.getUserUrl();
        ViewUtils.loadImage(binding.layoutToolbar.profileAvatar, ParserUtils.normaliseImageUrl(ParserUtils.loadSmartphoneNoAvatar(data.getUserAvatar())));
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

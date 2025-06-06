package com.mrikso.anitube.app.ui.library;

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
import com.mrikso.anitube.app.databinding.FragmentLibaryContentBinding;
import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.utils.GlideLoadUtils;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.HttpException;

@AndroidEntryPoint
public class LibaryContentFragment extends Fragment {
    private static final String MODE = "mode";

    private FragmentLibaryContentBinding binding;
    private LibaryFragmentViewModel viewModel;
    private AnimePagingAdapter animePpagingAdapter;

    public static LibaryContentFragment newInstance(int mode) {
        LibaryContentFragment fragment = new LibaryContentFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(MODE, mode);
        fragment.setArguments(bundle);
        return fragment;
    }

    public LibaryContentFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LibaryFragmentViewModel.class);
        int mode = getArguments().getInt(MODE, -1);
        viewModel.loadData(mode);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLibaryContentBinding.inflate(inflater, container, false);
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
        animePpagingAdapter.setOnItemClickListener(this::openDetailsFragment);
        animePpagingAdapter.addLoadStateListener(combinedLoadStates -> {
            LoadState refreshLoadState = combinedLoadStates.getRefresh();
            LoadState appendLoadState = combinedLoadStates.getAppend();
            if (refreshLoadState instanceof LoadState.Loading) {
                if (binding != null) {
                    binding.content.setVisibility(View.GONE);
                    binding.loadStateLayout.progressBar.setVisibility(View.VISIBLE);
                    binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
                }
            }
            if (refreshLoadState instanceof LoadState.NotLoading) {
                if (refreshLoadState.getEndOfPaginationReached() && animePpagingAdapter!=null && animePpagingAdapter.getItemCount() < 1) {
                    showNoDataState();
                } else {
                    if (binding != null) {
                        binding.content.setVisibility(View.VISIBLE);
                        binding.loadStateLayout.progressBar.setVisibility(View.GONE);
                        binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
                    }
                }
            } else if (refreshLoadState instanceof LoadState.Error) {
                LoadState.Error loadStateError = (LoadState.Error) refreshLoadState;
                if (loadStateError.getError() instanceof HttpException) {

                    if (((HttpException) loadStateError.getError()).code() == 404) {
                        showNoDataState();
                    }
                } else {
                    handleErrorState(loadStateError.getError().getLocalizedMessage());
                }
            }
            if (!(refreshLoadState instanceof LoadState.Loading) && appendLoadState instanceof LoadState.NotLoading) {
                if (appendLoadState.getEndOfPaginationReached() && animePpagingAdapter!=null && animePpagingAdapter.getItemCount() < 1) {
                    showNoDataState();
                }
            }
            return null;
        });

            binding.animeList.setAdapter(animePpagingAdapter.withLoadStateFooter(new MoviesLoadStateAdapter(v -> {
                animePpagingAdapter.retry();
            })));

            binding.swipeRefreshLayout.setOnRefreshListener(() -> {
                animePpagingAdapter.refresh();
                binding.swipeRefreshLayout.setRefreshing(false);
            });

    }

    private void handleErrorState(String error) {
        if (binding != null) {
            binding.loadStateLayout.progressBar.setVisibility(View.GONE);
            binding.content.setVisibility(View.GONE);
            binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
            binding.loadStateLayout.repeat.setOnClickListener(v -> animePpagingAdapter.retry());
            binding.loadStateLayout.errorMessage.setText(error);
        }
    }

    private void openDetailsFragment(final String link) {
        LibraryFragmentDirections.ActionNavLibraryToNavDetails action =
                LibraryFragmentDirections.actionNavLibraryToNavDetails(link);
        Navigation.findNavController(requireView()).navigate(action);
    }

    private void showNoDataState() {
        if (binding != null) {
            binding.loadStateLayout.ivIcon.setImageResource(R.drawable.image_no_data);
            binding.loadStateLayout.errorMessageTitle.setText(R.string.state_no_data);

            int mode = getArguments().getInt(MODE, 0);
            switch (mode) {
                case AnimeListType.LIST_ALL:
                    binding.loadStateLayout.errorMessage.setText(getString(R.string.state_no_data_favotie_anime_list_desc, getString(R.string.anime_all_list)));
                    break;
                case AnimeListType.LIST_ADAND:
                    binding.loadStateLayout.errorMessage.setText(getString(R.string.state_no_data_favotie_anime_list_desc, getString(R.string.anime_status_adand)));
                    break;
                case AnimeListType.LIST_FAVORITES:
                    binding.loadStateLayout.errorMessage.setText(getString(R.string.state_no_data_favotie_anime_list_desc, getString(R.string.anime_favorites_list)));
                    break;
                case AnimeListType.LIST_PONED:
                    binding.loadStateLayout.errorMessage.setText(getString(R.string.state_no_data_favotie_anime_list_desc, getString(R.string.anime_status_poned)));
                    break;
                case AnimeListType.LIST_SEEN:
                    binding.loadStateLayout.errorMessage.setText(getString(R.string.state_no_data_favotie_anime_list_desc, getString(R.string.anime_status_seen)));
                    break;
                case AnimeListType.LIST_WATCH:
                    binding.loadStateLayout.errorMessage.setText(getString(R.string.state_no_data_favotie_anime_list_desc, getString(R.string.anime_status_watch)));
                    break;
                case AnimeListType.LIST_WILL:
                    binding.loadStateLayout.errorMessage.setText(getString(R.string.state_no_data_favotie_anime_list_desc, getString(R.string.anime_status_will)));
                    break;
            }

            binding.content.setVisibility(View.GONE);
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
    }

    private void showAnimeList(final PagingData<AnimeReleaseModel> results) {
        animePpagingAdapter.submitData(getLifecycle(), results);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        animePpagingAdapter = null;
        binding = null;
    }
}

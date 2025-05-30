package com.mrikso.anitube.app.ui.search;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.paging.LoadState;
import androidx.paging.PagingData;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.KeyboardUtils;
import com.google.android.material.internal.TextWatcherAdapter;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.adapters.AnimePagingAdapter;
import com.mrikso.anitube.app.adapters.RecentSearchesAdapter;
import com.mrikso.anitube.app.adapters.SuggestionAdapter;
import com.mrikso.anitube.app.comparator.AnimeReleaseComparator;
import com.mrikso.anitube.app.databinding.FragmentSearchBinding;
import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.utils.GlideLoadUtils;
import com.mrikso.anitube.app.utils.PreferenceKeys;
import com.mrikso.anitube.app.utils.PreferenceUtils;
import com.mrikso.anitube.app.utils.ViewUtils;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@AndroidEntryPoint
public class SearchFragment extends Fragment
        implements RecentSearchesAdapter.OnItemClickListener, SuggestionAdapter.OnSuggestionClickListener {
    private RecentSearchesAdapter recentSearchAdapter;
    private SuggestionAdapter suggestionAdapter;
    private AnimePagingAdapter pagingAdapter;
    private FragmentSearchBinding binding;
    private SearchFragmentViewModel viewModel;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SearchFragmentViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        initViews();
        initializeListeners();
        observeEvents();
    }

    protected void initializeListeners() {
        binding.back.setOnClickListener(
                v -> {
                    KeyboardUtils.hideSoftInput(requireActivity());
                    Navigation.findNavController(requireView()).popBackStack();
                });

        AppCompatAutoCompleteTextView searchEdit = getAppCompatAutoCompleteTextView();

        searchEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(searchEdit.getText().toString(), true);
                return true;
            }
            return false;
        });

        binding.clear.setOnClickListener(v -> searchEdit.setText(""));

        handlePagingState();
    }

    @SuppressLint("RestrictedApi")
    @NonNull
    private AppCompatAutoCompleteTextView getAppCompatAutoCompleteTextView() {
        AppCompatAutoCompleteTextView searchEdit = binding.etSearch;
        searchEdit.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(@NonNull Editable editable) {
                if (searchEdit.hasFocus()) {
                    String content = editable.toString();
                    binding.clear.setVisibility(TextUtils.isEmpty(content) ? View.GONE : View.VISIBLE);
                    if (content.length() >= 3) {
                        quickSearch(content);
                    }
                }
            }
        });
        return searchEdit;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.setHideKeyboard();
        KeyboardUtils.hideSoftInput(requireActivity());
        recentSearchAdapter = null;
        suggestionAdapter = null;
        binding = null;
    }

    protected void observeEvents() {
        viewModel.getShowKeyboard().observe(getViewLifecycleOwner(), show -> {
            if (show) {
                KeyboardUtils.showSoftInput(binding.etSearch);
            }
        });
        viewModel.getSearchHistoryData().observe(getViewLifecycleOwner(), results -> {
            if (results != null && !results.isEmpty()) {
                recentSearchAdapter.submitList(results);
                showContentSate();
            } else {
                showNoDataState(true);
            }
        });

        viewModel.getQuickSearchResult().observe(getViewLifecycleOwner(), results -> {
            if (results != null && !results.isEmpty()) {
                suggestionAdapter.setData(results);
            }
        });

        viewModel.isShowSearchResultAdapter().observe(getViewLifecycleOwner(), result -> {
            // Log.i("tag", "isShowSearchResultAdapter observe called");
            if (binding != null) {
                binding.recyclerView.setAdapter(result ? pagingAdapter : recentSearchAdapter);
            }
        });

        viewModel.getAnimePagingData().observe(getViewLifecycleOwner(), results -> {
            // Log.i("tag", "getSearchResult subscribe called");
            if (results != null) {
                showResults(results);
            }
        });

        viewModel.isShowRecentSearchResultScreen().observe(getViewLifecycleOwner(), show -> {
            if (show) {
                showNoDataState(true);
            }
        });
    }

    protected void initViews() {
        recentSearchAdapter = new RecentSearchesAdapter();
        recentSearchAdapter.setOnItemClickListener(this);
        binding.recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        pagingAdapter = new AnimePagingAdapter(new AnimeReleaseComparator(), GlideLoadUtils.getGlide(requireContext()));
        pagingAdapter.setOnItemClickListener(this::openDetailsFragment);
        suggestionAdapter = new SuggestionAdapter(requireContext(), 0);
        suggestionAdapter.setOnClickListener(this);
        binding.etSearch.setAdapter(suggestionAdapter);
    }

    @Override
    public void onRecentItemClicked(String query) {
        binding.etSearch.setText(query);
        performSearch(query, false);
    }

    @Override
    public void onDeleteRecentItemClicked(String query) {
        viewModel.removeRecentSearch(query);
    }

    @Override
    public void onDeleteAllSearchHistory() {
        viewModel.clearAllRecentSearch();
        viewModel.showRecentSearchResultEmptyScreen();
    }

    private void performSearch(String query, boolean addToHistory) {
        KeyboardUtils.hideSoftInput(binding.etSearch);
        viewModel.setHideKeyboard();
        if (!TextUtils.isEmpty(query) && query.length() >= 3) {
            if (addToHistory) {
                viewModel.addRecentSearch(query);
            }
            preSearch();
            viewModel.getSearchResult(query);
        } else {
            ViewUtils.showSnackbar(requireActivity(), R.string.message_error_short_query);
        }
    }

    @Override
    public void suggestionClicked(String url) {
        SearchFragmentDirections.ActionNavSearchToNavDetails action =
                SearchFragmentDirections.actionNavSearchToNavDetails(url);
        Navigation.findNavController(requireView()).navigate(action);
    }

    private void quickSearch(String query) {
        String hash = PreferenceUtils.getPrefString(requireContext(), PreferenceKeys.PREF_KEY_DLE_HASH, "");
        viewModel.runQuickSearch(query, hash);
    }

    private void preSearch() {
        if (binding != null) {
            showLoadingState();
        }
    }

    private void handlePagingState() {
        pagingAdapter.addLoadStateListener(combinedLoadStates -> {
            LoadState refreshLoadState = combinedLoadStates.getRefresh();
            LoadState appendLoadState = combinedLoadStates.getAppend();
            if (refreshLoadState instanceof LoadState.Loading) {
                showLoadingState();
            }
            if (refreshLoadState instanceof LoadState.NotLoading) {
                if (refreshLoadState.getEndOfPaginationReached() && pagingAdapter.getItemCount() < 1) {
                    showNoDataState(false);
                } else {
                    showContentSate();
                }

            } else if (refreshLoadState instanceof LoadState.Error) {
                showErrorSate((LoadState.Error) refreshLoadState);
            }
            if (!(refreshLoadState instanceof LoadState.Loading) && appendLoadState instanceof LoadState.NotLoading) {
                if (appendLoadState.getEndOfPaginationReached() && pagingAdapter.getItemCount() < 1) {
                    showNoDataState(false);
                }
            }
            return null;
        });
    }

    private void showContentSate() {
        if (binding != null) {
            binding.content.setVisibility(View.VISIBLE);
            binding.loadStateLayout.progressBar.setVisibility(View.GONE);
            binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
        }
    }

    private void showLoadingState() {
        binding.content.setVisibility(View.GONE);
        binding.loadStateLayout.progressBar.setVisibility(View.VISIBLE);
        binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
    }

    private void showErrorSate(LoadState.Error refreshLoadState) {
        if (binding != null) {
            binding.loadStateLayout.progressBar.setVisibility(View.GONE);
            binding.content.setVisibility(View.GONE);
            binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
            binding.loadStateLayout.repeat.setOnClickListener(v -> pagingAdapter.retry());
            binding.loadStateLayout.errorMessage.setText(
                    refreshLoadState.getError().getLocalizedMessage());
        }
    }

    private void showNoDataState(boolean isNoRecentResult) {
        if (binding != null) {
            binding.loadStateLayout.ivIcon.setImageResource(R.drawable.image_no_data);
            binding.loadStateLayout.errorMessageTitle.setText(R.string.state_no_data);
            binding.loadStateLayout.errorMessage.setText(isNoRecentResult ? R.string.state_no_recent_search_desc : R.string.state_no_data_search_desc);

            binding.content.setVisibility(View.GONE);
            binding.loadStateLayout.progressBar.setVisibility(View.GONE);
            binding.loadStateLayout.buttonLl.setVisibility(View.GONE);
            binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
        }
    }

    private void openDetailsFragment(String link) {
        SearchFragmentDirections.ActionNavSearchToNavDetails action =
                SearchFragmentDirections.actionNavSearchToNavDetails(link);
        Navigation.findNavController(requireView()).navigate(action);
    }

    private void showResults(final PagingData<AnimeReleaseModel> results) {
        pagingAdapter.submitData(getLifecycle(), results);
        viewModel.showSearchResultAdapter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.dispose();
    }
}

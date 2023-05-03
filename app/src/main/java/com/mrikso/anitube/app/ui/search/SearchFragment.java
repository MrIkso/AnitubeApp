package com.mrikso.anitube.app.ui.search;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
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
import com.google.android.material.internal.TextWatcherAdapter;
import com.mrikso.anitube.app.adapters.AnimePagingAdapter;
import com.mrikso.anitube.app.adapters.RecentSearchesAdapter;
import com.mrikso.anitube.app.adapters.SuggestionAdapter;
import com.mrikso.anitube.app.comparator.AnimeReleaseComparator;
import com.mrikso.anitube.app.databinding.FragmentSearchBinding;
import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.utils.PreferenceKeys;
import com.mrikso.anitube.app.utils.PreferenceUtils;

import dagger.hilt.android.AndroidEntryPoint;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

@AndroidEntryPoint
public class SearchFragment extends Fragment
        implements RecentSearchesAdapter.OnItemClickListener,
                SuggestionAdapter.OnSuggestionClickListener {
    private RecentSearchesAdapter recentSearchAtapter;
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
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
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
                v -> Navigation.findNavController(requireView()).popBackStack());

        AppCompatAutoCompleteTextView searchEdit = binding.etSearch;
        searchEdit.addTextChangedListener(
                new TextWatcherAdapter() {
                    @Override
                    public void afterTextChanged(Editable editable) {
                        if (searchEdit.hasFocus()) {
                            //  Log.i("beb", "text changed");
                            if (editable != null) {
                                String content = editable.toString();
                                binding.clear.setVisibility(
                                        TextUtils.isEmpty(content) ? View.GONE : View.VISIBLE);
                                if (content != null && content.length() >= 3) {
                                    quickSearch(content);
                                }
                            }
                        }
                    }
                });

        searchEdit.setOnEditorActionListener(
                new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                            performSearch(searchEdit.getText().toString(), true);
                            return true;
                        }
                        return false;
                    }
                });

        binding.clear.setOnClickListener(v -> searchEdit.setText(""));

        handlePagingState();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recentSearchAtapter = null;
        suggestionAdapter = null;
        // binding = null;
    }

    protected void observeEvents() {
        viewModel
                .getSearchHistoryData()
                .observe(
                        getViewLifecycleOwner(),
                        results -> {
                            if (results != null && !results.isEmpty())
                                recentSearchAtapter.setData(results);
                        });

        viewModel
                .getQuickSearchResult()
                .observe(
                        getViewLifecycleOwner(),
                        results -> {
                            if (results != null && !results.isEmpty())
                                suggestionAdapter.setData(results);
                        });

        viewModel
                .isShowSearchResultAdapter()
                .observe(
                        getViewLifecycleOwner(),
                        result -> {
                            // Log.i("tag", "isShowSearchResultAdapter observe called");
                            binding.recyclerView.setAdapter(
                                    result ? pagingAdapter : recentSearchAtapter);
                        });

        viewModel
                .getAnimePagingData()
                .observe(
                        getViewLifecycleOwner(),
                        results -> {
                            // Log.i("tag", "getSearchResult subscribe called");
                            if (results != null) {
                                showResults(results);
                            }
                        });
    }

    protected void initViews() {
        recentSearchAtapter = new RecentSearchesAdapter();
        recentSearchAtapter.setOnItemClickListener(this);
        binding.recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        binding.loadStateLayout.stateFrame.setVisibility(View.GONE);

        pagingAdapter =
                new AnimePagingAdapter(new AnimeReleaseComparator(), getGlide(requireContext()));
        pagingAdapter.setOnItemClickListener(item -> openDeatailsFragment(item));
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
    }

    private void performSearch(String query, boolean addToHistory) {
        if (!TextUtils.isEmpty(query) && query.length() >= 3) {
            if (addToHistory) {
                viewModel.addRecentSearch(query);
            }
            preSearch();
            viewModel.getSearchResult(query);
        }
    }

    @Override
    public void suggestionClicked(String url) {
        SearchFragmentDirections.ActionNavSearchToNavDetails action =
                SearchFragmentDirections.actionNavSearchToNavDetails(url);
        Navigation.findNavController(requireView()).navigate(action);
    }

    private void quickSearch(String query) {
        String hash =
                PreferenceUtils.getPrefString(
                        requireContext(), PreferenceKeys.PREF_KEY_DLE_HASH, "");
        viewModel.runQuickSearch(query, hash);
    }

    private void preSearch() {
        binding.content.setVisibility(View.GONE);
        binding.loadStateLayout.stateFrame.setVisibility(View.VISIBLE);
    }

    private void handlePagingState() {
        pagingAdapter.addLoadStateListener(
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
                                && pagingAdapter.getItemCount() < 1) {
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
                                v -> pagingAdapter.retry());
                        LoadState.Error loadStateError = (LoadState.Error) refreshLoadState;
                        binding.loadStateLayout.errorMessageTv.setText(
                                loadStateError.getError().getLocalizedMessage());
                    }
                    if (!(refreshLoadState instanceof LoadState.Loading)
                            && appendLoadState instanceof LoadState.NotLoading) {
                        if (appendLoadState.getEndOfPaginationReached()
                                && pagingAdapter.getItemCount() < 1) {
                            showNoDataState();
                        }
                    }
                    return null;
                });
    }

    private void showNoDataState() {
        // TODO: Implement this method

    }

    private void openDeatailsFragment(String link) {
        SearchFragmentDirections.ActionNavSearchToNavDetails action =
                SearchFragmentDirections.actionNavSearchToNavDetails(link);
        Navigation.findNavController(requireView()).navigate(action);
    }

    private void showResults(final PagingData<AnimeReleaseModel> results) {
        // binding.loadStateLayout.stateFrame.setVisibility(View.GONE);
        pagingAdapter.submitData(getLifecycle(), results);
        viewModel.showSearchResultAdapter();
    }

    public RequestManager getGlide(Context context) {
        return Glide.with(context)
                .applyDefaultRequestOptions(
                        new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.dispose();
    }
}

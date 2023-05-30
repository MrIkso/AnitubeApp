package com.mrikso.anitube.app.ui.comments;

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
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.adapters.CommentsPagingAdapter;
import com.mrikso.anitube.app.adapters.MoviesLoadStateAdapter;
import com.mrikso.anitube.app.databinding.FragmentCommentsBinding;
import com.mrikso.anitube.app.model.CommentModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CommentsFragment extends Fragment {

    private FragmentCommentsBinding binding;
    private CommentsFragmentViewModel viewModel;
    private CommentsPagingAdapter commentsAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CommentsFragmentViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCommentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        initViews();
        initializeListeners();
        loadData();
        initObservers();
    }

    private void initializeListeners() {
        binding.toolbar.setNavigationOnClickListener(
                v -> Navigation.findNavController(requireView()).popBackStack());
    }

    private void initViews() {
        binding.recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        commentsAdapter = new CommentsPagingAdapter(getGlide(requireContext()));
        commentsAdapter.setOnItemClickListener(link -> {
            openProfileFragment(link);
        });
        commentsAdapter.addLoadStateListener(combinedLoadStates -> {
            LoadState refreshLoadState = combinedLoadStates.getRefresh();
            LoadState appendLoadState = combinedLoadStates.getAppend();
            if (refreshLoadState instanceof LoadState.Loading) {
                binding.content.setVisibility(View.GONE);
                binding.loadStateLayout.progressBar.setVisibility(View.VISIBLE);
                binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
            }
            if (refreshLoadState instanceof LoadState.NotLoading) {
                if (refreshLoadState.getEndOfPaginationReached() && commentsAdapter.getItemCount() < 1) {
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
                binding.loadStateLayout.repeat.setOnClickListener(v -> commentsAdapter.retry());
                LoadState.Error loadStateError = (LoadState.Error) refreshLoadState;
                binding.loadStateLayout.errorMessage.setText(
                        loadStateError.getError().getLocalizedMessage());
            }
            if (!(refreshLoadState instanceof LoadState.Loading) && appendLoadState instanceof LoadState.NotLoading) {
                if (appendLoadState.getEndOfPaginationReached() && commentsAdapter.getItemCount() < 1) {
                    showNoDataState();
                }
            }
            return null;
        });

        binding.recyclerView.setAdapter(commentsAdapter.withLoadStateFooter(new MoviesLoadStateAdapter(v -> {
            commentsAdapter.retry();
        })));
    }

    private void openProfileFragment(final String link) {
        CommentsFragmentDirections.ActionNavCommentsToNavProfile action =
                CommentsFragmentDirections.actionNavCommentsToNavProfile(link);
        Navigation.findNavController(requireView()).navigate(action);
    }

    private void showNoDataState() {
        binding.loadStateLayout.ivIcon.setImageResource(R.drawable.image_no_data);
        binding.loadStateLayout.errorMessageTitle.setText(R.string.state_no_data);
        binding.loadStateLayout.errorMessage.setText(R.string.state_no_data_comments_desc);

        binding.content.setVisibility(View.GONE);
        binding.loadStateLayout.progressBar.setVisibility(View.GONE);
        binding.loadStateLayout.buttonLl.setVisibility(View.GONE);
        binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
    }

    private void initObservers() {
        viewModel.getCommentsPagingData().observe(getViewLifecycleOwner(), results -> {
            if (binding != null) {
                if (results != null) {
                    showCommentList(results);
                } else {
                    showNoDataState();
                }
            }
        });
    }

    private void showCommentList(final PagingData<CommentModel> results) {
        commentsAdapter.submitData(getLifecycle(), results);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        commentsAdapter = null;
        binding = null;
    }

    public RequestManager getGlide(Context context) {
        return Glide.with(context)
                .applyDefaultRequestOptions(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL));
    }

    private void loadData() {
        CommentsFragmentArgs arg = CommentsFragmentArgs.fromBundle(getArguments());
        viewModel.loadComments(arg.getAnimeId());
    }
}

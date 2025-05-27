package com.mrikso.anitube.app.ui.comments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
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

import com.google.android.material.internal.TextWatcherAdapter;
import com.google.common.base.Strings;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.adapters.CommentsPagingAdapter;
import com.mrikso.anitube.app.adapters.MoviesLoadStateAdapter;
import com.mrikso.anitube.app.databinding.FragmentCommentsBinding;
import com.mrikso.anitube.app.model.CommentModel;
import com.mrikso.anitube.app.utils.PreferencesHelper;
import com.mrikso.anitube.app.utils.ViewUtils;

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

    @SuppressLint("RestrictedApi")
    private void initViews() {
        if (!PreferencesHelper.getInstance().isLogin()) {
            binding.sendMsgPanel.sendMsgPanel.setVisibility(View.GONE);
        }
        binding.recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        commentsAdapter = new CommentsPagingAdapter();
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

        binding.sendMsgPanel.send.setOnClickListener((v) -> sendComment());
        binding.sendMsgPanel.send.setEnabled(false);
        binding.sendMsgPanel.commentEt.addTextChangedListener(new TextWatcherAdapter(){
            @Override
            public void afterTextChanged(@NonNull Editable editable) {
                if (editable.length() >=3){
                    binding.sendMsgPanel.send.setEnabled(true);
                }
            }
        });
    }

    private void sendComment(){
       int animeId =  CommentsFragmentArgs.fromBundle(getArguments()).getAnimeId();
       String comment = binding.sendMsgPanel.commentEt.getText().toString();
       if(!Strings.isNullOrEmpty(comment)){
           viewModel.addComments(animeId, comment);
       }
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
        viewModel.getLoadState().observe(getViewLifecycleOwner(), results -> {
            if (binding != null && results != null) {
               switch (results.first){
                   case DONE:
                       commentsAdapter.refresh();
                       binding.sendMsgPanel.sendProgress.setVisibility(View.GONE);
                       binding.sendMsgPanel.send.setVisibility(View.VISIBLE);
                       binding.sendMsgPanel.commentEt.setText(null);
                       break;
                   case ERROR:
                       binding.sendMsgPanel.sendProgress.setVisibility(View.GONE);
                       binding.sendMsgPanel.send.setVisibility(View.VISIBLE);
                       ViewUtils.showSnackbar(this, String.valueOf(results.second));
                       break;
                   case LOADING:
                       binding.sendMsgPanel.sendProgress.setVisibility(View.VISIBLE);
                       binding.sendMsgPanel.send.setVisibility(View.GONE);
                       break;
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

    private void loadData() {
        CommentsFragmentArgs arg = CommentsFragmentArgs.fromBundle(getArguments());
        viewModel.loadComments(arg.getAnimeId());
    }
}

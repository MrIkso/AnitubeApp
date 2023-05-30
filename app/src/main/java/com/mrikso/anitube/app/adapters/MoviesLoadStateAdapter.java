package com.mrikso.anitube.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.LoadState;
import androidx.paging.LoadStateAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.anitube.app.databinding.LoadStateItemBinding;

import org.jetbrains.annotations.NotNull;

public class MoviesLoadStateAdapter extends LoadStateAdapter<MoviesLoadStateAdapter.LoadStateViewHolder> {
    // Define Retry Callback
    private View.OnClickListener mRetryCallback;

    public MoviesLoadStateAdapter(View.OnClickListener retryCallback) {
        // Init Retry Callback
        mRetryCallback = retryCallback;
    }

    @NotNull
    @Override
    public LoadStateViewHolder onCreateViewHolder(@NotNull ViewGroup parent, @NotNull LoadState loadState) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        LoadStateItemBinding binding = LoadStateItemBinding.inflate(inflater, parent, false);
        // Return new LoadStateViewHolder object
        return new LoadStateViewHolder(binding, mRetryCallback);
    }

    @Override
    public void onBindViewHolder(@NotNull LoadStateViewHolder holder, @NotNull LoadState loadState) {
        // Call Bind Method to bind visibility  of views
        holder.bind(loadState);
    }

    public static class LoadStateViewHolder extends RecyclerView.ViewHolder {
        // Define Progress bar
        private ProgressBar mProgressBar;
        // Define error TextView
        private TextView mErrorMsg;
        // Define Retry button
        private Button mRetry;

        LoadStateViewHolder(@NonNull LoadStateItemBinding binding, @NonNull View.OnClickListener retryCallback) {

            super(binding.getRoot());
            mProgressBar = binding.progressBar;
            mErrorMsg = binding.errorMsg;
            mRetry = binding.retryButton;
            mRetry.setOnClickListener(retryCallback);
        }

        public void bind(LoadState loadState) {
            // Check load state
            if (loadState instanceof LoadState.Error) {
                // Get the error
                LoadState.Error loadStateError = (LoadState.Error) loadState;
                // Set text of Error message
                mErrorMsg.setText(loadStateError.getError().getLocalizedMessage());
            }
            // set visibility of widgets based on LoadState
            mProgressBar.setVisibility(loadState instanceof LoadState.Loading ? View.VISIBLE : View.GONE);
            mRetry.setVisibility(loadState instanceof LoadState.Error ? View.VISIBLE : View.GONE);
            mErrorMsg.setVisibility(loadState instanceof LoadState.Error ? View.VISIBLE : View.GONE);
        }
    }
}

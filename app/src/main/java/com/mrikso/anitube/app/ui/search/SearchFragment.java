package com.mrikso.anitube.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import androidx.lifecycle.ViewModelProvider;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.base.BaseFragment;
import com.mrikso.anitube.app.databinding.FragmentSearchBinding;
import com.mrikso.anitube.app.ui.search.SearchFragmentViewModel;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SearchFragment extends BaseFragment<FragmentSearchBinding, SearchFragmentViewModel> {

    @Override
    protected FragmentSearchBinding inflateBinding(
            LayoutInflater inflater, ViewGroup container, boolean attachToParent) {
        return FragmentSearchBinding.inflate(inflater, container, false);
    }

    @Override
    protected SearchFragmentViewModel viewModel() {
        return new ViewModelProvider(this).get(SearchFragmentViewModel.class);
    }

    @Override
    protected void initializeListeners() {
		binding().counterBtn.setOnClickListener( v-> viewModel().count());
	}

    @Override
    protected void observeEvents() {
        viewModel().getCounterData().observe(getViewLifecycleOwner(), results -> {
			binding().counterTv.setText(String.valueOf(results));
		});
    }

    @Override
    protected void onCreateFinished() {}
}

package com.mrikso.anitube.app.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.adapters.SimpleAdapter;
import com.mrikso.anitube.app.databinding.FragmentSearchResultBinding;
import com.mrikso.anitube.app.model.SimpleModel;

import dagger.hilt.android.AndroidEntryPoint;

import java.util.List;

@AndroidEntryPoint
public class SimpleListFragment extends Fragment {
    private HomeFragmentViewModel viewModel;
    private FragmentSearchResultBinding binding;
    private SimpleAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(getActivity()).get(HomeFragmentViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchResultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        loadData();
        initViews();
        initializeListeners();
    }

    private void observeEvents(boolean isYears) {
        if (isYears) {

            viewModel.getYearsList().observe(getViewLifecycleOwner(), results -> {
                Log.i("ee", "size: " + results.size());
                if (binding != null) {
                    if (results != null) {
                        showList(results);
                    } else {
                        showNoDataState();
                    }
                }
            });

        } else {
            viewModel.getGenresList().observe(getViewLifecycleOwner(), results -> {
                Log.i("ee", "size: " + results.size());
                if (binding != null) {
                    if (results != null) {
                        showList(results);
                    } else {
                        showNoDataState();
                    }
                }
            });
        }
    }

    private void initializeListeners() {
        binding.back.setOnClickListener(
                v -> Navigation.findNavController(requireView()).popBackStack());
    }

    private void initViews() {
        binding.recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        adapter = new SimpleAdapter();
        adapter.setOnItemClickListener((name, link) -> {
            openDetailsFragment(name, link);
        });

        binding.recyclerView.setAdapter(adapter);
    }

    private void openDetailsFragment(final String name, final String link) {
        SimpleListFragmentDirections.ActionNavSimpleListToNavSearchResult action =
                SimpleListFragmentDirections.actionNavSimpleListToNavSearchResult(name, link);
        Navigation.findNavController(requireView()).navigate(action);
    }

    private void showNoDataState() {
        // TODO: Implement this method
        binding.loadStateLayout.progressBar.setVisibility(View.GONE);
        binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
        binding.loadStateLayout.repeat.setVisibility(View.GONE);
        binding.content.setVisibility(View.GONE);
    }

    private void showList(final List<SimpleModel> results) {
        adapter.submitList(results);

        binding.loadStateLayout.progressBar.setVisibility(View.GONE);
        binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
        binding.content.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        adapter = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void loadData() {
        SimpleListFragmentArgs arg = SimpleListFragmentArgs.fromBundle(getArguments());
        binding.tvSearchTitle.setText(arg.getIsYears() ? R.string.release_calendar : R.string.release_genres);
        observeEvents(arg.getIsYears());
    }
}

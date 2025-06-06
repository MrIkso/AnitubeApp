package com.mrikso.anitube.app.ui.home;

import android.os.Bundle;
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

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SimpleListFragment extends Fragment {
    private HomeFragmentViewModel viewModel;
    private FragmentSearchResultBinding binding;
    private SimpleAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HomeFragmentViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchResultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        loadData();
        initViews();
        initializeListeners();
    }

    private void observeEvents(boolean isYears) {
        if (isYears) {

            viewModel.getYearsList().observe(getViewLifecycleOwner(), results -> {
                //Log.i("ee", "size: " + results.size());
                if (binding != null) {
                    if (results != null) {
                        showList(results);
                    } else {
                        showNoDataState(isYears);
                    }
                }
            });

        } else {
            viewModel.getGenresList().observe(getViewLifecycleOwner(), results -> {
                //Log.i("ee", "size: " + results.size());
                if (binding != null) {
                    if (results != null) {
                        showList(results);
                    } else {
                        showNoDataState(isYears);
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
        adapter.setOnItemClickListener(this::openDetailsFragment);

        binding.recyclerView.setAdapter(adapter);
    }

    private void openDetailsFragment(final String name, final String link) {
        SimpleListFragmentDirections.ActionNavSimpleListToNavSearchResult action =
                SimpleListFragmentDirections.actionNavSimpleListToNavSearchResult(name, link);
        Navigation.findNavController(requireView()).navigate(action);
    }

    private void showNoDataState(boolean isYears) {
        binding.loadStateLayout.ivIcon.setImageResource(R.drawable.image_no_data);
        binding.loadStateLayout.errorMessageTitle.setText(R.string.state_no_data);
        binding.loadStateLayout.errorMessage.setText(isYears? R.string.state_no_data_years_list_desc : R.string.state_no_data_genres_list_desc);

        binding.content.setVisibility(View.GONE);
        binding.loadStateLayout.progressBar.setVisibility(View.GONE);
        binding.loadStateLayout.buttonLl.setVisibility(View.GONE);
        binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
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

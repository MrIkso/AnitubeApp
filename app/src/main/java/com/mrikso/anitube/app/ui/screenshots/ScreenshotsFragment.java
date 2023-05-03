package com.mrikso.anitube.app.ui.screenshots;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mrikso.anitube.app.adapters.ScreenshotsVpAdapter;
import com.mrikso.anitube.app.databinding.FragmentScreenshotsBinding;
import com.mrikso.anitube.app.model.ScreenshotModel;

import dagger.hilt.android.AndroidEntryPoint;

import java.util.List;

@AndroidEntryPoint
public class ScreenshotsFragment extends Fragment {

    private FragmentScreenshotsBinding binding;
    private ScreenshotsVpAdapter screenshotAdapter;
    private List<ScreenshotModel> screenshots;
    private int currentPosition;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseArgs();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentScreenshotsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        setStatusBarColor();
        setupScreenshotAdapter();
        setupViewPager();
    }

    private void setStatusBarColor() {
        requireActivity().getWindow().setStatusBarColor(android.R.color.transparent);
    }

    private void setupScreenshotAdapter() {
        screenshotAdapter = new ScreenshotsVpAdapter();
        screenshotAdapter.submitList(screenshots);
    }

    private void setupViewPager() {
        binding.viewPager2.setAdapter(screenshotAdapter);
        binding.viewPager2.setCurrentItem(currentPosition, false);
    }

    private void parseArgs() {
        ScreenshotsFragmentArgs args = ScreenshotsFragmentArgs.fromBundle(getArguments());
        screenshots = args.getScreenshots();
        currentPosition = args.getPosition();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        screenshotAdapter = null;
    }
}

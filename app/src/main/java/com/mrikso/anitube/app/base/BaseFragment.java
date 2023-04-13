package com.mrikso.anitube.app.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.viewbinding.ViewBinding;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class BaseFragment<VB extends ViewBinding, VM extends ViewModel> extends Fragment {
    private VB binding;

    protected abstract VB inflateBinding(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            boolean attachToParent);

    protected abstract VM viewModel();

    protected abstract void initializeListeners();

    protected abstract void observeEvents();

    protected abstract void onCreateFinished();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = inflateBinding(inflater, container, false);

        if (binding == null) {
            throw new IllegalArgumentException("Binding cannot be null");
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onCreateFinished();
        initializeListeners();
        observeEvents();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    protected VB binding() {
        return Objects.requireNonNull(binding);
    }
}

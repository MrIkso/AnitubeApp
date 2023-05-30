package com.mrikso.anitube.app.ui.dialogs.base;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mrikso.anitube.app.databinding.DialogBottomSheetBaseBinding;

public class BaseBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private DialogBottomSheetBaseBinding binding;

    @Nullable
    @Override
    public final View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogBottomSheetBaseBinding.inflate(inflater, container, false);
        FrameLayout containerFrame = binding.containerBottomSheetDialogBaseContent;
        View contentView = onCreateContentView(LayoutInflater.from(requireContext()), container, savedInstanceState);
        if (contentView != null) {
            onContentViewCreated(contentView, savedInstanceState);
            containerFrame.addView(contentView);
        }
        return binding.getRoot();

        //  return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public final void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    protected View onCreateContentView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return null;
    }

    protected void onContentViewCreated(View view, @Nullable Bundle savedInstanceState) {}

    protected Button getPositiveButton() {
        return binding.buttonBottomSheetDialogBaseOk;
    }

    protected Button getNegativeButton() {
        return binding.buttonBottomSheetDialogBaseCancel;
    }

    public void setTitle(@StringRes int title) {
        setTitle(getString(title));
    }

    public void setTitle(CharSequence title) {
        binding.tvBottomSheetDialogBaseTitle.setText(title);
    }

    protected void revealBottomSheet() {
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(binding.getRoot());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        Object parent = getParentFragment();
        if (parent == null) parent = requireActivity();

        if (parent instanceof OnDismissListener && getTag() != null)
            ((OnDismissListener) parent).onDialogDismissed(getTag());
    }

    public interface OnDismissListener {

        void onDialogDismissed(@NonNull String dialogTag);
    }
}

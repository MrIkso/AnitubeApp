package com.mrikso.anitube.app.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.databinding.DialogChangeAnimeStatusBinding;
import com.mrikso.anitube.app.repository.ViewStatusAnime;

public class ChangeAnimeStatusDialog extends BottomSheetDialogFragment {
    public static final String TAG = "ChangeAnimeStatusDialog";
    private static final String MODE_NUMBER = "mode_number";
    private DialogChangeAnimeStatusBinding binding;
    private OnItemClickListener listener;

    public static ChangeAnimeStatusDialog newInstance(int tabNumber) {
        ChangeAnimeStatusDialog fragment = new ChangeAnimeStatusDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(MODE_NUMBER, tabNumber);
        fragment.setArguments(bundle);
        return fragment;
    }

    public ChangeAnimeStatusDialog() {}

    public interface OnItemClickListener {
        void onChecked(int mode);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogChangeAnimeStatusBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int mode = getArguments().getInt(MODE_NUMBER, 1);
        setCheckedRadioButton(mode);
        if (listener != null) {
            sendChekedResult();
        }

        binding.buttonBottomSheetDialogBaseCancel.setOnClickListener(v -> dismiss());
    }

    void sendChekedResult() {
        binding.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            ViewStatusAnime mode = ViewStatusAnime.STATUS_WATCH;
            switch (checkedId) {
                case R.id.anime_status_no_watch:
                    mode = ViewStatusAnime.STATUS_NONE_WATCH;
                    break;
                case R.id.anime_status_adand:
                    mode = ViewStatusAnime.STATUS_ADAND;
                    break;
                case R.id.anime_status_seen:
                    mode = ViewStatusAnime.STATUS_SEEN;
                    break;
                case R.id.anime_status_will:
                    mode = ViewStatusAnime.STATUS_WILL;
                    break;
                case R.id.anime_status_watch:
                    mode = ViewStatusAnime.STATUS_WATCH;
                    break;
                case R.id.anime_status_poned:
                    mode = ViewStatusAnime.STATUS_PONED;
                    break;
            }
            listener.onChecked(mode.getStatusCode());
            dismiss();
        });
    }

    void setCheckedRadioButton(int mode) {
        ViewStatusAnime enumMode = ViewStatusAnime.fromId(mode);
        switch (enumMode) {
            case STATUS_NONE_WATCH:
                binding.animeStatusNoWatch.setChecked(true);
                break;
            case STATUS_SEEN:
                binding.animeStatusSeen.setChecked(true);
                break;
            case STATUS_WILL:
                binding.animeStatusWill.setChecked(true);
                break;
            case STATUS_WATCH:
                binding.animeStatusWatch.setChecked(true);
                break;
            case STATUS_PONED:
                binding.animeStatusPoned.setChecked(true);
                break;
            case STATUS_ADAND:
                binding.animeStatusAdand.setChecked(true);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        listener = null;
    }

    public OnItemClickListener getListener() {
        return this.listener;
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}

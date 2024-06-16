package com.mrikso.anitube.app.ui.registration;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.internal.TextWatcherAdapter;
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec;
import com.google.android.material.progressindicator.IndeterminateDrawable;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.databinding.FragmentRegistrationBinding;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.UserModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegistrationFragment extends Fragment {
    private RegistrationFragmentViewModel viewModel;
    private FragmentRegistrationBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RegistrationFragmentViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        initViews();
        //observeEvents();
    }

    @SuppressLint("RestrictedApi")
    private void initViews() {
        binding.toolbar.setNavigationOnClickListener(
                v -> Navigation.findNavController(requireView()).popBackStack());

        binding.signUpBtn.setOnClickListener(v -> {
            createUser();
        });

        binding.nameEt.addTextChangedListener(new TextWatcherAdapter(){
            @Override
            public void afterTextChanged(@NonNull Editable editable) {
                String name = editable.toString();
                if(!TextUtils.isEmpty(name) && name.length() >= 3){
                   // viewModel.checkName(name);
                }
            }
        });
    }

    private void createLoadingIndicator() {
        CircularProgressIndicatorSpec spec = new CircularProgressIndicatorSpec(
                requireContext(),
                /*attrs=*/ null,
                0,
                com.google.android.material.R.style.Widget_Material3_CircularProgressIndicator_ExtraSmall);

        IndeterminateDrawable<CircularProgressIndicatorSpec> progressIndicatorDrawable =
                IndeterminateDrawable.createCircularDrawable(requireContext(), spec);
        binding.signUpBtn.setIcon(progressIndicatorDrawable);
    }

  /*  private void observeEvents() {
        viewModel.getLoadState().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                LoadState state = result.first;
                switch (state) {
                    case ERROR:
                        binding.signUpBtn.setIcon(null);
                        Toast.makeText(getContext(), getText(R.string.registration_error), Toast.LENGTH_LONG).show();
                        break;
                    case LOADING:
                        createLoadingIndicator();
                        break;
                    case DONE:
                        //openLoginFragment(result.second);
                        break;
                }
            }
        });

        viewModel.getCheckNameState().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                LoadState state = result.first;
                switch (state) {
                    case ERROR:
                        binding.nameTil.setError(result.second);
                        break;
                    case LOADING:
                        break;
                    case DONE:
                        binding.nameTil.setErrorEnabled(false);
                        binding.nameTil.setError(null);
                         Toast.makeText(getContext(), result.second, Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
    }*/

    private void createUser() {

        String name = binding.nameEt.getText().toString();
        String userPassword = binding.passwordEt.getText().toString();
        String userEmail = binding.emailEt.getText().toString();

        if (TextUtils.isEmpty(userEmail)) {
            binding.nameTil.setError(getText(R.string.register_error_empty_user_name));
            binding.nameTil.requestFocus();
            return;
        } else if (TextUtils.isEmpty(userEmail)) {
            binding.emailTil.setError(getText(R.string.login_error_empty_email));
            binding.emailTil.requestFocus();
            return;
        } else if (TextUtils.isEmpty(userPassword)) {
            binding.passwordTil.setError(getText(R.string.login_error_empty_password));
            binding.passwordTil.requestFocus();
            return;
        }
    }

    private void openLoginFragment(UserModel userData) {
        Toast.makeText(requireContext(), getText(R.string.login_successful_login), Toast.LENGTH_SHORT)
                .show();

//        LoginFragmentDirections.ActionNavLoginToNavProfile action =
//                LoginFragmentDirections.actionNavLoginToNavProfile(userData.getUserUrl());
//        Navigation.findNavController(requireView()).navigate(action);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


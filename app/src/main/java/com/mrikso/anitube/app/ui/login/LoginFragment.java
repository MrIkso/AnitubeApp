package com.mrikso.anitube.app.ui.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.progressindicator.CircularProgressIndicatorSpec;
import com.google.android.material.progressindicator.IndeterminateDrawable;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.databinding.FragmentLoginBinding;
import com.mrikso.anitube.app.model.LoadState;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {
    private LoginFragmentViewModel viewModel;
    private FragmentLoginBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoginFragmentViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        initViews();
        observeEvents();
    }

    private void initViews() {

        binding.loginBtn.setOnClickListener(
                v -> {
                    loginUser();
                });
    }

    private void createLoadingIndicator() {
        CircularProgressIndicatorSpec spec =
                new CircularProgressIndicatorSpec(
                        getContext(),
                        /*attrs=*/ null,
                        0,
                        com.google
                                .android
                                .material
                                .R
                                .style
                                .Widget_Material3_CircularProgressIndicator_ExtraSmall);

        IndeterminateDrawable<CircularProgressIndicatorSpec> progressIndicatorDrawable =
                IndeterminateDrawable.createCircularDrawable(getContext(), spec);
        binding.loginBtn.setIcon(progressIndicatorDrawable);
    }

    private void observeEvents() {
        viewModel
                .getLoadState()
                .observe(
                        getViewLifecycleOwner(),
                        result -> {
                            if (result != null) {
                                LoadState state = result.first;
                                switch (state) {
                                    case ERROR:
                                        binding.loginBtn.setIcon(null);
                                        Toast.makeText(
                                                        getContext(),
                                                        getText(R.string.login_error),
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                        break;
                                    case LOADING:
                                        createLoadingIndicator();
                                        break;
                                    case DONE:
                                        openLoginFragment(result.second);
                                        break;
                                }
                            }
                        });
    }

    private void loginUser() {
        String userEmail = binding.userNameEt.getText().toString();
        String userPassword = binding.passwordEt.getText().toString();

        if (TextUtils.isEmpty(userEmail)) {
            binding.userNameEt.setError(getText(R.string.login_error_empty_email));
            binding.userNameEt.requestFocus();
        } else if (TextUtils.isEmpty(userPassword)) {
            binding.passwordEt.setError(getText(R.string.login_error_empty_password));
            binding.passwordEt.requestFocus();
        } else {
            viewModel.login(userEmail, userPassword);
        }
    }

    private void openLoginFragment(Pair<String, String> userData) {
        Toast.makeText(
                        requireContext(),
                        getText(R.string.login_successful_login),
                        Toast.LENGTH_SHORT)
                .show();

        LoginFragmentDirections.ActionNavLoginToNavProfile action =
                LoginFragmentDirections.actionNavLoginToNavProfile(userData.second);
        Navigation.findNavController(requireView()).navigate(action);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

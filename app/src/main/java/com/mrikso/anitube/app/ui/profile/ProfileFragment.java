package com.mrikso.anitube.app.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.common.base.Strings;
import com.mrikso.anitube.app.databinding.FragmentProfileBinding;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.UserProfileModel;
import com.mrikso.anitube.app.utils.ParserUtils;
import com.mrikso.anitube.app.utils.PreferencesHelper;
import com.mrikso.anitube.app.utils.ViewUtils;

import dagger.hilt.android.AndroidEntryPoint;

import java.util.HashSet;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {
    private ProfilePragmentViewModel viewModel;
    private FragmentProfileBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfilePragmentViewModel.class);
        loadPage();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        initViews();
        observeEvents();
    }

    private void observeEvents() {
        viewModel
                .getData()
                .observe(
                        getViewLifecycleOwner(),
                        result -> {
                            LoadState state = result.first;
                            switch (state) {
                                case ERROR:
                                    binding.content.setVisibility(View.GONE);
                                    binding.loadStateLayout.progressBar.setVisibility(View.GONE);
                                    binding.loadStateLayout.errorLayout.setVisibility(View.VISIBLE);
                                    break;
                                case LOADING:
                                    binding.content.setVisibility(View.GONE);
                                    binding.loadStateLayout.progressBar.setVisibility(View.VISIBLE);
                                    binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
                                    break;
                                case DONE:
                                    binding.loadStateLayout.progressBar.setVisibility(View.GONE);
                                    binding.loadStateLayout.errorLayout.setVisibility(View.GONE);
                                    binding.content.setVisibility(View.VISIBLE);
                                    showUserData(result.second);
                                    break;
                            }
                        });
    }

    private void initViews() {
        binding.ibBack.setOnClickListener(
                v -> Navigation.findNavController(requireView()).popBackStack());
        binding.swipeRefreshLayout.setOnRefreshListener(
                () -> {
                    reloadPage();
                    binding.swipeRefreshLayout.setRefreshing(false);
                });
        binding.loadStateLayout.repeat.setOnClickListener(v -> reloadPage());

        binding.logoutBtn.setOnClickListener(
                v -> {
                    PreferencesHelper.getInstance().saveCooikes(new HashSet<>());
                    PreferencesHelper.getInstance().setLogin(false);
                });
    }

    private void reloadPage() {
        ProfileFragmentArgs args = ProfileFragmentArgs.fromBundle(getArguments());
        viewModel.reloadData(args.getUrl());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadPage() {
        ProfileFragmentArgs args = ProfileFragmentArgs.fromBundle(getArguments());
        viewModel.loadData(args.getUrl());
    }

    private void showUserData(UserProfileModel model) {
        // https://anitube.in.ua/templates/smartphone/images/profile_bg.jpg
        ViewUtils.loadImage(
                binding.layoutProfileHeader.userAva,
                ParserUtils.normaliseImageUrl(model.getUserAvatarUrl()));
        ViewUtils.loadImage(
                binding.layoutProfileHeader.avatarBg,
                "https://anitube.in.ua/templates/smartphone/images/profile_bg.jpg");
        binding.layoutProfileHeader.userNickname.setText(model.getUsername());

        binding.layoutProfileHeader.userGroupTv.setText(model.getUserGroup());
        binding.layoutProfileHeader.status.setText(String.valueOf(model.getUserOnline()));
        binding.layoutProfileHeader.comments.setText(String.valueOf(model.getUserCommentsCount()));
        binding.layoutProfileHeader.commentsRating.setText(
                String.valueOf(model.getUserCommentsRating()));

        if (!Strings.isNullOrEmpty(model.getName())) {
            binding.profileProfileNameTr.setVisibility(View.VISIBLE);
            binding.profileNameTv.setText(model.getName());
        }
        if (!Strings.isNullOrEmpty(model.getUserCity())) {
            binding.profileCityTr.setVisibility(View.VISIBLE);
            binding.profileCityTv.setText(model.getUserCity());
        }

        binding.profileRegisterDateTv.setText(model.getUserRegisterData());
        binding.profileLastActivityTv.setText(model.getUserLastActibityData());

        if (!Strings.isNullOrEmpty(model.getUserInfo())) {
            binding.profileInfoTr.setVisibility(View.VISIBLE);
            binding.profileInfoTv.setText(model.getUserInfo());
        }

        if (!Strings.isNullOrEmpty(model.getUserStatus())) {
            binding.profileStatusTr.setVisibility(View.VISIBLE);
            binding.profileStatusTv.setText(model.getUserStatus());
        }
    }
}

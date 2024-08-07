package com.mrikso.anitube.app.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.common.base.Strings;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.databinding.FragmentProfileBinding;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.UserProfileModel;
import com.mrikso.anitube.app.network.ApiClient;
import com.mrikso.anitube.app.utils.DialogUtils;
import com.mrikso.anitube.app.utils.ParserUtils;
import com.mrikso.anitube.app.utils.PreferencesHelper;
import com.mrikso.anitube.app.utils.ViewUtils;

import java.util.HashSet;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {
    private ProfilePragmentViewModel viewModel;
    private FragmentProfileBinding binding;
    private boolean isMyProfile;

    public ProfileFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfilePragmentViewModel.class);
        loadPage();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        initViews();
        initToolbar();
        observeEvents();
    }

    private void observeEvents() {
        viewModel.getData().observe(getViewLifecycleOwner(), result -> {
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

    private void initToolbar() {
        binding.toolbar.setNavigationOnClickListener(
                v -> Navigation.findNavController(requireView()).popBackStack());


        binding.toolbar.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                if(isMyProfile) {
                    menuInflater.inflate(R.menu.profile_menu, menu);
                }
            }
            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.action_logout) {
                    DialogUtils.showConfirmation(
                            requireContext(),
                            R.string.dialog_confirm_title,
                            R.string.dialog_confirm_logout, () -> logout());
                } else if (itemId == R.id.action_settings) {
                    Navigation.findNavController(requireView()).navigate(R.id.nav_settings);
                }
                return true;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

    }

    private void initViews() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            reloadPage();
            binding.swipeRefreshLayout.setRefreshing(false);
        });
        binding.loadStateLayout.repeat.setOnClickListener(v -> reloadPage());
    }

    private void reloadPage() {
        ProfileFragmentArgs args = ProfileFragmentArgs.fromBundle(getArguments());
        isMyProfile = args.getUrl().contains(PreferencesHelper.getInstance().getUserLogin());

        viewModel.reloadData(args.getUrl());
    }

    private void logout() {
        PreferencesHelper.getInstance().saveCookies(new HashSet<>());
        PreferencesHelper.getInstance().setLogin(false);
        PreferencesHelper.getInstance().setUserLogin(null);
        Navigation.findNavController(requireView()).navigate(ProfileFragmentDirections.actionNavProfileToNavLogin());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadPage() {
        ProfileFragmentArgs args = ProfileFragmentArgs.fromBundle(getArguments());
        isMyProfile = args.getUrl().contains(PreferencesHelper.getInstance().getUserLogin());

        viewModel.loadData(args.getUrl());
    }

    private void showUserData(UserProfileModel model) {
        // https://anitube.in.ua/templates/smartphone/images/profile_bg.jpg

        ViewUtils.loadImage(
                binding.layoutProfileHeader.avatar, ParserUtils.normaliseImageUrl(ParserUtils.loadSmartphoneNoAvatar(model.getUserAvatarUrl())));

        boolean isOnline = model.getUserOnline().equalsIgnoreCase("онлайн");
        binding.layoutProfileHeader.online.setImageResource(
                isOnline ? R.drawable.ic_circle_status_online : R.drawable.ic_circle_status_offline);

        ViewUtils.loadImage(
                binding.layoutProfileHeader.avatarBg, model.getProfileBackground());
        binding.layoutProfileHeader.userNickname.setText(model.getUsername());

        binding.layoutProfileHeader.userGroupTv.setText(model.getUserGroup());
        // binding.layoutProfileHeader.status.setText(String.valueOf(model.getUserOnline()));
        binding.layoutProfileHeader.comments.setText(String.valueOf(model.getUserCommentsCount()));
        // binding.layoutProfileHeader.commentsRating.setText(String.valueOf(model.getUserCommentsRating()));

        if (!Strings.isNullOrEmpty(model.getName())) {
            binding.profileProfileNameTr.setVisibility(View.VISIBLE);
            binding.profileNameTv.setText(model.getName());
        }
        if (!Strings.isNullOrEmpty(model.getUserCity())) {
            binding.profileCityTr.setVisibility(View.VISIBLE);
            binding.profileCityTv.setText(model.getUserCity());
        }

        binding.profileRegisterDateTv.setText(model.getUserRegisterData());
        binding.profileLastActivityTv.setText(model.getUserLastActivityData());

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

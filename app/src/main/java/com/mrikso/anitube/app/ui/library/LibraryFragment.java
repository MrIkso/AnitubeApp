package com.mrikso.anitube.app.ui.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.databinding.FragmentLibraryBinding;
import com.mrikso.anitube.app.ui.history.WatchHistoryFragment;
import com.mrikso.anitube.app.utils.PreferencesHelper;

import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment {
    private FragmentLibraryBinding binding;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLibraryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        setupViewPager();
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager(), getLifecycle());
        adapter.addFragment(WatchHistoryFragment.newInstance());
        if (PreferencesHelper.getInstance().isLogin()) {
            adapter.addFragment(LibaryContentFragment.newInstance(AnimeListType.LIST_FAVORITES));
            adapter.addFragment(LibaryContentFragment.newInstance(AnimeListType.LIST_ALL));
            adapter.addFragment(LibaryContentFragment.newInstance(AnimeListType.LIST_SEEN));
            adapter.addFragment(LibaryContentFragment.newInstance(AnimeListType.LIST_WILL));
            adapter.addFragment(LibaryContentFragment.newInstance(AnimeListType.LIST_WATCH));
            adapter.addFragment(LibaryContentFragment.newInstance(AnimeListType.LIST_PONED));
            adapter.addFragment(LibaryContentFragment.newInstance(AnimeListType.LIST_ADAND));
        }
        binding.viewPager2.setAdapter(adapter);
        binding.viewPager2.setOffscreenPageLimit(adapter.getItemCount());

        TabLayout tabs = binding.tabLayout;
        new TabLayoutMediator(tabs, binding.viewPager2, (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText(R.string.anime_watch_history);
                            break;
                        case 1:
                            tab.setText(R.string.anime_favorites_list);
                            break;
                        case 2:
                            tab.setText(R.string.anime_all_list);
                            break;
                        case 3:
                            tab.setText(R.string.anime_status_seen);
                            break;
                        case 4:
                            tab.setText(R.string.anime_status_will);
                            break;
                        case 5:
                            tab.setText(R.string.anime_status_watch);
                            break;
                        case 6:
                            tab.setText(R.string.anime_status_poned);
                            break;
                        case 7:
                            tab.setText(R.string.anime_status_adand);
                            break;
                    }
                })
                .attach();
    }

    static class ViewPagerAdapter extends FragmentStateAdapter {
        private final List<Fragment> arrayList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fragmentManager, Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        public void addFragment(Fragment fragment) {
            arrayList.add(fragment);
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }

        @Override
        public Fragment createFragment(int position) {
            return arrayList.get(position);
        }
    }
}

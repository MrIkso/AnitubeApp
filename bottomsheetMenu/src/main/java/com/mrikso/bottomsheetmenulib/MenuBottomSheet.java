package com.mrikso.bottomsheetmenulib;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mrikso.bottomsheetmenulib.databinding.BottomSheetMenuBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MenuBottomSheet extends BottomSheetDialogFragment {
    @NotNull
    private static final String CLOSE_AFTER_SELECT = "CLOSE_AFTER_SELECT";

    @NotNull
    private static final String MENU_ITEMS = "MENU_ITEMS";

    private static final String HIDDEN_MENU_ITEMS = "HIDDEN_MENU_ITEMS";

    @NotNull
    private static final String MENU_RES = "MENU_RES";

    @NotNull
    public static final String TAG = "menu_bottom_sheet";

    private BottomSheetMenuBinding binding;

    @MenuRes
    private int menuRes;

    @Nullable
    private MenuBottomSheetListener onSelectMenuItemListener;

    @NotNull
    private List<MenuBottomSheetItem> menuItemsList = new ArrayList<>();

    @NotNull
    private List<Integer> hiddenIds = new ArrayList<>();

    @NotNull
    private final MenuBottomSheetAdapter adapter = new MenuBottomSheetAdapter();

    private boolean closeAfterSelect = true;

    public MenuBottomSheet() {}

    public static MenuBottomSheet newInstance(int menuRes, boolean closeAfterSelect) {
        MenuBottomSheet menuBottomSheet = new MenuBottomSheet();
        Bundle bundle = new Bundle();
        bundle.putInt(MENU_RES, menuRes);
        bundle.putBoolean(CLOSE_AFTER_SELECT, closeAfterSelect);
        menuBottomSheet.setArguments(bundle);
        return menuBottomSheet;
    }

    public static MenuBottomSheet newInstance(@MenuRes int menuRes, List<Integer> hiddenIds, boolean closeAfterSelect) {
        MenuBottomSheet menuBottomSheet = new MenuBottomSheet();
        Bundle bundle = new Bundle();
        bundle.putInt(MENU_RES, menuRes);
        bundle.putBoolean(CLOSE_AFTER_SELECT, closeAfterSelect);
        bundle.putIntegerArrayList(HIDDEN_MENU_ITEMS, (ArrayList<Integer>) hiddenIds);
        menuBottomSheet.setArguments(bundle);
        return menuBottomSheet;
    }

    public static MenuBottomSheet newInstance(List<MenuBottomSheetItem> menuItemsList, boolean closeAfterSelect) {
        MenuBottomSheet menuBottomSheet = new MenuBottomSheet();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(MENU_ITEMS, (ArrayList<MenuBottomSheetItem>) menuItemsList);
        bundle.putBoolean(CLOSE_AFTER_SELECT, closeAfterSelect);
        menuBottomSheet.setArguments(bundle);
        return menuBottomSheet;
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            this.menuRes = arguments.getInt(MENU_RES);
            if (arguments.getParcelableArrayList(MENU_ITEMS) != null) {
                this.menuItemsList = Objects.requireNonNull(arguments.getParcelableArrayList(MENU_ITEMS));
            }

            if (arguments.getIntegerArrayList(HIDDEN_MENU_ITEMS) != null) {
                this.hiddenIds = Objects.requireNonNull(arguments.getIntegerArrayList(HIDDEN_MENU_ITEMS));
            }
            this.closeAfterSelect = arguments.getBoolean(CLOSE_AFTER_SELECT);
        }
    }

    @NonNull
    @Override
    public final View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetMenuBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public final void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MenuInflater menuInflater;

        MenuBuilder menuBuilder = new MenuBuilder(requireContext());
        if (menuRes != -1) {
            menuInflater = requireActivity().getMenuInflater();
            menuInflater.inflate(menuRes, menuBuilder);
        }
        if (menuItemsList.isEmpty()) {
            ArrayList<MenuItemImpl> nonActionItems = menuBuilder.getNonActionItems();

            for (MenuItemImpl menuItemImpl : nonActionItems) {
                if (!hiddenIds.isEmpty() && !hiddenIds.contains(menuItemImpl.getItemId())) {
                    menuItemsList.add(new MenuBottomSheetItem(
                            menuItemImpl.getTitle().toString(),
                            null,
                            menuItemImpl.getIcon(),
                            menuItemImpl.getItemId()));
                }
            }
        }

        binding.recyclerView.setAdapter(adapter);
        adapter.clear();
        adapter.setItems(menuItemsList);
        adapter.setOnItemClickListener((v, menuBottomSheetItem, position) -> {
            onSelectMenuItemListener.onSelectMenuItemListener(position, menuBottomSheetItem.getId());
            if (closeAfterSelect) dismiss();
        });
    }

    public final void show(@NotNull Fragment fragment) {
        show(fragment.getChildFragmentManager(), TAG);
    }

    public final void show(@NotNull AppCompatActivity appCompatActivity) {
        show(appCompatActivity.getSupportFragmentManager(), TAG);
    }

    public final void show(@NotNull FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }

    public interface MenuBottomSheetListener {
        void onSelectMenuItemListener(int position, @IdRes int resId);
    }

    public static class Builder {
        private boolean closeAfterSelect = true;

        @Nullable
        private List<MenuBottomSheetItem> menuItems;

        @MenuRes
        private int menuRes;

        private List<Integer> hiddenIds;

        @NotNull
        public final Builder setMenuRes(int menuRes) {
            this.menuRes = menuRes;
            return this;
        }

        @NotNull
        public final Builder closeAfterSelect(boolean closeAfterSelect) {
            this.closeAfterSelect = closeAfterSelect;
            return this;
        }

        @NotNull
        public final Builder setMenuItems(@NotNull List<MenuBottomSheetItem> menuItems) {
            this.menuItems = menuItems;
            return this;
        }

        @NotNull
        public final Builder seHiddenItems(@NotNull List<Integer> hiddenIds) {
            this.hiddenIds = hiddenIds;
            return this;
        }

        @NotNull
        public MenuBottomSheet build() {
            if (this.menuRes != -1 && this.hiddenIds != null) {
                return MenuBottomSheet.newInstance(menuRes, hiddenIds, this.closeAfterSelect);
            }
            if (this.menuRes != -1) {
                return MenuBottomSheet.newInstance(menuRes, this.closeAfterSelect);
            }

            if (this.menuItems != null) {
                return MenuBottomSheet.newInstance(menuItems, this.closeAfterSelect);
            } else {
                throw new IllegalStateException("You should call builder.setMenuRes or builder.setMenuItems");
            }
        }
    }

    public void setOnSelectMenuItemListener(@Nullable MenuBottomSheetListener onSelectMenuItemListener) {
        this.onSelectMenuItemListener = onSelectMenuItemListener;
    }
}

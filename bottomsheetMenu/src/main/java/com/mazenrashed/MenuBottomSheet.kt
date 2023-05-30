package com.mazenrashed

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mazenrashed.bottomsheetmenulib.MenuBottomSheetAdapter
import com.mazenrashed.bottomsheetmenulib.MenuBottomSheetItem
import com.mazenrashed.bottomsheetmenulib.R
import com.mazenrashed.bottomsheetmenulib.base.BaseAdapter
import com.mazenrashed.bottomsheetmenulib.databinding.BottomSheetMenuBinding


class MenuBottomSheet : BottomSheetDialogFragment() {

    var onSelectMenuItemListener: ((position: Int, id: Int?) -> Unit)? = null
    private lateinit var binding: BottomSheetMenuBinding
    private var menuRes: Int? = null
    private var menuItemsList = ArrayList<MenuBottomSheetItem>()
    private val adapter = MenuBottomSheetAdapter()
    private var closeAfterSelect: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getInt(MENU_RES)?.let { menuRes = it }
        arguments?.getParcelableArrayList<MenuBottomSheetItem>(MENU_ITEMS)
            ?.let { menuItemsList = it }
        arguments?.getBoolean(CLOSE_AFTER_SELECT)?.let { closeAfterSelect = it }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menu = MenuBuilder(requireContext())
        menuRes?.let { res -> if (res != 0) activity?.menuInflater?.inflate(res, menu) }

        if (menuItemsList.isEmpty())
            menu.nonActionItems.forEach {
                menuItemsList.add(
                    MenuBottomSheetItem(
                        title = it.title.toString(),
                        iconDrawable = it.icon,
                        id = it.itemId
                    )
                )
            }

        binding.recyclerView.adapter = adapter
        adapter.clear()
        adapter.setItems(menuItemsList)

        adapter.setOnItemClickListener(object :
            BaseAdapter.OnItemClickListener<MenuBottomSheetItem> {
            override fun onItemClicked(view: View, item: MenuBottomSheetItem?, position: Int) {
                onSelectMenuItemListener?.invoke(position, item?.id)
                if (closeAfterSelect) this@MenuBottomSheet.dismiss()
            }
        })
    }

    fun show(fragment: Fragment) {
        show(fragment.childFragmentManager, TAG)
    }

    fun show(activity: AppCompatActivity) {
        show(activity.supportFragmentManager, TAG)
    }

    fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, TAG)
    }

    companion object {
        const val TAG = "menu_bottom_sheet"
        private const val MENU_RES = "MENU_RES"
        private const val MENU_ITEMS = "MENU_ITEMS"
        private const val CLOSE_AFTER_SELECT = "CLOSE_AFTER_SELECT"

        private fun newInstance(
            menuRes: Int,
            closeAfterSelect: Boolean
        ): MenuBottomSheet {
            return MenuBottomSheet().apply {
                arguments = Bundle().apply {
                    putInt(MENU_RES, menuRes)
                    putBoolean(CLOSE_AFTER_SELECT, closeAfterSelect)
                }
            }
        }

        private fun newInstance(
            menuItems: ArrayList<MenuBottomSheetItem>,
            closeAfterSelect: Boolean
        ): MenuBottomSheet {
            return MenuBottomSheet().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(MENU_ITEMS, menuItems)
                    putBoolean(CLOSE_AFTER_SELECT, closeAfterSelect)
                }
            }
        }
    }


    class Builder {
        private var menuRes: Int? = null
        private var menuItems: ArrayList<MenuBottomSheetItem>? = null
        private var closeAfterSelect: Boolean = true

        fun setMenuRes(menuRes: Int) = apply { this.menuRes = menuRes }

        fun closeAfterSelect(close: Boolean) = apply { this.closeAfterSelect = close }

        fun setMenuItems(items: ArrayList<MenuBottomSheetItem>) = apply { menuItems = items }

        fun build(): MenuBottomSheet {
            return menuRes?.let { newInstance(it, closeAfterSelect) }
                ?: menuItems?.let { newInstance(it, closeAfterSelect) }
                ?: throw Exception("You should call builder.setMenuRes or builder.setMenuItems")
        }
    }

}
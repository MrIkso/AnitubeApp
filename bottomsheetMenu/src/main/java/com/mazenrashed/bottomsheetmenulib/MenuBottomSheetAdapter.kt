package com.mazenrashed.bottomsheetmenulib

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mazenrashed.bottomsheetmenulib.base.BaseAdapter
import com.mazenrashed.bottomsheetmenulib.base.BaseViewHolder
import com.mazenrashed.bottomsheetmenulib.databinding.BottomSheetMenuItemBinding

class MenuBottomSheetAdapter :
    BaseAdapter<MenuBottomSheetItem, MenuBottomSheetAdapter.MenuBottomSheetViewHolder>() {

    class MenuBottomSheetViewHolder(viewDataBinding: BottomSheetMenuItemBinding) :
        BaseViewHolder<MenuBottomSheetItem>(
            viewDataBinding
        ) {
        override fun bind(position: Int, item: MenuBottomSheetItem?) {
            return bind<BottomSheetMenuItemBinding> {
                this.title.text = item?.title
                item?.iconRes?.let { this.icon.setImageResource(it) }
                item?.iconDrawable?.let { this.icon.setImageDrawable(it) }
                this.icon.visibility =
                    if (item?.iconDrawable == null && item?.iconRes == null) View.GONE else View.VISIBLE
            }
        }

    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): MenuBottomSheetViewHolder {
        return MenuBottomSheetViewHolder(
            BottomSheetMenuItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
}
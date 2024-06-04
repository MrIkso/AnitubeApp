package com.mrikso.bottomsheetmenulib.base

import androidx.viewbinding.ViewBinding
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder<M>(val viewBinding: ViewBinding) : RecyclerView.ViewHolder(viewBinding.root){
    abstract fun bind(position: Int, item: M?)

    @Suppress("UNCHECKED_CAST")
    inline fun <T : ViewBinding> bind(binding: T.() -> Unit) {
        binding(viewBinding as T)
    }
}
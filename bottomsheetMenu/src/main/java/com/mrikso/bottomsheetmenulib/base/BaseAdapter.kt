package com.mrikso.bottomsheetmenulib.base

import android.os.Handler
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.util.*

/**
 * @param M is data model
 * @param V is a ViewHolder
 */
abstract class BaseAdapter<M, V : BaseViewHolder<M>> : RecyclerView.Adapter<V>() {

    var list = mutableListOf<M>()
    private var rvHandler: Handler = Handler()
    private var onItemClickListener: OnItemClickListener<M>? = null

    fun isEmpty(): Boolean {
        return list.isEmpty()
    }

    fun setItems(items: List<M>) {
        rvHandler.post {
            list.addAll(items)
            notifyItemRangeInserted(/*positionStart*/list.size,/*itemCount*/ items.size)
        }
    }

    fun addItem(item: M) {
        rvHandler.post {
            list.add(item)
            notifyItemChanged(list.size - 1)
        }
    }

    fun updateItem(item: M, position: Int) {
        rvHandler.post {
            if (position >= 0 && position < list.size) {
                list.add(position, item)
                notifyItemChanged(position)
            }
        }
    }

    fun clear() {
        rvHandler.post {
            list.clear()
            notifyDataSetChanged()
        }
    }

    fun swapItems(dragFrom: Int, dragTo: Int) {
        Collections.swap(list, dragFrom, dragTo)
    }

    protected fun getItem(position: Int): M? {
        return if (position >= 0 && position < list.size) {
            list[position]
        } else {
            null
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): V {
        return getViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: V, position: Int) {
        holder.bind(position, list[position])
        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClicked(it, getItem(position), position)
        }
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener<M>?): BaseAdapter<M, V> {
        this.onItemClickListener = onItemClickListener
        return this
    }

    protected abstract fun getViewHolder(parent: ViewGroup, viewType: Int): V
    interface OnItemClickListener<M> {
        fun onItemClicked(view: View, item: M?, position: Int)
    }

}
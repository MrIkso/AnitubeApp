package com.mrikso.anitube.app.utils

import androidx.recyclerview.widget.*

abstract class ListAdapterWithHeader<T, VH : RecyclerView.ViewHolder>(
    private val diffCallback: DiffUtil.ItemCallback<T>,
    private val headerOffset: Int = 1
) : RecyclerView.Adapter<VH>() {

    private val mHelper by lazy {
        AsyncListDiffer<T>(
            OffsetListUpdateCallback(this, headerOffset),
            AsyncDifferConfig.Builder(diffCallback).build()
        )
    }

    protected val headerPosition = 0

    /**
     * Submits a new list to be diffed, and displayed.
     *
     * If a list is already being displayed, a diff will be computed on a background thread, which
     * will dispatch Adapter.notifyItem events on the main thread.
     *
     * @param list The new list to be displayed.
     */
    fun submitList(list: List<T>?) {
        mHelper.submitList(list)
    }

    /**
     * Accounts for header offset
     */
    fun getItem(position: Int): T {
        return mHelper.currentList[position - headerOffset]
    }

    /**
     * Returns the total number of items in the data set held by the adapter (includes header offset)
     *
     * @return The total number of items in this adapter (plus the header offset)
     */
    override fun getItemCount(): Int {
        return mHelper.currentList.size + headerOffset
    }

    /**
     * ListUpdateCallback that dispatches update events to the given adapter with a position offset to
     * allow for the number of header items.
     *
     *  @see DiffUtil.DiffResult#dispatchUpdatesTo(RecyclerView.Adapter)
     */
    private class OffsetListUpdateCallback(
        private val adapter: RecyclerView.Adapter<*>,
        private val offset: Int
    ) : ListUpdateCallback {

        fun offsetPosition(originalPosition: Int): Int {
            return originalPosition + offset
        }

        override fun onInserted(position: Int, count: Int) {
            adapter.notifyItemRangeInserted(offsetPosition(position), count)
        }

        override fun onRemoved(position: Int, count: Int) {
            adapter.notifyItemRangeRemoved(offsetPosition(position), count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            adapter.notifyItemMoved(offsetPosition(fromPosition), offsetPosition(toPosition))
        }

        override fun onChanged(position: Int, count: Int, payload: Any?) {
            adapter.notifyItemRangeChanged(offsetPosition(position), count, payload)
        }
    }
}
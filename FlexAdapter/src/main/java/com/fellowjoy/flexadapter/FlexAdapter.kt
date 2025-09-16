package com.fellowjoy.flexadapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import androidx.viewbinding.ViewBinding

class FlexAdapter<T : Any>(
    private val layoutProvider: (viewType: Int) -> Int,
    private val binder: (item: T, holder: FlexViewHolder, position: Int) -> Unit,
    private val viewTypeProvider: ((position: Int, item: T?) -> Int)? = null,
    private val itemIdProvider: ((item: T) -> Long)? = null
) : RecyclerView.Adapter<FlexAdapter.FlexViewHolder>() {

    private var onItemClick: ((item: T, position: Int) -> Unit)? = null
    private var onItemLongClick: ((item: T, position: Int) -> Boolean)? = null
    private var onChildClick: MutableMap<Int, (item: T, position: Int) -> Unit> = mutableMapOf()

    private var loadMoreCallback: (() -> Unit)? = null
    private var paginationThreshold = 3
    private var isLoading = false
    private var paginationFooterView: View? = null

    private val headerViews = mutableListOf<View>()
    private val footerViews = mutableListOf<View>()
    private var emptyView: View? = null
    private var loadingView: View? = null
    private var errorView: View? = null

    enum class State { CONTENT, EMPTY, LOADING, ERROR }
    private var currentState: State = State.CONTENT

    private val selectedPositions = mutableSetOf<Int>()

    private val differ = AsyncListDiffer(this, object : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
            return itemIdProvider?.invoke(oldItem) == itemIdProvider?.invoke(newItem)
                    || oldItem == newItem
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem == newItem
        }
    })

    init {
        setHasStableIds(itemIdProvider != null)
    }

    class FlexViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var binding: ViewBinding? = null
    }

    override fun getItemId(position: Int): Long {
        val item = differ.currentList.getOrNull(getRealPosition(position))
        return item?.let { itemIdProvider?.invoke(it) } ?: position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position < headerViews.size -> HEADER_TYPE
            currentState == State.EMPTY && emptyView != null -> EMPTY_TYPE
            currentState == State.LOADING && loadingView != null -> LOADING_TYPE
            currentState == State.ERROR && errorView != null -> ERROR_TYPE
            position >= headerViews.size + differ.currentList.size &&
                    position < headerViews.size + differ.currentList.size + footerViews.size -> FOOTER_TYPE
            paginationFooterView != null && position == itemCount - 1 -> PAGINATION_TYPE
            else -> {
                val realPos = getRealPosition(position)
                val item = differ.currentList.getOrNull(realPos)
                viewTypeProvider?.invoke(position, item) ?: super.getItemViewType(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlexViewHolder {
        val view = when (viewType) {
            HEADER_TYPE -> headerViews.getOrNull(0)
            FOOTER_TYPE -> footerViews.getOrNull(0)
            EMPTY_TYPE -> emptyView
            LOADING_TYPE -> loadingView
            ERROR_TYPE -> errorView
            PAGINATION_TYPE -> paginationFooterView
            else -> LayoutInflater.from(parent.context).inflate(layoutProvider(viewType), parent, false)
        } ?: LayoutInflater.from(parent.context).inflate(layoutProvider(viewType), parent, false)

        return FlexViewHolder(view).apply {
            if (viewType !in RESERVED_TYPES) {
                view.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        differ.currentList.getOrNull(getRealPosition(pos))?.let {
                            onItemClick?.invoke(it, pos)
                        }
                    }
                }

                view.setOnLongClickListener {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        differ.currentList.getOrNull(getRealPosition(pos))?.let {
                            return@setOnLongClickListener onItemLongClick?.invoke(it, pos) ?: false
                        }
                    }
                    false
                }

                onChildClick.forEach { (viewId, callback) ->
                    view.findViewById<View?>(viewId)?.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            differ.currentList.getOrNull(getRealPosition(pos))?.let {
                                callback(it, pos)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: FlexViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        if (viewType !in RESERVED_TYPES) {
            val realPos = getRealPosition(position)
            val item = differ.currentList[realPos]
            binder(item, holder, realPos)

            holder.view.isSelected = selectedPositions.contains(realPos)

            if (!isLoading && loadMoreCallback != null &&
                realPos >= differ.currentList.size - paginationThreshold
            ) {
                isLoading = true
                loadMoreCallback?.invoke()
            }
        }
    }

    override fun getItemCount(): Int {
        return when (currentState) {
            State.EMPTY -> if (emptyView != null) 1 else 0
            State.LOADING -> if (loadingView != null) 1 else 0
            State.ERROR -> if (errorView != null) 1 else 0
            State.CONTENT -> differ.currentList.size +
                    headerViews.size + footerViews.size +
                    (if (paginationFooterView != null && isLoading) 1 else 0)
        }
    }

    private fun getRealPosition(adapterPosition: Int): Int {
        return adapterPosition - headerViews.size
    }

    fun submitList(newItems: List<T>) {
        differ.submitList(newItems) { isLoading = false }
    }

    fun currentItems(): List<T> = differ.currentList.toList()

    fun removeAt(position: Int) {
        val newList = differ.currentList.toMutableList()
        if (position in newList.indices) {
            newList.removeAt(position)
            submitList(newList)
        }
    }

    fun insertAt(position: Int, item: T) {
        val newList = differ.currentList.toMutableList()
        if (position in 0..newList.size) {
            newList.add(position, item)
            submitList(newList)
        }
    }

    fun updateAt(position: Int, item: T) {
        val newList = differ.currentList.toMutableList()
        if (position in newList.indices) {
            newList[position] = item
            submitList(newList)
        }
    }


    fun clear() {
        submitList(emptyList())
    }

    fun setOnItemClickListener(listener: (item: T, position: Int) -> Unit) {
        onItemClick = listener
    }

    fun setOnItemLongClickListener(listener: (item: T, position: Int) -> Boolean) {
        onItemLongClick = listener
    }

    fun setOnChildClickListener(viewId: Int, listener: (item: T, position: Int) -> Unit) {
        onChildClick[viewId] = listener
    }

    fun setPagination(loadMore: () -> Unit, threshold: Int = 3) {
        loadMoreCallback = loadMore
        paginationThreshold = threshold
    }

    fun setPaginationFooter(view: View) { paginationFooterView = view }

    fun addHeader(view: View) { headerViews.add(view); notifyItemInserted(headerViews.size - 1) }
    fun addFooter(view: View) { footerViews.add(view); notifyItemInserted(itemCount - 1) }

    fun setEmptyView(view: View) { emptyView = view }
    fun setLoadingView(view: View) { loadingView = view }
    fun setErrorView(view: View) { errorView = view }

    fun setState(state: State) {
        currentState = state
        notifyDataSetChanged()
    }

    fun toggleSelection(position: Int) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position)
        } else {
            selectedPositions.add(position)
        }
        notifyItemChanged(position + headerViews.size)
    }

    fun clearSelection() {
        val old = selectedPositions.toList()
        selectedPositions.clear()
        old.forEach { notifyItemChanged(it + headerViews.size) }
    }

    fun getSelectedItems(): List<T> {
        return selectedPositions.mapNotNull { differ.currentList.getOrNull(it) }
    }

    fun attachSwipeActions(
        recyclerView: RecyclerView,
        onSwiped: (position: Int, direction: Int, item: T) -> Unit
    ) {
        val helper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false
            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                val pos = vh.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val realPos = getRealPosition(pos)
                    differ.currentList.getOrNull(realPos)?.let {
                        onSwiped(realPos, direction, it)
                    }
                }
            }
        })
        helper.attachToRecyclerView(recyclerView)
    }

    companion object {
        private const val HEADER_TYPE = -100
        private const val FOOTER_TYPE = -101
        private const val EMPTY_TYPE = -102
        private const val LOADING_TYPE = -103
        private const val ERROR_TYPE = -104
        private const val PAGINATION_TYPE = -105
        private val RESERVED_TYPES = setOf(HEADER_TYPE, FOOTER_TYPE, EMPTY_TYPE, LOADING_TYPE, ERROR_TYPE, PAGINATION_TYPE)

        fun useLinearLayout(recyclerView: RecyclerView, vertical: Boolean = true) {
            recyclerView.layoutManager = LinearLayoutManager(
                recyclerView.context,
                if (vertical) RecyclerView.VERTICAL else RecyclerView.HORIZONTAL,
                false
            )
        }

        fun useGridLayout(recyclerView: RecyclerView, spanCount: Int) {
            recyclerView.layoutManager = GridLayoutManager(recyclerView.context, spanCount)
        }

        fun useStaggeredGridLayout(recyclerView: RecyclerView, spanCount: Int, vertical: Boolean = true) {
            recyclerView.layoutManager = StaggeredGridLayoutManager(spanCount, if (vertical) RecyclerView.VERTICAL else RecyclerView.HORIZONTAL)
        }

        fun useAutoFitSquareGrid(recyclerView: RecyclerView, spanCount: Int) {
            val gridLayoutManager = GridLayoutManager(recyclerView.context, spanCount)
            recyclerView.layoutManager = gridLayoutManager

            recyclerView.viewTreeObserver.addOnGlobalLayoutListener {
                val totalWidth = recyclerView.width
                if (totalWidth > 0) {
                    val itemSize = totalWidth / spanCount
                    for (i in 0 until recyclerView.childCount) {
                        val child = recyclerView.getChildAt(i)
                        child?.layoutParams?.apply {
                            width = itemSize
                            height = itemSize
                        }
                    }
                    recyclerView.requestLayout()
                }
            }
        }

        fun enableFullSpanForReserved(gridLayoutManager: GridLayoutManager) {
            gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position < 0) 1
                    else if (gridLayoutManager.spanCount > 1 && position in 0 until gridLayoutManager.itemCount) {
                        if (position == 0 || position == gridLayoutManager.itemCount - 1) gridLayoutManager.spanCount else 1
                    } else 1
                }
            }
        }

        fun enforceAspectRatio(view: View, ratioWidth: Int, ratioHeight: Int) {
            view.post {
                val width = view.width
                val params = view.layoutParams
                params.height = (width.toFloat() * ratioHeight / ratioWidth).toInt()
                view.layoutParams = params
            }
        }

        fun enforce16by9(view: View) = enforceAspectRatio(view, 16, 9)
        fun enforce4by3(view: View) = enforceAspectRatio(view, 4, 3)
    }
}

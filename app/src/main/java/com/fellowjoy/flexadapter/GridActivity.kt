package com.fellowjoy.flexadapter

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class GridActivity : AppCompatActivity() {
    private lateinit var adapter: FlexAdapter<Item>
    private var currentPage = 1
    private val itemsPerPage = 10

    data class Item(
        val id: Long,
        val title: String,
        val description: String,
        val imageUrl: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grid)

        setupRecyclerView()
        loadItems()
    }

    private fun setupRecyclerView() {
        adapter = FlexAdapter(
            layoutProvider = { R.layout.item_grid },
            binder = { item, holder, position ->
                holder.itemView.findViewById<TextView>(R.id.title).text = item.title

                item.imageUrl?.let { url ->
                    Glide.with(this)
                        .load(url)
                        .placeholder(R.drawable.placeholder)
                        .into(holder.itemView.findViewById(R.id.image))
                }
            },
            itemIdProvider = { it.id }
        )

        adapter.setPagination(
            loadMore = { loadMoreItems() },
            threshold = 3
        )

        val footerView = LayoutInflater.from(this).inflate(R.layout.pagination_footer, null, false)
        adapter.setPaginationFooter(footerView)

        FlexAdapter.useGridLayout(findViewById(R.id.recyclerView), 2)
        findViewById<RecyclerView>(R.id.recyclerView).adapter = adapter
    }

    private fun loadItems() {
        val items = mutableListOf<Item>()
        for (i in 1..itemsPerPage) {
            items.add(Item(
                id = i.toLong(),
                title = "Grid Item $i",
                description = "Description for grid item $i",
                imageUrl = "https://picsum.photos/seed/item$i/400/300.jpg"
            ))
        }
        adapter.submitList(items)
    }

    private fun loadMoreItems() {
        // Simulate network delay
        Handler(Looper.getMainLooper()).postDelayed({
            val newItems = mutableListOf<Item>()
            val startId = currentPage * itemsPerPage + 1

            for (i in 0 until itemsPerPage) {
                newItems.add(Item(
                    id = (startId + i).toLong(),
                    title = "Grid Item ${startId + i}",
                    description = "Description for grid item ${startId + i}",
                    imageUrl = "https://picsum.photos/seed/item${startId + i}/400/300.jpg"
                ))
            }

            val currentList = adapter.currentItems().toMutableList()
            currentList.addAll(newItems)
            adapter.submitList(currentList)
            currentPage++
        }, 1500)
    }
}
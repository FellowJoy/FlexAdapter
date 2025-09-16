package com.fellowjoy.flexadapter

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class LinearActivity : AppCompatActivity() {
    private lateinit var adapter: FlexAdapter<Item>
    private var nextId = 1L

    data class Item(
        val id: Long,
        val title: String,
        val description: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_linear)

        setupRecyclerView()
        setupButtons()
        loadItems()
    }

    private fun setupRecyclerView() {
        adapter = FlexAdapter(
            layoutProvider = { R.layout.item_linear },
            binder = { item, holder, position ->
                holder.itemView.findViewById<TextView>(R.id.title).text = item.title
                holder.itemView.findViewById<TextView>(R.id.description).text = item.description
                holder.itemView.findViewById<TextView>(R.id.position).text = "Position: $position"
            },
            itemIdProvider = { it.id }
        )

        adapter.setOnItemClickListener { item, position ->
            Toast.makeText(this, "Clicked: ${item.title}", Toast.LENGTH_SHORT).show()
        }

        adapter.setOnChildClickListener(R.id.actionButton) { item, position ->
            Toast.makeText(this, "Action for: ${item.title}", Toast.LENGTH_SHORT).show()
        }

        FlexAdapter.useLinearLayout(findViewById(R.id.recyclerView))
        findViewById<RecyclerView>(R.id.recyclerView).adapter = adapter
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnLinear).setOnClickListener {
            FlexAdapter.useLinearLayout(findViewById(R.id.recyclerView))
        }

        findViewById<Button>(R.id.btnGrid).setOnClickListener {
            FlexAdapter.useGridLayout(findViewById(R.id.recyclerView), 2)
        }

        findViewById<Button>(R.id.btnStaggered).setOnClickListener {
            FlexAdapter.useStaggeredGridLayout(findViewById(R.id.recyclerView), 2)
        }

        findViewById<Button>(R.id.btnAutoFit).setOnClickListener {
            FlexAdapter.useAutoFitSquareGrid(findViewById(R.id.recyclerView), 2)
        }

        findViewById<Button>(R.id.btnAddItem).setOnClickListener {
            addItem()
        }

        findViewById<Button>(R.id.btnRemoveItem).setOnClickListener {
            removeLastItem()
        }

        findViewById<Button>(R.id.btnClear).setOnClickListener {
            adapter.clear()
        }
    }

    private fun loadItems() {
        val items = mutableListOf<Item>()
        for (i in 1..15) {
            items.add(Item(
                id = i.toLong(),
                title = "Linear Item $i",
                description = "Description for linear item $i"
            ))
        }
        nextId = 16L
        adapter.submitList(items)
    }

    private fun addItem() {
        val newItem = Item(
            id = nextId++,
            title = "New Item $nextId",
            description = "Added dynamically"
        )
        adapter.insertAt(adapter.currentItems().size, newItem)
    }

    private fun removeLastItem() {
        if (adapter.currentItems().isNotEmpty()) {
            adapter.removeAt(adapter.currentItems().size - 1)
        }
    }
}
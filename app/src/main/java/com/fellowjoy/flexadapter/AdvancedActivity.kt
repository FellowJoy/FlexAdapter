package com.fellowjoy.flexadapter

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class AdvancedActivity : AppCompatActivity() {
    private lateinit var adapter: FlexAdapter<Item>

    data class Item(
        val id: Long,
        val title: String,
        val description: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advanced)

        setupRecyclerView()
        setupButtons()
        loadItems()
    }

    private fun setupRecyclerView() {
        adapter = FlexAdapter(
            layoutProvider = { R.layout.item_advanced },
            binder = { item, holder, position ->
                holder.itemView.findViewById<TextView>(R.id.title).text = item.title
                holder.itemView.findViewById<TextView>(R.id.description).text = item.description

                // Set up checkbox for selection - only set the state, not the listener
                val checkbox = holder.itemView.findViewById<CheckBox>(R.id.checkbox)
                checkbox.isChecked = adapter.getSelectedItems().contains(item)
            },
            itemIdProvider = { it.id }
        )

        // Set up child click listener for the checkbox
        adapter.setOnChildClickListener(R.id.checkbox) { item, position ->
            adapter.toggleSelection(position)
            // The adapter will update the checkbox state via notifyItemChanged
        }

        // Set up state views
        adapter.setEmptyView(LayoutInflater.from(this).inflate(R.layout.empty_view, null, false))
        adapter.setLoadingView(LayoutInflater.from(this).inflate(R.layout.loading_view, null, false))
        adapter.setErrorView(LayoutInflater.from(this).inflate(R.layout.error_view, null, false))

        // Set up swipe actions
        adapter.attachSwipeActions(findViewById(R.id.recyclerView)) { position, direction, item ->
            Toast.makeText(this, "Swiped: ${item.title}", Toast.LENGTH_SHORT).show()
            adapter.removeAt(position)
        }

        FlexAdapter.useLinearLayout(findViewById(R.id.recyclerView))
        findViewById<RecyclerView>(R.id.recyclerView).adapter = adapter
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnShowContent).setOnClickListener {
            adapter.setState(FlexAdapter.State.CONTENT)
        }

        findViewById<Button>(R.id.btnShowEmpty).setOnClickListener {
            adapter.setState(FlexAdapter.State.EMPTY)
        }

        findViewById<Button>(R.id.btnShowLoading).setOnClickListener {
            adapter.setState(FlexAdapter.State.LOADING)
        }

        findViewById<Button>(R.id.btnShowError).setOnClickListener {
            adapter.setState(FlexAdapter.State.ERROR)
        }
    }

    private fun loadItems() {
        adapter.setState(FlexAdapter.State.LOADING)

        Handler(Looper.getMainLooper()).postDelayed({
            val items = mutableListOf<Item>()
            for (i in 1..15) {
                items.add(Item(
                    id = i.toLong(),
                    title = "Advanced Item $i",
                    description = "Description for advanced item $i"
                ))
            }
            adapter.submitList(items)
            adapter.setState(FlexAdapter.State.CONTENT)
        }, 1500)
    }
}
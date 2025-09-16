package com.fellowjoy.flexadapter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: FlexAdapter<Feature>

    data class Feature(
        val id: Long,
        val title: String,
        val description: String,
        val activityClass: Class<out Activity>
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = FlexAdapter(
            layoutProvider = { R.layout.item_feature },
            binder = { feature, holder, _ ->
                holder.itemView.findViewById<TextView>(R.id.title).text = feature.title
                holder.itemView.findViewById<TextView>(R.id.description).text = feature.description
            },
            itemIdProvider = { it.id }
        )

        adapter.setOnItemClickListener { feature, _ ->
            startActivity(Intent(this, feature.activityClass))
        }

        FlexAdapter.useLinearLayout(findViewById(R.id.featuresRecyclerView))
        findViewById<RecyclerView>(R.id.featuresRecyclerView).adapter = adapter

        loadFeatures()
    }

    private fun loadFeatures() {
        val features = listOf(
            Feature(
                id = 1,
                title = "Linear Layout",
                description = "Basic linear layout with layout switching options",
                activityClass = LinearActivity::class.java
            ),
            Feature(
                id = 2,
                title = "Grid Layout",
                description = "Grid layout with pagination and images",
                activityClass = GridActivity::class.java
            ),
            Feature(
                id = 3,
                title = "Advanced Features",
                description = "State management, swipe actions, drag & drop",
                activityClass = AdvancedActivity::class.java
            )
        )

        adapter.submitList(features)
    }
}
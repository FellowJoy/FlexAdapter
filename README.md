# FlexAdapter 🚀

A powerful, flexible, and feature-rich RecyclerView adapter for Android that simplifies complex list implementations with minimal boilerplate code.

![Android](https://img.shields.io/badge/Platform-Android-green.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

## ✨ Features

- **🔄 DiffUtil Integration** - Automatic efficient list updates
- **📄 Pagination Support** - Built-in load-more functionality
- **🎭 Multiple View Types** - Easy handling of different item layouts
- **📌 Headers & Footers** - Dynamic header and footer views
- **🎯 Selection Management** - Multi-select with visual feedback
- **👆 Touch Interactions** - Click, long-click, and child view clicks
- **📱 Various States** - Empty, loading, error states
- **↔️ Swipe Actions** - Built-in swipe-to-delete/action support
- **🎨 Layout Managers** - Convenient layout manager setup
- **📐 Aspect Ratio** - Automatic aspect ratio enforcement

## 📦 Installation

### Gradle (Module level)
```gradle
dependencies {
    implementation 'com.github.FellowJoy:FlexAdapter:v2.0.0'
    
    // Required dependencies
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    implementation 'androidx.viewbinding:viewbinding:8.1.4'
}
```

### Maven
```xml
<dependency>
    <groupId>com.github.FellowJoy</groupId>
    <artifactId>FlexAdapter</artifactId>
    <version>v2.0.0</version>
</dependency>
```

### Manual Installation
1. Download the `FlexAdapter.kt` file
2. Copy it to your project's package
3. Update the package declaration at the top of the file

## 🚀 Quick Start

```kotlin
// Basic setup
val adapter = FlexAdapter<User>(
    layoutProvider = { R.layout.item_user },
    binder = { user, holder, position ->
        holder.view.findViewById<TextView>(R.id.tvName).text = user.name
    }
)

recyclerView.adapter = adapter
FlexAdapter.useLinearLayout(recyclerView)
adapter.submitList(userList)
```

## 📋 API Reference

### Constructor
```kotlin
FlexAdapter<T>(
    layoutProvider: (viewType: Int) -> Int,
    binder: (item: T, holder: FlexViewHolder, position: Int) -> Unit,
    viewTypeProvider: ((position: Int, item: T?) -> Int)? = null,
    itemIdProvider: ((item: T) -> Long)? = null
)
```

### Data Management
```kotlin
adapter.submitList(newItems)
adapter.currentItems()
adapter.removeAt(position)
adapter.insertAt(position, item)
adapter.updateAt(position, item)
adapter.clear()
```

### Click Listeners
```kotlin
adapter.setOnItemClickListener { item, position -> }
adapter.setOnItemLongClickListener { item, position -> true }
adapter.setOnChildClickListener(R.id.button) { item, position -> }
```

### Multiple View Types
```kotlin
FlexAdapter<Item>(
    layoutProvider = { viewType -> 
        when(viewType) {
            TYPE_HEADER -> R.layout.item_header
            TYPE_USER -> R.layout.item_user
            else -> R.layout.item_default
        }
    },
    binder = { item, holder, position -> /* bind based on type */ },
    viewTypeProvider = { _, item -> item?.type ?: TYPE_DEFAULT }
)
```

### Pagination
```kotlin
adapter.setPagination(
    loadMore = { viewModel.loadNextPage() },
    threshold = 3
)

// Optional loading footer
adapter.setPaginationFooter(loadingFooterView)
```

### States
```kotlin
adapter.setEmptyView(emptyView)
adapter.setLoadingView(loadingView)
adapter.setErrorView(errorView)

adapter.setState(FlexAdapter.State.LOADING)
adapter.setState(FlexAdapter.State.EMPTY)
adapter.setState(FlexAdapter.State.ERROR)
adapter.setState(FlexAdapter.State.CONTENT)
```

### Headers & Footers
```kotlin
adapter.addHeader(headerView)
adapter.addFooter(footerView)
```

### Selection
```kotlin
adapter.toggleSelection(position)
adapter.clearSelection()
val selected = adapter.getSelectedItems()
```

### Swipe Actions
```kotlin
adapter.attachSwipeActions(recyclerView) { position, direction, item ->
    when(direction) {
        ItemTouchHelper.LEFT -> deleteItem(item)
        ItemTouchHelper.RIGHT -> favoriteItem(item)
    }
}
```

### Layout Managers
```kotlin
// Linear
FlexAdapter.useLinearLayout(recyclerView)
FlexAdapter.useLinearLayout(recyclerView, vertical = false)

// Grid
FlexAdapter.useGridLayout(recyclerView, spanCount = 2)
FlexAdapter.useStaggeredGridLayout(recyclerView, spanCount = 2)
FlexAdapter.useAutoFitSquareGrid(recyclerView, spanCount = 3)

// Grid full span for headers/footers
FlexAdapter.enableFullSpanForReserved(gridLayoutManager)
```

### Aspect Ratio
```kotlin
// In binder
FlexAdapter.enforce16by9(imageView)
FlexAdapter.enforce4by3(imageView)
FlexAdapter.enforceAspectRatio(imageView, 3, 2)
```

### With ViewBinding
```kotlin
val adapter = FlexAdapter<User>(
    layoutProvider = { R.layout.item_user },
    binder = { user, holder, position ->
        val binding = ItemUserBinding.bind(holder.view)
        binding.tvName.text = user.name
        binding.tvEmail.text = user.email
    },
    itemIdProvider = { it.id } // For stable IDs
)
```

## 🔧 Advanced Usage

### Complete Example
```kotlin
class UserAdapter : FlexAdapter<User>(
    layoutProvider = { R.layout.item_user },
    binder = { user, holder, position ->
        ItemUserBinding.bind(holder.view).apply {
            tvName.text = user.name
            tvEmail.text = user.email
            root.isSelected = /* selection logic */
        }
    },
    itemIdProvider = { it.id }
) {
    init {
        setOnItemClickListener { user, pos -> /* handle click */ }
        setOnItemLongClickListener { user, pos -> toggleSelection(pos); true }
        setOnChildClickListener(R.id.btnFavorite) { user, pos -> /* handle favorite */ }
    }
}
```

## 📱 Proguard/R8
```proguard
# Keep FlexAdapter
-keep class com.fellowjoy.flexadapter.** { *; }
```

## 🛠 Requirements
- Android API 21+
- AndroidX
- Kotlin 1.8+

## 🤝 Contributing
Contributions are welcome! Please submit a Pull Request.

## 📄 License
```
MIT License - see LICENSE file for details
```

## 🌟 Support
If this library helped you, please ⭐ star this repository!

---
Made with ❤️ by [FellowJoy](https://github.com/fellowjoy)

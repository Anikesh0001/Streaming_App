package com.livestreamplayer.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.livestreamplayer.app.R
import com.livestreamplayer.app.model.Category

class CategoryAdapter(
    private val onCategoryClick: (Category) -> Unit
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryName: TextView = itemView.findViewById(R.id.categoryName)
        private val categoryCount: TextView = itemView.findViewById(R.id.categoryCount)

        fun bind(category: Category) {
            categoryName.text = category.name
            categoryCount.text = "${category.channelCount} channels"
            itemView.setOnClickListener { onCategoryClick(category) }
        }
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean =
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean =
            oldItem == newItem
    }
}

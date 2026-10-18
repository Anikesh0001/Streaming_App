package com.livestreamplayer.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.livestreamplayer.app.R
import com.livestreamplayer.app.data.PlaylistRepository
import com.livestreamplayer.app.model.Category

class CategoriesFragment : Fragment() {

    private lateinit var categoryList: RecyclerView
    private lateinit var emptyText: TextView

    private val repository = PlaylistRepository()

    private val categoryAdapter by lazy {
        CategoryAdapter { category ->
            (activity as? MainActivity)?.navigateToCategory(category.name)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_categories, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryList = view.findViewById(R.id.categoryList)
        emptyText = view.findViewById(R.id.emptyText)

        categoryList.layoutManager = LinearLayoutManager(requireContext())
        categoryList.adapter = categoryAdapter
    }

    override fun onResume() {
        super.onResume()
        updateCategories()
    }

    private fun updateCategories() {
        val homeFragment = (activity as? MainActivity)?.supportFragmentManager
            ?.fragments?.filterIsInstance<HomeFragment>()?.firstOrNull()

        // Access channels from a shared ViewModel or get from home fragment
        // For simplicity, we use the home fragment's data
        val categories = emptyList<Category>() // Will be populated via shared data

        if (categories.isEmpty()) {
            emptyText.visibility = View.VISIBLE
            categoryList.visibility = View.GONE
        } else {
            emptyText.visibility = View.GONE
            categoryList.visibility = View.VISIBLE
            categoryAdapter.submitList(categories)
        }
    }
}

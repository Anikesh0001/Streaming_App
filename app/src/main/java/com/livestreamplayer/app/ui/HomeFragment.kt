package com.livestreamplayer.app.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.livestreamplayer.app.App
import com.livestreamplayer.app.R
import com.livestreamplayer.app.data.PlaylistRepository
import com.livestreamplayer.app.model.Channel
import com.livestreamplayer.app.model.FavoriteChannel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var searchBar: EditText
    private lateinit var categoryChips: ChipGroup
    private lateinit var channelList: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var loadingProgress: ProgressBar
    private lateinit var emptyText: TextView
    private lateinit var fabAddPlaylist: FloatingActionButton
    private lateinit var channelCount: TextView

    private val repository = PlaylistRepository()
    private var allChannels = mutableListOf<Channel>()
    private var filteredChannels = mutableListOf<Channel>()
    private var selectedCategory = "All"
    private var searchQuery = ""

    private val channelAdapter by lazy {
        ChannelAdapter(
            onChannelClick = { channel ->
                startActivity(PlayerActivity.newIntent(
                    requireContext(), channel.name, channel.url,
                    channel.logoUrl, channel.group
                ))
            },
            onFavoriteClick = { channel -> toggleFavorite(channel) }
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchBar = view.findViewById(R.id.searchBar)
        categoryChips = view.findViewById(R.id.categoryChips)
        channelList = view.findViewById(R.id.channelList)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        loadingProgress = view.findViewById(R.id.loadingProgress)
        emptyText = view.findViewById(R.id.emptyText)
        fabAddPlaylist = view.findViewById(R.id.fabAddPlaylist)
        channelCount = view.findViewById(R.id.channelCount)

        channelList.layoutManager = LinearLayoutManager(requireContext())
        channelList.adapter = channelAdapter

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString() ?: ""
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        swipeRefresh.setOnRefreshListener { loadPlaylists() }
        fabAddPlaylist.setOnClickListener { showAddPlaylistDialog() }
        loadPlaylists()
    }

    override fun onResume() {
        super.onResume()
        updateFavoriteStatus()
    }

    fun filterByCategory(category: String) {
        selectedCategory = category
        applyFilters()
        updateCategoryChips()
    }

    private fun loadPlaylists() {
        lifecycleScope.launch {
            loadingProgress.visibility = View.VISIBLE
            emptyText.visibility = View.GONE
            allChannels.clear()

            // 1. Always load bundled default channels first
            try {
                val bundled = repository.loadBundledPlaylist(requireContext())
                allChannels.addAll(bundled)
            } catch (e: Exception) { e.printStackTrace() }

            // 2. Load any user-added playlists
            val urls = App.instance.prefsManager.playlistUrls.first()
            for (url in urls) {
                try {
                    val channels = repository.loadPlaylist(url)
                    allChannels.addAll(channels)
                } catch (e: Exception) { e.printStackTrace() }
            }

            // Remove duplicates by URL
            val seen = mutableSetOf<String>()
            allChannels = allChannels.filter { seen.add(it.url) }.toMutableList()

            updateFavoriteStatus()
            updateCategoryChips()
            applyFilters()

            channelCount.text = getString(R.string.channels_count, allChannels.size)

            loadingProgress.visibility = View.GONE
            swipeRefresh.isRefreshing = false

            if (allChannels.isEmpty()) {
                emptyText.visibility = View.VISIBLE
                emptyText.text = if (urls.isEmpty()) "Tap + to add more M3U playlists" else getString(R.string.error_loading)
            }
        }
    }

    private fun updateCategoryChips() {
        categoryChips.removeAllViews()
        val allChip = Chip(requireContext()).apply {
            text = "All (${allChannels.size})"
            isCheckable = true
            isChecked = selectedCategory == "All"
            setOnClickListener { selectedCategory = "All"; applyFilters() }
        }
        categoryChips.addView(allChip)

        val categories = repository.getCategories(allChannels)
        for (cat in categories) {
            val chip = Chip(requireContext()).apply {
                text = "${cat.name} (${cat.channelCount})"
                isCheckable = true
                isChecked = selectedCategory == cat.name
                setOnClickListener { selectedCategory = cat.name; applyFilters() }
            }
            categoryChips.addView(chip)
        }
    }

    private fun applyFilters() {
        filteredChannels.clear()
        var result = allChannels.toList()
        result = repository.filterByCategory(result, selectedCategory)
        result = repository.searchChannels(result, searchQuery)
        filteredChannels.addAll(result)
        channelAdapter.submitList(filteredChannels.toList())

        if (filteredChannels.isEmpty() && allChannels.isNotEmpty()) {
            emptyText.visibility = View.VISIBLE
            emptyText.text = "No channels match your search"
        } else if (allChannels.isNotEmpty()) {
            emptyText.visibility = View.GONE
        }
    }

    private fun updateFavoriteStatus() {
        lifecycleScope.launch {
            val favoriteIds = App.instance.database.favoriteDao().getAllFavoriteIds()
            allChannels = allChannels.map { it.copy(isFavorite = favoriteIds.contains(it.id)) }.toMutableList()
            applyFilters()
        }
    }

    private fun toggleFavorite(channel: Channel) {
        lifecycleScope.launch {
            val dao = App.instance.database.favoriteDao()
            if (channel.isFavorite) {
                dao.removeFavoriteById(channel.id)
                Toast.makeText(requireContext(), R.string.removed_from_favorites, Toast.LENGTH_SHORT).show()
            } else {
                dao.addFavorite(FavoriteChannel(
                    channelId = channel.id, name = channel.name,
                    url = channel.url, logoUrl = channel.logoUrl, group = channel.group
                ))
                Toast.makeText(requireContext(), R.string.added_to_favorites, Toast.LENGTH_SHORT).show()
            }
            updateFavoriteStatus()
        }
    }

    private fun showAddPlaylistDialog() {
        val items = arrayOf("Add from URL", "Load India (780+ channels)", "Load All Countries (9000+ channels)")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.select_playlist_source)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> showUrlInputDialog()
                    1 -> addAndReload("https://iptv-org.github.io/iptv/countries/in.m3u")
                    2 -> addAndReload("https://iptv-org.github.io/iptv/index.m3u")
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showUrlInputDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_playlist, null)
        val input = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playlistUrlInput)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_playlist)
            .setView(dialogView)
            .setPositiveButton(R.string.load) { _, _ ->
                val url = input.text?.toString()?.trim() ?: ""
                if (url.isNotBlank()) addAndReload(url)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun addAndReload(url: String) {
        lifecycleScope.launch {
            App.instance.prefsManager.addPlaylistUrl(url)
            loadPlaylists()
        }
    }
}

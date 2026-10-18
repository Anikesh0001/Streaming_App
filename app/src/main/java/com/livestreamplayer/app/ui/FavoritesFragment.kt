package com.livestreamplayer.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.livestreamplayer.app.App
import com.livestreamplayer.app.R
import com.livestreamplayer.app.model.Channel
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {

    private lateinit var favoritesList: RecyclerView
    private lateinit var emptyText: TextView

    private val channelAdapter by lazy {
        ChannelAdapter(
            onChannelClick = { channel ->
                startActivity(
                    PlayerActivity.newIntent(
                        requireContext(),
                        channel.name,
                        channel.url,
                        channel.logoUrl,
                        channel.group
                    )
                )
            },
            onFavoriteClick = { channel -> removeFavorite(channel) }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_favorites, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoritesList = view.findViewById(R.id.favoritesList)
        emptyText = view.findViewById(R.id.emptyText)

        favoritesList.layoutManager = LinearLayoutManager(requireContext())
        favoritesList.adapter = channelAdapter

        App.instance.database.favoriteDao().getAllFavorites().observe(viewLifecycleOwner) { favorites ->
            val channels = favorites.map { fav ->
                Channel(
                    name = fav.name,
                    url = fav.url,
                    logoUrl = fav.logoUrl,
                    group = fav.group,
                    isFavorite = true
                )
            }
            channelAdapter.submitList(channels)
            emptyText.visibility = if (channels.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun removeFavorite(channel: Channel) {
        lifecycleScope.launch {
            App.instance.database.favoriteDao().removeFavoriteById(channel.id)
            Toast.makeText(requireContext(), R.string.removed_from_favorites, Toast.LENGTH_SHORT).show()
        }
    }
}

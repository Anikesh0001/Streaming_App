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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.livestreamplayer.app.App
import com.livestreamplayer.app.R
import com.livestreamplayer.app.model.Channel
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private lateinit var historyList: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var clearBtn: View

    private val channelAdapter by lazy {
        ChannelAdapter(
            onChannelClick = { channel ->
                startActivity(
                    PlayerActivity.newIntent(
                        requireContext(), channel.name, channel.url,
                        channel.logoUrl, channel.group
                    )
                )
            },
            onFavoriteClick = { }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_history, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        historyList = view.findViewById(R.id.historyList)
        emptyText = view.findViewById(R.id.emptyText)
        clearBtn = view.findViewById(R.id.btnClearHistory)

        historyList.layoutManager = LinearLayoutManager(requireContext())
        historyList.adapter = channelAdapter

        clearBtn.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.clear_history)
                .setMessage("Remove all watch history?")
                .setPositiveButton("Clear") { _, _ ->
                    lifecycleScope.launch {
                        App.instance.database.watchHistoryDao().clearAll()
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        App.instance.database.watchHistoryDao().getRecentHistory().observe(viewLifecycleOwner) { history ->
            val channels = history.map { entry ->
                Channel(
                    name = entry.name,
                    url = entry.url,
                    logoUrl = entry.logoUrl,
                    group = entry.group
                )
            }
            channelAdapter.submitList(channels)
            emptyText.visibility = if (channels.isEmpty()) View.VISIBLE else View.GONE
            clearBtn.visibility = if (channels.isEmpty()) View.GONE else View.VISIBLE
        }
    }
}

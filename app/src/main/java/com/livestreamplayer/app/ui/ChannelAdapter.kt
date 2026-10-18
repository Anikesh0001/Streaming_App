package com.livestreamplayer.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.livestreamplayer.app.R
import com.livestreamplayer.app.model.Channel

class ChannelAdapter(
    private val onChannelClick: (Channel) -> Unit,
    private val onFavoriteClick: (Channel) -> Unit
) : ListAdapter<Channel, ChannelAdapter.ChannelViewHolder>(ChannelDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_channel, parent, false)
        return ChannelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val channelLogo: ImageView = itemView.findViewById(R.id.channelLogo)
        private val channelName: TextView = itemView.findViewById(R.id.channelName)
        private val channelGroup: TextView = itemView.findViewById(R.id.channelGroup)
        private val favoriteBtn: ImageButton = itemView.findViewById(R.id.favoriteBtn)

        fun bind(channel: Channel) {
            channelName.text = channel.name
            channelGroup.text = channel.group

            if (channel.logoUrl.isNotBlank()) {
                channelLogo.load(channel.logoUrl) {
                    placeholder(R.drawable.channel_logo_placeholder)
                    error(R.drawable.channel_logo_placeholder)
                    transformations(CircleCropTransformation())
                    crossfade(true)
                }
            } else {
                channelLogo.setImageResource(R.drawable.ic_tv)
                channelLogo.setBackgroundResource(R.drawable.channel_logo_placeholder)
            }

            favoriteBtn.setImageResource(
                if (channel.isFavorite) R.drawable.ic_favorite
                else R.drawable.ic_favorite_border
            )
            favoriteBtn.setColorFilter(
                itemView.context.getColor(
                    if (channel.isFavorite) R.color.favorite_active
                    else R.color.favorite_inactive
                )
            )

            itemView.setOnClickListener { onChannelClick(channel) }
            favoriteBtn.setOnClickListener { onFavoriteClick(channel) }
        }
    }

    class ChannelDiffCallback : DiffUtil.ItemCallback<Channel>() {
        override fun areItemsTheSame(oldItem: Channel, newItem: Channel): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Channel, newItem: Channel): Boolean =
            oldItem == newItem
    }
}

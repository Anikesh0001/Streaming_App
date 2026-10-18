package com.livestreamplayer.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.livestreamplayer.app.App
import com.livestreamplayer.app.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private lateinit var switchDarkMode: MaterialSwitch

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        switchDarkMode = view.findViewById(R.id.switchDarkMode)

        lifecycleScope.launch {
            switchDarkMode.isChecked = App.instance.prefsManager.darkMode.first()
        }

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                App.instance.prefsManager.setDarkMode(isChecked)
                AppCompatDelegate.setDefaultNightMode(
                    if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
            }
        }

        view.findViewById<View>(R.id.btnAddPlaylist).setOnClickListener {
            showAddPlaylistDialog()
        }

        view.findViewById<View>(R.id.btnClearPlaylists).setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.clear_playlists)
                .setMessage("Remove all saved playlist URLs?")
                .setPositiveButton("Clear") { _, _ ->
                    lifecycleScope.launch { App.instance.prefsManager.clearPlaylists() }
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        view.findViewById<View>(R.id.btnClearFavorites).setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.clear_favorites)
                .setMessage("Remove all favorite channels?")
                .setPositiveButton("Clear") { _, _ ->
                    lifecycleScope.launch { App.instance.database.favoriteDao().clearAll() }
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        view.findViewById<View>(R.id.btnClearHistory).setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.clear_history)
                .setMessage("Remove all watch history?")
                .setPositiveButton("Clear") { _, _ ->
                    lifecycleScope.launch { App.instance.database.watchHistoryDao().clearAll() }
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        view.findViewById<TextView>(R.id.aboutText).text = getString(R.string.app_description)
        view.findViewById<TextView>(R.id.versionText).text = getString(R.string.version)
    }

    private fun showAddPlaylistDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_playlist, null)
        val input = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playlistUrlInput)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_playlist)
            .setView(dialogView)
            .setPositiveButton(R.string.load) { _, _ ->
                val url = input.text?.toString()?.trim() ?: ""
                if (url.isNotBlank()) {
                    lifecycleScope.launch { App.instance.prefsManager.addPlaylistUrl(url) }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}

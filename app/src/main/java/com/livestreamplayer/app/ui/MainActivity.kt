package com.livestreamplayer.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.livestreamplayer.app.App
import com.livestreamplayer.app.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private val homeFragment = HomeFragment()
    private val categoriesFragment = CategoriesFragment()
    private val favoritesFragment = FavoritesFragment()
    private val historyFragment = HistoryFragment()
    private val settingsFragment = SettingsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottomNav)

        if (savedInstanceState == null) {
            loadFragment(homeFragment)
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(homeFragment)
                R.id.nav_categories -> loadFragment(categoriesFragment)
                R.id.nav_favorites -> loadFragment(favoritesFragment)
                R.id.nav_history -> loadFragment(historyFragment)
                R.id.nav_settings -> loadFragment(settingsFragment)
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        return true
    }

    private fun applyTheme() {
        lifecycleScope.launch {
            val isDark = App.instance.prefsManager.darkMode.first()
            AppCompatDelegate.setDefaultNightMode(
                if (isDark) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    fun navigateToCategory(categoryName: String) {
        homeFragment.filterByCategory(categoryName)
        bottomNav.selectedItemId = R.id.nav_home
    }
}

package com.example.tmdbclient

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.tmdbclient.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val viewModel : ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Set up viewBinding
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Set action bar
        setSupportActionBar(binding.toolbar)

        //Configure top-level navigation
        val navFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        val bottomNavView = binding.bottomNav

        val navController = navFragment?.findNavController()
            ?: throw NoSuchElementException("Could not find NavController")
        val configuration = AppBarConfiguration(
            setOf(R.id.profileFragment, R.id.movieFragment, R.id.tvFragment)
        )
        setupActionBarWithNavController(navController, configuration)
        bottomNavView.setupWithNavController(navController)

        //Load the session cookies if they exist
        val prefs = getPreferences(Context.MODE_PRIVATE)
        val sessionId = prefs?.getString(SESSION_ID_TAG, null)
        viewModel.setSession(sessionId)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.fragment_container).navigateUp()
    }

    companion object {
        const val SESSION_ID_TAG = "SESSION_ID"
    }
}
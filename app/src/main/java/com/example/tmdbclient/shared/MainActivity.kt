package com.example.tmdbclient.shared

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.tmdbclient.R
import com.example.tmdbclient.databinding.ActivityMainBinding
import com.example.tmdbclient.profile.ProfileState
import com.example.tmdbclient.profile.ProfileViewModel
import com.example.tmdbclient.shared.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    //TODO main goal - make multiple backstacks work (one backstack for each top navigation target)

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
            setOf(R.id.profileFragment, R.id.movieListFragment, R.id.tvShowListFragment)
        )
        setupActionBarWithNavController(navController, configuration)
        bottomNavView.setupWithNavController(navController)

        //Load the session cookies if they exist
        lifecycleScope.launch {
            val prefs = getPreferences(Context.MODE_PRIVATE)
            val sessionId = prefs?.getString(SESSION_ID_TAG, null)
            if (!sessionId.isNullOrBlank()) {
                viewModel.handleAction(ProfileState.Action.Restore(sessionId))
            }
        }

//        setContent {
//            MyApplicationTheme {
//                Surface(color = MaterialTheme.colors.background) {
//                    Text(text = "Hello Compose!")
//                }
//            }
//
//        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.fragment_container).navigateUp()
    }

    companion object {
        const val SESSION_ID_TAG = "SESSION_ID"
    }
}
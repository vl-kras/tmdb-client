package com.example.tmdbclient.shared

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.tmdbclient.R
import com.example.tmdbclient.databinding.ActivityMainBinding
import com.example.tmdbclient.movie.list.ui.MovieList
import com.example.tmdbclient.profile.ProfileScreen
import com.example.tmdbclient.profile.ProfileState
import com.example.tmdbclient.profile.ProfileViewModel
import com.example.tmdbclient.shared.theme.MyApplicationTheme
import com.example.tmdbclient.tvshow.list.TvShowList
import kotlinx.coroutines.launch

val Context.datastore: DataStore<Preferences> by preferencesDataStore(name = "cookies")
val SESSION_ID_KEY = stringPreferencesKey("session_id")

class MainActivity : AppCompatActivity() {

    //TODO main goal - make multiple backstacks work (one backstack for each top navigation target)

//    private val viewModel : ProfileViewModel by viewModels()

    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Set up viewBinding
        val binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)

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
//        lifecycleScope.launch {
//            val prefs = getPreferences(Context.MODE_PRIVATE)
//            val sessionId = prefs?.getString(SESSION_ID_TAG, null)
//            if (!sessionId.isNullOrBlank()) {
//                viewModel.handleAction(ProfileState.Action.Restore(sessionId))
//            }
//        }



        val composeView = ComposeView(this).apply {
            setContent {
                MyApplicationTheme {
                    Initial()
                }
            }
        }
        setContentView(composeView)

    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.fragment_container).navigateUp()
    }

    companion object {
        const val SESSION_ID_TAG = "SESSION_ID"
    }

    @ExperimentalMaterialApi
    @ExperimentalFoundationApi
    @Composable
    fun Initial() {
        val navController = rememberNavController()

        val profileVM: ProfileViewModel = viewModel()

        val items = listOf(
            Screen.Profile,
            Screen.MovieList,
            Screen.TvShowList,
        )

        Scaffold(
            bottomBar = {
                BottomNavigation {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    items.forEach { screen ->
                        BottomNavigationItem(
                            icon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    // on the back stack as users select items
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // reselecting the same item
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(navController, startDestination = Screen.Profile.route, Modifier.padding(innerPadding)) {
                composable(Screen.Profile.route) { ProfileScreen(profileVM, this@MainActivity) }
                composable(Screen.MovieList.route) { MovieList(profileVM) }
                composable(Screen.TvShowList.route) { TvShowList() }
            }
        }

    }

//    @Composable
//    fun Profile() {
//        Text(text = "Profile")
//    }
//    @Composable
//    fun MovieList() {
//        Text(text = "Movie List")
//    }
//    @Composable
//    fun TvShowList() {
//        Text(text = "TV Show List")
//    }


}

sealed class Screen(val route: String, val label: String) {
    object Profile: Screen("profile", "Profile")
    object MovieList: Screen("movielist", "Movie List")
    object TvShowList: Screen("tvshowlist", "TV Show List")
}
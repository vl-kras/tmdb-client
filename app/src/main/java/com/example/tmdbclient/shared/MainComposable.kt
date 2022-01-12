package com.example.tmdbclient.shared

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tmdbclient.movie.MoviesNavigation
import com.example.tmdbclient.profile.ui.ProfileScreen
import com.example.tmdbclient.profile.ui.ProfileState
import com.example.tmdbclient.profile.ui.ProfileViewModel
import com.example.tmdbclient.tvshow.TvShowsNavigation
import kotlinx.coroutines.launch

@Composable
fun MainScreen(context: Context) {

    val profileVM: ProfileViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    if (profileVM.getState().value !is ProfileState.ActiveSession) {
        SideEffect {
            coroutineScope.launch {
                context.datastore.edit { cookies ->
                    //read stored sessionId, if there is any
                    cookies[SESSION_ID_KEY].let { sessionId ->
                        profileVM.handleAction(ProfileState.Action.Restore(sessionId ?: "") {} )
                    }
                }
            }
        }
    }

    val writeSessionId: (String) -> Unit = { sessionId ->
        coroutineScope.launch {
            context.datastore.edit { cookies ->

                if (sessionId.isNotBlank()) {

                    if (cookies[SESSION_ID_KEY] != sessionId) {
                        cookies[SESSION_ID_KEY] = sessionId
                    }
                } else {
                    cookies.remove(SESSION_ID_KEY)
                }
            }
        }
    }

    val navController = rememberNavController()

    Scaffold(bottomBar = { BottomNav(navController) } ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ScreenTab.Profile.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ScreenTab.Profile.route) { ProfileScreen(profileVM, writeSessionId) }
            composable(ScreenTab.Movies.route) { MoviesNavigation(profileVM) }
            composable(ScreenTab.TvShows.route) { TvShowsNavigation(profileVM) }
        }
    }
}

@Composable
fun BottomNav(navController: NavController) {

    val bottomNavItems = listOf(
        ScreenTab.Profile,
        ScreenTab.Movies,
        ScreenTab.TvShows,
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    BottomNavigation {

        bottomNavItems.forEach { screen ->
            BottomNavigationItem(
                icon = { Icon(imageVector = screen.icon , contentDescription = null) },
                label = { Text(screen.label) },
                selected = (currentDestination?.hierarchy?.any { it.route == screen.route } == true),
                onClick = { onBottomNavItemClick(navController, screen) }
            )
        }
    }
}

fun onBottomNavItemClick(navController: NavController, screen: ScreenTab) {

    //Copy pasted from Android Developer guide
    navController.navigate(screen.route) {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        // on the back stack as users select items
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        // Avoid multiple copies of the same destination when
        // re-selecting the same item
        launchSingleTop = true
        // Restore state when re-selecting a previously selected item
        restoreState = true
    }
}

sealed class ScreenTab(val route: String, val label: String, val icon: ImageVector) {
    object Profile: ScreenTab("profile", "Profile", Icons.Default.Person) //TODO add proper icons
    object Movies: ScreenTab("movies", "Movies", Icons.Outlined.Movie)
    object TvShows: ScreenTab("tv_shows", "TV", Icons.Default.Tv)
}
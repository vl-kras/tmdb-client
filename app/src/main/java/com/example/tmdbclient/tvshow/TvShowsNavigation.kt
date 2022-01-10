package com.example.tmdbclient.tvshow

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tmdbclient.profile.ui.ProfileViewModel
import com.example.tmdbclient.tvshow.details.ui.TvShowDetailsScreen
import com.example.tmdbclient.tvshow.list.ui.TvShowListScreen

@Composable
fun TvShowsNavigation(profileVM: ProfileViewModel) {

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "show_list") {
        composable("show_list") { TvShowListScreen(navController = navController) }
        composable(
            route = "tv/{showId}",
            arguments = listOf(navArgument("showId") { type = NavType.IntType } )
        ) {
            TvShowDetailsScreen(profileVM = profileVM, showId = it.arguments?.getInt("showId") ?: 0)
        }
    }
}
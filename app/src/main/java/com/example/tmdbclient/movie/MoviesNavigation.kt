package com.example.tmdbclient.movie

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tmdbclient.movie.details.ui.MovieDetailsScreen
import com.example.tmdbclient.movie.list.ui.MovieListScreen
import com.example.tmdbclient.profile.ui.ProfileViewModel

@Composable
fun MoviesNavigation(profileVM: ProfileViewModel) {

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "movie_list") {
        composable("movie_list") { MovieListScreen(profileVM = profileVM, navController = navController) }
        composable(
            route = "movie/{movieId}",
            arguments = listOf(navArgument("movieId") { type = NavType.IntType } )
        ) {
            MovieDetailsScreen(profileVM = profileVM, movieId = it.arguments?.getInt("movieId") ?: 0, navController)
        }
    }
}
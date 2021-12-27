package com.example.tmdbclient.movie.list

import com.example.tmdbclient.*
import com.example.tmdbclient.movie.list.logic.MovieListRepository
import com.example.tmdbclient.shared.ApiError
import com.example.tmdbclient.shared.ServiceLocator
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException

interface TmdbMovieListApi {
    @GET("movie/popular")
    fun getMoviesPopular(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int? = null,
        @Query("language") language: String? = null
    ): Call<GetPopularMovies.ResponseBody>
}

class MovieListBackend: MovieListRepository.MovieListBackendContract {

    private val apiKey = BuildConfig.TMDB_API_KEY
    private val service = ServiceLocator.retrofit.create(TmdbMovieListApi::class.java)

    override fun fetchPopularMovies(page: Int): List<MovieListRepository.Movie> {

        return getPopularMoviesByPage(page).map { movie ->
            MovieListRepository.Movie(
                movie.id,
                movie.title,
                movie.posterPath ?: ""
            )
        }
    }

    private fun getPopularMoviesByPage(page: Int): List<GetPopularMovies.ResponseBody.MovieInfo> {

        val request = service.getMoviesPopular(apiKey, page)
        val response = request.execute()

        //TODO maybe a better, safer way to get data from server
//        val result = if (response.isSuccessful) {
//            Result.success(response.body()!!.movieList)
//        } else {
//            val apiError = response.errorBody()?.let {
//                ApiError.from(it)
//            }
//            Result.failure(apiError!!)
//        }
        return response.body()?.movieList
            ?: throw IOException("Could not load popular movies")
    }
}

object GetPopularMovies {

    data class ResponseBody (

        @SerializedName("page") val page : Int,
        @SerializedName("results") val movieList : List<MovieInfo>,
        @SerializedName("total_results") val totalResults : Int,
        @SerializedName("total_pages") val totalPages : Int

    ) {
        data class MovieInfo(

            @SerializedName("id") val id : Int,
            @SerializedName("adult") val isAdult : Boolean,
            @SerializedName("backdrop_path") val backdropPath : String,
            @SerializedName("genre_ids") val genreIds : List<Int>,
            @SerializedName("original_language") val originalLanguage : String,
            @SerializedName("original_title") val originalTitle : String,
            @SerializedName("overview") val overview : String,
            @SerializedName("popularity") val popularity : Double,
            @SerializedName("poster_path") val posterPath : String?,
            @SerializedName("release_date") val releaseDate : String,
            @SerializedName("title") val title : String,
            @SerializedName("video") val hasVideo : Boolean,
            @SerializedName("vote_average") val voteAverage : Double,
            @SerializedName("vote_count") val voteCount : Int
        )
    }
}


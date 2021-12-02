package com.example.tmdbclient

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class PopularShows (

    @SerializedName("page") val page : Int,
    @SerializedName("results") val results : List<TvShow>,
    @SerializedName("total_results") val totalResults : Int,
    @SerializedName("total_pages") val totalPages : Int
)

data class PopularMovies (

    @SerializedName("page") val page : Int,
    @SerializedName("results") val results : List<Movie>,
    @SerializedName("total_results") val totalResults : Int,
    @SerializedName("total_pages") val totalPages : Int
)

data class Movie(

    @SerializedName("id") val id : Int,
    @SerializedName("adult") val isAdult : Boolean,
    @SerializedName("backdrop_path") val backdropPath : String,
    @SerializedName("genre_ids") val genreIds : List<Int>,
    @SerializedName("original_language") val originalLanguage : String,
    @SerializedName("original_title") val originalTitle : String,
    @SerializedName("overview") val overview : String,
    @SerializedName("popularity") val popularity : Double,
    @SerializedName("poster_path") val posterPath : String,
    @SerializedName("release_date") val releaseDate : String,
    @SerializedName("title") val title : String,
    @SerializedName("video") val hasVideo : Boolean,
    @SerializedName("vote_average") val voteAverage : Double,
    @SerializedName("vote_count") val voteCount : Int
)

data class TvShow (

    @SerializedName("poster_path") val posterPath : String,
    @SerializedName("popularity") val popularity : Double,
    @SerializedName("id") val id : Int,
    @SerializedName("backdrop_path") val backdropPath : String,
    @SerializedName("vote_average") val voteAverage : Double,
    @SerializedName("overview") val overview : String,
    @SerializedName("first_air_date") val firstAirDate : String,
    @SerializedName("origin_country") val originCountry : List<String>,
    @SerializedName("genre_ids") val genreIds : List<Int>,
    @SerializedName("original_language") val originalLanguage : String,
    @SerializedName("vote_count") val voteCount : Int,
    @SerializedName("name") val name : String,
    @SerializedName("original_name") val originalName : String
)

data class GuestSession (

    @SerializedName("success") val isSuccess: Boolean,
    @SerializedName("guest_session_id") val sessionId : String,
    @SerializedName("expires_at") val expiresAt : String,
)

interface TmdbAPi {

    @GET("authentication/guest_session/new")
    fun getNewGuestSession(
        @Query("api_key") apiKey: String,
    ): Call<GuestSession>

    @GET("movie/popular")
    fun getMoviesPopular(
        @Query("api_key") apiKey: String,
        @Query("language") language: String? = null,
        @Query("page") page: Int? = null
    ): Call<PopularMovies>

    @GET("tv/popular")
    fun getShowsPopular(
        @Query("api_key") apiKey: String,
        @Query("language") language: String? = null,
        @Query("page") page: Int? = null
    ): Call<PopularShows>
}

class Backend {

    private val API_BASE_PATH = "https://api.themoviedb.org/3/"
    private val API_KEY = "c23761b45323bcad507e18c946b0d939"

    private val retrofit = Retrofit.Builder()
        .baseUrl(API_BASE_PATH)
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient.Builder().build())
        .build()

    private fun <T> buildService(service: Class<T>): T {
        return retrofit.create(service)
    }

    private val service = buildService(TmdbAPi::class.java)

    fun createGuestSession() : GuestSession {
        return service.getNewGuestSession(apiKey = API_KEY).execute().body()
            ?: throw NoSuchElementException("Mission failed, we'll get 'em next time.")
    }

    fun getPopularTvShowsByPage(page: Int = 1) : List<TvShow> {
        return service.getShowsPopular(apiKey = API_KEY, page = page).execute().body()?.results
            ?: throw NoSuchElementException("We ain't found shit!")
    }

    fun getPopularMoviesByPage(page: Int = 1) : List<Movie> {
        return service.getMoviesPopular(apiKey = API_KEY, page = page).execute().body()?.results
            ?: throw NoSuchElementException("We ain't found shit!")
    }
}
package com.example.tmdbclient.movie.details

import com.example.tmdbclient.*
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.*
import java.io.IOException

interface TmdbMovieDetailsApi {

    @DELETE("movie/{movie_id}/rating")
    @Headers("Content-Type:application/json;charset=utf-8")
    fun deleteMovieRating(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("session_id") sessionId: String,
    ): Call<DeleteMovieRating.ResponseBody>

    @POST("movie/{movie_id}/rating")
    @Headers("Content-Type:application/json;charset=utf-8")
    fun postMovieRating(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("session_id") sessionId: String,
        @Body body: PostMovieRating.RequestBody
    ): Call<PostMovieRating.ResponseBody>

    @GET("movie/{movie_id}")
    fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String? = null,
        @Query("append_to_response") appendToResponse: String? = null
    ): Call<GetMovieDetails.ResponseBody>
}

class MovieDetailsBackend: MovieDetailsRepository.MovieDetailsBackendContract {

    private val apiKey = BuildConfig.TMDB_API_KEY
    private val service = ServiceLocator.retrofit.create(TmdbMovieDetailsApi::class.java)

    override fun fetchMovieDetails(movieId: Int): MovieDetailsRepository.MovieDetails {

        val movie = getMovieDetails(movieId)
        return MovieDetailsRepository.MovieDetails(
            title = movie.title,
            isAdult = movie.isAdult,
            genres = movie.genres.map { it.name },
            overview = movie.overview ?: "",
            posterPath = movie.posterPath ?: "",
            runtime = movie.runtime ?: 0,
            userScore = movie.voteAverage.toFloat(),
            tagline = movie.tagline ?: "",
        )
    }

    override fun rateMovie(movieId: Int, sessionId: String, rating: Float) {
        postMovieRating(movieId, rating, sessionId)
    }

    override fun removeMovieRating(movieId: Int, sessionId: String) {
        deleteMovieRating(movieId, sessionId)
    }

    private fun getMovieDetails(movieId: Int): GetMovieDetails.ResponseBody {
        val request = service.getMovieDetails(movieId, apiKey)
        val response = request.execute()
        return response.body()
            ?: throw NoSuchElementException("Could not fetch movie details")
    }

    private fun deleteMovieRating(movieId: Int, sessionId: String) {

        val request = service.deleteMovieRating(movieId, apiKey, sessionId)
        val response = request.execute()
        if (!response.isSuccessful) {
            throw IOException("Failed to post movie rating")
        }
    }

    private fun postMovieRating(movieId: Int, rating: Float, sessionId: String){

        val requestBody = PostMovieRating.RequestBody(rating)
        val request = service.postMovieRating(
            movieId, apiKey, sessionId, requestBody
        )
        val response = request.execute()
        if (!response.isSuccessful) {
            throw IOException("Failed to post movie rating")
        }
    }
}

object GetMovieDetails {

    data class ResponseBody(

        @SerializedName("adult") val isAdult : Boolean,
        @SerializedName("backdrop_path") val backdropPath : String?,
        @SerializedName("belongs_to_collection") val belongsToCollection : Collection?,
        @SerializedName("budget") val budget : Int,
        @SerializedName("genres") val genres : List<Genre>,
        @SerializedName("homepage") val homepage : String?,
        @SerializedName("id") val id : Int,
        @SerializedName("imdb_id") val imdbId : String?,
        @SerializedName("original_language") val originalLanguage : String,
        @SerializedName("original_title") val originalTitle : String,
        @SerializedName("overview") val overview : String?,
        @SerializedName("popularity") val popularity : Double,
        @SerializedName("poster_path") val posterPath : String?,
        @SerializedName("production_companies") val productionCompanies : List<ProductionCompany>,
        @SerializedName("production_countries") val productionCountries : List<ProductionCountry>,
        @SerializedName("release_date") val releaseDate : String,
        @SerializedName("revenue") val revenue : Int,
        @SerializedName("runtime") val runtime : Int?,
        @SerializedName("spoken_languages") val spokenLanguages : List<Language>,
        @SerializedName("status") val status : String,
        @SerializedName("tagline") val tagline : String?,
        @SerializedName("title") val title : String,
        @SerializedName("video") val isVideo : Boolean,
        @SerializedName("vote_average") val voteAverage : Double,
        @SerializedName("vote_count") val voteCount : Int
    ) {

        data class Genre(

            @SerializedName("id") val id : Int,
            @SerializedName("name") val name : String
        )

        data class Collection(

            @SerializedName("id") val id : Int,
            @SerializedName("name") val name : String,
            @SerializedName("poster_path") val posterPath : String?,
            @SerializedName("backdrop_path") val backdropPath : String
        )

        data class ProductionCompany(

            @SerializedName("id") val id : Int,
            @SerializedName("logo_path") val logoPath : String?,
            @SerializedName("name") val name : String,
            @SerializedName("origin_country") val originCountry : String
        )

        data class ProductionCountry(

            @SerializedName("iso_3166_1") val iso_3166_1 : String,
            @SerializedName("name") val name : String
        )

        data class Language(

            @SerializedName("english_name") val englishName : String,
            @SerializedName("iso_639_1") val iso_639_1 : String,
            @SerializedName("name") val name : String
        )
    }
}

object PostMovieRating {

    data class RequestBody(
        @SerializedName("value") val rating: Float
    )
    data class ResponseBody(
        @SerializedName("status_code") val statusCode: Int,
        @SerializedName("status_message") val statusMessage : String
    )
}

object DeleteMovieRating {
    data class ResponseBody(
        @SerializedName("status_code") val statusCode: Int,
        @SerializedName("status_message") val statusMessage : String
    )
}
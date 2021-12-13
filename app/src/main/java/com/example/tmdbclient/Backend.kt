package com.example.tmdbclient

import android.util.Log
import com.example.tmdbclient.TmdbBasePaths.API_BASE_PATH
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.IOException


//TODO implement SOLID & Clean architecture

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

data class Session(
    @SerializedName("success") val isSuccess: Boolean = true,
    @SerializedName("session_id") val sessionId: String
)

data class Logout(
    @SerializedName("success") val isSuccess: Boolean
)

data class LogoutRequestBody(
    @SerializedName("session_id") val sessionId: String
)

data class RequestTokenResponseBody(
    @SerializedName("success") val isSuccess: Boolean,
    @SerializedName("expires_at") val expiresAt : String,
    @SerializedName("request_token") val requestToken : String
)

data class ValidateTokenWithLoginBody(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password : String,
    @SerializedName("request_token") val requestToken : String
)

data class ValidateTokenWithLoginResponse(
    @SerializedName("success") val isSuccess: Boolean,
    @SerializedName("expires_at") val expiresAt : String,
    @SerializedName("request_token") val requestToken : String
)

data class CreateSessionRequestBody(
    @SerializedName("request_token") val requestToken : String
)

data class CreateSessionResponseBody(
    @SerializedName("success") val isSuccess: Boolean,
    @SerializedName("session_id") val sessionId: String,
)

data class UserAccount(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("username") val username: String?,
)

data class Genre(

    @SerializedName("id") val id : Int,
    @SerializedName("name") val name : String
)

data class MovieDetails(

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
    data class Collection(

        @SerializedName("id") val id : Int,
        @SerializedName("name") val name : String,
        @SerializedName("poster_path") val posterPath : String?,
        @SerializedName("backdrop_path") val backdropPath : String
    )
}

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

data class TvShowDetails(

    @SerializedName("backdrop_path") val backdropPath : String?,
    @SerializedName("created_by") val createdBy : List<Creator>,
    @SerializedName("episode_run_time") val episodeRunTime : List<Int>,
    @SerializedName("first_air_date") val firstAirDate : String,
    @SerializedName("genres") val genres : List<Genre>,
    @SerializedName("homepage") val homepage : String,
    @SerializedName("id") val id : Int,
    @SerializedName("in_production") val isInProduction : Boolean,
    @SerializedName("languages") val languages : List<String>,
    @SerializedName("last_air_date") val lastAirDate : String,
    @SerializedName("last_episode_to_air") val lastEpisodeToAir : Episode,
    @SerializedName("name") val name : String,
    @SerializedName("next_episode_to_air") val nextEpisodeToAir : Episode?,
    @SerializedName("networks") val networks : List<Network>,
    @SerializedName("number_of_episodes") val numberOfEpisodes : Int,
    @SerializedName("number_of_seasons") val numberOfSeasons : Int,
    @SerializedName("origin_country") val originCountry : List<String>,
    @SerializedName("original_language") val originalLanguage : String,
    @SerializedName("original_name") val originalName : String,
    @SerializedName("overview") val overview : String,
    @SerializedName("popularity") val popularity : Double,
    @SerializedName("poster_path") val posterPath : String?,
    @SerializedName("production_companies") val productionCompanies : List<ProductionCompany>,
    @SerializedName("production_countries") val productionCountries : List<ProductionCountry>,
    @SerializedName("seasons") val seasons : List<Season>,
    @SerializedName("spoken_languages") val spokenLanguages : List<Language>,
    @SerializedName("status") val status : String,
    @SerializedName("tagline") val tagline : String,
    @SerializedName("type") val type : String,
    @SerializedName("vote_average") val voteAverage : Double,
    @SerializedName("vote_count") val voteCount : Int

) {

    data class Creator (

        @SerializedName("id") val id : Int,
        @SerializedName("credit_id") val creditId : String,
        @SerializedName("name") val name : String,
        @SerializedName("gender") val gender : Int,
        @SerializedName("profile_path") val profilePath : String?
    )

    data class Episode (

        @SerializedName("air_date") val airDate : String,
        @SerializedName("episode_number") val episodeNumber : Int,
        @SerializedName("id") val id : Int,
        @SerializedName("name") val name : String,
        @SerializedName("overview") val overview : String,
        @SerializedName("production_code") val productionCode : String,
        @SerializedName("season_number") val seasonNumber : Int,
        @SerializedName("still_path") val stillPath : String?,
        @SerializedName("vote_average") val voteAverage : Double,
        @SerializedName("vote_count") val voteCount : Int
    )

    data class Network (

        @SerializedName("name") val name : String,
        @SerializedName("id") val id : Int,
        @SerializedName("logo_path") val logoPath : String?,
        @SerializedName("origin_country") val originCountry : String
    )

    data class Season (

        @SerializedName("air_date") val airDate : String,
        @SerializedName("episode_count") val episodeCount : Int,
        @SerializedName("id") val id : Int,
        @SerializedName("name") val name : String,
        @SerializedName("overview") val overview : String,
        @SerializedName("poster_path") val posterPath : String,
        @SerializedName("season_number") val seasonNumber : Int
    )
}

object PostTvShowRating {
    data class RequestBody(
        @SerializedName("value") val rating: Float
    )
    data class ResponseBody(
        @SerializedName("status_code") val statusCode: Int,
        @SerializedName("status_message") val statusMessage : String
    )
}

object DeleteTvShowRating {
    data class ResponseBody(
        @SerializedName("status_code") val statusCode: Int,
        @SerializedName("status_message") val statusMessage : String
    )
}

interface TmdbAPi {

    @DELETE("tv/{tv_id}/rating")
    @Headers("Content-Type:application/json;charset=utf-8")
    fun deleteTvShowRating(
        @Path("tv_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("session_id") sessionId: String,
    ): Call<DeleteTvShowRating.ResponseBody>

    @POST("tv/{tv_id}/rating")
    @Headers("Content-Type:application/json;charset=utf-8")
    fun postTvShowRating(
        @Path("tv_id") showId: Int,
        @Query("api_key") apiKey: String,
        @Query("session_id") sessionId: String,
        @Body body: PostTvShowRating.RequestBody
    ): Call<PostTvShowRating.ResponseBody>

    @GET("tv/{tv_id}")
    fun getTvShowDetails(
        @Path("tv_id") tvShowId: Int,
        @Query("api_key") apiKey: String
    ): Call<TvShowDetails>

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
    ): Call<MovieDetails>

    @GET("account")
    fun getAccountDetails(
        @Query("api_key") apiKey: String,
        @Query("session_id") sessionId: String
    ): Call<UserAccount>

    @GET("authentication/token/new")
    fun createRequestToken(
        @Query("api_key") apiKey: String
    ): Call<RequestTokenResponseBody>

    @POST("authentication/token/validate_with_login")
    fun validateTokenWithLogin(
        @Query("api_key") apiKey: String,
        @Body body: ValidateTokenWithLoginBody
    ): Call<ValidateTokenWithLoginResponse>

    @POST("authentication/session/new")
    fun createSession(
        @Query("api_key") apiKey: String,
        @Body body: CreateSessionRequestBody
    ): Call<CreateSessionResponseBody>

    @HTTP(method = "DELETE", path = "authentication/session", hasBody = true)
//    @DELETE("authentication/session")
    fun deleteSession(
        @Query("api_key") apiKey: String,
        @Body body: LogoutRequestBody
    ): Call<Logout>

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

object Backend {
    private const val API_KEY = "c23761b45323bcad507e18c946b0d939"

    private val statusCodes = mapOf(
        1 to "Success",
        2 to "Invalid service: this service does not exist.",
        3 to "Authentication failed: You do not have permissions to access the service.",
        4 to "Invalid format: This service doesn't exist in that format.",
        5 to "Invalid parameters: Your request parameters are incorrect.",
        6 to "Invalid id: The pre-requisite id is invalid or not found.",
        7 to "Invalid API key: You must be granted a valid key.",
        8 to "Duplicate entry: The data you tried to submit already exists.",
        9 to "Service offline: This service is temporarily offline, try again later.",
        10 to "Suspended API key: Access to your account has been suspended, contact TMDB.",
        11 to "Internal error: Something went wrong, contact TMDB.",
        12 to "The item/record was updated successfully.",
        13 to "The item/record was deleted successfully.",
        14 to "Authentication failed.",
        15 to "Failed.",
        16 to "Device denied.",
        17 to "Session denied.",
        18 to "Validation failed.",
        19 to "Invalid accept header.",
        20 to "Invalid date range: Should be a range no longer than 14 days.",
        21 to "Entry not found: The item you are trying to edit cannot be found.",
        22 to "Invalid page: Pages start at 1 and max at 1000. They are expected to be an integer.",
        23 to "Invalid date: Format needs to be YYYY-MM-DD.",
        24 to "Your request to the backend server timed out. Try again.",
        25 to "Your request count (#) is over the allowed limit of (40).",
        26 to "You must provide a username and password.",
        27 to "Too many append to response objects: The maximum number of remote calls is 20.",
        28 to "Invalid timezone: Please consult the documentation for a valid timezone.",
        29 to "You must confirm this action: Please provide a confirm=true parameter.",
        30 to "Invalid username and/or password: You did not provide a valid login.",
        31 to "Account disabled: Your account is no longer active. Contact TMDB if this is an error.",
        32 to "Email not verified: Your email address has not been verified.",
        33 to "Invalid request token: The request token is either expired or invalid.",
        34 to "The resource you requested could not be found.",
        35 to "Invalid token.",
        36 to "This token hasn't been granted write permission by the user.",
        37 to "The requested session could not be found.",
        38 to "You don't have permission to edit this resource.",
        39 to "This resource is private.",
        40 to "Nothing to update.",
        41 to "This request token hasn't been approved by the user.",
        42 to "This request method is not supported for this resource.",
        43 to "Couldn't connect to the backend server.",
        44 to "The ID is invalid.",
        45 to "This user has been suspended.",
        46 to "The API is undergoing maintenance. Try again later.",
        47 to "The input is not valid.",
    )

    private val retrofit = Retrofit.Builder()
        .baseUrl(API_BASE_PATH)
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient.Builder().build())
        .build()

    private fun <T> buildService(service: Class<T>): T {
        return retrofit.create(service)
    }

    private val service = buildService(TmdbAPi::class.java)


    ////////////////////////////////////////////////////////

    fun deleteTvShowRating(showId: Int, sessionId: String): Boolean {
        val request = service.deleteTvShowRating(showId, API_KEY, sessionId)
        val response = request.execute()
        return response.isSuccessful
    }

    fun postTvShowRating(showId: Int, rating: Float, sessionId: String): Boolean {
        //rating in 0.5..10 step 0.5, otherwise service returns 400 "Bad Request"
        return if ((rating in 0.5f..10.0f) && (rating.mod(0.5f) == 0f)) {
            val requestBody = PostTvShowRating.RequestBody(rating)

            Log.d("BLABLA", "$showId, $API_KEY $rating, $sessionId")
            Log.d("BLABLA", Gson().toJson(requestBody))
            val request = service.postTvShowRating(
                showId, API_KEY, sessionId, requestBody
            )

            val response = request.execute()
            Log.d("BLABLA", response.raw().toString())
            response.isSuccessful //
        } else {
            false //invalid rating value
        }
    }

    fun getTvShowDetails(tvShowId: Int): TvShowDetails {
        val request = service.getTvShowDetails(tvShowId, API_KEY)
        val response = request.execute()
        return response.body()
            ?: throw IOException("Failed to fetch show details")
    }

    fun deleteMovieRating(movieId: Int, sessionId: String): Boolean {
        val request = service.deleteMovieRating(movieId, API_KEY, sessionId)
        val response = request.execute()
        return response.isSuccessful
    }

    fun postMovieRating(movieId: Int, rating: Float, sessionId: String): Boolean {
        //rating in 0.5..10 step 0.5, otherwise service returns 400 "Bad Request"
        return if ((rating in 0.5f..10.0f) && (rating.mod(0.5f) == 0f)) {
            val requestBody = PostMovieRating.RequestBody(rating)

            Log.d("BLABLA", "$movieId, $API_KEY $rating, $sessionId")
            Log.d("BLABLA", Gson().toJson(requestBody))
            val request = service.postMovieRating(
                movieId, API_KEY, sessionId, requestBody
            )

            val response = request.execute()
            Log.d("BLABLA", response.raw().toString())
            response.isSuccessful //
        } else {
            false //invalid rating value
        }
    }

    fun getMovieDetails(movieId: Int) : MovieDetails {
        val response = service.getMovieDetails(movieId, API_KEY).execute()
        Log.i("Movie Details", response.body().toString())
        return response.body()
            ?: throw NoSuchElementException("Could not fetch movie details")
    }

    fun getAccountDetails(sessionId: String) : UserAccount {
        val response = service.getAccountDetails(API_KEY, sessionId).execute()
        Log.d("BLABLA", response.raw().toString())
        return response.body()
            ?: throw NoSuchElementException("Could not fetch user account")
    }

    fun createSession(requestToken: String): String {
        val request = service
            .createSession(apiKey = API_KEY, body = CreateSessionRequestBody(requestToken))

        return request.execute().body()?.sessionId
            ?: throw NoSuchElementException("Failed to create session")
    }

    fun validateTokenWithLogin(username: String, password: String, token: String): Boolean {
        val requestBody = ValidateTokenWithLoginBody(username, password, token)
        return service.validateTokenWithLogin(apiKey = API_KEY, body = requestBody)
            .execute().isSuccessful
    }

    fun createRequestToken() : String {
        return service.createRequestToken(apiKey = API_KEY).execute().body()?.requestToken
            ?: throw NoSuchElementException("Failed to create request token")
    }

    fun deleteSession(sessionId: String) : Logout {
        val requestBody = LogoutRequestBody(sessionId)
        val result = service.deleteSession(apiKey = API_KEY, body = requestBody).execute()
        return result.body()
            ?: throw NoSuchElementException("Body: ${result.code()}")
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
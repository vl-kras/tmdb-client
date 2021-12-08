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

    data class Genre(

        @SerializedName("id") val id : Int,
        @SerializedName("name") val name : String
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

object PostMovieRating {
    data class RequestBody(
        @SerializedName("value") val rating: Float
    )
    data class ResponseBody(
        @SerializedName("status_code") val statusCode: Int,
        @SerializedName("status_message") val statusMessage : String
    )
}

interface TmdbAPi {


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
        Pair(1, "Success"),
        Pair(2, "Invalid service: this service does not exist."),
        Pair(3, "Authentication failed: You do not have permissions to access the service."),
        Pair(4, "Invalid format: This service doesn't exist in that format."),
        Pair(5, "Invalid parameters: Your request parameters are incorrect."),
        Pair(6, "Invalid id: The pre-requisite id is invalid or not found."),
        Pair(7, "Invalid API key: You must be granted a valid key."),
        Pair(8, "Duplicate entry: The data you tried to submit already exists."),
        Pair(9, "Service offline: This service is temporarily offline, try again later."),
        Pair(10, "Suspended API key: Access to your account has been suspended, contact TMDB."),
        Pair(11, "Internal error: Something went wrong, contact TMDB."),
        Pair(12, "The item/record was updated successfully."),
        Pair(13, "The item/record was deleted successfully."),
        Pair(14, "Authentication failed."),
        Pair(15, "Failed."),
        Pair(16, "Device denied."),
        Pair(17, "Session denied."),
        Pair(18, "Validation failed."),
        Pair(19, "Invalid accept header."),
        Pair(20, "Invalid date range: Should be a range no longer than 14 days."),
        Pair(21, "Entry not found: The item you are trying to edit cannot be found."),
        Pair(22, "Invalid page: Pages start at 1 and max at 1000. They are expected to be an integer."),
        Pair(23, "Invalid date: Format needs to be YYYY-MM-DD."),
        Pair(24, "Your request to the backend server timed out. Try again."),
        Pair(25, "Your request count (#) is over the allowed limit of (40)."),
        Pair(26, "You must provide a username and password."),
        Pair(27, "Too many append to response objects: The maximum number of remote calls is 20."),
        Pair(28, "Invalid timezone: Please consult the documentation for a valid timezone."),
        Pair(29, "You must confirm this action: Please provide a confirm=true parameter."),
        Pair(30, "Invalid username and/or password: You did not provide a valid login."),
        Pair(31, "Account disabled: Your account is no longer active. Contact TMDB if this is an error."),
        Pair(32, "Email not verified: Your email address has not been verified."),
        Pair(33, "Invalid request token: The request token is either expired or invalid."),
        Pair(34, "The resource you requested could not be found."),
        Pair(35, "Invalid token."),
        Pair(36, "This token hasn't been granted write permission by the user."),
        Pair(37, "The requested session could not be found."),
        Pair(38, "You don't have permission to edit this resource."),
        Pair(39, "This resource is private."),
        Pair(40, "Nothing to update."),
        Pair(41, "This request token hasn't been approved by the user."),
        Pair(42, "This request method is not supported for this resource."),
        Pair(43, "Couldn't connect to the backend server."),
        Pair(44, "The ID is invalid."),
        Pair(45, "This user has been suspended."),
        Pair(46, "The API is undergoing maintenance. Try again later."),
        Pair(47, "The input is not valid."),
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

    fun postMovieRating(movieId: Int, rating: Float, sessionId: String) : Boolean {
        //TODO Propagate up the stack: Rating from 0.5 to 10 with step 0.5
        return if (rating in 0.5f..10.0f && (rating.mod(0.5f) == 0f)) {
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
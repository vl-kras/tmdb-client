package com.example.tmdbclient

import android.util.Log
import com.example.tmdbclient.TmdbBasePaths.API_BASE_PATH
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

interface TmdbAPi {

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
        @Body() body: CreateSessionRequestBody
    ): Call<CreateSessionResponseBody>

    @HTTP(method = "DELETE", path = "authentication/session", hasBody = true)
//    @DELETE("authentication/session")
    fun deleteSession(
        @Query("api_key") apiKey: String,
        @Body body: Any
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

    private val retrofit = Retrofit.Builder()
        .baseUrl(API_BASE_PATH)
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient.Builder().build())
        .build()

    private fun <T> buildService(service: Class<T>): T {
        return retrofit.create(service)
    }

    private val service = buildService(TmdbAPi::class.java)

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
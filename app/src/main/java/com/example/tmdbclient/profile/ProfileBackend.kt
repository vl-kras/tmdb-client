package com.example.tmdbclient.profile

import com.example.tmdbclient.*
import com.example.tmdbclient.shared.ServiceLocator
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.*
import java.io.IOException

// API endpoints interface for Retrofit
interface TmdbAccountApi {

    @GET("account")
    fun getAccountDetails(
        @Query("api_key") apiKey: String,
        @Query("session_id") sessionId: String
    ): Call<GetAccountDetails.ResponseBody>

    @GET("authentication/token/new")
    fun createRequestToken(
        @Query("api_key") apiKey: String
    ): Call<CreateRequestToken.ResponseBody>

    @POST("authentication/token/validate_with_login")
    fun validateTokenWithLogin(
        @Query("api_key") apiKey: String,
        @Body body: ValidateTokenWithLogin.RequestBody
    ): Call<ValidateTokenWithLogin.ResponseBody>

    @POST("authentication/session/new")
    fun createSession(
        @Query("api_key") apiKey: String,
        @Body body: CreateSession.RequestBody
    ): Call<CreateSession.ResponseBody>

    @HTTP(method = "DELETE", path = "authentication/session", hasBody = true)
    fun deleteSession(
        @Query("api_key") apiKey: String,
        @Body body: DeleteSession.RequestBody
    ): Call<DeleteSession.ResponseBody>
}

class ProfileBackend: ProfileRepository.ProfileBackendContract {

    private val apiKey = BuildConfig.TMDB_API_KEY
    private val service = ServiceLocator.retrofit.create(TmdbAccountApi::class.java)

    /* Methods required by repository */

    override fun signIn(username: String, password: String): ProfileRepository.UserSession {

        val token = createRequestToken().requestToken
        validateTokenWithLogin(username, password, token)
        val sessionId = createSession(token)
        val accountDetails = getAccountDetails(sessionId)

        return ProfileRepository.UserSession(
            userId = accountDetails.id,
            username = accountDetails.username,
            name = accountDetails.name,
            sessionId = sessionId
        )
    }

    override fun fetchAccountDetails(sessionId: String): ProfileRepository.UserSession {
        val accountDetails = getAccountDetails(sessionId)

        return ProfileRepository.UserSession(
            userId = accountDetails.id,
            username = accountDetails.username,
            name = accountDetails.name,
            sessionId = sessionId
        )
    }

    override fun signOut(sessionId: String) {
        deleteSession(sessionId)
    }

    /* API implementation methods */

    private fun getAccountDetails(sessionId: String): GetAccountDetails.ResponseBody {
        val request = service.getAccountDetails(apiKey, sessionId)
        val response = request.execute()

        return response.body()
            ?: throw NoSuchElementException("Could not fetch user account")
    }

    private fun createSession(requestToken: String): String {
        val requestBody = CreateSession.RequestBody(requestToken)
        val request = service.createSession(apiKey, requestBody)
        val response = request.execute()

        return response.body()?.sessionId
            ?: throw NoSuchElementException("Failed to create session")
    }

    private fun validateTokenWithLogin(
        username: String,
        password: String,
        token: String
    ): ValidateTokenWithLogin.ResponseBody {

        val requestBody = ValidateTokenWithLogin.RequestBody(username, password, token)
        val request = service.validateTokenWithLogin(apiKey, requestBody)
        val response = request.execute()

        return response.body() ?: throw IOException("Failed to validate token")
    }

    private fun createRequestToken() : CreateRequestToken.ResponseBody {

        val request = service.createRequestToken(apiKey = apiKey)
        val response = request.execute()

        return response.body() ?: throw IOException("Failed to create token")
    }

    private fun deleteSession(sessionId: String) : DeleteSession.ResponseBody {

        val requestBody = DeleteSession.RequestBody(sessionId)
        val request = service.deleteSession(apiKey, requestBody)
        val response = request.execute()

        return response.body() ?: throw IOException("Failed to delete session")
    }
}

/* data classes, grouped by API call */

object DeleteSession {

    data class ResponseBody(
        @SerializedName("success") val isSuccess: Boolean
    )

    data class RequestBody(
        @SerializedName("session_id") val sessionId: String
    )
}

object CreateRequestToken {

    data class ResponseBody(
        @SerializedName("success") val isSuccess: Boolean,
        @SerializedName("expires_at") val expiresAt : String,
        @SerializedName("request_token") val requestToken : String
    )
}

object ValidateTokenWithLogin {

    data class RequestBody(
        @SerializedName("username") val username: String,
        @SerializedName("password") val password : String,
        @SerializedName("request_token") val requestToken : String
    )

    data class ResponseBody(
        @SerializedName("success") val isSuccess: Boolean,
        @SerializedName("expires_at") val expiresAt : String,
        @SerializedName("request_token") val requestToken : String
    )
}

object CreateSession {

    data class RequestBody(
        @SerializedName("request_token") val requestToken : String
    )

    data class ResponseBody(
        @SerializedName("success") val isSuccess: Boolean,
        @SerializedName("session_id") val sessionId: String,
    )
}

object GetAccountDetails {

    data class ResponseBody(
        @SerializedName("id") val id: Int,
        @SerializedName("name") val name: String,
        @SerializedName("username") val username: String,
    )
}
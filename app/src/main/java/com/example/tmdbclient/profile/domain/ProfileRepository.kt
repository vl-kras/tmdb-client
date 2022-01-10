package com.example.tmdbclient.profile.domain

class ProfileRepository(private val backend: ProfileBackendContract) {

    interface ProfileBackendContract {
        fun signIn(username: String, password: String): UserSession
        fun signOut(sessionId: String)
        fun fetchAccountDetails(sessionId: String): UserSession
    }

    data class UserSession(
        val sessionId: String,
        val userId: Int,
        val username: String,
        val name: String
    )

    fun signIn(username: String, password: String): UserSession {
        return backend.signIn(username, password)
    }

    fun signOut(sessionId: String) {
        return backend.signOut(sessionId)
    }

    fun fetchAccountDetails(sessionId: String): UserSession {
        return backend.fetchAccountDetails(sessionId)
    }
}
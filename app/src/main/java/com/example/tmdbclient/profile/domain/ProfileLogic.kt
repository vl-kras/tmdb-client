package com.example.tmdbclient.profile.domain

class ProfileInteractor(private val dataSource: DataSource) {

    interface DataSource {
        fun signIn(username: String, password: String): Result<UserSession>
        fun signOut(sessionId: String): Result<Unit>
        fun fetchAccountDetails(sessionId: String): Result<UserSession>
    }

    fun signIn(username: String, password: String): Result<UserSession> {
        return dataSource.signIn(username, password)
    }

    fun signOut(sessionId: String): Result<Unit> {
        return dataSource.signOut(sessionId)
    }

    fun fetchAccountDetails(sessionId: String): Result<UserSession> {
        return dataSource.fetchAccountDetails(sessionId)
    }
}

data class UserSession(
    val sessionId: String,
    val userId: Int,
    val username: String,
    val name: String
)
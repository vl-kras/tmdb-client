package com.example.tmdbclient.tvshow.details

import com.example.tmdbclient.*
import com.example.tmdbclient.shared.ServiceLocator
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.*
import java.io.IOException

interface TmdbShowDetailsApi {

    @GET("tv/{tv_id}")
    fun getTvShowDetails(
        @Path("tv_id") tvShowId: Int,
        @Query("api_key") apiKey: String
    ): Call<GetTvShowDetails.ResponseBody>

    @POST("tv/{tv_id}/rating")
    @Headers("Content-Type:application/json;charset=utf-8")
    fun postTvShowRating(
        @Path("tv_id") showId: Int,
        @Query("api_key") apiKey: String,
        @Query("session_id") sessionId: String,
        @Body body: PostTvShowRating.RequestBody
    ): Call<PostTvShowRating.ResponseBody>

    @DELETE("tv/{tv_id}/rating")
    @Headers("Content-Type:application/json;charset=utf-8")
    fun deleteTvShowRating(
        @Path("tv_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("session_id") sessionId: String,
    ): Call<DeleteTvShowRating.ResponseBody>
}

class TvShowDetailsBackend: TvShowDetailsRepository.TvShowDetailsBackendContract {

    private val apiKey = BuildConfig.TMDB_API_KEY
    private val service = ServiceLocator.retrofit.create(TmdbShowDetailsApi::class.java)

    override fun fetchTvShowDetails(showId: Int): TvShowDetailsRepository.TvShowDetails {

        return getTvShowDetails(showId).let { tvShow ->
            TvShowDetailsRepository.TvShowDetails(
                title = tvShow.name,
                id = tvShow.id,
                posterPath = tvShow.posterPath ?: "",
                overview = tvShow.overview,
                tagline = tvShow.tagline,
                status = tvShow.status,
                userScore = tvShow.voteAverage.toFloat(),
                genres = tvShow.genres.map { it.name }
            )
        }
    }

    override fun rateTvShow(showId: Int, sessionId: String, rating: Float) {
        postTvShowRating(showId, rating, sessionId)
    }

    override fun removeTvShowRating(showId: Int, sessionId: String) {
        deleteTvShowRating(showId, sessionId)
    }

    private fun getTvShowDetails(tvShowId: Int): GetTvShowDetails.ResponseBody {

        val request = service.getTvShowDetails(tvShowId, apiKey)
        val response = request.execute()
        return response.body()
            ?: throw IOException("Failed to fetch show details")
    }

    private fun postTvShowRating(showId: Int, rating: Float, sessionId: String) {

        val requestBody = PostTvShowRating.RequestBody(rating)
        val request = service.postTvShowRating(
            showId, apiKey, sessionId, requestBody
        )
        val response = request.execute()

        if (!response.isSuccessful) {
            throw IOException("Failed to post movie rating")
        }
    }

    private fun deleteTvShowRating(showId: Int, sessionId: String) {

        val request = service.deleteTvShowRating(showId, apiKey, sessionId)
        val response = request.execute()

        if (!response.isSuccessful) {
            throw IOException("Failed to remove movie rating")
        }
    }
}

object GetTvShowDetails {

    data class ResponseBody(
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
        @SerializedName("networks") val networks : List<TvNetwork>,
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

        data class TvNetwork (
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
package com.example.tmdbclient.tvshow.list

import com.example.tmdbclient.BuildConfig
import com.example.tmdbclient.shared.ServiceLocator
import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException
import java.lang.reflect.Type

interface TmdbShowlistApi {

    @GET("tv/popular")
    fun getShowsPopular(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int? = null,
        @Query("language") language: String? = null
    ): Call<GetPopularShows.ResponseBody>
}

class TvShowListBackend: TvShowListRepository.TvShowListBackendContract {

    private val apiKey = BuildConfig.TMDB_API_KEY
    private val service = ServiceLocator.retrofit.create(TmdbShowlistApi::class.java)

    override fun fetchPopularShows(page: Int): List<TvShowListRepository.TvShow> {

        return getPopularTvShowsByPage(page).map {
            TvShowListRepository.TvShow(
                id = it.id,
                title = it.name,
                posterPath = it.posterPath ?: ""
            )
        }
    }

    private fun getPopularTvShowsByPage(page: Int) : List<GetPopularShows.ResponseBody.TvShowInfo> {

        val request = service.getShowsPopular(apiKey, page)
        val response = request.execute()
        return response.body()?.shows
            ?: throw IOException("Failed to fetch popular shows")
    }
}

object GetPopularShows {

    data class ResponseBody (
        @SerializedName("page") val page : Int,
        @SerializedName("results") val shows : List<TvShowInfo>,
        @SerializedName("total_results") val totalResults : Int,
        @SerializedName("total_pages") val totalPages : Int
    ) {

        data class TvShowInfo (
            @SerializedName("poster_path") val posterPath : String?,
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
    }
}
package com.example.tmdbclient.shared

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Converter
import java.io.IOException

data class ApiError(
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("status_message")val statusMessage: String
): IOException(statusMessage) {

    companion object {

        fun from(errorBody: ResponseBody): ApiError {

            val converter: Converter<ResponseBody, ApiError> =
                ServiceLocator.retrofit.responseBodyConverter(
                    ApiError::class.java,
                    arrayOfNulls<Annotation>(0)
                )

            return converter.convert(errorBody)
                ?: throw Exception("Could not parse API error")
        }
    }
}

package com.example.tmdbclient.shared

import okhttp3.ResponseBody
import retrofit2.Converter
import java.io.IOException

data class ApiError(
    val statusCode: Int,
    val statusMessage: String
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

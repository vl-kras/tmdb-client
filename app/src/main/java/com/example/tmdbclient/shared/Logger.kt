package com.example.tmdbclient.shared

import android.content.Context
import android.util.Log
import androidx.work.*

object Logs {

    interface Logger {
        fun log(message: String)
    }

    private var loggers: MutableList<Logger> = mutableListOf()

    fun addLogger(logger: Logger) = loggers.add(logger)

    fun log(message: String) {
        loggers.forEach {
            it.log(message)
        }
    }
}

class LocalLogger(private val appContext: Context): Logs.Logger {

    override fun log(message: String) {

        val errorData: Data = Data.Builder()
            .putString(MESSAGE_TAG, message)
            .build()

        val uploadWorkRequest: WorkRequest =
            OneTimeWorkRequestBuilder<LoggerWorker>()
                .setInputData(errorData)
                .build()

        WorkManager
            .getInstance(appContext)
            .enqueue(uploadWorkRequest)
    }

    class LoggerWorker(
        appContext: Context, workerParams: WorkerParameters
    ) : Worker(appContext, workerParams) {

        override fun doWork(): Result {

            // Do the work here
            val message = inputData.getString(MESSAGE_TAG) ?: "Log message is empty!"
            Log.i(MESSAGE_TAG, message)

            // Indicate whether the work finished successfully with the Result
            return Result.success()
        }
    }

    companion object {
        const val MESSAGE_TAG = "message"
    }
}
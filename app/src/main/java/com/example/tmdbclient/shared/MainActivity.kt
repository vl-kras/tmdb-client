package com.example.tmdbclient.shared

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import com.example.tmdbclient.shared.theme.MyApplicationTheme


class MainActivity : AppCompatActivity() {

    //TODO de-hardcode all string values

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activityContext = this
        val composeView = ComposeView(activityContext).apply {
            setContent {
                MyApplicationTheme {
                    MainScreen(context = activityContext)
                }
            }
        }

        setContentView(composeView)
    }
}
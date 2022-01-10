package com.example.tmdbclient.shared

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import com.example.tmdbclient.shared.theme.MyApplicationTheme


class MainActivity : AppCompatActivity() {

    //TODO implement toolBar with navigateUp
    //TODO implement error handling in retrofit calls (200,300,400,500 codes into Result)
    //TODO de-hardcode all string values
    //TODO add FAB to screen scaffolds, implement onNavigateApp

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

//    override fun onSupportNavigateUp(): Boolean {
//        return findNavController(R.id.fragment_container).navigateUp()
//    }
}
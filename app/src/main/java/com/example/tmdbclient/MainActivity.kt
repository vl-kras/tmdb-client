package com.example.tmdbclient

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
        val bottomNavView = findViewById<BottomNavigationView>(R.id.navView)

        val navController = navFragment?.findNavController()
            ?: throw NoSuchElementException("Could not find NavController")
        val configuration = AppBarConfiguration(
            setOf(R.id.profileFragment, R.id.movieFragment, R.id.tvFragment)
        )

        setupActionBarWithNavController(navController, configuration)
        bottomNavView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {

        return findNavController(R.id.fragmentContainerView).navigateUp()
    }
}
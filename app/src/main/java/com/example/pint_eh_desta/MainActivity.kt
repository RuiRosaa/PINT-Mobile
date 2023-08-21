package com.example.pint_eh_desta

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import app.navdrawer.R
import app.navdrawer.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {







    //override val defaultViewModelProviderFactory: ViewModelProvider.Factory
    //  get() = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    private lateinit var binding: ActivityMainBinding
    private var isLoginSuccessful = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Verificar o estado do login

        val navView: BottomNavigationView = binding.lownavView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    /*override fun onStart() {
        super.onStart()
        isLoginSuccessful = checkLoginStatus()

        if (!isLoginSuccessful) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }*/
    private fun checkLoginStatus(): Boolean {
        return isLoginSuccessful
    }
}
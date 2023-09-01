package com.hardtinsa

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.hardtinsa.R
import com.instamobile.kotlinlogin.R
import com.instamobile.kotlinlogin.databinding.ActivityMainBinding


class NavBottom : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
       setContentView(R.layout.bottom_nav)

    }



}
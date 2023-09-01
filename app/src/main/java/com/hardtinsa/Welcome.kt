package com.hardtinsa


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.hardtinsa.R

class Welcome : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val handler = Handler()
        handler.postDelayed({
            val intent = Intent(this, MainPage::class.java)
            intent.putExtra("autenticado", false)
            startActivity(intent)
            finish()
        }, 1500)
    }
}
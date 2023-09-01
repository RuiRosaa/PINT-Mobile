package com.hardtinsa

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.hardtinsa.R
import org.json.JSONObject

class VerifyEmail : AppCompatActivity() {

    private lateinit var loginError1: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify)

        loginError1 = findViewById(R.id.loginError)

        val btncancel = findViewById<Button>(R.id.buttonCancelar)
        btncancel.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("autenticado", false)
            startActivity(intent)
        }

        val btnverify = findViewById<Button>(R.id.buttonVerify)
        val emailEditText = findViewById<EditText>(R.id.etEmail)

        btnverify.setOnClickListener {
            val email = emailEditText.text.toString()
            if (email.isEmpty()) {
                loginError1.visibility = View.VISIBLE
                loginError1.text = "Por favor insira um Email"
            } else {
                loginError1.visibility = View.GONE
                if (isInternetConnected()) {
                    recuperarPassword(email)
                } else {
                    Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun recuperarPassword(email: String) {
        val apiurl = Globals.apiurl
        val url = apiurl + "utilizador/recuperar"

        val requestQueue = Volley.newRequestQueue(this)
        val requestBody = JSONObject().apply {
            put("EMAIL", email)
        }
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response ->
                val successMessage = "Verifique o seu email para gerar a nova palavra passe!"
                Log.d("Response", response.toString())
                val spannableTitle = SpannableString("Success")
                val titleColorSpan = ForegroundColorSpan(Color.GREEN)
                spannableTitle.setSpan(titleColorSpan, 0, spannableTitle.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                val builder = AlertDialog.Builder(this)
                builder.setTitle("Pedido Enviado")
                    .setMessage(successMessage)
                    .setPositiveButton("OK") { dialog, _ ->
                        // Navigate to MainActivity
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("autenticado", false)
                        startActivity(intent)
                        dialog.dismiss()
                    }
                    .show()
            },
            { error ->
                Log.e("Error", error.toString())
                // Handle the error
            }
        )
        requestQueue.add(jsonObjectRequest)
    }

    protected fun isInternetConnected(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(network) ?: return false

            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo?.isConnected ?: false
        }
    }
}
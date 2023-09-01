package com.hardtinsa

//import com.instamobile.kotlinlogin.R
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

class SignUP : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)


        val nomeEditText = findViewById<EditText>(R.id.suUsername)
        val emailEditText = findViewById<EditText>(R.id.suEmail)
        val passwordEditText = findViewById<EditText>(R.id.suPassword)
        val confirmPasswordEditText = findViewById<EditText>(R.id.suPassword2)

        val btncancel = findViewById<Button>(R.id.buttonCancel)
        btncancel.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("autenticado", false)
            startActivity(intent)
            Log.d("MainActivity", "Button clicked!")
        }

        val btnreg = findViewById<Button>(R.id.buttonRegister)
        btnreg.setOnClickListener {
            val nome = nomeEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (isInternetConnected()) {
                if (password == confirmPassword) {
                    UtilizadorInserirApi(nome, email, password)
                } else {
                    val errorText = findViewById<TextView>(R.id.errorText)
                    errorText.text = "As senhas não coincidem"
                    errorText.visibility = View.VISIBLE
                }
            } else {
                Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun UtilizadorInserirApi(nome: String, email: String, password: String) {
        val apiurl = Globals.apiurl
        val url = apiurl + "utilizador/inserir"

        val params = JSONObject()
        params.put("NOME", nome)
        params.put("EMAIL", email)
        params.put("PASSWD", password)

        val requestQueue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, params,
            { response ->
                // Handle the response
                Log.d("Response", response.toString())
                val successMessage = "Verifique o seu email para ativar a sua conta!!"
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
                // Handle the error
                Log.e("Error", error.toString())
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
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
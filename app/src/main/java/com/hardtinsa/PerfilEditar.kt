package com.hardtinsa

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.hardtinsa.R
import com.google.android.material.bottomnavigation.BottomNavigationView

//import com.instamobile.kotlinlogin.R
//import org.joda.time.DateTime
//import org.joda.time.format.DateTimeFormat
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

class PerfilEditar : BaseActivity() {

    private lateinit var etNome: EditText
    private lateinit var etNascimento: EditText
    private lateinit var etLinkFoto: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_editar)

        etNome = findViewById(R.id.et_nome)
        etNascimento = findViewById(R.id.et_nova)
        etLinkFoto = findViewById(R.id.et_confirmar)


        val internetStatusImageView = findViewById<ImageView>(R.id.internet_status)
        updateInternetStatusIcon(internetStatusImageView)

        val nome = Globals.username

        val linkFoto = Globals.linkfoto

        val nascimento = Globals.dtnascimento
        Log.d("PerfilActivity", "Data de Nascimento: $nascimento")
        etNome.setText(nome)

        etLinkFoto.setText(linkFoto)
        val formattedNascimento = formatNascimento(nascimento)
        etNascimento.setText(formattedNascimento)

        val back = findViewById<Button>(R.id.back_button)
        back.setOnClickListener {
            val intent = Intent(this, PerfilPreview::class.java)
            startActivity(intent)
        }
        btnSave = findViewById(R.id.btn_save1)
            btnSave.setOnClickListener {
                if (isInternetConnected()) {
                    editarPerfil()
                } else {
                    Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_SHORT).show()
                }
            }

        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener { menuItem ->
            handleNavigationItemSelected(menuItem) // Call the overridden function
            drawerLayout.closeDrawer(GravityCompat.START) // Close the navigation drawer after item selection
            true
        }
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            handleBottomNavigationItemSelected(menuItem) // Call the function from BaseActivity
            true

        }

    }
    fun editarPerfil() {

        val nome = etNome.text.toString().trim()
        val nascimento = etNascimento.text.toString().trim()
        val linkFoto = etLinkFoto.text.toString().trim()

        val token = Globals.token
        val idUser = Globals.userID
        val apiurl = Globals.apiurl
        val url = apiurl + "utilizador/editardados"

        val nascimentoDateTime = formatNascimentoToDateTime(nascimento)
        val requestData = JSONObject().apply {
        put("TK", token)
        put("IDUSER", idUser)
        put("NOME", nome)
        put("DTNASCIMENTO", nascimentoDateTime)
        put("LINKFOTO", linkFoto)
        }
        val queue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.PUT, url, requestData,
            { response ->

                val success = response.getBoolean("success")

                if (success) {
                    val tvSuccess = findViewById<TextView>(R.id.tv_success1)
                    tvSuccess.text = "Perfil Atualizado"
                    tvSuccess.visibility = View.VISIBLE
                    Log.d("Success", "Perfil modificado")

                } else {

                }
            },
            { error ->

                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(jsonObjectRequest)
    }
    private fun formatNascimento(nascimento: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(nascimento)
        return outputFormat.format(date)
    }
    private fun formatNascimentoToDateTime(nascimento: String): String {
        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val outputFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val date = inputFormat.parse(nascimento)
        val dateTime = DateTime(date)
        return dateTime.toString(outputFormat)
    }

}
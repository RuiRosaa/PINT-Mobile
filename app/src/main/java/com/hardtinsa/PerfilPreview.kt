package com.hardtinsa

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.sft4all.BeneficiosActivity
import com.sft4all.CalendarioActivity
import com.sft4all.ClientesActivity
import com.instamobile.kotlinlogin.R
import org.json.JSONException

class PerfilPreview : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_preview)

        val token = Globals.token
        val userId = Globals.userID

        fetchUserDetails(token, userId)

        val perfil = findViewById<Button>(R.id.btn_editar)
        perfil.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)
        }

        val perfileditar = findViewById<Button>(R.id.btn_dados)
        perfileditar.setOnClickListener {

            val intent = Intent(this, PerfilEditar::class.java)
            startActivity(intent)
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

        val internetStatusImageView = findViewById<ImageView>(R.id.internet_status)
        updateInternetStatusIcon(internetStatusImageView)


        val globalUsername = Globals.username
        val labeluserID = findViewById<TextView>(R.id.name)
        labeluserID.text = globalUsername

        val globalEmail = Globals.email
        val labelemail = findViewById<TextView>(R.id.email)
        labelemail.text = globalEmail

        val globalfunc = Globals.funcionalidade
        val labelnum = findViewById<TextView>(R.id.funcionalidade)
        labelnum.text = globalfunc.toString()

        val globalcarg = Globals.cargo
        val labelcargo = findViewById<TextView>(R.id.qualifications1)
        labelcargo.text = globalcarg
    }

    private fun fetchUserDetails(token: String, userId: String) {
        val apiurl = Globals.apiurl
        val url = apiurl + "utilizador/detalhe/$token/$userId"

        val requestQueue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        val data = response.getJSONArray("data")
                        if (data.length() > 0) {
                            val jsonObject = data.getJSONObject(0)
                            val dtNascimento = jsonObject.getString("DTNASCIMENTO")

                            Globals.dtnascimento = dtNascimento
                            Log.d("User Details", "DTNASCIMENTO: $dtNascimento")
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                Log.e("User Details Error", "Error retrieving user details: ${error.message}")
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

}
/*
    private fun saveFields() {
        val username = etUsername.text.toString()
        val phoneContact = etPhoneContact.text.toString()
        val qualifications = etQualifications.text.toString()
        val email = etEmail.text.toString()

        // TODO: Guardar os campos depois de clicar no botão para a base de Dados
    }
*/


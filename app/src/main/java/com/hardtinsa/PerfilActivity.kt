package com.hardtinsa

//import com.instamobile.kotlinlogin.R
import android.content.Intent
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
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.hardtinsa.R
import org.json.JSONObject

class PerfilActivity : BaseActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPhoneContact: EditText
    private lateinit var etQualifications: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnSave: Button
    private lateinit var btnSendData: Button
    private lateinit var ed_nova: EditText
    private lateinit var ed_atual: EditText
    private lateinit var ed_confirmar: EditText
    private lateinit var btnAdd: Button
    private lateinit var btn_Back: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

         val alteraPassProx = Globals.alterapassprox

        ed_atual = findViewById(R.id.et_atual)
        ed_nova = findViewById(R.id.et_nova)
        ed_confirmar = findViewById(R.id.et_confirmar)

        btn_Back = findViewById(R.id.back_button)
        btn_Back.setOnClickListener {
            if (!alteraPassProx) {
                val intent = Intent(this, MainPage::class.java)
                startActivity(intent)
            }
        }
       val  btnAdd = findViewById<Button>(R.id.btn_save)
        btnAdd.setOnClickListener {
            if (isInternetConnected()) {
            alterarPasswd()
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
            handleNavigationItemSelected(menuItem)
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        val internetStatusImageView = findViewById<ImageView>(R.id.internet_status)
        updateInternetStatusIcon(internetStatusImageView)

         bottomNavigationView = findViewById(R.id.bottomNavigationView)

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            handleBottomNavigationItemSelected(menuItem)
            true
        }
        // Desabilita o botao se o user for conta nova
        if (alteraPassProx) {
            btn_Back.isEnabled = false
            val navMenu = navView.menu
            for (i in 0 until navMenu.size()) {
                navMenu.getItem(i).isEnabled = false
            }
            val menu = bottomNavigationView.menu
            for (i in 0 until menu.size()) {
                menu.getItem(i).isEnabled = false
            }
        }
    }
    private fun alterarPasswd() {

        val token = Globals.token
        val userID = Globals.userID
        val oldPassword = ed_atual.text.toString()
        val newPassword = ed_nova.text.toString()
        val confirmPassword = ed_confirmar.text.toString()

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Por favor preencha os campos", Toast.LENGTH_SHORT).show()
            return
        }
        if (oldPassword != Globals.password) {
            Toast.makeText(this, "Password atual incorreta", Toast.LENGTH_SHORT).show()
            return
        }
        if (newPassword != confirmPassword) {
            Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
            return
        }

        val requestData = JSONObject().apply {
            put("TK", token)
            put("IDUSER", userID)
            put("PASSWDOLD", oldPassword)
            put("PASSWD", newPassword)
        }

        val queue = Volley.newRequestQueue(this)
        val apiurl = Globals.apiurl
        val url = apiurl + "utilizador/mudapass"
        val request = JsonObjectRequest(
            Request.Method.POST, url, requestData,
            { response ->
                val tvSuccess = findViewById<TextView>(R.id.tv_success)
                tvSuccess.text = "Password Alterada com Sucesso"
                tvSuccess.visibility = View.VISIBLE
                Log.d("Success", "Password modificada")
                Toast.makeText(this, "Password modificada", Toast.LENGTH_SHORT).show()
                btn_Back.isEnabled = true
                val navMenu = navView.menu
                for (i in 0 until navMenu.size()) {
                    navMenu.getItem(i).isEnabled = true
                }
                val menu = bottomNavigationView.menu
                for (i in 0 until menu.size()) {
                    menu.getItem(i).isEnabled = true
                }

            },
            { error ->
                Log.e("Error", "Password request falhou: ${error.message}")
                Toast.makeText(this, "Password change failed. Please try again.", Toast.LENGTH_SHORT).show()
                btn_Back.isEnabled = true
                val navMenu = navView.menu
                for (i in 0 until navMenu.size()) {
                    navMenu.getItem(i).isEnabled = true
                }
                val menu = bottomNavigationView.menu
                for (i in 0 until menu.size()) {
                    menu.getItem(i).isEnabled = true
                }
            }
        )

        // Add the request to the request queue
        queue.add(request)
    }
}



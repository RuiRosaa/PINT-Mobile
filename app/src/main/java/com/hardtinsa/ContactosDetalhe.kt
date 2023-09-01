package com.hardtinsa

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.instamobile.kotlinlogin.R

class ContactosDetalhe : BaseActivity() {

    private lateinit var tvnome: TextView
    private lateinit var tvmorada: TextView
    private lateinit var tvfuncao: TextView
    private lateinit var tvtelefone: TextView
    private lateinit var tvcliente: TextView
    private lateinit var tvemail: TextView
    private lateinit var tvphone: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contactos_detalhe)

        tvnome = findViewById(R.id.tv_nome)
        tvmorada = findViewById(R.id.tv_morada)
        tvfuncao = findViewById(R.id.tv_funcao)
        tvtelefone = findViewById(R.id.tv_telefone)
        tvcliente = findViewById(R.id.tv_cliente)
        tvemail = findViewById(R.id.tv_email)
        tvphone = findViewById(R.id.tv_phone)

        val internetStatusImageView = findViewById<ImageView>(R.id.internet_status)
        updateInternetStatusIcon(internetStatusImageView)

        // Retrieve the data passed from the previous activity
        val nome = intent.getStringExtra("nome")
        val morada = intent.getStringExtra("morada")
        val funcao = intent.getStringExtra("funcao")
        val telefone = intent.getStringExtra("telefone")
        val telemovel = intent.getStringExtra("telemovel")
        val email = intent.getStringExtra("email")
        val idcliente = intent.getIntExtra("idcliente", 0)


        // Set the data to the TextViews
        tvnome.text = "Nome: $nome"
        tvmorada.text = "Morada: $morada"
        tvfuncao.text = "Função: $funcao"
        tvtelefone.text = "Telefone: $telefone"
        tvcliente.text = "IDCliente: $idcliente"
        tvemail.text = "Email: $email"
        tvphone.text = "Telemovel: $telemovel"

        val backButton = findViewById<Button>(R.id.btn_back)
        backButton.setOnClickListener {
            val intent = Intent(this, ContactosActivity::class.java)
            startActivity(intent)
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
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            handleBottomNavigationItemSelected(menuItem)
            true

        }

    }


}



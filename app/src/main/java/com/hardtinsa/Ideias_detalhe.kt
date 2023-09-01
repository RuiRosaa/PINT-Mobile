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

class Ideias_detalhe : BaseActivity() {

    private lateinit var tvDescricao: TextView
    private lateinit var tvDetalhe: TextView
    private lateinit var tvEstado: TextView
    private lateinit var tvCriadodata: TextView
    private lateinit var tvTipo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ideias_detalhe)

        tvDescricao = findViewById(R.id.tv_descricao)
        tvDetalhe = findViewById(R.id.tv_detalhe)
        tvEstado = findViewById(R.id.tv_estado)
        tvCriadodata = findViewById(R.id.tv_criadodata)
        tvTipo = findViewById(R.id.tv_tipo)

        val internetStatusImageView = findViewById<ImageView>(R.id.internet_status)
        updateInternetStatusIcon(internetStatusImageView)

        // Retrieve the data passed from the previous activity
        val descricao = intent.getStringExtra("descricao")
        val detalhe = intent.getStringExtra("detalhe")
        val estado = intent.getStringExtra("estado")
        val criadodata = intent.getStringExtra("criadodata")
        val idtipo = intent.getIntExtra("idtipo", 0)

                 // Atribuição dos estados do server
        val estadoText = when (estado) {
            "0" -> "Não Ativo"
            "1" -> "Ativo/Criado"
            "2" -> "Em Curso"
            "5" -> "Em Análise"
            "8" -> "Concluído"
            "10" -> "Arquivado"
            "99" -> "Eliminado"
            else -> "N/A"
        }
        val idtipoText = when (idtipo) {
            1 -> "Generico"
            2 -> "Software"
            3 -> "Consultoria"
            4 -> "Redes"
            5 -> "Financeiro"
            else -> "N/A"
        }

        // Set the data to the TextViews
        tvDescricao.text = "Descrição: $descricao"
        tvDetalhe.text = "Detalhe: $detalhe"
        tvEstado.text = "Estado: $estadoText"
        tvCriadodata.text = "Criado em: $criadodata"
        tvTipo.text = "Área de Negócio: $idtipoText"

        val backButton = findViewById<Button>(R.id.btn_back)
        backButton.setOnClickListener {
            val intent = Intent(this, IdeiasActivity::class.java)
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



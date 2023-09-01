package com.hardtinsa

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.example.hardtinsa.R
import com.google.android.material.bottomnavigation.BottomNavigationView
/*import com.instamobile.kotlinlogin.R*/

class ClientesDetalhes : BaseActivity() {

    private lateinit var tvDescricao: TextView
    private lateinit var tvDetalhe: TextView
    private lateinit var tvTipoPJ: TextView
    private lateinit var tvEstado: TextView
    private lateinit var tvValorprev: TextView
    private lateinit var tvCriadodata: TextView
    private lateinit var tvEstado1: TextView
    private lateinit var tv_Cliente: TextView
    private lateinit var tv_telemovel: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clientes_detalhes)

        tvDescricao = findViewById(R.id.tv_nome)
        tvDetalhe = findViewById(R.id.tv_email)
        tvEstado = findViewById(R.id.tv_estado1)
        tvCriadodata = findViewById(R.id.tv_criadodata)
        tvEstado1 = findViewById(R.id.tv_cliente)
        tv_Cliente = findViewById(R.id.tv_estado)
        tv_telemovel = findViewById(R.id.tv_valorprev)


        val internetStatusImageView = findViewById<ImageView>(R.id.internet_status)
        updateInternetStatusIcon(internetStatusImageView)

        val nome = intent.getStringExtra("nome")
        val email = intent.getStringExtra("email")
        val estado = intent.getIntExtra("estado", 0)
        val criadodata = intent.getStringExtra("criadodata")
        val nif = intent.getIntExtra("nif", 0)
        val morada = intent.getStringExtra("morada")
        val telemovel = intent.getStringExtra("telemovel")

        val estadoText = when (estado) {
            0 -> "Não Ativo"
            1 -> "Ativo/Criado"
            2 -> "Em Curso"
            5 -> "Em Análise"
            8 -> "Concluído"
            10 -> "Arquivado"
            99 -> "Eliminado"
            else -> "N/A"
        }

        tvDescricao.text = "Descrição: $nome"
        tvDetalhe.text = "Email: $email"
        tvEstado.text = "Estado: $estadoText"
        tvCriadodata.text = "Criadodata: $criadodata"
        tvEstado1.text = "Nif: $nif"
        tv_Cliente.text = "Morada: $morada"
        tv_telemovel.text = "Telemóvel: $telemovel"

        val back = findViewById<Button>(R.id.btn_back)
        back.setOnClickListener {
            val intent = Intent(this, ClientesActivity::class.java)
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
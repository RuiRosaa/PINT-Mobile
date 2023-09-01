package com.hardtinsa

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.hardtinsa.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.hardtinsa.BeneficiosActivity
import com.hardtinsa.CalendarioActivity
import com.hardtinsa.ClientesActivity
/*import com.instamobile.kotlinlogin.R*/

class VerNotificacoes : BaseActivity() {

    private lateinit var tvDescricao: TextView
    private lateinit var tvEstado: TextView
    private lateinit var tvDataInicio: TextView
    private lateinit var tvDataFim: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ver_notificacoes)

        tvDescricao = findViewById(R.id.tv_descricao)
        tvEstado = findViewById(R.id.tv_estado)
        tvDataInicio = findViewById(R.id.tv_datainicio)
        tvDataFim = findViewById(R.id.tv_datafim)

        val back = findViewById<Button>(R.id.back_button)
        back.setOnClickListener {
            val intent = Intent(this, NotificacoesActivity::class.java)
            startActivity(intent)
        }

        // Retrieve the data passed from the previous activity
        val descricao = intent.getStringExtra("descricao")
        val estado = intent.getStringExtra("estado")
        val data_inicio = intent.getStringExtra("data_inicio")
        val data_fim = intent.getStringExtra("data_inicio")

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
        // Set the data to the TextViews
        tvDescricao.text = "Descrição: $descricao"
        tvEstado.text = "Estado: $estadoText"
        tvDataInicio.text = "Data Inicio: $data_inicio"
        tvDataFim.text = "Data Fim: $data_fim"

        drawerLayout = findViewById(R.id.drawerLayout)
        val navView: NavigationView = findViewById(R.id.nav_view)

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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)) {

            return true
        }

        return super.onOptionsItemSelected(item)


    }


}

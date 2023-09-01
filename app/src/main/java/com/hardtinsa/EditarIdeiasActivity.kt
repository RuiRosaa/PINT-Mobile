package com.hardtinsa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.instamobile.kotlinlogin.R

class EditarIdeiasActivity : BaseActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var btnUpdate: Button
    private lateinit var ed_descricao: EditText
    private lateinit var ed_detalhe: EditText
    private lateinit var ed_tipo: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_ideias)

        dbHelper = DatabaseHelper(this)

        val button1 = findViewById<Button>(R.id.back_button)
        button1.setOnClickListener {
            val intent = Intent(this, IdeiasActivity::class.java)
            startActivity(intent)
        }

        val ideaId = intent.getIntExtra("idea_id", -1)
        val desc = intent.getStringExtra("descricao")
        val detalhe = intent.getStringExtra("detalhe")
        val idtipo = intent.getIntExtra("idtipo", 0)


        val internetStatusImageView = findViewById<ImageView>(R.id.internet_status)
        updateInternetStatusIcon(internetStatusImageView)

        ed_descricao = findViewById(R.id.et_descricao)
        ed_detalhe = findViewById(R.id.et_detalhe)
        btnUpdate = findViewById(R.id.btn_save)
        ed_tipo = findViewById(R.id.spinner_tipo)

        ed_descricao.setText(desc)
        ed_detalhe.setText(detalhe)
        // Set the selection based on the idtipo value
        val tipoArray = resources.getStringArray(R.array.tipo_options)
        if (idtipo in 1 until tipoArray.size) {
            ed_tipo.setSelection(idtipo)
        }

        btnUpdate.setOnClickListener {
            val descricao = ed_descricao.text.toString()
            val detalhe1 = ed_detalhe.text.toString()
            val estadoreg = 2
            val idTipo = ed_tipo.selectedItemPosition + 1

            Log.d("EditarIdeiasActivity", "Descricao: $descricao, Detalhe: $detalhe1, Tipo: $idTipo")

            if (descricao.isNotEmpty() && detalhe1.isNotEmpty()) {
                val result = dbHelper.atualizaIdeia(ideaId, descricao, detalhe1, estadoreg, idTipo)
                Log.d("EditarIdeiasActivity", "Update result: $result")
                if (result > 0) {
                    Toast.makeText(this, "Ideia atualizada com sucesso", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, IdeiasActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Erro ao atualizar Ideia", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
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

}
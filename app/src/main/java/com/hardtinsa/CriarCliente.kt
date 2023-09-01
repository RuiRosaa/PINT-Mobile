package com.hardtinsa

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.instamobile.kotlinlogin.R

class CriarCliente : BaseActivity() {
    private lateinit var btnAdd: Button
    private lateinit var db: SQLiteDatabase
    private lateinit var ed_nome: EditText
    private lateinit var ed_nif: EditText
    private lateinit var ed_morada: EditText
    private lateinit var ed_codpostal: EditText
    private lateinit var ed_localidade: EditText
    private lateinit var ed_email: EditText
    private lateinit var ed_telefone: EditText
    private lateinit var ed_telemovel: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_criar_cliente)

        db = BaseDeDados(this).writableDatabase

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

        ed_nome = findViewById(R.id.et_nome)
        ed_nif = findViewById(R.id.et_NIF)
        ed_morada = findViewById(R.id.et_morada)
        ed_codpostal = findViewById(R.id.et_codpostal)
        ed_localidade = findViewById(R.id.et_localidade)
        ed_email = findViewById(R.id.et_email)
        ed_telefone = findViewById(R.id.et_telefone)
        ed_telemovel = findViewById(R.id.et_telemovel)


        val button1 = findViewById<Button>(R.id.back_button)
        button1.setOnClickListener {
            val intent = Intent(this, ClientesActivity::class.java)
            startActivity(intent)
        }

        btnAdd = findViewById(R.id.btn_criar)
        btnAdd.setOnClickListener {
            val nome = ed_nome.text.toString()
            val nif = ed_nif.text.toString()
            val morada = ed_morada.text.toString()
            val codpostal = ed_codpostal.text.toString()
            val localidade = ed_localidade.text.toString()
            val email = ed_email.text.toString()
            val telefone = ed_telefone.text.toString()
            val telemovel = ed_telemovel.text.toString()
            val estado = 1
            val estadoreg = 1

            if ((telefone.isNotEmpty()  && nome.isNotEmpty()) || (telemovel.isNotEmpty() && nome.isNotEmpty())) {
                val values = ContentValues()
                values.put("nome", nome)
                values.put("nif", nif)
                values.put("morada", morada)
                values.put("codpostal", codpostal)
                values.put("localidade", localidade )
                values.put("email", email)
                values.put("estado", estado)
                values.put("telefone", telefone)
                values.put("telemovel", telemovel)
                values.put("estadoreg", estadoreg)

                Log.d("DadosCliente", "Nome: $nome")
                Log.d("DadosCliente", "NIF: $nif")
                Log.d("DadosCliente", "Morada: $morada")
                Log.d("DadosCliente", "Código Postal: $codpostal")
                Log.d("DadosCliente", "Localidade: $localidade")
                Log.d("DadosCliente", "Email: $email")
                Log.d("DadosCliente", "Telefone: $telefone")
                Log.d("DadosCliente", "Telemóvel: $telemovel")
                Log.d("DadosCliente", "Estado Reg: $estadoreg")


                val result = db.insert("Cliente", null, values)

                if (result > 0) {
                    Toast.makeText(this, "Sucesso", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, ClientesActivity::class.java).apply {
                        putExtra("nome", nome)
                        putExtra("nif", nif)
                        putExtra("morada", morada)
                        putExtra("codpostal", codpostal)
                        putExtra("localidade", localidade)
                        putExtra("email", email)
                        putExtra("telefone", telefone)
                        putExtra("telemovel", telemovel)
                        putExtra("estadoreg", estadoreg)
                    }
                    startActivity(intent)
                    finish()

                } else {
                    Toast.makeText(this, "Falha", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Escreva os dados necessarios", Toast.LENGTH_SHORT).show()
            }
        }

    }
}
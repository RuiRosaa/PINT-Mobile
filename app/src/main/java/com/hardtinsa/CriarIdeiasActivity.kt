package com.hardtinsa

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import java.util.Date
import java.text.SimpleDateFormat



class CriarIdeiasActivity : BaseActivity() {

    private lateinit var db: SQLiteDatabase
    private lateinit var btnAdd: Button
    private lateinit var ed_descricao: EditText
    private lateinit var ed_detalhe: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_criar_ideias)

        db = BaseDeDados(this).writableDatabase

        ed_descricao = findViewById(R.id.et_descricao)
        ed_detalhe = findViewById(R.id.et_detalhe)
        btnAdd = findViewById(R.id.btn_criar)
        val spinnerTipo = findViewById<Spinner>(R.id.spinner_tipo)


        val button1 = findViewById<Button>(R.id.back_button)
        button1.setOnClickListener {
            val intent = Intent(this, IdeiasActivity::class.java)
            startActivity(intent)
        }

        btnAdd.setOnClickListener {
            val descricao = ed_descricao.text.toString()
            val detalhe = ed_detalhe.text.toString()
            val criadodata = getCurrentDate()
            val estado = "0"
            val estadoreg = 1
            val userID = Globals.userID
            val idtipo: Int

            val selectedTipo = spinnerTipo.selectedItem.toString().lowercase()

            idtipo = when (selectedTipo) {
                "generico" -> 1
                "software" -> 2
                "consultoria" -> 3
                "redes" -> 4
                "financiero" -> 5
                else -> 0
            }
            Log.d("SelectedTipo", "Selected Tipo: $selectedTipo")
            Log.d("IDTipo", "IDTipo: $idtipo")

            if (descricao.isNotEmpty() && detalhe.isNotEmpty()&& idtipo != 0) {
                val values = ContentValues()
                values.put("iduser", userID)
                values.put("idtipo", idtipo)
                values.put("descricao", descricao)
                values.put("detalhe", detalhe)
                values.put("estado", estado)
                values.put("estadoreg", estadoreg)
                values.put("criadodata", criadodata)

                val result = db.insert("Ideia", null, values)
                if (result > 0) {
                    Toast.makeText(this, "Sucesso", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, IdeiasActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    Toast.makeText(this, "Falha", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Escreve a descricao e detalhe. Selected Tipo: $selectedTipo, IDTipo: $idtipo", Toast.LENGTH_SHORT).show()
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
        val internetStatusImageView = findViewById<ImageView>(R.id.internet_status)
        updateInternetStatusIcon(internetStatusImageView)

    }



    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd") // Choose the desired date format
        val currentDate = Date()
        return dateFormat.format(currentDate)
    }
}
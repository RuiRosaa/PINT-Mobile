package com.hardtinsa

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.instamobile.kotlinlogin.R
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.hardtinsa.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_beneficios_item.idea_image
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors

class BeneficiosActivity : BaseActivity() {

    private lateinit var db: BaseDeDados
    private lateinit var beneficiosRecyclerView: RecyclerView
    private lateinit var beneficiosAdapter: BeneficiosAdapter
    private lateinit var sqliteDatabase: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beneficios)

        db = BaseDeDados(this)
        sqliteDatabase = db.writableDatabase
        beneficiosRecyclerView = findViewById(R.id.rv_beneficios)
        beneficiosAdapter = BeneficiosAdapter(mutableListOf())

        // Fetch the list of beneficts from the database
        val beneficiosList = db.todosBeneficios(sqliteDatabase).toMutableList()

        // Update the adapter with the retrieved list of beneficts
        beneficiosAdapter = BeneficiosAdapter(beneficiosList)
        beneficiosRecyclerView.adapter = beneficiosAdapter
        beneficiosRecyclerView.layoutManager = LinearLayoutManager(this)

        val buttonFetch = findViewById<Button>(R.id.btn_fetch)
        buttonFetch.setOnClickListener {
            if (isInternetConnected()) {
                Toast.makeText(this, "A recarregar..", Toast.LENGTH_SHORT)
                    .show() // Example toast message
                fetchBeneficiosFromServer()
            } else {
                Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_SHORT).show()
            }
        }



        val internetStatusImageView = findViewById<ImageView>(R.id.internet_status)
        updateInternetStatusIcon(internetStatusImageView)

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
    private fun fetchBeneficiosFromServer() {
        val apiurl = Globals.apiurl
        val url = apiurl + "beneficio/listar"

        val request = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                val beneficiosList = mutableListOf<Beneficio>()

                val db = BaseDeDados(this)
                val writableDb = db.writableDatabase

                writableDb.delete("Beneficio", null, null)

                for (i in 0 until response.length()) {
                    val jsonObject = response.getJSONObject(i)

                    val idbeneficio = jsonObject.getInt("IDBENEFICIO")
                    val iduser = jsonObject.getInt("IDUSER")
                    val detalhe = jsonObject.getString("DETALHE")
                    val descricao = jsonObject.getString("DESCRICAO")
                    val estado = jsonObject.getString("ESTADO")
                    val criadodata = jsonObject.getString("CRIADODATA")

                    val dateTimeFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    val parsedCriadoData = dateTimeFormatter.parse(criadodata)
                    val formattedDataHora = parsedCriadoData?.let { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it)
                    }

                    val beneficio = Beneficio(idbeneficio, iduser, detalhe, descricao, estado, formattedDataHora)
                    beneficiosList.add(beneficio)

                    val contentValues = ContentValues().apply {
                        put("idbeneficio", beneficio.idbeneficio)
                        put("iduser", beneficio.iduser)
                        put("detalhe", beneficio.detalhe)
                        put("descricao", beneficio.descricao)
                        put("estado", beneficio.estado)
                        put("criadodata", beneficio.criadodata)
                    }
                    writableDb.insert("Beneficio", null, contentValues)
                }

                runOnUiThread {
                    beneficiosAdapter.clearBeneficios()
                    beneficiosAdapter.addBeneficios(beneficiosList)
                    beneficiosAdapter.notifyDataSetChanged()
                }
            },
            { error ->
                // Handle error
            }
        )
        Volley.newRequestQueue(this).add(request)
    }
}

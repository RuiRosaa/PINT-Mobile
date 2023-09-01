package com.hardtinsa

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.instamobile.kotlinlogin.R
import org.json.JSONException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class VagasHistorico: BaseActivity() {

    private lateinit var db: BaseDeDados
    private lateinit var sqliteDatabase: SQLiteDatabase
    private lateinit var listView: ListView


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vagas_historico)

        db = BaseDeDados(this)
        sqliteDatabase = db.writableDatabase

        val token = Globals.token
        val userId = Globals.userID

        fetchUserDetails(token, userId)

        listView = findViewById(R.id.list_view1)

        if (isInternetConnected()) {
            Toast.makeText(this, "A recarregar..", Toast.LENGTH_SHORT)
                .show()
        fetchCandidatura()
        } else {
            getCandidaturaFromLocalDB()
            Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_SHORT).show()
        }

        val internetStatusImageView = findViewById<ImageView>(R.id.internet_status)
        updateInternetStatusIcon(internetStatusImageView)


        val back = findViewById<Button>(R.id.btn_back)
        back.setOnClickListener {
            val intent = Intent(this, VagasActivity::class.java)
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
    private fun fetchCandidatura() {
        val iduser = Globals.userID
        val apiurl = Globals.apiurl
        val url = apiurl + "candidatura/listarminhas/$iduser"

        val requestQueue = Volley.newRequestQueue(this)
        val jsonObjectRequest  = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        val data = response.getJSONArray("data")
                        val candidaturas = mutableListOf<String>()

                        clearCandidaturaTable()

                        for (i in 0 until data.length()) {
                            val candidaturaObject = data.getJSONObject(i)
                            val idCandidatura = candidaturaObject.getInt("IDCANDIDATURA")
                            val idOferta = candidaturaObject.getInt("IDOFERTA")
                            val estadoD = candidaturaObject.getString("ESTADOD")
                            val criadodata = candidaturaObject.getString("CRIADODATA")

                            val dateTimeFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                            val parsedCriadoData = dateTimeFormatter.parse(criadodata)
                            val formattedDataHora = parsedCriadoData?.let { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it)
                            }

                            Log.d("Variable", "idCandidatura: $idCandidatura")
                            Log.d("Variable", "idOferta: $idOferta")
                            Log.d("Variable", "estadoD: $estadoD")
                            Log.d("Variable", "criadodata: $formattedDataHora")

                            val candidatura =
                                "ID Candidatura: $idCandidatura\nID Oferta: $idOferta\nEstado: $estadoD\nCriadoData: $formattedDataHora"
                            candidaturas.add(candidatura)

                            val contentValues = ContentValues().apply {
                                put("idcandidatura", idCandidatura)
                                put("idoferta", idOferta)
                                put("estadod", estadoD)
                                put("criadodata", criadodata)
                            }
                            val rowId = sqliteDatabase.insert("Candidatura", null, contentValues)
                            Log.d("Candidatura", "Inserted candidatura with row ID: $rowId")
                        }

                        if (candidaturas.isNotEmpty()) {
                            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, candidaturas)
                            listView.adapter = adapter
                        } else {
                            val emptyMessage = "Não existem candidaturas disponíveis"
                            val emptyAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayOf(emptyMessage))
                            listView.adapter = emptyAdapter
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                Log.e("Candidatura Error", "Error retrieving candidatura: ${error.message}")
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    private fun getCandidaturaFromLocalDB() {
        val candidaturas = mutableListOf<String>()

        val query = "SELECT idcandidatura, idoferta, estadod, criadodata FROM Candidatura"
        val cursor = sqliteDatabase.rawQuery(query, null)

        val idCandidaturaIndex = cursor.getColumnIndex("idcandidatura")
        val idOfertaIndex = cursor.getColumnIndex("idoferta")
        val estadoDIndex = cursor.getColumnIndex("estadod")
        val criadodataIndex = cursor.getColumnIndex("criadodata")

            while (cursor.moveToNext()) {
                val idCandidatura = if (idCandidaturaIndex != -1) cursor.getInt(idCandidaturaIndex) else 0
                val idOferta = if (idOfertaIndex != -1) cursor.getInt(idOfertaIndex) else 0
                val estadoD = if (estadoDIndex != -1) cursor.getString(estadoDIndex) else ""
                val criadodata = if (criadodataIndex != -1) cursor.getString(criadodataIndex) else ""

                val dateTimeFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val parsedCriadoData = dateTimeFormatter.parse(criadodata)
                val formattedDataHora = parsedCriadoData?.let { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it)
                }

                val candidatura =
                    "ID Candidatura: $idCandidatura\nID Oferta: $idOferta\nEstado: $estadoD\nCriadoData: $formattedDataHora"
                candidaturas.add(candidatura)
            }
            cursor.close()

        if (candidaturas.isNotEmpty()) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, candidaturas)
            listView.adapter = adapter
        } else {
            val emptyMessage = "Não existem candidaturas disponíveis"
            val emptyAdapter =
                ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayOf(emptyMessage))
            listView.adapter = emptyAdapter
        }
    }
    private fun clearCandidaturaTable() {
        val table = "Candidatura"
        sqliteDatabase.delete(table, null, null)
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
                            val colaborador = jsonObject.getBoolean("COLABORADOR")

                            Globals.colaborador = colaborador
                            Log.d("User Details", "COLABORADOR: $colaborador")
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
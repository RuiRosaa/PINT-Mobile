package com.hardtinsa

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.hardtinsa.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.Locale


class VagasActivity : BaseActivity() {

    private lateinit var db: BaseDeDados
    private lateinit var vagasRecyclerView: RecyclerView
    private lateinit var vagasAdapter: VagasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vagas)

        val buttonHistorico = findViewById<Button>(R.id.btn_historico)
        val token = Globals.token
        val userId = Globals.userID

        db = BaseDeDados(this)
        vagasRecyclerView = findViewById(R.id.rv_vagas)
        vagasAdapter = VagasAdapter(mutableListOf())

        if (isInternetConnected()) {
            fetchUserDetails(token, userId)
            fetchVagasFromServer()
        } else {
        Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_SHORT).show()
    }


        buttonHistorico.setOnClickListener {
                    val intent = Intent(this, VagasHistorico::class.java)
                    startActivity(intent)
        }



        // Fetch the list of ideas from the database
        val VagasList = db.todasVagas(db.readableDatabase).toMutableList()


        val buttonFetch = findViewById<Button>(R.id.btn_fetch)
        buttonFetch.setOnClickListener {
            if (isInternetConnected()) {
                Toast.makeText(this, "A recarregar..", Toast.LENGTH_SHORT)
                    .show() // Example toast message
                fetchVagasFromServer()
            } else {
                Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_SHORT).show()
            }
        }

        // Update the adapter with the retrieved list of VAGAs
        val filteredVagasList = if (Globals.colaborador) {
            VagasList.toMutableList()       // Mostra todas as vagas quando Colaborador é true
        } else {
            VagasList.filterNot { it.interna }.toMutableList()  // quando o Colaborador = false, filtra fora as vagas com a interna
        }
        vagasAdapter = VagasAdapter(filteredVagasList)
        vagasRecyclerView.adapter = vagasAdapter
        vagasRecyclerView.layoutManager = LinearLayoutManager(this)

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

    private fun fetchVagasFromServer() {
        val apiurl = Globals.apiurl
        val url = apiurl + "oferta/listar"

        val request = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                val vagasList = mutableListOf<Vaga>()

                val db = BaseDeDados(this)
                val writableDb = db.writableDatabase

                // Delete existing data in the Vaga table
                writableDb.delete("Vaga", null, null)

                for (i in 0 until response.length()) {
                    val jsonObject = response.getJSONObject(i)

                    val idoferta = jsonObject.getInt("IDOFERTA")
                    val descricao = jsonObject.getString("DESCRICAO")
                    val detalhe = jsonObject.getString("DETALHE")
                    val estado = jsonObject.getString("ESTADO")
                    val estadod = jsonObject.getString("ESTADOD")
                    val datainicio = jsonObject.getString("DATA_INICIO")
                    val datafim = jsonObject.getString("DATA_FIM")
                    val nome = jsonObject.getString("NOME")
                    val morada = jsonObject.getString("MORADA")
                    val interna = jsonObject.getBoolean("INTERNA")
                    val tags = jsonObject.getString("TAGS")

                    if (estado == "1") {
                        val dateTimeFormatter =
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                        val parsedDataInicio = dateTimeFormatter.parse(datainicio)
                        val parsedDataDataFim = dateTimeFormatter.parse(datafim)
                        val formattedDataInicio =
                            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(
                                parsedDataInicio
                            )
                        val formattedDataFim =
                            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(
                                parsedDataDataFim
                            )

                        val vaga = Vaga(
                            idoferta,
                            descricao,
                            detalhe,
                            estado,
                            estadod,
                            formattedDataInicio,
                            formattedDataFim,
                            nome,
                            morada,
                            interna,
                            tags
                        )
                        vagasList.add(vaga)

                        // Insert the fetched vagas into the local database table
                        val contentValues = ContentValues().apply {
                            put("idoferta", vaga.idoferta)
                            put("descricao", vaga.descricao)
                            put("detalhe", vaga.detalhe)
                            put("estado", vaga.estado)
                            put("estadod", vaga.estadod)
                            put("datainicio", vaga.datainicio)
                            put("datafim", vaga.datafim)
                            put("nome", vaga.nome)
                            put("morada", vaga.morada)
                            put("interna", vaga.interna)
                            put("tags", vaga.tags)
                        }
                        writableDb.insert("Vaga", null, contentValues)
                    }
                }

                runOnUiThread {
                    val filteredVagasList = if (Globals.colaborador) {
                        vagasList.toMutableList()       // Mostra todas as vagas quando Colaborador é true
                    } else {
                        vagasList.filterNot { it.interna }.toMutableList()  // filtra fora as vagas com a interna = true quando o Colaborador = false
                    }

                    vagasAdapter.clearVagas()
                    vagasAdapter.addVagas(filteredVagasList)
                    vagasAdapter.notifyDataSetChanged()
                }
            },
            { error ->
                // Handle error
            }
        )

        // Add the request to the Volley request queue
        Volley.newRequestQueue(this).add(request)
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



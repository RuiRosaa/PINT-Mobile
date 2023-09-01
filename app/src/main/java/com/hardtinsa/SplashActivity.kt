package com.hardtinsa

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.instamobile.kotlinlogin.R
import org.json.JSONException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SplashActivity : MainActivity() {

    private lateinit var db: BaseDeDados
    private lateinit var vagasRecyclerView: RecyclerView
    private lateinit var vagasAdapter: VagasAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Globals.autenticado = false

        db = BaseDeDados(this)
        vagasRecyclerView = findViewById(R.id.rv_vagas)
        vagasAdapter = VagasAdapter(mutableListOf())

        if (isInternetConnected()) {
        fetchVagasFromServer()
        } else {
            Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_SHORT).show()
        }

        // Fetch the list of ideas from the database
        val VagasList = db.todasVagas(db.readableDatabase).toMutableList()

        // Update the adapter with the retrieved list of VAGAs
        val filteredVagasList = if (Globals.colaborador) {
            VagasList.toMutableList()       // Mostra todas as vagas quando Colaborador é true
        } else {
            VagasList.filterNot { it.interna }.toMutableList()  // quando o Colaborador = false, filtra fora as vagas com a interna
        }
        vagasAdapter = VagasAdapter(filteredVagasList)
        vagasRecyclerView.adapter = vagasAdapter
        vagasRecyclerView.layoutManager = LinearLayoutManager(this)


        val entrarMain = findViewById<Button>(R.id.loginButton1)
        entrarMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("autenticado", false)
            startActivity(intent)

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
}
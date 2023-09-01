package com.hardtinsa

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.hardtinsa.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONException

class OportunidadesHistorico: BaseActivity() {

    private lateinit var db: BaseDeDados
    private lateinit var sqliteDatabase: SQLiteDatabase
    private lateinit var listView: ListView


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oportunidades_historico)

        db = BaseDeDados(this)
        sqliteDatabase = db.writableDatabase

        val token = Globals.token
        val userId = Globals.userID

        fetchUserDetails(token, userId)

        listView = findViewById(R.id.list_view2)

        if (isInternetConnected()) {
            Toast.makeText(this, "A recarregar..", Toast.LENGTH_SHORT)
                .show()

        } else {

            Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_SHORT).show()
        }

        val internetStatusImageView = findViewById<ImageView>(R.id.internet_status)
        updateInternetStatusIcon(internetStatusImageView)

        fetchOportunidadesFromLocalDB()

        val back = findViewById<Button>(R.id.btn_back)
        back.setOnClickListener {
            val intent = Intent(this, OportunidadesActivity::class.java)
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

    private fun fetchOportunidadesFromLocalDB() {
        val oportunidades = mutableListOf<String>()

        val query = "SELECT idoportunidade, idtipo, idcliente, idtpprojeto, descricao, detalhe, estado, criadodata FROM Oportunidade " +
                "WHERE estado NOT IN (7, 8, 10, 99, 0) AND iduser = ${Globals.userID}"

        val cursor = sqliteDatabase.rawQuery(query, null)

        val idOportunidadeIndex = cursor.getColumnIndex("idoportunidade")
        val idTipoIndex = cursor.getColumnIndex("idtipo")
        val idClienteIndex = cursor.getColumnIndex("idcliente")
        val idTpProjetoIndex = cursor.getColumnIndex("idtpprojeto")
        val descricaoIndex = cursor.getColumnIndex("descricao")
        val detalheIndex = cursor.getColumnIndex("detalhe")
        val estadoIndex = cursor.getColumnIndex("estado")
        val criadoDataIndex = cursor.getColumnIndex("criadodata")

        while (cursor.moveToNext()) {
            val idOportunidade = if (idOportunidadeIndex != -1) cursor.getInt(idOportunidadeIndex) else 0
            val idTipo = if (idTipoIndex != -1) cursor.getInt(idTipoIndex) else 0
            val idCliente = if (idClienteIndex != -1) cursor.getInt(idClienteIndex) else 0
            val idTpProjeto = if (idTpProjetoIndex != -1) cursor.getInt(idTpProjetoIndex) else 0
            val descricao = if (descricaoIndex != -1) cursor.getString(descricaoIndex) else ""
            val detalhe = if (detalheIndex != -1) cursor.getString(detalheIndex) else ""
            val estado = if (estadoIndex != -1) cursor.getInt(estadoIndex) else 0
            val criadoData = if (criadoDataIndex != -1) cursor.getString(criadoDataIndex) else ""

            val estadoText = when (estado) {
                1 -> "Ativo/Criado"
                2 -> "Em Curso"
                3 -> "Em Standby"
                4 -> "Em Proposta"
                5 -> "Em Análise"
                6 -> "Em Negociacao"
                else -> "N/A"
            }
            val idtpprojetoText = when (idTpProjeto) {
                1 -> "Desenvolvimento WEB"
                2 -> "Desenvolvimento Mobile"
                3 -> "Consultoria"
                else -> "N/A"
            }

            val idtipoText = when (idTipo) {
                1 -> "Generico"
                2 -> "Software"
                3 -> "Consultoria"
                4 -> "Redes"
                5 -> "Financeiro"
                else -> "N/A"
            }

            val oportunidade = "ID Oportunidade: $idOportunidade\n" +
                    "Tipo de Projeto: $idtpprojetoText\n" +
                    "Área de Negócio: $idtipoText\n" +
                    "Descrição: $descricao\n" +
                    "Detalhe: $detalhe\n" +
                    "ID Cliente: $idCliente\n" +
                    "Estado: $estadoText\n" +
                    "Criado Data: $criadoData"

            oportunidades.add(oportunidade)
        }
        cursor.close()

        if (oportunidades.isNotEmpty()) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, oportunidades)
            listView.adapter = adapter
        } else {
            val emptyMessage = "Não existem oportunidades disponíveis"
            val emptyAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayOf(emptyMessage))
            listView.adapter = emptyAdapter
        }
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
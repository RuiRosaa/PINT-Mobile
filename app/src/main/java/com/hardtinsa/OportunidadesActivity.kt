package com.hardtinsa

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.hardtinsa.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONException
import org.json.JSONObject


class OportunidadesActivity : BaseActivity() {

    private lateinit var db: BaseDeDados
    private lateinit var oportunidadesRecyclerView: RecyclerView
    private lateinit var oportunidadesAdapter: OportunidadesAdapter
    private lateinit var sqliteDatabase: SQLiteDatabase
    private var estado: String? = null
    private var oportunidadeList: MutableList<Oportunidade> = mutableListOf()
    private var isFetchButtonClicked: Boolean = false
    private var ordenacao: String = "CRIADODATA"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oportunidades)

        val buttonHistorico = findViewById<Button>(R.id.btn_historico)
        val token = Globals.token
        val userId = Globals.userID

        db = BaseDeDados(this)
        sqliteDatabase = db.writableDatabase
        oportunidadesRecyclerView = findViewById(R.id.rv_oport)
        oportunidadesAdapter = OportunidadesAdapter(mutableListOf())


        val oportunidadesList = db.todasOportunidades(db.readableDatabase).toMutableList()

        oportunidadesAdapter = OportunidadesAdapter(oportunidadesList)
        oportunidadesRecyclerView.adapter = oportunidadesAdapter
        oportunidadesRecyclerView.layoutManager = LinearLayoutManager(this)

        fetchUserDetails(token, userId)

        buttonHistorico.setOnClickListener {
            if (Globals.colaborador) {
                val intent = Intent(this, OportunidadesHistorico::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Não tens permissões", Toast.LENGTH_SHORT).show()
            }
        }

        val internetStatusImageView = findViewById<ImageView>(R.id.internet_status)
        updateInternetStatusIcon(internetStatusImageView)

        val buttonOrdenacao = findViewById<Button>(R.id.btn_ordenacao)
        buttonOrdenacao.setOnClickListener {
            showOrdenacaoDialog()
        }

        val buttonC = findViewById<Button>(R.id.btn_create)
        buttonC.setOnClickListener {
            val intent = Intent(this, CriarOportunidadesActivity::class.java)
            intent.putExtra("estado", "0")
            startActivity(intent)
        }

        val buttonFetch = findViewById<Button>(R.id.btn_fetch)
        buttonFetch.setOnClickListener {
            if (isInternetConnected()) {
                OportunidadesInserirApi()
                Toast.makeText(this, "A recarregar..", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_SHORT).show()
            }
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
    private fun OportunidadesInserirApi() {
        val iduser = Globals.userID
        val apiurl = Globals.apiurl
        val token = Globals.token

        val sqliteDatabase = BaseDeDados(this)
        val db = sqliteDatabase.readableDatabase

        val query = "SELECT idoportunidade, idtipo, idcliente, iduser, idtpprojeto, descricao, detalhe, valorprev, estado, estadoreg, criadodata, publicado FROM Oportunidade WHERE estadoreg = 1"
        val query2 = "SELECT idoportunidade, idtipo, idcliente, iduser, idtpprojeto, descricao, detalhe, valorprev, estado, estadoreg, criadodata, publicado FROM Oportunidade WHERE estadoreg = 2"
        Log.d("Query", "Query: $query")
        val cursor = db.rawQuery(query, null)

        if (cursor != null && cursor.moveToFirst()) {
            val descricaoIndex = cursor.getColumnIndex("descricao")
            val detalheIndex = cursor.getColumnIndex("detalhe")
            val idtipoIndex = cursor.getColumnIndex("idtipo")
            val estadoIndex = cursor.getColumnIndex("estado")
            val idtpprojetoIndex = cursor.getColumnIndex("idtpprojeto")
            val idclienteIndex = cursor.getColumnIndex("idcliente")
            val valorprevIndex = cursor.getColumnIndex("valorprev")

            val url = apiurl + "oportunidade/inserir"
            val requestQueue = Volley.newRequestQueue(this)

            do {
                val descricao = cursor.getString(descricaoIndex)
                val detalhe = cursor.getString(detalheIndex)
                val idtipo = cursor.getString(idtipoIndex)
                val idtpprojeto = cursor.getString(idtpprojetoIndex)
                val idcliente = cursor.getString(idclienteIndex)
                val valorprev = cursor.getString(valorprevIndex)


                val params = JSONObject()
                params.put("TK", token)
                params.put("IDUSER", iduser)
                params.put("IDUSERTK", iduser)
                params.put("IDTIPO", idtipo)
                params.put("IDCLIENTE", idcliente)
                params.put("IDTPPROJETO", idtpprojeto)
                params.put("DESCRICAO", descricao)
                params.put("DETALHE", detalhe)
                params.put("VALORPREV", valorprev)
                params.put("PUBLICADO", true)
                params.put("ESTADO", 1)

                Log.d("Variable", "TK: $token")
                Log.d("Variable", "IDUSER: $iduser")
                Log.d("Variable", "IDUSERTK: $iduser")
                Log.d("Variable", "IDTIPO: $idtipo")
                Log.d("Variable", "IDCLIENTE: $idcliente")
                Log.d("Variable", "IDTPPROJETO: $idtpprojeto")
                Log.d("Variable", "DESCRICAO: $descricao")
                Log.d("Variable", "DETALHE: $detalhe")
                Log.d("Variable", "VALORPREV: $valorprev")


                val jsonObjectRequest  = JsonObjectRequest  (
                    Request.Method.POST, url, params,
                    { response ->
                        // Handle the response
                        Log.d("Response", response.toString())

                    },
                    { error ->
                        // Handle the error
                        Log.e("Error", error.toString())
                        Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                )

                requestQueue.add(jsonObjectRequest)
            } while (cursor.moveToNext())
            cursor.close()
        }

        Log.d("Query", "Query 2: $query2")
        val cursor2 = db.rawQuery(query2, null)

        if (cursor2 != null && cursor2.moveToFirst()) {
            // Handle the second query and URL (url2) here
            val descricaoIndex = cursor2.getColumnIndex("descricao")
            val detalheIndex = cursor2.getColumnIndex("detalhe")
            val idoportunidadeIndex = cursor2.getColumnIndex("idoportunidade")
            val idtipoIndex = cursor2.getColumnIndex("idtipo")
            val idtpprojetoIndex = cursor2.getColumnIndex("idtpprojeto")
            val idclienteIndex = cursor2.getColumnIndex("idcliente")
            val valorprevIndex = cursor2.getColumnIndex("valorprev")
            val estadoIndex = cursor2.getColumnIndex("estado")

            val url2 = apiurl + "oportunidade/editar"
            val requestQueue = Volley.newRequestQueue(this)

            do {
                val descricao = cursor2.getString(descricaoIndex)
                val detalhe = cursor2.getString(detalheIndex)
                val idoportunidade = cursor2.getInt(idoportunidadeIndex)
                val idtipo = cursor2.getString(idtipoIndex)
                val idtpprojeto = cursor2.getString(idtpprojetoIndex)
                val idcliente = cursor2.getString(idclienteIndex)
                val valorprev = cursor2.getString(valorprevIndex)
                val estado = cursor2.getInt(estadoIndex)

                val params2 = JSONObject()
                params2.put("TK", token)
                params2.put("IDUSERTK", iduser)
                params2.put("IDOPORTUNIDADE", idoportunidade)
                params2.put("IDTIPO", idtipo)
                params2.put("IDCLIENTE", idcliente)
                params2.put("IDTPPROJETO", idtpprojeto)
                params2.put("DESCRICAO", descricao)
                params2.put("DETALHE", detalhe)
                params2.put("VALORPREV", valorprev)
                params2.put("PUBLICADO", true)
                params2.put("ESTADO", estado)

                Log.d("Variable", "TK: $token")
                Log.d("Variable", "IDUSERTK: $iduser")
                Log.d("Variable", "IDUSER: $iduser")
                Log.d("Variable", "idoportunidade: $idoportunidade")
                Log.d("Variable", "IDTIPO: $idtipo")
                Log.d("Variable", "IDCLIENTE: $idcliente")
                Log.d("Variable", "IDTPPROJETO: $idtpprojeto")
                Log.d("Variable", "DESCRICAO: $descricao")
                Log.d("Variable", "DETALHE: $detalhe")
                Log.d("Variable", "VALORPREV: $valorprev")
                Log.d("Variable", "ESTADO: $estado")

                val jsonObjectRequest = JsonObjectRequest(
                    Request.Method.PUT, url2, params2,
                    { response ->
                        // Handle the response
                        Log.d("Response", response.toString())
                    },
                    { error ->
                        // Handle the error
                        Log.e("Error", error.toString())
                        Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                )

                requestQueue.add(jsonObjectRequest)
            } while (cursor2.moveToNext())
            cursor2.close()
        }

        db.close()
        fetchOportunidadesFromServer()
    }

    private fun fetchOportunidadesFromServer() {
        val IDuser = Globals.userID
        val token = Globals.token
        val apiurl = Globals.apiurl
        val url = apiurl + "oportunidade/listarminhas/$token/$IDuser"
        val requestQueue = Volley.newRequestQueue(this)

        val jsonObjectRequest  = JsonObjectRequest (
            Request.Method.GET, url, null,
            { response ->
                Log.d("Response", response.toString())
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        val data = response.getJSONArray("data")

                        val sqliteDatabase = BaseDeDados(this)
                        val db = sqliteDatabase.writableDatabase

                        db.delete("Oportunidade", null, null)
                        val oportList = mutableListOf<Oportunidade>()
                        oportunidadesAdapter.clearOportunidades()

                        for (i in 0 until data.length()) {
                            val Oportunidade = data.getJSONObject(i)
                            val idOportunidade = Oportunidade.getInt("IDOPORTUNIDADE")
                            val idTipo = Oportunidade.getInt("IDTIPO")
                            val idCliente = Oportunidade.getInt("IDCLIENTE")
                            val cliente = Oportunidade.getString("CLIENTE")
                            val idUser = Oportunidade.getInt("IDUSER")
                            val nome = Oportunidade.getString("NOME")
                            val idTpProjeto = Oportunidade.getInt("IDTPPROJETO")
                            val valorprev = Oportunidade.getInt("VALORPREV")
                            val descricao = Oportunidade.getString("DESCRICAO")
                            val detalhe = Oportunidade.getString("DETALHE")
                            val estado = Oportunidade.getInt("ESTADO")
                            val criadoData = Oportunidade.getString("CRIADODATA")
                            val publicado = Oportunidade.getBoolean("PUBLICADO")

                            val oportu = Oportunidade(idOportunidade, idTipo, idCliente, idUser, idTpProjeto, descricao,
                                detalhe, valorprev, estado, estadoreg = 0, criadoData,publicado)
                            oportList.add(oportu)

                            // Guarda as Ideias buscadas na BD local
                            val contentValues = ContentValues()
                            contentValues.put("IDOPORTUNIDADE", idOportunidade)
                            contentValues.put("IDTIPO", idTipo)
                            contentValues.put("IDCLIENTE", idCliente)
                            contentValues.put("IDUSER", idUser)
                            contentValues.put("NOMECLIENTE", cliente)
                            contentValues.put("NOMEUSER", nome)
                            contentValues.put("IDTPPROJETO", idTpProjeto)
                            contentValues.put("DESCRICAO", descricao)
                            contentValues.put("VALORPREV", valorprev)
                            contentValues.put("DETALHE", detalhe)
                            contentValues.put("ESTADO", estado)
                            contentValues.put("CRIADODATA", criadoData)
                            contentValues.put("PUBLICADO", publicado)

                            Log.d("Variable", "estado: $estado")

                            db.insert("Oportunidade", null, contentValues)
                        }
                        oportunidadeList = oportList
                        runOnUiThread {
                            oportunidadesAdapter.clearOportunidades()
                            oportunidadesAdapter.addOportunidades(oportList)
                            oportunidadesAdapter.notifyDataSetChanged()
                        }

                    } else {
                        response.getString("RES_MSG")
                        // Handle the error message
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    // Handle JSON parsing error
                }
            },
            { error ->
                // Handle the error
                Log.e("Error", error.toString())
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
        ordenarOportunidades(ordenacao)
    }


    val ORDENAR_POR_DATA = "CRIADODATA"
    val ORDENAR_POR_ESTADO = "ESTADO"
    val ORDENAR_POR_TIPO = "IDTIPO"
    val ORDENAR_POR_TPPROJETO = "IDTPPROJETO"

    private fun ordenarOportunidades(ordenacao: String) {
        val sqliteDatabase = BaseDeDados(this)
        val db = sqliteDatabase.readableDatabase

        val query = when (ordenacao) {
            ORDENAR_POR_DATA -> "SELECT * FROM Oportunidade ORDER BY CRIADODATA"
            ORDENAR_POR_ESTADO -> "SELECT * FROM Oportunidade ORDER BY ESTADO"
            ORDENAR_POR_TIPO -> "SELECT * FROM Oportunidade ORDER BY IDTIPO"
            ORDENAR_POR_TPPROJETO -> "SELECT * FROM Oportunidade ORDER BY IDTPPROJETO"
            else -> "SELECT * FROM Oportunidade"
        }

        val cursor = db.rawQuery(query, null)

        val oportList = mutableListOf<Oportunidade>()

        if (cursor != null && cursor.moveToFirst()) {
            val descricaoIndex = cursor.getColumnIndex("descricao")
            val detalheIndex = cursor.getColumnIndex("detalhe")
            val criadodataIndex = cursor.getColumnIndex("criadodata")
            val valorprevIndex = cursor.getColumnIndex("valorprev")
            val idOportunidadeIndex = cursor.getColumnIndex("idoportunidade")
            val idTipoIndex = cursor.getColumnIndex("idtipo")
            val idClienteIndex = cursor.getColumnIndex("idcliente")
            val idUserIndex = cursor.getColumnIndex("iduser")
            val idTpProjetoIndex = cursor.getColumnIndex("idtpprojeto")
            val estadoIndex = cursor.getColumnIndex("estado")
            val estadoregIndex = cursor.getColumnIndex("estadoreg")
            val publicadoIndex = cursor.getColumnIndex("publicado")

            do {
                val idOportunidade = cursor.getInt(idOportunidadeIndex)
                val idTipo = cursor.getInt(idTipoIndex)
                val idCliente = cursor.getInt(idClienteIndex)
                val idUser = cursor.getInt(idUserIndex)
                val idTpProjeto = cursor.getInt(idTpProjetoIndex)
                val descricao = cursor.getString(descricaoIndex)
                val detalhe = cursor.getString(detalheIndex)
                val valorprev = cursor.getInt(valorprevIndex)
                val estado = cursor.getInt(estadoIndex)
                val estadoreg = cursor.getInt(estadoregIndex)
                val criadoData = cursor.getString(criadodataIndex)
                val publicado = cursor.getInt(publicadoIndex) == 1

                val oportu = Oportunidade(
                    idOportunidade, idTipo, idCliente, idUser, idTpProjeto, descricao,
                    detalhe, valorprev, estado, estadoreg, criadoData, publicado
                )
                oportList.add(oportu)
            } while (cursor.moveToNext())
            cursor.close()
        }

        db.close()

        oportunidadeList = oportList

        runOnUiThread {
            oportunidadesAdapter.clearOportunidades()
            oportunidadesAdapter.addOportunidades(oportList)
            oportunidadesAdapter.notifyDataSetChanged()
        }
    }

    private fun showOrdenacaoDialog() {
        val ordenacoes = arrayOf("Data de Criação", "Estado", "Área de Negócio", "Tipo de Projeto")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecionar Ordenação")
        builder.setItems(ordenacoes) { dialog, which ->
            when (which) {
                0 -> ordenacao = "CRIADODATA"
                1 -> ordenacao = "ESTADO"
                2 -> ordenacao = "IDTIPO"
                3 -> ordenacao = "IDTPPROJETO"
            }
            dialog.dismiss()
            ordenarOportunidades(ordenacao)
        }

        builder.show()
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
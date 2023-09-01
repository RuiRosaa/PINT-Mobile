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
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.instamobile.kotlinlogin.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

class ClientesActivity : BaseActivity() {

    private lateinit var db: BaseDeDados
    private lateinit var clientesRecyclerView: RecyclerView
    private lateinit var clientesAdapter: ClientesAdapter
    private lateinit var sqliteDatabase: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clientes)

        db = BaseDeDados(this)
        sqliteDatabase = db.writableDatabase

        clientesRecyclerView = findViewById(R.id.rv_clientes)
        clientesAdapter = ClientesAdapter(mutableListOf())

        val clientesList = db.todosClientes(db.readableDatabase).toMutableList()

        clientesAdapter = ClientesAdapter(clientesList)
        clientesRecyclerView.adapter = clientesAdapter
        clientesRecyclerView.layoutManager = LinearLayoutManager(this)


        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val buttonA = findViewById<Button>(R.id.btn_add)
        buttonA.setOnClickListener {
            val intent = Intent(this, CriarCliente::class.java)
            startActivity(intent)
        }

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

        val buttonFetch = findViewById<Button>(R.id.btn_fetch)
        buttonFetch.setOnClickListener {
            if (isInternetConnected()) {
                Toast.makeText(this, "A recarregar...", Toast.LENGTH_SHORT).show()
                inserirClientesApi()
            } else {
                Toast.makeText(this, "Não existe conexão com a Internet", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun fetchClientesFromServer() {
        val apiurl = Globals.apiurl
        val token = Globals.token
        val iduser = Globals.userID.toInt()
        val url = apiurl + "cliente/listarminhas/$token/$iduser"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val success = response.getBoolean("success")
                    val data = response.getJSONArray("data")

                    if (success) {
                        val clientesList = mutableListOf<Cliente>()

                        val db = BaseDeDados(this)
                        val writableDb = db.writableDatabase

                        writableDb.delete("Cliente", null, null)

                        for (i in 0 until data.length()) {
                            val jsonObject = data.getJSONObject(i)

                            val idcliente = jsonObject.getInt("IDCLIENTE")
                            val iduser = jsonObject.getInt("IDUSER")
                            val nome = jsonObject.getString("NOME")
                            val nif = jsonObject.getInt("NIF")
                            val morada = jsonObject.getString("MORADA")
                            val codpostal = jsonObject.getString("CODPOSTAL")
                            val localidade = jsonObject.getString("LOCALIDADE")
                            val email = jsonObject.getString("EMAIL")
                            val telefone = jsonObject.getString("TELEFONE")
                            val telemovel = jsonObject.getString("TELEMOVEL")
                            val estado = jsonObject.getInt("ESTADO")
                            val criadodata = jsonObject.getString("CRIADODATA")

                            val dateTimeFormatter = SimpleDateFormat(
                                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                                Locale.getDefault()
                            )
                            val parsedCriadoData = dateTimeFormatter.parse(criadodata)
                            val formattedDataHora = parsedCriadoData?.let {
                                SimpleDateFormat(
                                    "dd/MM/yyyy HH:mm",
                                    Locale.getDefault()
                                ).format(it)
                            }

                            val cliente = Cliente(idcliente, iduser, nome, nif, morada, codpostal, localidade, email, telefone,
                                telemovel,
                                estado,
                                formattedDataHora
                            )
                            clientesList.add(cliente)

                            val contentValues = ContentValues().apply {
                                put("idcliente", idcliente)
                                put("iduser", iduser)
                                put("nome", nome)
                                put("nif", nif)
                                put("morada", morada)
                                put("codpostal", codpostal)
                                put("localidade", localidade)
                                put("email", email)
                                put("telefone", telefone)
                                put("telemovel", telemovel)
                                put("estado", estado)
                                put("criadodata", criadodata)
                            }
                            writableDb.insert("Cliente", null, contentValues)

                            Log.d("FetchClientes", "IDCLIENTE: $idcliente")
                            Log.d("FetchClientes", "IDUSER: $iduser")
                            Log.d("FetchClientes", "NOME: $nome")
                            Log.d("FetchClientes", "NIF: $nif")
                            Log.d("FetchClientes", "MORADA: $morada")
                            Log.d("FetchClientes", "CODPOSTAL: $codpostal")
                            Log.d("FetchClientes", "LOCALIDADE: $localidade")
                            Log.d("FetchClientes", "EMAIL: $email")
                            Log.d("FetchClientes", "TELEFONE: $telefone")
                            Log.d("FetchClientes", "TELEMOVEL: $telemovel")
                            Log.d("FetchClientes", "ESTADO: $estado")
                            Log.d("FetchClientes", "CRIADODATA: $criadodata")
                        }

                        runOnUiThread {
                            clientesAdapter.clearClientes()
                            clientesAdapter.addClientes(clientesList)
                            clientesAdapter.notifyDataSetChanged()
                        }
                    } else {
                        Log.e(
                            "FetchClientes",
                            "API request failed: ${response.getString("RES_MSG")}"
                        )
                    }
                } catch (e: JSONException) {
                    Log.e("FetchClientes", "Error parsing JSON response: ${e.message}")
                    e.printStackTrace()
                }
            },
            { error ->
                Log.e("FetchClientes", "Error fetching clientes: ${error.message}")
                error.printStackTrace()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun inserirClientesApi(){
        val token = Globals.token
        val iduser = Globals.userID.toInt()
        val apiurl = Globals.apiurl

        val sqliteDatabase = BaseDeDados(this)
        val db = sqliteDatabase.readableDatabase

        val query = "SELECT nome, nif, morada, codpostal, localidade, email, telefone, telemovel, estado FROM Cliente WHERE estadoreg = 1"
        val cursor = db.rawQuery(query, null)

        if (cursor != null && cursor.moveToFirst()) {
            val nomeIndex = cursor.getColumnIndex("nome")
            val nifIndex = cursor.getColumnIndex("nif")
            val moradaIndex = cursor.getColumnIndex("morada")
            val codpostalIndex = cursor.getColumnIndex("codpostal")
            val localidadeIndex = cursor.getColumnIndex("localidade")
            val emailIndex = cursor.getColumnIndex("email")
            val telefoneIndex = cursor.getColumnIndex("telefone")
            val telemovelIndex = cursor.getColumnIndex("telemovel")
            val estadoIndex = cursor.getColumnIndex("estado")

            val url = apiurl + "cliente/inserir"
            val requestQueue = Volley.newRequestQueue(this)

            do {
                val Nome = cursor.getString(nomeIndex)
                val Nif = cursor.getInt(nifIndex)
                val Morada = cursor.getString(moradaIndex)
                val CodPostal = cursor.getString(codpostalIndex)
                val Localidade = cursor.getString(localidadeIndex)
                val Email = cursor.getString(emailIndex)
                val Telefone = cursor.getInt(telefoneIndex)
                val Telemovel = cursor.getInt(telemovelIndex)
                val Estado = cursor.getInt(estadoIndex)

                val params = JSONObject()
                Log.d("Variable", "TK: $token")
                params.put("TK", token)
                Log.d("Variable", "IDUSER: $iduser")
                params.put("IDUSER", iduser)
                Log.d("Variable", "NOME: $Nome")
                params.put("NOME", Nome)
                Log.d("Variable", "NIF: $Nif")
                params.put("NIF", Nif)
                Log.d("Detalhe", "MORADA: $Morada")
                params.put("MORADA", Morada)
                Log.d("Detalhe", "CODPOSTAL: $CodPostal")
                params.put("CODPOSTAL", CodPostal)
                Log.d("Detalhe", "LOCALIDADE: $Localidade")
                params.put("LOCALIDADE", Localidade)
                Log.d("Detalhe", "EMAIL: $Email")
                params.put("EMAIL", Email)
                Log.d("Detalhe", "TELEFONE: $Telefone")
                params.put("TELEFONE", Telefone)
                Log.d("Detalhe", "TELEMOVEL: $Telemovel")
                params.put("TELEMOVEL", Telemovel)
                Log.d("Detalhe", "ESTADO: $Estado")
                params.put("ESTADO", Estado)


                val jsonObjectRequest = JsonObjectRequest(
                    Request.Method.POST, url, params,
                    { response ->
                        // Handle the response
                        Log.d("Response", response.toString())

                    },
                    { error ->
                        // Handle the error
                        Log.e("Error", error.toString())

                    }
                )
                //Estou a dar delete da Entrevista editada após Inserir na API
                val deletedRows = db.delete("Cliente", null, null)
                if (deletedRows > 0) {
                    Log.d("Clientes deleted", "Deleted $deletedRows rows")
                } else {
                    Log.d("Cliente deletion failed", "Failed to delete the Cliente")
                }
                requestQueue.add(jsonObjectRequest)
            } while (cursor.moveToNext())
            cursor.close()
        }
        db.close()
        fetchClientesFromServer()
    }

}
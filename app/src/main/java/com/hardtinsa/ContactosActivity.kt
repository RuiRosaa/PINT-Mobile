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
import com.example.hardtinsa.R
import com.google.android.material.bottomnavigation.BottomNavigationView
//import com.instamobile.kotlinlogin.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

class ContactosActivity : BaseActivity() {

    private lateinit var db: BaseDeDados
    private lateinit var contactosRecyclerView: RecyclerView
    private lateinit var contactosAdapter: ContactosAdapter
    private lateinit var sqliteDatabase: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contactos)

        db = BaseDeDados(this)
        sqliteDatabase = db.writableDatabase

        contactosRecyclerView = findViewById(R.id.rv_contatos)
        contactosAdapter = ContactosAdapter(mutableListOf())

        val contactosList = db.todosContactos(db.readableDatabase).toMutableList()

        contactosAdapter = ContactosAdapter(contactosList)
        contactosRecyclerView.adapter = contactosAdapter
        contactosRecyclerView.layoutManager = LinearLayoutManager(this)

        val internetStatusImageView = findViewById<ImageView>(R.id.internet_status)
        updateInternetStatusIcon(internetStatusImageView)

        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val buttonA = findViewById<Button>(R.id.btn_add)
        buttonA.setOnClickListener {
            val intent = Intent(this, CriarContactosActivity::class.java)
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
                inserirContatosApi()
            } else {
                Toast.makeText(this, "Não existe conexão com a Internet", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun fetchContatosFromServer() {
        val apiurl = Globals.apiurl
        val token = Globals.token
        val iduser = Globals.userID.toInt()
        val url = apiurl + "contato/listarminhas/$token/$iduser"


        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val success = response.getBoolean("success")
                    val data = response.getJSONArray("data")

                    if (success) {
                        val contatoList = mutableListOf<Contato>()

                        val db = BaseDeDados(this)
                        val writableDb = db.writableDatabase

                        writableDb.delete("Contato", null, null)

                        for (i in 0 until data.length()) {
                            val jsonObject = data.getJSONObject(i)

                            val idcontato = jsonObject.getInt("IDCONTACTO")
                            val idcliente = jsonObject.getInt("IDCLIENTE")
                            val nome = jsonObject.getString("NOME")
                            val funcao = jsonObject.getString("FUNCAO")
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

                            val contato = Contato(
                                idcontato, idcliente, iduser, nome, funcao, nif, morada, codpostal, localidade,
                                email, telefone, telemovel, estado, estadoreg = 0, formattedDataHora)
                            contatoList.add(contato)

                            val contentValues = ContentValues().apply {
                                put("idcontato", idcontato)
                                put("idcliente", idcliente)
                                put("iduser", iduser)
                                put("nome", nome)
                                put("funcao", funcao)
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
                            writableDb.insert("Contato", null, contentValues)

                            Log.d("FetchContatos", "IDCONTATO: $idcontato")
                            Log.d("FetchContatos", "IDCLIENTE: $idcliente")
                            Log.d("FetchContatos", "IDUSER: $iduser")
                            Log.d("FetchContatos", "NOME: $nome")
                            Log.d("FetchContatos", "FUNCAO: $funcao")
                            Log.d("FetchContatos", "NIF: $nif")
                            Log.d("FetchContatos", "MORADA: $morada")
                            Log.d("FetchContatos", "CODPOSTAL: $codpostal")
                            Log.d("FetchContatos", "LOCALIDADE: $localidade")
                            Log.d("FetchContatos", "EMAIL: $email")
                            Log.d("FetchContatos", "TELEFONE: $telefone")
                            Log.d("FetchContatos", "TELEMOVEL: $telemovel")
                            Log.d("FetchContatos", "ESTADO: $estado")
                            Log.d("FetchContatos", "CRIADODATA: $criadodata")
                        }

                        runOnUiThread {
                            contactosAdapter.clearContato()
                            contactosAdapter.addContato(contatoList)
                            contactosAdapter.notifyDataSetChanged()
                        }
                    } else {
                        Log.e("FetchContatos", "API request failed: ${response.getString("RES_MSG")}")
                    }
                } catch (e: JSONException) {
                    Log.e("FetchContatos", "Error parsing JSON response: ${e.message}")
                    e.printStackTrace()
                }
            },
            { error ->
                Log.e("FetchContatos", "Error fetching contacts: ${error.message}")
                error.printStackTrace()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }


    private fun inserirContatosApi() {
        val token = Globals.token
        val iduser = Globals.userID.toInt()
        val apiurl = Globals.apiurl

        val sqliteDatabase = BaseDeDados(this)
        val db = sqliteDatabase.readableDatabase

        val query =
            "SELECT idcliente, nome, funcao, nif, morada, codpostal, localidade, email, telefone, telemovel, estado, estadoreg FROM Contato WHERE estadoreg = 1"
        val cursor = db.rawQuery(query, null)

        if (cursor != null && cursor.moveToFirst()) {
            val idclienteIndex = cursor.getColumnIndex("idcliente")
            val nomeIndex = cursor.getColumnIndex("nome")
            val funcaoIndex = cursor.getColumnIndex("funcao")
            val nifIndex = cursor.getColumnIndex("nif")
            val moradaIndex = cursor.getColumnIndex("morada")
            val codpostalIndex = cursor.getColumnIndex("codpostal")
            val localidadeIndex = cursor.getColumnIndex("localidade")
            val emailIndex = cursor.getColumnIndex("email")
            val telefoneIndex = cursor.getColumnIndex("telefone")
            val telemovelIndex = cursor.getColumnIndex("telemovel")
            val estadoIndex = cursor.getColumnIndex("estado")

            val url = apiurl + "contato/inserir"
            val requestQueue = Volley.newRequestQueue(this)

            do {
                val idcliente = cursor.getString(idclienteIndex)
                val Nome = cursor.getString(nomeIndex)
                val Funcao = cursor.getString(funcaoIndex)
                val Nif = cursor.getInt(nifIndex)
                val Morada = cursor.getString(moradaIndex)
                val CodPostal = cursor.getString(codpostalIndex)
                val Localidade = cursor.getString(localidadeIndex)
                val Email = cursor.getString(emailIndex)
                val Telefone = cursor.getString(telefoneIndex)
                val Telemovel = cursor.getString(telemovelIndex)
                val Estado = cursor.getInt(estadoIndex)

                val params = JSONObject()
                params.put("TK", token)
                params.put("IDUSER", iduser)
                params.put("IDCLIENTE", idcliente)
                params.put("NOME", Nome)
                params.put("FUNCAO", Funcao)
                params.put("NIF", Nif)
                params.put("MORADA", Morada)
                params.put("CODPOSTAL", CodPostal)
                params.put("LOCALIDADE", Localidade)
                params.put("EMAIL", Email)
                params.put("TELEFONE", Telefone)
                params.put("TELEMOVEL", Telemovel)
                params.put("ESTADO", 1)

                Log.d("Variable", "IDCLIENTE: $idcliente")
                Log.d("Variable", "token: $token")
                Log.d("Variable", "IDUSER: $iduser")
                Log.d("Variable", "Nome: $Nome")
                Log.d("Variable", "Funcao: $Funcao")
                Log.d("Variable", "Nif: $Nif")
                Log.d("Variable", "Morada: $Morada")
                Log.d("Variable", "CodPostal: $CodPostal")
                Log.d("Variable", "Localidade: $Localidade")
                Log.d("Variable", "Email: $Email")
                Log.d("Variable", "Telefone: $Telefone")
                Log.d("Variable", "Telemovel: $Telemovel")


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

                val deletedRows = db.delete("Contato", null, null)
                if (deletedRows > 0) {
                    Log.d("Contatos deleted", "Deleted $deletedRows rows")
                } else {
                    Log.d("Contato deletion failed", "Failed to delete the Contato")
                }
                requestQueue.add(jsonObjectRequest)
            } while (cursor.moveToNext())
            cursor.close()
        }
        db.close()
        fetchContatosFromServer()
    }

}
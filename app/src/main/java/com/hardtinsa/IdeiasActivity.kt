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
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.hardtinsa.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

class  IdeiasActivity : BaseActivity() {

    private lateinit var db: BaseDeDados
    private lateinit var ideasRecyclerView: RecyclerView
    private lateinit var ideasAdapter: IdeasAdapter
    private lateinit var sqliteDatabase: SQLiteDatabase
    private var estado: String? = null
    private var userID: Int? = null
    private var ideasList: MutableList<Idea> = mutableListOf()
    private var isFetchButtonClicked: Boolean = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ideias)

        db = BaseDeDados(this)
        sqliteDatabase = db.writableDatabase

        ideasAdapter = IdeasAdapter(ideasList)
        ideasRecyclerView = findViewById(R.id.rv_ideias)
        //atualiza a lista de ideias da BD
        ideasList = db.todasIdeias(db.readableDatabase).toMutableList()
        ideasAdapter.updateData(ideasList)

        // Dá update ao adapter
        ideasRecyclerView.adapter = ideasAdapter
        ideasRecyclerView.layoutManager = LinearLayoutManager(this)


        val internetStatusImageView = findViewById<ImageView>(R.id.internet_status)
        updateInternetStatusIcon(internetStatusImageView)

        val buttonC = findViewById<Button>(R.id.btn_create)
        buttonC.setOnClickListener {
            val intent = Intent(this, CriarIdeiasActivity::class.java)
            ideasAdapter.setFetchButtonClicked(false)
            intent.putExtra("estado", "0") // Passa estado como Nãoativo
            startActivity(intent)
        }

        val buttonFetch = findViewById<Button>(R.id.btn_fetch)
        buttonFetch.setOnClickListener {
            isFetchButtonClicked = true //para conseguir dar delete antes de enviar
            ideasAdapter.setFetchButtonClicked(true)
            if (isInternetConnected()) {
                Toast.makeText(this, "A recarregar..", Toast.LENGTH_SHORT)
                    .show() // Example toast message
                IdeiasInserirApi()
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

    private fun IdeiasInserirApi() {
        val token = Globals.token
        val iduser = Globals.userID.toInt()
        val apiurl = Globals.apiurl

        val sqliteDatabase = BaseDeDados(this)
        val db = sqliteDatabase.readableDatabase

        val query = "SELECT idideia, iduser, idtipo, descricao, detalhe FROM Ideia WHERE estadoreg = 1 OR estadoreg = 2"
        val cursor = db.rawQuery(query, null)

        if (cursor != null && cursor.moveToFirst()) {
            val idIdeiaIndex = cursor.getColumnIndex("idideia")
            val descricaoIndex = cursor.getColumnIndex("descricao")
            val detalheIndex = cursor.getColumnIndex("detalhe")
            val idtipoIndex = cursor.getColumnIndex("idtipo")

            val url = apiurl + "ideia/inserir"
            val requestQueue = Volley.newRequestQueue(this)

            do {
                val idIdeia = cursor.getInt(idIdeiaIndex)
                val descricao = cursor.getString(descricaoIndex)
                val detalhe = cursor.getString(detalheIndex)
                val idtipo = cursor.getString(idtipoIndex)


                val params = JSONObject()
                Log.d("Variable", "IDUSER: $iduser")
                params.put("IDUSER", iduser)
                Log.d("Variable", "IDTIPO: $idtipo")
                params.put("IDTIPO", idtipo)
                Log.d("Variable", "DESCRICAO: $descricao")
                params.put("DESCRICAO", descricao)
                Log.d("Detalhe", "DETALHE: $detalhe")
                params.put("DETALHE", detalhe)


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

                requestQueue.add(jsonObjectRequest)
            } while (cursor.moveToNext())
            cursor.close()
        }
        db.close()
        fetchIdeiasFromServer()
    }

    private fun fetchIdeiasFromServer() {
        val IDuser = Globals.userID
        val token = Globals.token
        val apiurl = Globals.apiurl
        val url = apiurl + "ideia/listarminhas/$IDuser/$token"

        val requestQueue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                Log.d("Response", response.toString())
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        val data = response.getJSONArray("data")

                        val sqliteDatabase = BaseDeDados(this)
                        val db = sqliteDatabase.writableDatabase

                        // Delete todas as Ideias da db
                        db.delete("Ideia", null, null)

                        val ideasList = mutableListOf<Idea>()
                        ideasAdapter.clearIdeas()

                        for (i in 0 until data.length()) {
                            val ideia = data.getJSONObject(i)
                            val idideia = ideia.getInt("IDIDEIA")
                            val descricao = ideia.getString("DESCRICAO")
                            val detalhe = ideia.getString("DETALHE")
                            val estado = ideia.getString("ESTADO")
                            val criadodata = ideia.getString("CRIADODATA")
                            val idtipo = ideia.getString("IDTIPO")

                                val dateTimeFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                                val parsedCriadoData = dateTimeFormatter.parse(criadodata)
                                val formattedDataHora = parsedCriadoData?.let {
                                    SimpleDateFormat(
                                        "dd/MM/yyyy HH:mm",
                                        Locale.getDefault()
                                    ).format(it)
                                }

                                val idea = Idea(idideia, iduser = Globals.userID.toInt(), idtipo.toInt(), descricao, detalhe, estado, estadoreg = 0, formattedDataHora)
                                ideasList.add(idea)


                                val contentValues = ContentValues().apply {
                                    put("IDIDEIA", idea.idideia)
                                    put("DESCRICAO", idea.descricao)
                                    put("DETALHE", idea.detalhe)
                                    put("ESTADO", idea.estado)
                                    put("CRIADODATA", idea.criadodata)
                                    put("IDTIPO", idea.idtipo)
                                }
                                db.insert("Ideia", null, contentValues)
                            }

                        runOnUiThread {
                            ideasAdapter.clearIdeas()
                            ideasAdapter.addIdeas(ideasList)
                            ideasAdapter.notifyDataSetChanged()
                        }
                    } else {
                        Log.d("Response", "Response: $response")
                        val jsonArray = JSONArray(response)

                        for (i in 0 until jsonArray.length()) {
                            val messageObject = jsonArray.getJSONObject(i)
                            val resCode = messageObject.getInt("RES_CODE")
                            val resMsg = messageObject.getString("RES_MSG")
                            val resIdd = messageObject.getInt("RES_IDD")

                                if (resCode == 1) {
                                    Log.d("Success", "Code: $resCode, Message: $resMsg, ID: $resIdd")

                                } else {
                                    Log.e("Error", "Code: $resCode, Message: $resMsg, ID: $resIdd")

                                }
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    // Handle JSON parsing error
                }

            },
            { error ->
                // Handle the error
                Log.e("Error", error.toString())

            }
        )

        requestQueue.add(jsonObjectRequest)
    }


}
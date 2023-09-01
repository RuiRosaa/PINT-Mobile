package com.hardtinsa

//import com.instamobile.kotlinlogin.R
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.hardtinsa.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CriarOportunidadesActivity : BaseActivity() {

    private lateinit var db: SQLiteDatabase
    private lateinit var btnAdd: Button
    private lateinit var ed_descricao: EditText
    private lateinit var ed_detalhe: EditText
    private lateinit var ed_valor: EditText
    private var selectedClientId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_criar_oportunidades)

        db = BaseDeDados(this).writableDatabase


        if (isInternetConnected()) {
            Toast.makeText(this, "A recarregar Clientes...", Toast.LENGTH_SHORT).show()
            fetchClientesFromServer()
        } else {
            Toast.makeText(this, "Não existe conexão com a Internet", Toast.LENGTH_SHORT).show()
        }


        ed_descricao = findViewById(R.id.et_descricao1)
        ed_detalhe = findViewById(R.id.et_detalhe)
        ed_valor = findViewById(R.id.et_valor)
        btnAdd = findViewById(R.id.btn_save)

        val spinnerArea = findViewById<Spinner>(R.id.spinner_tipo)
        val spinnerTipo = findViewById<Spinner>(R.id.spinner_tipotp)


        val clientList = getClientList()
        Log.d("getClientList", "Number of clients: ${clientList.size}")
        val emailList = clientList.map { it.second }

        val spinnerClientes = findViewById<Spinner>(R.id.spinner_cliente)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, emailList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerClientes.adapter = adapter

        val button1 = findViewById<Button>(R.id.back_button)
        button1.setOnClickListener {
            val intent = Intent(this, OportunidadesActivity::class.java)
            startActivity(intent)
        }



        btnAdd.setOnClickListener {
            val descricao = ed_descricao.text.toString()
            val detalhe = ed_detalhe.text.toString()
            val valorprev = ed_valor.text.toString()
            val userID = Globals.userID
            val idtipo: Int
            val idtpprojeto: Int
            val estado = 0
            val estadoreg = 1
            val publicado = 1
            val email = spinnerClientes.selectedItem.toString()
            val idcliente = getClientIdByEmail(email)
            Log.d("RERERERERERERERERE", "CLIENTETEETETETETETE: $idcliente $email")


            val selectedTipo = spinnerTipo.selectedItem.toString().trim()

             idtpprojeto = when (selectedTipo) {
                "Desenvolvimento WEB" -> 1
                "Desenvolvimento Mobile" -> 2
                "Consultoria" -> 3
                else -> {
                    Log.d("selectedTipo", "Opa selectedTipo value: $selectedTipo")
                    -1
                }
            }

            Log.d("idtpprojeto", "Selected Projeti: $selectedTipo")
            Log.d("idtpprojeto", "idtpprojeto: $idtpprojeto")

            val selectedTipo2 = spinnerArea.selectedItem.toString().lowercase()

            idtipo = when (selectedTipo2) {
                "generico" -> 1
                "software" -> 2
                "consultoria" -> 3
                "redes" -> 4
                "financiero" -> 5
                else -> 0
            }
            Log.d("SelectedTipo", "Selected Tipo: $selectedTipo2")
            Log.d("IDTipo", "IDTipo: $idtipo")

            if (descricao.isNotEmpty() && detalhe.isNotEmpty()) {
                val values = ContentValues()
                values.put("iduser", userID)
                values.put("idtipo", idtipo)
                values.put("idcliente", idcliente)
                values.put("idtpprojeto", idtpprojeto)
                values.put("descricao", descricao)
                values.put("detalhe", detalhe)
                values.put("estado", estado)
                values.put("estadoreg", estadoreg)
                values.put("valorprev", valorprev)
                values.put("publicado", publicado)

                Log.d("Variable", "IDUSER do put: $userID")
                Log.d("Variable", "IDTIPO do put: $idtipo")
                Log.d("Variable", "idcliente do put: $idcliente")
                Log.d("Variable", "IDTPPROJETO do put: $idtpprojeto")
                Log.d("Variable", "DESCRICAO do put: $descricao")
                Log.d("Variable", "DETALHE do put: $detalhe")
                Log.d("Variable", "VALORPREV do put: $valorprev")


                val result = db.insert("Oportunidade", null, values)

                if (result > 0) {
                    Toast.makeText(this, "Sucesso", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, OportunidadesActivity::class.java).apply {
                        putExtra("iduser", userID)
                        putExtra("idtipo", idtipo)
                        putExtra("idcliente", idcliente)
                        putExtra("idtpprojeto", idtpprojeto)
                        putExtra("descricao", descricao)
                        putExtra("detalhe", detalhe)
                        putExtra("estado", estado)
                        putExtra("estadoreg", estadoreg)
                        putExtra("valorprev", valorprev)
                    }
                    startActivity(intent)
                    finish()

                } else {
                    Toast.makeText(this, "Falha", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Escreva a descrição e detalhe", Toast.LENGTH_SHORT).show()
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

    @SuppressLint("Range")
    private fun getClientIdByEmail(email: String): Int? {
        val query = "SELECT idcliente FROM Cliente WHERE email = ?"
        val cursor = db.rawQuery(query, arrayOf(email))
        var clientId: Int? = null

        if (cursor != null && cursor.moveToFirst()) {
            val idColumnIndex = cursor.getColumnIndex("idcliente")
            clientId = cursor.getInt(idColumnIndex)
            cursor.close()
        }
        return clientId
    }


    private fun getClientList(): List<Pair<Int, String>> {
        val clientList = mutableListOf<Pair<Int, String>>()
        // Query the database and retrieve the client data
        val query = "SELECT idcliente, email FROM Cliente"
        val cursor = db.rawQuery(query, null)

        if (cursor != null && cursor.moveToFirst()) {
            val idColumnIndex = cursor.getColumnIndex("idcliente")
            val emailColumnIndex = cursor.getColumnIndex("email")

            do {
                val id = cursor.getInt(idColumnIndex)
                val email = cursor.getString(emailColumnIndex)
                clientList.add(Pair(id, email))
            } while (cursor.moveToNext())

            cursor.close()

            Log.d("getClientList", "Number of clients: $clientList")
        } else {
            Log.d("getClientList", "Client list is empty")
        }
        return clientList
    }
    private fun fetchClientesFromServer() {
        val apiurl = Globals.apiurl
        val token = Globals.token
        val iduser = Globals.userID.toInt()
        val url =
            apiurl + "cliente/listarminhas/$token/$iduser"

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

}














package com.hardtinsa

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.instamobile.kotlinlogin.R
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.Locale

class EditarOportunidadesActivity : BaseActivity() {

    private lateinit var db: SQLiteDatabase
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var btnUpdate: Button
    private lateinit var edDescricao: EditText
    private lateinit var edDetalhe: EditText
    private lateinit var ed_tipo: Spinner
    private lateinit var ed_cliente: Spinner
    private lateinit var edEstado: EditText
    private lateinit var edValorPrev: EditText
    private lateinit var edidtpprojeto: Spinner
    private lateinit var edestado: Spinner
    private var selectedClient: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_oportunidades)

        db = BaseDeDados(this).writableDatabase
        dbHelper = DatabaseHelper(this)

        if (isInternetConnected()) {
            Toast.makeText(this, "A recarregar Clientes...", Toast.LENGTH_SHORT).show()
            fetchClientesFromServer()
        } else {
            Toast.makeText(this, "Não existe conexão com a Internet", Toast.LENGTH_SHORT).show()
        }

        val button1 = findViewById<Button>(R.id.back_button)
        button1.setOnClickListener {
            val intent = Intent(this, OportunidadesActivity::class.java)
            startActivity(intent) }

        val oportunidadeId = intent.getIntExtra("oportunidade_id", -1)
        val descricao = intent.getStringExtra("descricao")
        val detalhe = intent.getStringExtra("detalhe")
        val idtipo = intent.getIntExtra("idtipo", 0)
        val idtpprojeto = intent.getIntExtra("idtpprojeto", 0)
        val cliente = intent.getIntExtra("idcliente", 0)
        Log.d("A ser passado", "IDCLIENTE: $cliente")
        val estado1 = intent.getIntExtra("estado", 0)
        val valorprev = intent.getIntExtra("valorprev", 0)



        val internetStatusImageView = findViewById<ImageView>(R.id.internet_status)
        updateInternetStatusIcon(internetStatusImageView)

        edDescricao = findViewById(R.id.et_descricao1)
        edDetalhe = findViewById(R.id.et_detalhe)
        edValorPrev = findViewById(R.id.et_valorprev)
        ed_tipo = findViewById(R.id.spinner_tipo)
        edestado = findViewById(R.id.spinner_estado)
        edidtpprojeto = findViewById(R.id.spinner_tipotp)

        edDescricao.setText(descricao)
        edDetalhe.setText(detalhe)
        edValorPrev.setText(valorprev.toString())


        val clientList = getClientList()
        Log.d("getClientList", "Number of clients: ${clientList.size}")

        val emailList = clientList.map { it.second }

        val spinnerClientes = findViewById<Spinner>(R.id.spinner_cliente)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, emailList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerClientes.adapter = adapter

        selectedClient?.let { selectedEmail ->
            val selectedIndex = clientList.indexOfFirst { it.second.equals(selectedEmail, ignoreCase = true) }
            if (selectedIndex != -1) {
                spinnerClientes.setSelection(selectedIndex)
            }
        }


        val tipoArray = resources.getStringArray(R.array.tipo_options)
        if (idtipo in 1 until tipoArray.size) {
            ed_tipo.setSelection(idtipo)
        }

        val tipoArray2 = resources.getStringArray(R.array.tipotp_options)
        if (idtpprojeto in 1 until tipoArray2.size) {
            edidtpprojeto.setSelection(idtpprojeto)
        }

        val tipoArray3 = resources.getStringArray(R.array.tipo_estado)
        if (estado1 in 1 until tipoArray3.size) {
            edestado.setSelection(estado1)
        }

        btnUpdate = findViewById(R.id.btn_save)
        btnUpdate.setOnClickListener {
            val newDescricao = edDescricao.text.toString()
            val newDetalhe = edDetalhe.text.toString()
            val estado = edestado.selectedItemPosition
            val idTipo = ed_tipo.selectedItemPosition + 1
            val idtpprojeto = edidtpprojeto.selectedItemPosition + 1
            val valorprevString = edValorPrev.text.toString()
            val valorprev = valorprevString.toIntOrNull() ?: 0
            val estadoreg = 2
            val email = spinnerClientes.selectedItem.toString()
            val idcliente = getClientIdByEmail(email)
            Log.d("TESTEENVIAR CLI", "CLIENTE: $idcliente $email")

            if (newDescricao.isNotEmpty() && newDetalhe.isNotEmpty() ) {
                val result = dbHelper.atualizaOportunidade(oportunidadeId, idcliente,  newDescricao, newDetalhe, idTipo, estadoreg, estado, valorprev, idtpprojeto)
                Log.d("EditarOportunidades", "estado: $estado")
                if (result > 0) {
                    Toast.makeText(this, "Oportunidade atualizada com sucesso", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, OportunidadesActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Erro ao atualizar oportunidade", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }

        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener { menuItem ->
            handleNavigationItemSelected(menuItem) // Call the function to handle the item selection
            true
        }


        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            handleBottomNavigationItemSelected(menuItem) // Call the function from BaseActivity
            true
        }
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
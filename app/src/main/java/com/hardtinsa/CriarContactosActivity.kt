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
import com.example.hardtinsa.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Date

class CriarContactosActivity : BaseActivity() {

    private lateinit var db: SQLiteDatabase
    private lateinit var btnAdd: Button
    private lateinit var ed_nome: EditText
    private lateinit var ed_codpostal: EditText
    private lateinit var ed_morada: EditText
    private lateinit var ed_localidade: EditText
    private lateinit var ed_email: EditText
    private lateinit var ed_telefone: EditText
    private lateinit var ed_telemovel: EditText
    private lateinit var ed_nif: EditText
    private lateinit var ed_maybefunc: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_criar_contactos)

        db = BaseDeDados(this).writableDatabase

        ed_nome = findViewById(R.id.et_nome)
        ed_morada = findViewById(R.id.et_morada)
        ed_codpostal = findViewById(R.id.et_codpostal)
        ed_localidade = findViewById(R.id.et_localidade)
        ed_email = findViewById(R.id.et_email)
        ed_telefone = findViewById(R.id.et_telefone)
        ed_telemovel = findViewById(R.id.et_telemovel)
        ed_nif = findViewById(R.id.et_nif)
        ed_maybefunc = findViewById(R.id.et_func)
        btnAdd = findViewById(R.id.btn_criar)


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
            val nome = ed_nome.text.toString()
            val morada = ed_morada.text.toString()
            val codpostal = ed_codpostal.text.toString()
            val localidade = ed_localidade.text.toString()
            val emailcontacto = ed_email.text.toString()
            val telefone = ed_telefone.text.toString()
            val telemovel = ed_telemovel.text.toString()
            val nif = ed_nif.text.toString()
            val func = ed_maybefunc.text.toString()
            val userID = Globals.userID
            val estado = 0
            val estadoreg = 1
            val email = spinnerClientes.selectedItem.toString()
            val idcliente = getClientIdByEmail(email)
            Log.d("RERERERERERERERERE", "CLIENTETEETETETETETE: $idcliente $email")

            if (nome.isNotEmpty()) {
                val values = ContentValues()
                values.put("nome", nome)
                values.put("morada", morada)
                values.put("idcliente", idcliente)
                values.put("codpostal", codpostal)
                values.put("localidade", localidade)
                values.put("email", emailcontacto)
                values.put("telefone", telefone)
                values.put("telemovel", telemovel)
                values.put("estado", estado)
                values.put("estadoreg", estadoreg)
                values.put("nif", nif)
                values.put("funcao", func)


                Log.d("Variable", "nome do put: $nome")
                Log.d("Variable", "morada do put: $morada")
                Log.d("Variable", "idcliente do put: $idcliente")
                Log.d("Variable", "codpostal do put: $codpostal")
                Log.d("Variable", "localidade do put: $localidade")
                Log.d("Variable", "emailcontacto do put: $emailcontacto")
                Log.d("Variable", "telefone do put: $telefone")
                Log.d("Variable", "telemovel do put: $telemovel")
                Log.d("Variable", "estado do put: $estado")
                Log.d("Variable", "estadoreg do put: $estadoreg")
                Log.d("Variable", "nif do put: $nif")
                Log.d("Variable", "funcao do put: $func")


                val result = db.insert("Contato", null, values)

                if (result > 0) {
                    Toast.makeText(this, "Sucesso", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, ContactosActivity::class.java).apply {
                        putExtra("nome", nome)
                        putExtra("morada", morada)
                        putExtra("idcliente", idcliente)
                        putExtra("codpostal", codpostal)
                        putExtra("localidade", localidade)
                        putExtra("emailcontacto", emailcontacto)
                        putExtra("telefone", telefone)
                        putExtra("telemovel", telemovel)
                        putExtra("estado", estado)
                        putExtra("estadoreg", estadoreg)
                        putExtra("nif", nif)
                    }
                    startActivity(intent)
                    finish()

                } else {
                    Toast.makeText(this, "Falha", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Escreva o nome do contato", Toast.LENGTH_SHORT).show()
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
}
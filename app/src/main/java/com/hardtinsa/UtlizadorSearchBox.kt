package com.hardtinsa

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.hardtinsa.R
import org.json.JSONException

class UtlizadorSearchBox : AppCompatActivity() {
    private lateinit var db: BaseDeDados
    private lateinit var sqliteDatabase: SQLiteDatabase
    private lateinit var editTextSearch: EditText
    private lateinit var recyclerViewResults: RecyclerView
    private lateinit var searchResultAdapter: SearchResultAdapter
    private lateinit var allEmails: List<String>
    private var filteredItems: MutableList<String> = mutableListOf()
    private val listaEmailsAdicionados: MutableList<String> = mutableListOf()
    private val listaIdsUsuarios: MutableList<Int> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_utlizador_search_box)

        db = BaseDeDados(this)
        sqliteDatabase = db.writableDatabase
        fetchUserEmail()

        editTextSearch = findViewById(R.id.editTextSearch)
        recyclerViewResults = findViewById(R.id.recyclerViewResults)
        setResult(Activity.RESULT_CANCELED)

        // Obtenha todos os itens para pesquisa
        allEmails = getEmails()

        // Inicialize o adaptador do RecyclerView com a lista completa de itens
        searchResultAdapter = SearchResultAdapter(allEmails)

        // Defina o layout manager e o adaptador do RecyclerView
        recyclerViewResults.layoutManager = LinearLayoutManager(this)
        recyclerViewResults.adapter = searchResultAdapter

        // Adicione um TextWatcher ao EditText para filtrar os itens com base no texto digitado
        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterItems(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Defina um item click listener no adaptador do RecyclerView
        searchResultAdapter.setOnItemClickListener(object : SearchResultAdapter.OnItemClickListener {
            override fun onItemClick(email: String) {
                editTextSearch.setText(email)
            }
        })

        val buttonVoltar = findViewById<Button>(R.id.buttonVoltar)
        buttonVoltar.setOnClickListener {
            //sqliteDatabase.delete("UserEmail", null, null)
            val intent = Intent()
            intent.putExtra("listaIdsUsuarios", listaIdsUsuarios.toIntArray())
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        val buttonAdicionar = findViewById<Button>(R.id.buttonAdicionar)
        buttonAdicionar.setOnClickListener {
            val email = editTextSearch.text.toString()
            if (email.isNotEmpty()) {
                listaEmailsAdicionados.add(email)
                val idUser = buscarIdUserPorEmail(email)
                if (idUser != null) {
                    listaIdsUsuarios.add(idUser)
                }
                val mensagem = "Utilizador adicionado: id-$idUser email-$email"
                Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchUserEmail(){
        val IDuserString = Globals.userID
        val token = Globals.token
        val apiurl = Globals.apiurl
        val url = apiurl + "utilizador/listarbasic/$token/$IDuserString"

        val requestQueue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                Log.d("Response", response.toString())
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        val data = response.getJSONArray("data")
                        Log.d("Data", data.toString())

                        val sqliteDatabase = BaseDeDados(this)
                        val db = sqliteDatabase.writableDatabase

                        for (i in 0 until data.length()) {
                            val useremail = data.getJSONObject(i)
                            val iduser = useremail.getInt("IDUSER")
                            val nome = useremail.getString("NOME")
                            val email = useremail.getString("EMAIL")
                            val colaborador = useremail.getString("COLABORADOR")

                            if (iduser != 0) {
                                // Verifica se a Entrevista já existe na tabela "Entrevista"
                                val existingEntrevistaQuery =
                                    "SELECT * FROM UserEmail WHERE IDUSER = $iduser"
                                val existingEntrevistaCursor =
                                    db.rawQuery(existingEntrevistaQuery, null)
                                if (existingEntrevistaCursor != null && existingEntrevistaCursor.count > 0) {
                                    existingEntrevistaCursor.close()
                                    continue // Se já existe, pula para a próxima iteração
                                }
                                existingEntrevistaCursor?.close()
                                // Insere os dados na tabela "Entrevista"
                                val useremailValues = ContentValues()
                                useremailValues.put("IDUSER", iduser)
                                useremailValues.put("NOME", nome)
                                useremailValues.put("EMAIL", email)
                                useremailValues.put("COLABORADOR", colaborador)
                                db.insert("UserEmail", null, useremailValues)
                            }
                        }
                        db.close()
                    } else {
                        val errorMsg = response.getString("RES_MSG")
                        Log.d("Error", errorMsg)
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
    }

    private fun filterItems(query: String) {
        filteredItems.clear()
        for (email in allEmails) {
            if (email.contains(query, ignoreCase = true)) {
                filteredItems.add(email)
            }
        }
        searchResultAdapter = SearchResultAdapter(filteredItems)
        recyclerViewResults.adapter = searchResultAdapter
        searchResultAdapter.notifyDataSetChanged()
    }

    @SuppressLint("Range")
    private fun getEmails(): MutableList<String> {
        val emails = mutableListOf<String>()

        val query = "SELECT email FROM UserEmail"
        val cursor = sqliteDatabase.rawQuery(query, null)

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val email = cursor.getString(cursor.getColumnIndex("email"))
                emails.add(email)
            }
        }

        cursor.close()
        return emails
    }

    @SuppressLint("Range")
    private fun buscarIdUserPorEmail(email: String): Int? {
        val query = "SELECT iduser FROM UserEmail WHERE email = ?"
        val cursor = sqliteDatabase.rawQuery(query, arrayOf(email))

        var idUser: Int? = null
        if (cursor != null && cursor.moveToFirst()) {
            idUser = cursor.getInt(cursor.getColumnIndex("iduser"))
        }

        cursor.close()
        return idUser
    }
}

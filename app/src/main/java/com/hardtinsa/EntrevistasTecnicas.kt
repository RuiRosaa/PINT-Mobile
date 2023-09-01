package com.hardtinsa

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.hardtinsa.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class EntrevistasTecnicas : BaseActivity() {
    private lateinit var db: BaseDeDados
    private lateinit var sqliteDatabase: SQLiteDatabase
    private var selectedStartDate: LocalDate? = null
    private var selectedStartTime: LocalTime? = null
    private var selectedEndTime: LocalTime? = null
    private lateinit var btnVoltar: Button
    private lateinit var btnAtualiza: Button


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entrevistas_tecnicas)

        db = BaseDeDados(this)
        sqliteDatabase = db.writableDatabase

        btnVoltar = findViewById(R.id.btnCriar)
        btnVoltar.setOnClickListener {
            val intent = Intent(this, CalendarioActivity::class.java)
            startActivity(intent)
        }

        if (isInternetConnected()) {
            Toast.makeText(this, "A recarregar..", Toast.LENGTH_SHORT)
                .show()
                fetchEntrevistasFromServer()
                showALL()
        } else {
            Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_SHORT).show()
            showALL()
        }

        btnAtualiza = findViewById(R.id.btnAtualiza)
        btnAtualiza.setOnClickListener {
            if (isInternetConnected()) {
                Toast.makeText(this, "A recarregar..", Toast.LENGTH_SHORT)
                    .show()
                    inserirEntrevistaApi()
                    showALL()
            } else {
                Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_SHORT).show()
            }
        }

        val internetStatusImageView: ImageView = findViewById(R.id.internet_status)
        updateInternetStatusIcon(internetStatusImageView)

        drawerLayout = findViewById(R.id.drawerLayout)
        val navView: NavigationView = findViewById(R.id.nav_view)

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
        bottomNavigationView.menu.findItem(R.id.calendario).isChecked = true
    }

    private fun fetchEntrevistasFromServer() {
        val IDuserString = Globals.userID
        val token = Globals.token
        val apiurl = Globals.apiurl
        val url = apiurl + "entrevista/listartec/$token/$IDuserString"
        val IDuserInt = IDuserString.toInt()
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
                            val entrevista = data.getJSONObject(i)
                            val idEntrevista = entrevista.getInt("IDENTREVISTA")
                            val idCandidatura = entrevista.getInt("IDCANDIDATURA")
                            val idUser = entrevista.getInt("IDUSER")
                            val nomeCandidato = entrevista.getString("NOME_CANDIDATO")
                            val externoNome = entrevista.getString("EXTERNO_NOME")
                            val externoEmail = entrevista.getString("EXTERNO_EMAIL")
                            val data_hora_prev = entrevista.getString("DATA_HORA_PREV")
                            val data_hora_inicio = entrevista.getString("DATA_HORA")
                            val data_hora_fim = entrevista.getString("DATA_HORA_FIM")
                            val comentarios = entrevista.getString("COMENTARIOS")
                            val classificacao = entrevista.getInt("CLASSIFICACAO")
                            val idEntrevistador = entrevista.getInt("IDENTREVISTADOR")
                            val estado = entrevista.getInt("ESTADO")
                            val criadodata = entrevista.getString("CRIADODATA")

                            if (idEntrevista != 0) {
                                // Verifica se a Entrevista já existe na tabela "Entrevista"
                                val existingEntrevistaQuery =
                                    "SELECT * FROM Entrevista WHERE IDENTREVISTA = $idEntrevista"
                                val existingEntrevistaCursor =
                                    db.rawQuery(existingEntrevistaQuery, null)
                                if (existingEntrevistaCursor != null && existingEntrevistaCursor.count > 0) {
                                    existingEntrevistaCursor.close()
                                    continue // Se já existe, pula para a próxima iteração
                                }
                                existingEntrevistaCursor?.close()
                                // Insere os dados na tabela "Entrevista"
                                val entrevistaValues = ContentValues()
                                entrevistaValues.put("IDENTREVISTA", idEntrevista)
                                entrevistaValues.put("IDCANDIDATURA", idCandidatura)
                                entrevistaValues.put("IDUSER", idUser)
                                entrevistaValues.put("NOMECANDIDATO", nomeCandidato)
                                entrevistaValues.put("NOMEEXTERNO", externoNome)
                                entrevistaValues.put("DATA_HORA_PREV", data_hora_prev)
                                entrevistaValues.put("DATA_HORA", data_hora_inicio)
                                entrevistaValues.put("DATA_HORA_FIM", data_hora_fim)
                                entrevistaValues.put("COMENTARIOS", comentarios)
                                entrevistaValues.put("CLASSIFICACAO", classificacao)
                                entrevistaValues.put("IDENTREVISTADOR", idEntrevistador)
                                entrevistaValues.put("ESTADO", estado)
                                entrevistaValues.put("ENTREVISTATECNICA", 1)
                                entrevistaValues.put("CRIADODATA", criadodata)
                                db.insert("Entrevista", null, entrevistaValues)
                                Log.d("Entrevista Insert", "IDENTREVISTA: $idEntrevista")
                                Log.d("Entrevista Insert", "IDCANDIDATURA: $idCandidatura")
                                Log.d("Entrevista Insert", "IDUSER: $idUser")
                                Log.d("Entrevista Insert", "NOMECANDIDATO: $nomeCandidato")
                                Log.d("Entrevista Insert", "NOMEEXTERNO: $externoNome")
                                Log.d("Entrevista Insert", "DATA_HORA_PREV: $data_hora_prev")
                                Log.d("Entrevista Insert", "DATA_HORA: $data_hora_inicio")
                                Log.d("Entrevista Insert", "DATA_HORA_FIM: $data_hora_fim")
                                Log.d("Entrevista Insert", "COMENTARIOS: $comentarios")
                                Log.d("Entrevista Insert", "CLASSIFICACAO: $classificacao")
                                Log.d("Entrevista Insert", "IDENTREVISTADOR: $idEntrevistador")
                                Log.d("Entrevista Insert", "ESTADO: $estado")
                                Log.d("Entrevista Insert", "ENTREVISTATECNICA: 1")
                                Log.d("Entrevista Insert", "CRIADODATA: $criadodata")
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

    private fun inserirEntrevistaApi(){
        val token = Globals.token
        val iduser = Globals.userID.toInt()
        val apiurl = Globals.apiurl

        val sqliteDatabase = BaseDeDados(this)
        val db = sqliteDatabase.readableDatabase

        val query = "SELECT identrevista, data_hora, data_hora_fim, comentarios, classificacao, estado FROM Entrevista WHERE estadoreg = 2"
        val cursor = db.rawQuery(query, null)

        if (cursor != null && cursor.moveToFirst()) {
            val idEntrevistaIndex = cursor.getColumnIndex("identrevista")
            val data_horaIndex = cursor.getColumnIndex("data_hora")
            val data_hora_fimIndex = cursor.getColumnIndex("data_hora_fim")
            val comentariosIndex = cursor.getColumnIndex("comentarios")
            val classificacaoIndex = cursor.getColumnIndex("classificacao")
            val estadoIndex = cursor.getColumnIndex("estado")

            val url = apiurl + "entrevista/editar"
            val requestQueue = Volley.newRequestQueue(this)

            do {
                val idEntrevista = cursor.getInt(idEntrevistaIndex)
                val data_hora = cursor.getString(data_horaIndex)
                val data_hora_fim = cursor.getString(data_hora_fimIndex)
                val comentarios = cursor.getString(comentariosIndex)
                val classificacao = cursor.getInt(classificacaoIndex)
                val estado = cursor.getInt(estadoIndex)

                val params = JSONObject()
                Log.d("Variable", "TK: $token")
                params.put("TK", token)
                Log.d("Variable", "IDUSERTK: $iduser")
                params.put("IDUSERTK", iduser)
                Log.d("Variable", "IDENTREVISTA: $idEntrevista")
                params.put("IDENTREVISTA", idEntrevista)
                Log.d("Variable", "DATA_HORA: $data_hora")
                params.put("DATA_HORA", data_hora)
                Log.d("Detalhe", "DATA_HORA_FIM: $data_hora_fim")
                params.put("DATA_HORA_FIM", data_hora_fim)
                Log.d("Detalhe", "COMENTARIOS: $comentarios")
                params.put("COMENTARIOS", comentarios)
                Log.d("Detalhe", "CLASSIFICACAO: $classificacao")
                params.put("CLASSIFICACAO", classificacao)
                Log.d("Detalhe", "ESTADO: $estado")
                params.put("ESTADO", estado)


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
                val deletedRows = db.delete("Entrevista", "estadoreg = 2", null)
                if (deletedRows > 0) {
                    Log.d("Entrevista deleted", "Deleted $deletedRows rows")
                } else {
                    Log.d("Entrev deletion failed", "Failed to delete the Entrevista")
                }
                requestQueue.add(jsonObjectRequest)
            } while (cursor.moveToNext())
            cursor.close()
        }
        db.close()
        fetchEntrevistasFromServer()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showALL(){
        val entrevistas = getEntrevista()
        val listView: ListView = findViewById(R.id.listView)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, entrevistas)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val item = entrevistas[position]
            val identrevista = Calutils.getIdEntrevistaFromListItem(item)
            showEntrevista(identrevista)
        }
        adapter.notifyDataSetChanged()
    }
    @SuppressLint("Range")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getEntrevista(): List<String> {
        val IDuserString = Globals.userID
        val idUser = IDuserString.toInt()
        val entrevistas = mutableListOf<String>()

        val entrevistaQuery = "SELECT identrevista, idcandidatura, nomecandidato, nomeexterno, estado, data_hora, data_hora_fim, comentarios, classificacao FROM Entrevista WHERE entrevistatecnica = 1"

        val entrevistaCursor = sqliteDatabase.rawQuery(entrevistaQuery, null)

        if (entrevistaCursor != null) {
            while (entrevistaCursor.moveToNext()) {
                val identrevista = entrevistaCursor.getInt(entrevistaCursor.getColumnIndex("identrevista"))
                val idcandidatura = entrevistaCursor.getInt(entrevistaCursor.getColumnIndex("idcandidatura"))
                val currentNomeExterno = entrevistaCursor.getString(entrevistaCursor.getColumnIndex("nomeexterno"))
                var nomecandidato = entrevistaCursor.getString(entrevistaCursor.getColumnIndex("nomecandidato"))
                if (currentNomeExterno != null && currentNomeExterno != "null") {
                    nomecandidato = currentNomeExterno
                }
                val dataHora = entrevistaCursor.getString(entrevistaCursor.getColumnIndex("data_hora"))
                val dataHoraFim = entrevistaCursor.getString(entrevistaCursor.getColumnIndex("data_hora_fim"))
                val comentario = entrevistaCursor.getString(entrevistaCursor.getColumnIndex("comentarios"))
                val classificacao = entrevistaCursor.getString(entrevistaCursor.getColumnIndex("classificacao"))
                val estado = entrevistaCursor.getInt(entrevistaCursor.getColumnIndex("estado"))
                val estadoDesc = estado?.let { obterDescricaoEstadoId(it) }

                val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                val parsedDataHora = LocalDateTime.parse(dataHora, dateTimeFormatter)
                val parsedDataHoraFim = LocalDateTime.parse(dataHoraFim, dateTimeFormatter)

                val formattedDataHora = parsedDataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                val formattedDataHoraFim = parsedDataHoraFim.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))

                val entrevista = "Entrevista: $identrevista\n\tCandidatura: $idcandidatura\n\tNome Candidato: $nomecandidato\n\tEstado: $estadoDesc\n\tData/Hora Inicio: $formattedDataHora\n\tData/Hora Fim: $formattedDataHoraFim\n\tComentários: $comentario\n\tClassificação: $classificacao\n"
                entrevistas.add(entrevista)
            }
            entrevistaCursor.close()
        }
        Log.d("Entrevistas", entrevistas.toString())
        return entrevistas
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDateTimePickerEditarEntrevista(identrevista: Int, date: LocalDate) {
        val currentDateTime = LocalDateTime.now()
        val currentHour = currentDateTime.hour
        val currentMinute = currentDateTime.minute

        val startDatePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                val selectedDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
                selectedStartDate = selectedDate
                showTimePickerEditarEntrevista(selectedDate, currentHour, currentMinute, isStartTime = true, identrevista)
            },
            date.year,
            date.monthValue - 1,
            date.dayOfMonth
        )
        startDatePickerDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showTimePickerEditarEntrevista(
        selectedDate: LocalDate,
        currentHour: Int,
        currentMinute: Int,
        isStartTime: Boolean,
        identrevista: Int // Parâmetro adicional para editar uma marcação existente
    ) {
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val selectedTime = LocalTime.of(hourOfDay, minute)
                if (isStartTime) {
                    selectedStartTime = selectedTime
                    showTimePickerEditarEntrevista(selectedDate, currentHour, currentMinute, isStartTime = false, identrevista)
                } else {
                    selectedEndTime = selectedTime
                    editarEntrevista(identrevista) // Chamar a função de editar marcação
                }
            },
            currentHour,
            currentMinute,
            true
        )

        timePickerDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    private fun obterDadosEntrevista(identrevista: Int): Calutils.DadosEntrevistaTecnicas? {
        val IDuserString = Globals.userID
        val idUser = IDuserString.toInt()
        val query = "SELECT identrevista, idcandidatura, nomecandidato, nomeexterno, data_hora, data_hora_fim, estado, comentarios, classificacao FROM Entrevista WHERE identrevista = $identrevista AND entrevistatecnica = 1"
        val cursor = sqliteDatabase.rawQuery(query, null)
        var dadosEntrevista: Calutils.DadosEntrevistaTecnicas? = null

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val currentIdEntrevista = cursor.getInt(cursor.getColumnIndex("identrevista"))
                val currentIdCandidatura = cursor.getInt(cursor.getColumnIndex("idcandidatura"))
                val currentNomeExterno = cursor.getString(cursor.getColumnIndex("nomeexterno"))
                var currentCandidato = cursor.getString(cursor.getColumnIndex("nomecandidato"))
                if (currentNomeExterno != null && currentNomeExterno != "null") {
                    currentCandidato = currentNomeExterno
                }
                val currentDataHora = cursor.getString(cursor.getColumnIndex("data_hora"))
                val currentDataHoraFim = cursor.getString(cursor.getColumnIndex("data_hora_fim"))
                val currentEstado = cursor.getInt(cursor.getColumnIndex("estado"))
                val currentComentarios = cursor.getString(cursor.getColumnIndex("comentarios"))
                val currentClassificacao = cursor.getString(cursor.getColumnIndex("classificacao"))

                val dataHora = LocalDateTime.parse(currentDataHora, DateTimeFormatter.ISO_DATE_TIME)
                val dataHoraFim = LocalDateTime.parse(currentDataHoraFim, DateTimeFormatter.ISO_DATE_TIME)

                dadosEntrevista = Calutils.DadosEntrevistaTecnicas(currentIdEntrevista, currentIdCandidatura, currentCandidato,  dataHora, dataHoraFim, currentEstado, currentComentarios, currentClassificacao)
            }
        }
        cursor?.close()
        return dadosEntrevista
    }

    @SuppressLint("Range")
    private fun obterDescricaoEstadoId(idestado: Int): String? {
        val query = "SELECT descricao FROM EstadoDesc WHERE idestado = $idestado"
        val cursor = sqliteDatabase.rawQuery(query, null)
        var descricao: String? = null

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                descricao = cursor.getString(cursor.getColumnIndex("descricao"))
            }
        }
        cursor?.close()
        return descricao
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showEntrevista(identrevista: Int) {
        val currentEntrevista = obterDadosEntrevista(identrevista)
        val idcandidatura = currentEntrevista?.idcandidatura
        val dataHora = currentEntrevista?.data_hora
        val dataHoraFim = currentEntrevista?.data_hora_fim
        val estado = currentEntrevista?.estado
        val estadoDesc = estado?.let { obterDescricaoEstadoId(it) }
        val comentarios = currentEntrevista?.comentarios
        val classificacao = currentEntrevista?.classificacao
        val nc = currentEntrevista?.nc

        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

        val dataHoraFormatted = dataHora?.format(formatter)
        val dataHoraFimFormatted = dataHoraFim?.format(formatter)
        //tenho de adicionar campos que so vao poder ser vistos pelos colaboradores
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Detalhes da Entrevista")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        val idTextView = TextView(this)
        idTextView.text = "Entrevista: $identrevista"
        layout.addView(idTextView)

        val nomeCandidatoTextView = TextView(this)
        nomeCandidatoTextView.text = "Nome Candidato: $nc"
        layout.addView(nomeCandidatoTextView)

        val dataHoraTextView = TextView(this)
        dataHoraTextView.text = "Data/Hora Inicio: $dataHoraFormatted"
        layout.addView(dataHoraTextView)

        val dataHoraFimTextView = TextView(this)
        dataHoraFimTextView.text = "Data/Hora Fim: $dataHoraFimFormatted"
        layout.addView(dataHoraFimTextView)

        val estadoTextView = TextView(this)
        estadoTextView.text = "Estado: $estadoDesc"
        layout.addView(estadoTextView)

        val comentarioTextView = TextView(this)
        comentarioTextView.text = "Comentario: $comentarios"
        layout.addView(comentarioTextView)

        val classificacaoTextView = TextView(this)
        classificacaoTextView.text = "Classificacao: $classificacao"
        layout.addView(classificacaoTextView)
        builder.setView(layout)
        builder.setPositiveButton("Editar") { _, _ ->
            val currentDate = LocalDate.now()
            showDateTimePickerEditarEntrevista(identrevista, currentDate)
        }

        builder.setView(layout)

        builder.setNegativeButton("Sair") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    @SuppressLint("Range")
    fun getNomeEstadoDesc(): List<String> {
        val estadodesc = mutableListOf<String>()
        val query = "SELECT descricao FROM EstadoDesc"
        val cursor = sqliteDatabase.rawQuery(query, null)

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val nome = cursor.getString(cursor.getColumnIndex("descricao"))
                estadodesc.add(nome)
            }
            cursor.close()
        }
        return estadodesc
    }

    private fun getIndexByEstadoDesc(estadoDesc: String): Int {
        val nomesEstadoDesc = getNomeEstadoDesc()
        return nomesEstadoDesc.indexOf(estadoDesc)
    }

    @SuppressLint("Range")
    private fun getIdEstadoByDescricao(descricao: String): Int {
        val query = "SELECT idestado FROM EstadoDesc WHERE descricao = ?"
        val cursor = sqliteDatabase.rawQuery(query, arrayOf(descricao))

        var idEstado = -1

        if (cursor != null && cursor.moveToFirst()) {
            idEstado = cursor.getInt(cursor.getColumnIndex("idestado"))
            cursor.close()
        }

        return idEstado
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun editarEntrevista(identrevista: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Editar Entrevista")
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        val estadoLabel = TextView(this)
        estadoLabel.text = "Estado"
        layout.addView(estadoLabel)
        val estadoDescSpinner = Spinner(this)
        layout.addView(estadoDescSpinner)
        val comentarioInput = EditText(this)
        comentarioInput.hint = "Comentario"
        layout.addView(comentarioInput)
        val classificacaoLabel = TextView(this)
        classificacaoLabel.text = "Classificação (0-20)"
        layout.addView(classificacaoLabel)
        val classificacaoInput = EditText(this)
        classificacaoInput.hint = "Classificação"
        classificacaoInput.inputType = InputType.TYPE_CLASS_NUMBER
        layout.addView(classificacaoInput)
        builder.setView(layout)

        // Carregar os valores atuais da marcação existente
        val currentEntrevista = obterDadosEntrevista(identrevista)
        val currentEstado = currentEntrevista?.estado
        val currentEstadoDesc = currentEstado?.let { obterDescricaoEstadoId(it) }
        val currentComentario = currentEntrevista?.comentarios
        val currentClassificacao = currentEntrevista?.classificacao
        // Definir os valores iniciais nos componentes de entrada
        currentEstadoDesc?.let { getIndexByEstadoDesc(it) }
            ?.let { estadoDescSpinner.setSelection(it) }
        comentarioInput.setText(currentComentario as CharSequence)
        classificacaoInput.setText(currentClassificacao)

        builder.setPositiveButton("OK") { _, _ ->
            val estadoDesc = estadoDescSpinner.selectedItem.toString()
            val comentarios = comentarioInput.text.toString()
            val classificacao = classificacaoInput.text.toString().toInt()
            val idestado = getIdEstadoByDescricao(estadoDesc)
            if (classificacao in 0..5) {
                if (selectedStartDate != null && selectedStartTime != null && selectedEndTime != null) {
                    val startTime = LocalDateTime.of(selectedStartDate, selectedStartTime)
                    val endTime = LocalDateTime.of(selectedStartDate, selectedEndTime)
                    editaEntrevista(identrevista, startTime, endTime, idestado, comentarios, classificacao)
                } else {
                    Toast.makeText(
                        this,
                        "Erro ao obter a data e hora da marcação existente",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this, "A classificação deve estar entre 0 e 5",
                    Toast.LENGTH_SHORT
                ).show()
                editarEntrevista(identrevista)
            }
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        val nomesEstadoDesc = getNomeEstadoDesc()
        val adapterEstadoDesc =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, nomesEstadoDesc)
        estadoDescSpinner.adapter = adapterEstadoDesc

        builder.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun editaEntrevista(
        entrevistaId: Int,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        estado: Int,
        comentario: String,
        classificacao: Int
    ) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

        val formattedStartDate = startTime.format(formatter)
        val formattedEndDate = endTime.format(formatter)

        Toast.makeText(
            this,
            "Marcação atualizada para $formattedStartDate",
            Toast.LENGTH_LONG
        ).show()

        val values = ContentValues().apply {
            put("data_hora", formattedStartDate)
            put("data_hora_fim", formattedEndDate)
            put("estado", estado)
            put("estadoreg", 2)
            put("comentarios", comentario)
            put("classificacao", classificacao)
        }
        val whereClause = "identrevista = ?"
        val whereArgs = arrayOf(entrevistaId.toString())

        sqliteDatabase.update("Entrevista", values, whereClause, whereArgs)
        showALL()
    }
}
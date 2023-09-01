package com.hardtinsa

//import com.instamobile.kotlinlogin.R
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
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
import com.android.volley.toolbox.JsonArrayRequest
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

class CalendarioActivity : BaseActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var db: BaseDeDados
    private lateinit var sqliteDatabase: SQLiteDatabase
    private lateinit var btnList: Button
    private lateinit var btnEntre: Button
    private lateinit var buttonCriar: Button

    private var selectedStartDate: LocalDate? = null
    private var selectedStartTime: LocalTime? = null
    private var selectedEndTime: LocalTime? = null
    private var listaIdsUsuarios: IntArray? = null
    private lateinit var textViewListaIds: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendario)

        db = BaseDeDados(this)
        sqliteDatabase = db.writableDatabase

        btnList = findViewById(R.id.btnListView)
        btnList.setOnClickListener {
            val intent = Intent(this, CalendarioListView::class.java)
            startActivity(intent)
        }

        if (isInternetConnected()) {
            Toast.makeText(this, "A recarregar..", Toast.LENGTH_SHORT)
                .show() // Example toast message
            fetchMarcacoesFromServer()
            fetchEstadoDescGeralFromServer()
            fetchOportuniGeralFromServer()
            fetchTipoAtivFromServer()
        } else {
            Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_SHORT).show()
        }

        val buttonFetch = findViewById<Button>(R.id.btnAtualiza)
        buttonFetch.setOnClickListener {
            if (isInternetConnected()) {
                Toast.makeText(this, "A recarregar..", Toast.LENGTH_SHORT)
                    .show() // Example toast message
                    MarcacaoInserirApi()
            } else {
                Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_SHORT).show()
            }
        }

        buttonCriar = findViewById<Button>(R.id.btnCriar)
        buttonCriar.setOnClickListener {
            val currentDate = LocalDate.now()
            showAllTipoAtiv()
            showDateTimePicker(currentDate)
        }
        btnEntre = findViewById(R.id.btnEntrevista)
        if (Globals.cargo == "GESTOR DE CONTEÚDOS") {
            btnEntre.visibility = View.VISIBLE
            btnEntre.setOnClickListener {
                val intent = Intent(this, EntrevistasTecnicas::class.java)
                startActivity(intent)
            }
        } else {
            btnEntre.visibility = View.INVISIBLE
        }

        val internetStatusImageView: ImageView = findViewById(R.id.internet_status)
        updateInternetStatusIcon(internetStatusImageView)

        calendarView = findViewById(R.id.calendarView)
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            showMarcacao(selectedDate)
        }

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
/*
        val deleteQuery = "DELETE FROM Utilizador"
        sqliteDatabase.execSQL(deleteQuery)
        val deleteQueryO = "DELETE FROM Oportunidade"
        sqliteDatabase.execSQL(deleteQueryO)

        val deleteQueryE = "DELETE FROM Entrevista"
        sqliteDatabase.execSQL(deleteQueryE)

        val deleteQueryA = "DELETE FROM Atividade"
        sqliteDatabase.execSQL(deleteQueryA)*/

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun UtilizadorSearch(idatividade: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Adicionar utilizadores")
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        textViewListaIds = TextView(this)
        layout.addView(textViewListaIds)

        builder.setNegativeButton("Sair") { dialog, _ ->
            dialog.dismiss()
        }
        builder.setPositiveButton("Continuar") { _, _ ->
            inserirTabelaAtividadeUser(idatividade, listaIdsUsuarios)
        }
        val button = Button(this)
        button.text = "Adicionar"
        layout.addView(button)
        button.setOnClickListener {
            if (isInternetConnected()) {
                val intent = Intent(this, UtlizadorSearchBox::class.java)
                startActivityForResult(intent, 1)
            } else {
                Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setView(layout)
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val idsUsuarios = data?.getIntArrayExtra("listaIdsUsuarios")
            if (idsUsuarios != null) {
                listaIdsUsuarios = idsUsuarios
                val listaUsuariosText = buildString {
                    append("Usuários: ")
                    listaIdsUsuarios!!.forEachIndexed { index, id ->
                        append("user:$id")
                        if (index < listaIdsUsuarios!!.size - 1) {
                            append(", ")
                        }
                    }
                }
               // textViewListaIds.text = listaUsuariosText
            }
        }
    }

    @SuppressLint("Range")
    private fun fetchMarcacoesFromServer() {
        val IDuserString = Globals.userID
        val token = Globals.token
        val apiurl = Globals.apiurl
        val url = apiurl + "agenda/listarminhas/$token/$IDuserString"
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
                            val atividade = data.getJSONObject(i)
                            val iduser = atividade.getInt("IDUSER")
                            val idAtividade = atividade.getInt("IDATIVIDADE")
                            val tipoAtividade = atividade.getString("TIPOATIV")
                            val criadodata = atividade.getString("CRIADODATA")
                            val data_hora_inicio = atividade.getString("DATA_HORA")
                            val data_hora_fim = atividade.getString("DATA_HORA_FIM")
                            val assunto = atividade.getString("ASSUNTO")
                            val detalhe = atividade.getString("DETALHE")
                            val idOportunidade = atividade.getInt("IDOPORTUNIDADE")
                            val oportunidade = atividade.getString("OPORTUNIDADE")
                            val estado = atividade.getInt("ESTADO")
                            val estadoDescricao = atividade.getString("ESTADODESC")
                            val idEntrevista = atividade.getInt("IDENTREVISTA")
                            val idCandidatura = atividade.getInt("IDCANDIDATURA")

                            if (idAtividade != 0) {
                                // Verifica se a Atividade já existe na tabela "Atividade"
                                val existingAtividadeQuery =
                                    "SELECT * FROM Atividade WHERE IDATIVIDADE = $idAtividade"
                                val existingAtividadeCursor =
                                    db.rawQuery(existingAtividadeQuery, null)
                                if (existingAtividadeCursor != null && existingAtividadeCursor.count > 0) {
                                    existingAtividadeCursor.close()
                                    continue // Se já existe, pula para a próxima iteração
                                }
                                existingAtividadeCursor?.close()

                                // Insere os dados na tabela "Atividade"
                                val atividadeValues = ContentValues()
                                atividadeValues.put("IDATIVIDADE", idAtividade)
                                atividadeValues.put("IDUSER", iduser)
                                atividadeValues.put("IDOPORTUNIDADE", idOportunidade)
                                atividadeValues.put("TIPOATIV", tipoAtividade)
                                atividadeValues.put("CRIADODATA", criadodata)
                                atividadeValues.put("DATA_HORA", data_hora_inicio)
                                atividadeValues.put("DATA_HORA_FIM", data_hora_fim)
                                atividadeValues.put("ESTADO", estado)
                                atividadeValues.put("ASSUNTO", assunto)
                                atividadeValues.put("ESTADOREG", 0)
                                atividadeValues.put("DETALHE", detalhe)
                                db.insert("Atividade", null, atividadeValues)
                            }

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
                                entrevistaValues.put("IDUSER", IDuserInt)
                                entrevistaValues.put("DATA_HORA_PREV", "")
                                entrevistaValues.put("DATA_HORA", data_hora_inicio)
                                entrevistaValues.put("DATA_HORA_FIM", data_hora_fim)
                                entrevistaValues.put("COMENTARIOS", "")
                                entrevistaValues.put("CLASSIFICACAO", 0)
                                entrevistaValues.put("IDENTREVISTADOR", 0)
                                entrevistaValues.put("ESTADO", estado)
                                entrevistaValues.put("CRIADODATA", "")
                                db.insert("Entrevista", null, entrevistaValues)
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

    private fun fetchOportuniGeralFromServer() {
        val IDuserString = Globals.userID
        val token = Globals.token
        val apiurl = Globals.apiurl
        val url = apiurl + "agenda/listarminhas/$token/$IDuserString"
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
                            val atividade = data.getJSONObject(i)
                            val idOportunidade = atividade.getInt("IDOPORTUNIDADE")
                            val oportunidade = atividade.getString("OPORTUNIDADE")
                            if (idOportunidade != 0) {
                                // Verifica se a Oportunidade já existe na tabela "Oportunidade"
                                val existingOportunidadeQuery =
                                    "SELECT * FROM Oportunidade WHERE IDOPORTUNIDADE = $idOportunidade"
                                val existingOportunidadeCursor =
                                    db.rawQuery(existingOportunidadeQuery, null)
                                if (existingOportunidadeCursor != null && existingOportunidadeCursor.count > 0) {
                                    existingOportunidadeCursor.close()
                                    continue // Se já existe, pula para a próxima iteração
                                }
                                existingOportunidadeCursor?.close()
                                // Insere os dados na tabela "Oportunidade"
                                val oportunidadeValues = ContentValues()
                                oportunidadeValues.put("idoportunidade", idOportunidade)
                                oportunidadeValues.put("idtipo", 0)
                                oportunidadeValues.put("idcliente", 0)
                                oportunidadeValues.put("iduser", IDuserInt)
                                oportunidadeValues.put("idtpprojeto", 0)
                                oportunidadeValues.put("descricao", oportunidade)
                                oportunidadeValues.put("detalhe", "")
                                oportunidadeValues.put("estado", 0)
                                oportunidadeValues.put("estadoreg", 0)
                                oportunidadeValues.put("criadodata", "")
                                oportunidadeValues.put("publicado", 0)
                                db.insert("Oportunidade", null, oportunidadeValues)
                            }
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    // Lidar com o erro de análise JSON
                }
            },
            { error ->
                // Lidar com o erro
                Log.e("Error", error.toString())
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )
        requestQueue.add(jsonObjectRequest)
    }

    private fun fetchEstadoDescGeralFromServer() {
        val apiurl = Globals.apiurl
        val url = apiurl + "estado/listar/Oportunidade"
        val requestQueue = Volley.newRequestQueue(this)
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                Log.d("Response", response.toString())
                try {
                    for (i in 0 until response.length()) {
                        val estadodesc = response.getJSONObject(i)
                        val idestado = estadodesc.getInt("IDESTADO")
                        val descricao = estadodesc.getString("DESCRICAO")

                        if (idestado != 0) {
                            val existingestadodescQuery =
                                "SELECT * FROM EstadoDesc WHERE IDESTADO = $idestado"
                            val existingestadodescCursor =
                                sqliteDatabase.rawQuery(existingestadodescQuery, null)
                            if (existingestadodescCursor != null && existingestadodescCursor.count > 0) {
                                existingestadodescCursor.close()
                                continue
                            }
                            existingestadodescCursor?.close()

                            val estadodescValues = ContentValues()
                            estadodescValues.put("IDESTADO", idestado)
                            estadodescValues.put("DESCRICAO", descricao)

                            sqliteDatabase.insert("EstadoDesc", null, estadodescValues)
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    // Lidar com o erro de análise JSON
                }
            }, { error ->
                // Lidar com o erro
                Log.e("Error", error.toString())
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )
        requestQueue.add(jsonArrayRequest)
    }

    private fun fetchTipoAtivFromServer() {
        val IDuserString = Globals.userID
        val token = Globals.token
        val apiurl = Globals.apiurl
        val url = apiurl + "oportunidade/tipoativlistar/$token/$IDuserString"
        val requestQueue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                Log.d("Response", response.toString())
                try {
                    val bd = sqliteDatabase // Obtain a reference to the database

                    val data = response.getJSONArray("data")
                    for (i in 0 until data.length()) {
                        val tipoAtiv = data.getJSONObject(i).getString("TIPOATIV")

                        val existingTipoAtivQuery =
                            "SELECT * FROM tipoativ WHERE nomeativ = '$tipoAtiv'"
                        val existingTipoAtivCursor = bd.rawQuery(existingTipoAtivQuery, null)
                        if (existingTipoAtivCursor != null && existingTipoAtivCursor.count > 0) {
                            existingTipoAtivCursor.close()
                            continue
                        }
                        existingTipoAtivCursor?.close()

                        val tipoAtivValues = ContentValues()
                        tipoAtivValues.put("nomeativ", tipoAtiv)

                        bd.insert("tipoativ", null, tipoAtivValues)
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



    @SuppressLint("Range")
    private fun showAllTipoAtiv() {
        val query = "SELECT * FROM tipoativ"
        val cursor = sqliteDatabase.rawQuery(query, null)

        cursor?.let {
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndex("idtipoativ"))
                    val nomeAtividade = cursor.getString(cursor.getColumnIndex("nomeativ"))

                    Log.d("TipoAtiv", "ID: $id, Nome: $nomeAtividade")
                } while (cursor.moveToNext())
            } else {
                Log.d("TipoAtiv", "A tabela tipoativ está vazia.")
            }
            cursor.close()
        }
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
    private fun showMarcacao(selectedDate: LocalDate) {
        val appointments = getAtividadePorDia(selectedDate)
        val entrevistas = getEntrevistaPorDia(selectedDate)

        val combinedList = mutableListOf<String>()
        combinedList.addAll(appointments)
        combinedList.addAll(entrevistas)

        val listView: ListView = findViewById(R.id.listView)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, combinedList)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val item = combinedList[position]
            val idatividade = Calutils.getIdAtividadeFromListItem(item)
            val identrevista = Calutils.getIdEntrevistaFromListItem(item)

            if (item in appointments) {
                showAtividade(idatividade)
            } else if (item in entrevistas) {
                showEntrevista(identrevista)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    private fun obterDadosAtividade(idatividade: Int): Calutils.DadosAtividade? {
        val IDuserString = Globals.userID
        val idUser = IDuserString.toInt()
        val query = "SELECT idoportunidade, tipoativ, estado, assunto, detalhe, data_hora, data_hora_fim FROM Atividade WHERE idatividade = $idatividade AND iduser = $idUser AND estadoreg <> 3"
        val cursor = sqliteDatabase.rawQuery(query, null)
        var dadosAtividade: Calutils.DadosAtividade? = null

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val currentIdOportunidade = cursor.getInt(cursor.getColumnIndex("idoportunidade"))
                val currentTipoAtiv = cursor.getString(cursor.getColumnIndex("tipoativ"))
                val currentEstado = cursor.getInt(cursor.getColumnIndex("estado"))
                val currentAssunto = cursor.getString(cursor.getColumnIndex("assunto"))
                val currentDetalhe = cursor.getString(cursor.getColumnIndex("detalhe"))
                val currentStartTime = cursor.getString(cursor.getColumnIndex("data_hora"))
                val currentEndTime = cursor.getString(cursor.getColumnIndex("data_hora_fim"))

                val startTime = LocalDateTime.parse(currentStartTime, DateTimeFormatter.ISO_DATE_TIME)
                val endTime = LocalDateTime.parse(currentEndTime, DateTimeFormatter.ISO_DATE_TIME)

                dadosAtividade = Calutils.DadosAtividade(currentIdOportunidade, currentTipoAtiv, currentEstado, currentAssunto, currentDetalhe, startTime, endTime)
            }
        }
        cursor?.close()
        return dadosAtividade
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    private fun obterDadosEntrevista(identrevista: Int): Calutils.DadosEntrevista? {
        val IDuserString = Globals.userID
        val idUser = IDuserString.toInt()
        val query = "SELECT data_hora, data_hora_fim, estado FROM Entrevista WHERE identrevista = $identrevista AND iduser = $idUser AND entrevistatecnica = 0"
        val cursor = sqliteDatabase.rawQuery(query, null)
        var dadosEntrevista: Calutils.DadosEntrevista? = null

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val currentDataHora = cursor.getString(cursor.getColumnIndex("data_hora"))
                val currentDataHoraFim = cursor.getString(cursor.getColumnIndex("data_hora_fim"))
                val currentEstado = cursor.getInt(cursor.getColumnIndex("estado"))


                val dataHora = LocalDateTime.parse(currentDataHora, DateTimeFormatter.ISO_DATE_TIME)
                val dataHoraFim = LocalDateTime.parse(currentDataHoraFim, DateTimeFormatter.ISO_DATE_TIME)

                dadosEntrevista = Calutils.DadosEntrevista(dataHora, dataHoraFim, currentEstado)
            }
        }
        cursor?.close()
        return dadosEntrevista
    }

    @SuppressLint("Range")
    private fun buscarDescricaoPorIdOportunidade(idOportunidade: Int): String? {
        val query = "SELECT descricao FROM Oportunidade WHERE idoportunidade = $idOportunidade"
        val cursor = sqliteDatabase.rawQuery(query, null)
        var descricao: String? = null
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                descricao = cursor.getString(cursor.getColumnIndex("descricao"))
            }
            cursor.close()
        }
        return descricao
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showAtividade(idatividade: Int) {
        val currentMarcacao = obterDadosAtividade(idatividade)
        val tipoativ = currentMarcacao?.tipoativ
        val oportunidade = currentMarcacao?.idoportunidade
        val descricaoOportunidade = oportunidade?.let { buscarDescricaoPorIdOportunidade(it) }
        val dataHora = currentMarcacao?.startTime
        val dataHoraFim = currentMarcacao?.endTime
        val estado = currentMarcacao?.estado
        val estadoDesc = estado?.let { obterDescricaoEstadoId(it) }
        val assunto = currentMarcacao?.assunto
        val detalhe = currentMarcacao?.detalhe
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

        val dataHoraFormatted = dataHora?.format(formatter)
        val dataHoraFimFormatted = dataHoraFim?.format(formatter)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Detalhes da Atividade")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        val tipoativTextView = TextView(this)
        tipoativTextView.text = "Tipo de Atividade: $tipoativ"
        layout.addView(tipoativTextView)

        val dataHoraTextView = TextView(this)
        dataHoraTextView.text = "Data/Hora Inicio: $dataHoraFormatted"
        layout.addView(dataHoraTextView)

        val dataHoraFimTextView = TextView(this)
        dataHoraFimTextView.text = "Data/Hora Fim: $dataHoraFimFormatted"
        layout.addView(dataHoraFimTextView)

        val oportunidadeTextView = TextView(this)
        oportunidadeTextView.text = "Oportunidade: $descricaoOportunidade"
        layout.addView(oportunidadeTextView)

        val estadoTextView = TextView(this)
        estadoTextView.text = "Estado: $estadoDesc"
        layout.addView(estadoTextView)

        val assuntoTextView = TextView(this)
        assuntoTextView.text = "Assunto: $assunto"
        layout.addView(assuntoTextView)

        val detalheTextView = TextView(this)
        detalheTextView.text = "Detalhe: $detalhe"
        layout.addView(detalheTextView)

        builder.setView(layout)

        builder.setNegativeButton("Sair") { dialog, _ ->
            dialog.dismiss()
        }

        builder.setNeutralButton("Eliminar") { _, _ ->
            eliminarAtividade(idatividade)
        }

        builder.setPositiveButton("Editar") { _, _ ->
            val currentDate = LocalDate.now()
            showDateTimePickerEditar(idatividade, currentDate)
        }

        builder.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showEntrevista(identrevista: Int) {
        val currentEntrevista = obterDadosEntrevista(identrevista)
        val dataHora = currentEntrevista?.data_hora
        val dataHoraFim = currentEntrevista?.data_hora_fim
        val estado = currentEntrevista?.estado
        val estadoDesc = estado?.let { obterDescricaoEstadoId(it) }
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        val dataHoraFormatted = dataHora?.format(formatter)
        val dataHoraFimFormatted = dataHoraFim?.format(formatter)
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Detalhes da Entrevista")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        val idTextView = TextView(this)
        idTextView.text = "Entrevista"
        layout.addView(idTextView)

        val dataHoraTextView = TextView(this)
        dataHoraTextView.text = "Data/Hora Inicio: $dataHoraFormatted"
        layout.addView(dataHoraTextView)

        val dataHoraFimTextView = TextView(this)
        dataHoraFimTextView.text = "Data/Hora Fim: $dataHoraFimFormatted"
        layout.addView(dataHoraFimTextView)

        val estadoTextView = TextView(this)
        estadoTextView.text = "Estado: $estadoDesc"
        layout.addView(estadoTextView)

        builder.setView(layout)

        builder.setNegativeButton("Sair") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    @SuppressLint("Range")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getAtividadePorDia(date: LocalDate): List<String> {
        val IDuserString = Globals.userID
        val idUser = IDuserString.toInt()
        val appointments = mutableListOf<String>()
        val formattedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

        val query = "SELECT idatividade, tipoativ, assunto, data_hora, data_hora_fim FROM Atividade WHERE DATE(data_hora) = ? AND iduser = $idUser AND estadoreg <> 3"
        val selectionArgs = arrayOf(formattedDate)

        val cursor = sqliteDatabase.rawQuery(query, selectionArgs)

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val idatividade = cursor.getInt(cursor.getColumnIndex("idatividade"))
                val tipoativ = cursor.getString(cursor.getColumnIndex("tipoativ"))
                val assunto = cursor.getString(cursor.getColumnIndex("assunto"))
                val dataHora = cursor.getString(cursor.getColumnIndex("data_hora"))
                val dataHoraFim = cursor.getString(cursor.getColumnIndex("data_hora_fim"))

                val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                val parsedDataHora = LocalDateTime.parse(dataHora, dateTimeFormatter)
                val parsedDataHoraFim = LocalDateTime.parse(dataHoraFim, dateTimeFormatter)

                val formattedDataHora = parsedDataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                val formattedDataHoraFim = parsedDataHoraFim.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))

                val appointment = "Tipo de Atividade:$idatividade $tipoativ\nAssunto: $assunto\nData/Hora Inicio: $formattedDataHora\nData/Hora Fim: $formattedDataHoraFim"
                appointments.add(appointment)
            }
            cursor.close()
        }
        return appointments
    }

    @SuppressLint("Range")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getEntrevistaPorDia(date: LocalDate): List<String> {
        val IDuserString = Globals.userID
        val idUser = IDuserString.toInt()
        val entrevistas = mutableListOf<String>()
        val formattedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

        val entrevistaQuery = "SELECT identrevista, data_hora, data_hora_fim FROM Entrevista WHERE DATE(data_hora) = ? AND iduser = $idUser AND entrevistatecnica = 0"
        val selectionArgs = arrayOf(formattedDate)

        val entrevistaCursor = sqliteDatabase.rawQuery(entrevistaQuery, selectionArgs)

        if (entrevistaCursor != null) {
            while (entrevistaCursor.moveToNext()) {
                val identrevista = entrevistaCursor.getInt(entrevistaCursor.getColumnIndex("identrevista"))
                val dataHora = entrevistaCursor.getString(entrevistaCursor.getColumnIndex("data_hora"))
                val dataHoraFim = entrevistaCursor.getString(entrevistaCursor.getColumnIndex("data_hora_fim"))

                val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                val parsedDataHora = LocalDateTime.parse(dataHora, dateTimeFormatter)
                val parsedDataHoraFim = LocalDateTime.parse(dataHoraFim, dateTimeFormatter)

                val formattedDataHora = parsedDataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                val formattedDataHoraFim = parsedDataHoraFim.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))

                val entrevista = "Entrevista: $identrevista\nData/Hora Inicio: $formattedDataHora\nData/Hora Fim: $formattedDataHoraFim"
                entrevistas.add(entrevista)
            }
            entrevistaCursor.close()
        }

        return entrevistas
    }

    private fun eliminarAtividade(idatividade: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Eliminar Atividade")
        builder.setMessage("Tem certeza de que deseja eliminar esta atividade?")

        builder.setPositiveButton("Eliminar") { _, _ ->
            val sqliteDatabase = BaseDeDados(this)
            val db = sqliteDatabase.readableDatabase

            val values = ContentValues().apply {
                put("estadoreg", 3)
            }

            val selection = "idatividade = ?"
            val selectionArgs = arrayOf(idatividade.toString())

            val rowsUpdated = db.update("Atividade", values, selection, selectionArgs)

            if (rowsUpdated > 0) {
                Toast.makeText(this, "Atividade eliminada com sucesso", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Falha ao eliminar a atividade", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDateTimePicker(date: LocalDate) {
        val currentDateTime = LocalDateTime.now()
        val currentHour = currentDateTime.hour
        val currentMinute = currentDateTime.minute

        val startDatePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                val selectedDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
                selectedStartDate = selectedDate
                showTimePicker(selectedDate, currentHour, currentMinute, isStartTime = true)
            },
            date.year,
            date.monthValue - 1,
            date.dayOfMonth
        )

        startDatePickerDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDateTimePickerEditar(idatividade: Int, date: LocalDate) {
        val currentDateTime = LocalDateTime.now()
        val currentHour = currentDateTime.hour
        val currentMinute = currentDateTime.minute

        val startDatePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                val selectedDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
                selectedStartDate = selectedDate
                showTimePickerEditar(selectedDate, currentHour, currentMinute, isStartTime = true, idatividade)
            },
            date.year,
            date.monthValue - 1,
            date.dayOfMonth
        )

        startDatePickerDialog.show()
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun showTimePicker(
        selectedDate: LocalDate,
        currentHour: Int,
        currentMinute: Int,
        isStartTime: Boolean,

    ) {
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val selectedTime = LocalTime.of(hourOfDay, minute)
                if (isStartTime) {
                    selectedStartTime = selectedTime
                    showTimePicker(selectedDate, currentHour, currentMinute, isStartTime = false)
                } else {
                    selectedEndTime = selectedTime
                    createMarcacao()
                }
            },
            currentHour,
            currentMinute,
            true
        )

        timePickerDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showTimePickerEditar(
        selectedDate: LocalDate,
        currentHour: Int,
        currentMinute: Int,
        isStartTime: Boolean,
        idatividade: Int // Parâmetro adicional para editar uma marcação existente
    ) {
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val selectedTime = LocalTime.of(hourOfDay, minute)
                if (isStartTime) {
                    selectedStartTime = selectedTime
                    showTimePickerEditar(selectedDate, currentHour, currentMinute, isStartTime = false, idatividade)
                } else {
                    selectedEndTime = selectedTime
                    editarMarcacao(idatividade) // Chamar a função de editar marcação
                }
            },
            currentHour,
            currentMinute,
            true
        )

        timePickerDialog.show()
    }



    @SuppressLint("Range")
    private fun getNomeTipoAtividade(): List<String> {
        val nomes = mutableListOf<String>()
        val query = "SELECT nomeativ FROM tipoativ"
        val cursor = sqliteDatabase.rawQuery(query, null)

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val nome = cursor.getString(cursor.getColumnIndex("nomeativ"))
                nomes.add(nome)
            }
            cursor.close()
        }

        return nomes
    }
    private fun getIndexByTipoAtividade(tipoativ: String): Int {
        val nomesTipoAtividade = getNomeTipoAtividade()
        return nomesTipoAtividade.indexOf(tipoativ)
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

    @SuppressLint("Range")
    private fun getIdDescricaoOportunidade(): List<Pair<Int, String>> {
        val IDuserString = Globals.userID
        val idUser = IDuserString.toInt()
        val idDescList = mutableListOf<Pair<Int, String>>()
        val query = "SELECT idoportunidade, descricao FROM Oportunidade"
        val cursor = sqliteDatabase.rawQuery(query, null)

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndex("idoportunidade"))
                val descricao = cursor.getString(cursor.getColumnIndex("descricao"))
                idDescList.add(Pair(id, descricao))
            }
            cursor.close()
        }

        return idDescList
    }

    private fun getIndexByIdOportunidade(idOportunidade: Int): Int {
        val idDescList = getIdDescricaoOportunidade()
        for (i in idDescList.indices) {
            val (id, _) = idDescList[i]
            if (id == idOportunidade) {
                return i
            }
        }
        return -1
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun editarMarcacao(idatividade: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Editar Marcação")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        val tipoAtividadeLabel = TextView(this)
        tipoAtividadeLabel.text = "Tipo Atividade"
        layout.addView(tipoAtividadeLabel)

        val tipoativSpinner = Spinner(this)
        layout.addView(tipoativSpinner)

        val estadoLabel = TextView(this)
        estadoLabel.text = "Estado"
        layout.addView(estadoLabel)

        val estadoDescSpinner = Spinner(this)
        layout.addView(estadoDescSpinner)

        val oportunidadeLabel = TextView(this)
        oportunidadeLabel.text = "Oportunidade"
        layout.addView(oportunidadeLabel)

        val oportunidadeSpinner = Spinner(this)
        layout.addView(oportunidadeSpinner)

        val assuntoInput = EditText(this)
        assuntoInput.hint = "Assunto"
        layout.addView(assuntoInput)

        val detalheInput = EditText(this)
        detalheInput.hint = "Detalhes"
        layout.addView(detalheInput)

        builder.setView(layout)

        // Carregar os valores atuais da marcação existente
        val currentMarcacao = obterDadosAtividade(idatividade)
        val currentTipoAtiv = currentMarcacao?.tipoativ
        val currentEstado = currentMarcacao?.estado
        val currentEstadoDesc = currentEstado?.let { obterDescricaoEstadoId(it) }
        val currentAssunto = currentMarcacao?.assunto
        val currentDetalhe = currentMarcacao?.detalhe
        val currentOportunidadeId = currentMarcacao?.idoportunidade


        // Definir os valores iniciais nos componentes de entrada
        currentTipoAtiv?.let { getIndexByTipoAtividade(it) }
            ?.let { tipoativSpinner.setSelection(it) }

        currentEstadoDesc?.let { getIndexByEstadoDesc(it) }
            ?.let { estadoDescSpinner.setSelection(it) }

        currentOportunidadeId?.let { getIndexByIdOportunidade(it) }
            ?.let { oportunidadeSpinner.setSelection(it) }


        assuntoInput.setText(currentAssunto as CharSequence)
        detalheInput.setText(currentDetalhe as CharSequence)

        builder.setPositiveButton("OK") { _, _ ->
            val tipoativ = tipoativSpinner.selectedItem.toString()
            val estadoDesc = estadoDescSpinner.selectedItem.toString()
            val oportunidadeIdDesc = oportunidadeSpinner.selectedItem as? Pair<Int, String>
            val assunto = assuntoInput.text.toString()
            val detalhe = detalheInput.text.toString()

            val idestado = getIdEstadoByDescricao(estadoDesc)
            val idoportunidade = oportunidadeIdDesc?.first?.toInt()!!

            if (selectedStartDate != null && selectedStartTime != null && selectedEndTime != null) {
                val startTime = LocalDateTime.of(selectedStartDate, selectedStartTime)
                val endTime = LocalDateTime.of(selectedStartDate, selectedEndTime)
                editaMarcacao(
                    idatividade,
                    idoportunidade,
                    startTime,
                    endTime,
                    tipoativ,
                    idestado,
                    assunto,
                    detalhe
                )
            } else {
                Toast.makeText(
                    this,
                    "Erro ao obter a data e hora da marcação existente",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        val nomesTipoAtividade = getNomeTipoAtividade()
        val adapterTipoAtividade =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, nomesTipoAtividade)
        tipoativSpinner.adapter = adapterTipoAtividade

        val nomesEstadoDesc = getNomeEstadoDesc()
        val adapterEstadoDesc =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, nomesEstadoDesc)
        estadoDescSpinner.adapter = adapterEstadoDesc

        val idDescList = getIdDescricaoOportunidade()
        val adapterOportunidade = ArrayAdapter(this, android.R.layout.simple_spinner_item, idDescList)
        adapterOportunidade.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        oportunidadeSpinner.adapter = adapterOportunidade

        builder.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun editaMarcacao(atividadeId: Int, opportunityId: Int, startTime: LocalDateTime,
                              endTime: LocalDateTime,
                              tipoativ: String,
                              estado: Int,
                              assunto: String,
                              detalhe: String
    ) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val formattedStartDate = startTime.format(formatter)
        val formattedEndDate = endTime.format(formatter)

        val values = ContentValues().apply {
            put("idoportunidade", opportunityId)
            put("tipoativ", tipoativ)
            put("data_hora", formattedStartDate)
            put("data_hora_fim", formattedEndDate)
            put("estado", estado)
            put("estadoreg", 2)
            put("assunto", assunto)
            put("detalhe", detalhe)
        }
        val whereClause = "idatividade = ?"
        val whereArgs = arrayOf(atividadeId.toString())
        val rowsAffected = sqliteDatabase.update("Atividade", values, whereClause, whereArgs)
        if (rowsAffected > 0) {
            Toast.makeText(
                this,
                "Marcação atualizada para $formattedStartDate",
                Toast.LENGTH_LONG
            ).show()
            UtilizadorSearch(atividadeId)
        } else {
            Toast.makeText(
                this,
                "Não foi possível editar a marcação",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createMarcacao() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Criar Marcação")
        val IDuserString = Globals.userID
        val idUser = IDuserString.toInt()

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        val tipoAtividadeLabel = TextView(this)
        tipoAtividadeLabel.text = "Tipo Atividade"
        layout.addView(tipoAtividadeLabel)

        val tipoativSpinner = Spinner(this)
        layout.addView(tipoativSpinner)

        val estadoLabel = TextView(this)
        estadoLabel.text = "Estado"
        layout.addView(estadoLabel)

        val estadoDescSpinner = Spinner(this)
        layout.addView(estadoDescSpinner)

        val oportunidadeLabel = TextView(this)
        oportunidadeLabel.text = "Oportunidade"
        layout.addView(oportunidadeLabel)

        val oportunidadeSpinner = Spinner(this)
        layout.addView(oportunidadeSpinner)

        val assuntoInput = EditText(this)
        assuntoInput.hint = "Assunto"
        layout.addView(assuntoInput)

        val detalheInput = EditText(this)
        detalheInput.hint = "Detalhes"
        layout.addView(detalheInput)

        builder.setView(layout)

        builder.setPositiveButton("OK") { _, _ ->
            val tipoativ = tipoativSpinner.selectedItem.toString()
            val estadoDesc = estadoDescSpinner.selectedItem.toString()
            val oportunidadeIdDesc = oportunidadeSpinner.selectedItem as? Pair<Int, String>
            val estado = getIdEstadoByDescricao(estadoDesc)
            val assunto = assuntoInput.text.toString()
            val detalhe = detalheInput.text.toString()
            val idoportunidade = oportunidadeIdDesc?.first?.toInt()!!
            if (selectedStartDate != null && selectedStartTime != null && selectedEndTime != null) {
                val startTime = LocalDateTime.of(selectedStartDate, selectedStartTime)
                val endTime = LocalDateTime.of(selectedStartDate, selectedEndTime)
                fazerMarcacao(idUser, idoportunidade, startTime, endTime,  tipoativ, estado, assunto, detalhe)
            } else {
                Toast.makeText(this, "Por favor, selecione a data e hora de início e fim", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        val nomesTipoAtividade = getNomeTipoAtividade()
        val adapterTipoAtividade = ArrayAdapter(this, android.R.layout.simple_spinner_item, nomesTipoAtividade)
        tipoativSpinner.adapter = adapterTipoAtividade

        val nomesEstadoDesc = getNomeEstadoDesc()
        val adapterEstadoDesc = ArrayAdapter(this, android.R.layout.simple_spinner_item, nomesEstadoDesc)
        estadoDescSpinner.adapter = adapterEstadoDesc

        val idDescList = getIdDescricaoOportunidade()
        val adapterOportunidade = ArrayAdapter(this, android.R.layout.simple_spinner_item, idDescList)
        adapterOportunidade.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        oportunidadeSpinner.adapter = adapterOportunidade

        builder.show()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun fazerMarcacao(
        idUser: Int,
        opportunityId: Int,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        tipoativ: String,
        estado: Int,
        assunto: String,
        detalhe: String
    ) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val formattedStartDate = startTime.format(formatter)
        val formattedEndDate = endTime.format(formatter)

        // Save the new appointment to the database
        val currentDate = LocalDate.now()
        val formattedCurrentDate = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

        val values = ContentValues().apply {
            put("iduser", idUser)
            put("idoportunidade", opportunityId)
            put("tipoativ", tipoativ)
            put("criadodata", formattedCurrentDate)
            put("data_hora", formattedStartDate)
            put("data_hora_fim", formattedEndDate)
            put("estado", estado)
            put("estadoreg", 1)
            put("assunto", assunto)
            put("detalhe", detalhe)
        }
        val idatividade = sqliteDatabase.insert("Atividade", null, values).toInt()
        if (idatividade != -1) {
            Toast.makeText(
                this,
                "Marcação criada para $formattedStartDate",
                Toast.LENGTH_LONG
            ).show()
            UtilizadorSearch(idatividade)
        } else {
            Toast.makeText(
                this,
                "Erro ao inserir na tabela Atividade",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @SuppressLint("Range")
    private fun obterIdUsersPorIdAtividade(idatividade: Int): String {
        val IDuserString = Globals.userID
        val idUser = IDuserString.toInt()
        val query = "SELECT iduser FROM Atividade_User WHERE idatividade = ?"
        val cursor = sqliteDatabase.rawQuery(query, arrayOf(idatividade.toString()))
        val idUsers = mutableListOf<Int>()

        while (cursor.moveToNext()) {
            val iduser = cursor.getInt(cursor.getColumnIndex("iduser"))
            idUsers.add(iduser)
        }
        cursor.close()
        idUsers.add(idUser)
        val idUsersString = idUsers.joinToString("_")
        Log.d("obterIdUsersPorIdAtivid", "idUsers: $idUsersString")
        return idUsersString
    }

    private fun inserirTabelaAtividadeUser(idatividade: Int, listaIdsUsuarios: IntArray?) {
        listaIdsUsuarios?.let { ids ->
            for (id in ids) {
                val existsQuery = "SELECT COUNT(*) FROM Atividade_User WHERE iduser = ? AND idatividade = ?"
                val existsArgs = arrayOf(id.toString(), idatividade.toString())
                val cursor = sqliteDatabase.rawQuery(existsQuery, existsArgs)

                cursor.moveToFirst()
                val count = cursor.getInt(0)
                cursor.close()

                if (count == 0) {
                    val values = ContentValues().apply {
                        put("iduser", id)
                        put("idatividade", idatividade)
                    }
                    sqliteDatabase.insert("Atividade_User", null, values)
                    Log.d("Atividade_User_Insert", "Inserido na tabela Atividade_User: user=$id, idatividade=$idatividade")
                } else {
                    Log.d("Atividade_User_Insert", "Usuário já adicionado à atividade: user=$id, idatividade=$idatividade")
                }
            }
        }
    }

    private fun MarcacaoInserirApi() {
        val iduser = Globals.userID
        val apiurl = Globals.apiurl
        val token = Globals.token

        val sqliteDatabase = BaseDeDados(this)
        val db = sqliteDatabase.readableDatabase

        val query =
            "SELECT idatividade, iduser, idoportunidade, tipoativ, criadodata, data_hora, data_hora_fim, estado, assunto, detalhe FROM Atividade WHERE estadoreg = 1"
        val cursor = db.rawQuery(query, null)

        if (cursor != null && cursor.moveToFirst()) {
            val idAtividadeIndex = cursor.getColumnIndex("idatividade")
            val idUserDonoIndex = cursor.getColumnIndex("iduser")
            val idOportunidadeIndex = cursor.getColumnIndex("idoportunidade")
            val tipoativIndex = cursor.getColumnIndex("tipoativ")
            val criadoDataIndex = cursor.getColumnIndex("criadodata")
            val dataHoraIndex = cursor.getColumnIndex("data_hora")
            val dataHoraFimIndex = cursor.getColumnIndex("data_hora_fim")
            val estadoIndex = cursor.getColumnIndex("estado")
            val assuntoIndex = cursor.getColumnIndex("assunto")
            val detalheIndex = cursor.getColumnIndex("detalhe")

            val url = apiurl + "atividade/inserir"
            val requestQueue = Volley.newRequestQueue(this)


            do {
                val idAtividade= cursor.getInt(idAtividadeIndex)
                val idUserDono= cursor.getInt(idUserDonoIndex)
                val idOportunidade = cursor.getInt(idOportunidadeIndex)
                val tipoativ = cursor.getString(tipoativIndex)
                val estado = cursor.getInt(estadoIndex)
                val assunto = cursor.getString(assuntoIndex)
                val detalhe = cursor.getString(detalheIndex)
                val criadoData = cursor.getString(criadoDataIndex)
                val dataHora = cursor.getString(dataHoraIndex)
                val dataHoraFim = cursor.getString(dataHoraFimIndex)
                val destinat: String = obterIdUsersPorIdAtividade(idAtividade)
                Log.d("Response", "Destinatarios: $destinat")
                Log.d("Response", "IdAtividade: $idAtividade")


                val params = JSONObject()
                params.put("TK", token)
                params.put("IDUSERTK", iduser)
                params.put("IDOPORTUNIDADE", idOportunidade)
                params.put("IDUSER", idUserDono)
                params.put("TIPOATIV", tipoativ)
                params.put("DATA_HORA", dataHora)
                params.put("DATA_HORA_FIM", dataHoraFim)
                params.put("ASSUNTO", assunto)
                params.put("DETALHE", detalhe)
                params.put("DESTINAT", destinat)
                params.put("ESTADO", estado)
                //params.put("CRIADODATA", criadoData)
                Log.d("Response", "IDUSERTK $iduser")
                Log.d("Response", "IDUSER $idUserDono")


                val jsonObjectRequest = JsonObjectRequest(
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

                val deletedRows = db.delete("Atividade", "estadoreg = 1", null)
                if (deletedRows > 0) {
                    Log.d("Atividade deleted", "Deleted $deletedRows rows")
                } else {
                    Log.d("Ativid deletion failed", "Failed to delete the atividade")
                }

                requestQueue.add(jsonObjectRequest)
            } while (cursor.moveToNext())

            cursor.close()
        }

        db.close()
        fetchMarcacoesFromServer()
    }
}

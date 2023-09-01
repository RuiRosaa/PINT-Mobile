package com.hardtinsa

import android.annotation.SuppressLint
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.instamobile.kotlinlogin.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CalendarioListView : BaseActivity() {

    private lateinit var currentDate: LocalDate
    private lateinit var currentViewMode: ViewMode

    private lateinit var listViewDay: ListView
    private lateinit var listViewWeek: ListView
    private lateinit var listViewMonth: ListView
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnDay: Button
    private lateinit var btnWeek: Button
    private lateinit var btnMonth: Button
    private lateinit var btnNext: ImageButton

    private lateinit var db: BaseDeDados
    private lateinit var sqliteDatabase: SQLiteDatabase

    private enum class ViewMode {
        DAY, WEEK, MONTH
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendario_list_view)

        db = BaseDeDados(this)
        sqliteDatabase = db.writableDatabase

        currentDate = LocalDate.now()
        currentViewMode = ViewMode.DAY

        listViewDay = findViewById(R.id.listViewDay)
        listViewWeek = findViewById(R.id.listViewWeek)
        listViewMonth = findViewById(R.id.listViewMonth)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnDay = findViewById(R.id.btnDay)
        btnWeek = findViewById(R.id.btnWeek)
        btnMonth = findViewById(R.id.btnMonth)
        btnNext = findViewById(R.id.btnNext)

        btnPrevious.setOnClickListener {
            navigateToPrevious()
        }

        btnDay.setOnClickListener {
            setViewMode(ViewMode.DAY)
        }

        btnWeek.setOnClickListener {
            setViewMode(ViewMode.WEEK)
        }

        btnMonth.setOnClickListener {
            setViewMode(ViewMode.MONTH)
        }

        btnNext.setOnClickListener {
            navigateToNext()
        }

        updateCalendarView()

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

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setViewMode(viewMode: ViewMode) {
        currentViewMode = viewMode
        updateCalendarView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun navigateToPrevious() {
        when (currentViewMode) {
            ViewMode.DAY -> currentDate = currentDate.minusDays(1)
            ViewMode.WEEK -> currentDate = currentDate.minusWeeks(1)
            ViewMode.MONTH -> currentDate = currentDate.minusMonths(1)
        }
        updateCalendarView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun navigateToNext() {
        when (currentViewMode) {
            ViewMode.DAY -> currentDate = currentDate.plusDays(1)
            ViewMode.WEEK -> currentDate = currentDate.plusWeeks(1)
            ViewMode.MONTH -> currentDate = currentDate.plusMonths(1)
        }
        updateCalendarView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateCalendarView() {
        when (currentViewMode) {
            ViewMode.DAY -> {
                listViewDay.visibility = ListView.VISIBLE
                listViewWeek.visibility = ListView.GONE
                listViewMonth.visibility = ListView.GONE
                val (formattedDate, appointments) = getMarcacaoPorDia(currentDate)
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf(formattedDate) + appointments)
                listViewDay.adapter = adapter
            }
            ViewMode.WEEK -> {
                listViewDay.visibility = ListView.GONE
                listViewWeek.visibility = ListView.VISIBLE
                listViewMonth.visibility = ListView.GONE
                val (formattedDate, appointments) = getMarcacaoPorSemana(currentDate)
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf(formattedDate) + appointments)
                listViewWeek.adapter = adapter
            }
            ViewMode.MONTH -> {
                listViewDay.visibility = ListView.GONE
                listViewWeek.visibility = ListView.GONE
                listViewMonth.visibility = ListView.VISIBLE
                val (formattedDate, appointments) = getMarcacaoPorMes(currentDate)
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf(formattedDate) + appointments)
                listViewMonth.adapter = adapter
            }
        }
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
    private fun formatarData(date: LocalDate): String {
        val formatoDiaSemana = DateTimeFormatter.ofPattern("EEEE")
        val formatoDiaMesAno = DateTimeFormatter.ofPattern("dd MMMM yyyy")
        val formatoMes = DateTimeFormatter.ofPattern("MMMM yyyy")

        return when (currentViewMode) {
            ViewMode.DAY -> "${date.format(formatoDiaSemana)}, ${date.format(formatoDiaMesAno)}"
            ViewMode.WEEK -> {
                val startDate = date.with(java.time.DayOfWeek.MONDAY)
                val endDate = startDate.plusDays(6)
                "${startDate.format(formatoDiaMesAno)} - ${endDate.format(formatoDiaMesAno)}"
            }
            ViewMode.MONTH -> date.format(formatoMes)
        }
    }

    @SuppressLint("Range")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getMarcacaoPorDia(date: LocalDate): Pair<String, List<String>> {
        val IDuserString = Globals.userID
        val idUser = IDuserString.toInt()
        val appointments = mutableListOf<String>()

        val formattedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

        val query = "SELECT idoportunidade, tipoativ, estado, assunto, detalhe, data_hora, data_hora_fim FROM Atividade WHERE DATE(data_hora) = ? AND iduser = $idUser"
        val selectionArgs = arrayOf(formattedDate)

        val cursor = sqliteDatabase.rawQuery(query, selectionArgs)

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val idoportunidade = cursor.getInt(cursor.getColumnIndex("idoportunidade"))
                val descricaoOportunidade = buscarDescricaoPorIdOportunidade(idoportunidade)
                val tipoativ = cursor.getString(cursor.getColumnIndex("tipoativ"))
                val estado = cursor.getInt(cursor.getColumnIndex("estado"))
                val estadoDesc = obterDescricaoEstadoId(estado)
                val assunto = cursor.getString(cursor.getColumnIndex("assunto"))
                val detalhe = cursor.getString(cursor.getColumnIndex("detalhe"))
                val dataHora = cursor.getString(cursor.getColumnIndex("data_hora"))
                val dataHoraFim = cursor.getString(cursor.getColumnIndex("data_hora_fim"))

                val formattedDataHora = LocalDateTime.parse(dataHora, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                val formattedDataHoraFim = LocalDateTime.parse(dataHoraFim, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))

                val appointment = "Tipo de Atividade: $tipoativ\nOportunidade: $descricaoOportunidade\nEstado: $estadoDesc\nData/Hora: $formattedDataHora\nData/Hora Fim: $formattedDataHoraFim\nAssunto: $assunto\nDetalhe: $detalhe"
                appointments.add(appointment)
            }
            cursor.close()
        }

        val entrevistaQuery = "SELECT identrevista, data_hora, data_hora_fim, estado, comentarios, classificacao FROM Entrevista WHERE DATE(data_hora) = ? AND iduser =  $idUser"
        val entrevistaCursor = sqliteDatabase.rawQuery(entrevistaQuery, selectionArgs)

        if (entrevistaCursor != null) {
            while (entrevistaCursor.moveToNext()) {
                val identrevista = entrevistaCursor.getInt(entrevistaCursor.getColumnIndex("identrevista"))
                val dataHora = entrevistaCursor.getString(entrevistaCursor.getColumnIndex("data_hora"))
                val dataHoraFim = entrevistaCursor.getString(entrevistaCursor.getColumnIndex("data_hora_fim"))
                val estado = entrevistaCursor.getInt(entrevistaCursor.getColumnIndex("estado"))
                val estadoDesc = obterDescricaoEstadoId(estado)
                val comentario = entrevistaCursor.getString(entrevistaCursor.getColumnIndex("comentarios"))
                val classificacao = entrevistaCursor.getInt(entrevistaCursor.getColumnIndex("classificacao"))

                val formattedDataHora = LocalDateTime.parse(dataHora, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                val formattedDataHoraFim = LocalDateTime.parse(dataHoraFim, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))

                if(Globals.cargo == "ADMINISTRADOR"){
                    val appointment = "Entrevista: $identrevista\nEstado: $estadoDesc\nData/Hora Inicio: $formattedDataHora\nData/Hora Fim: $formattedDataHoraFim\nComentários: $comentario\nClassificação: $classificacao"
                    appointments.add(appointment)
                }else{
                val appointment = "Entrevista: $identrevista\nData/Hora Inicio: $formattedDataHora\nData/Hora Fim: $formattedDataHoraFim"
                appointments.add(appointment)
                }
            }
            entrevistaCursor.close()
        }

        val formattedDateString = formatarData(date)

        return Pair(formattedDateString, appointments)
    }

    @SuppressLint("Range")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getMarcacaoPorSemana(date: LocalDate): Pair<String, List<String>> {
        val IDuserString = Globals.userID
        val idUser = IDuserString.toInt()
        val appointments = mutableListOf<String>()

        val startDate = date.with(java.time.DayOfWeek.MONDAY)
        val endDate = startDate.plusDays(6)

        val formattedStartDate = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val formattedEndDate = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

        val atividadeQuery = "SELECT idoportunidade, tipoativ, estado, assunto, detalhe, data_hora, data_hora_fim FROM Atividade WHERE DATE(data_hora) BETWEEN ? AND ? AND iduser = $idUser"
        val atividadeSelectionArgs = arrayOf(formattedStartDate, formattedEndDate)

        val atividadeCursor = sqliteDatabase.rawQuery(atividadeQuery, atividadeSelectionArgs)

        if (atividadeCursor != null) {
            while (atividadeCursor.moveToNext()) {
                val idoportunidade = atividadeCursor.getInt(atividadeCursor.getColumnIndex("idoportunidade"))
                val descricaoOportunidade = buscarDescricaoPorIdOportunidade(idoportunidade)
                val tipoativ = atividadeCursor.getString(atividadeCursor.getColumnIndex("tipoativ"))
                val estado = atividadeCursor.getInt(atividadeCursor.getColumnIndex("estado"))
                val estadoDesc = obterDescricaoEstadoId(estado)
                val assunto = atividadeCursor.getString(atividadeCursor.getColumnIndex("assunto"))
                val detalhe = atividadeCursor.getString(atividadeCursor.getColumnIndex("detalhe"))
                val dataHora = atividadeCursor.getString(atividadeCursor.getColumnIndex("data_hora"))
                val dataHoraFim = atividadeCursor.getString(atividadeCursor.getColumnIndex("data_hora_fim"))

                val formattedDataHora = LocalDateTime.parse(dataHora, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                val formattedDataHoraFim = LocalDateTime.parse(dataHoraFim, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))

                val appointment = "Tipo de Atividade: $tipoativ\nOportunidade: $descricaoOportunidade\nEstado: $estadoDesc\nData/Hora: $formattedDataHora\nData/Hora Fim: $formattedDataHoraFim\nAssunto: $assunto\nDetalhe: $detalhe"
                appointments.add(appointment)
            }
            atividadeCursor.close()
        }

        val entrevistaQuery = "SELECT identrevista, data_hora, data_hora_fim, estado, comentarios, classificacao FROM Entrevista WHERE DATE(data_hora) BETWEEN ? AND ? AND iduser = $idUser"
        val entrevistaSelectionArgs = arrayOf(formattedStartDate, formattedEndDate)

        val entrevistaCursor = sqliteDatabase.rawQuery(entrevistaQuery, entrevistaSelectionArgs)

        if (entrevistaCursor != null) {
            while (entrevistaCursor.moveToNext()) {
                val identrevista = entrevistaCursor.getInt(entrevistaCursor.getColumnIndex("identrevista"))
                val dataHora = entrevistaCursor.getString(entrevistaCursor.getColumnIndex("data_hora"))
                val dataHoraFim = entrevistaCursor.getString(entrevistaCursor.getColumnIndex("data_hora_fim"))
                val estado = entrevistaCursor.getInt(entrevistaCursor.getColumnIndex("estado"))
                val estadoDesc = obterDescricaoEstadoId(estado)
                val comentario = entrevistaCursor.getString(entrevistaCursor.getColumnIndex("comentarios"))
                val classificacao = entrevistaCursor.getInt(entrevistaCursor.getColumnIndex("classificacao"))

                val formattedDataHora = LocalDateTime.parse(dataHora, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                val formattedDataHoraFim = LocalDateTime.parse(dataHoraFim, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))

                if(Globals.cargo == "ADMINISTRADOR"){
                    val appointment = "Entrevista: $identrevista\nEstado: $estadoDesc\nData/Hora Inicio: $formattedDataHora\nData/Hora Fim: $formattedDataHoraFim\nComentários: $comentario\nClassificação: $classificacao"
                    appointments.add(appointment)
                }else{
                    val appointment = "Entrevista: $identrevista\nData/Hora Inicio: $formattedDataHora\nData/Hora Fim: $formattedDataHoraFim"
                    appointments.add(appointment)
                }
            }
            entrevistaCursor.close()
        }

        val formattedDateString = formatarData(date)

        return Pair(formattedDateString, appointments)
    }

    @SuppressLint("Range")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getMarcacaoPorMes(date: LocalDate): Pair<String, List<String>> {
        val IDuserString = Globals.userID
        val idUser = IDuserString.toInt()
        val appointments = mutableListOf<String>()

        val formattedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

        val atividadeQuery = "SELECT idoportunidade, tipoativ, estado, assunto, detalhe, data_hora, data_hora_fim FROM Atividade WHERE strftime('%m', data_hora) = ? AND iduser = $idUser"
        val atividadeSelectionArgs = arrayOf(formattedDate.substring(5, 7))

        val atividadeCursor = sqliteDatabase.rawQuery(atividadeQuery, atividadeSelectionArgs)

        if (atividadeCursor != null) {
            while (atividadeCursor.moveToNext()) {
                val idoportunidade = atividadeCursor.getInt(atividadeCursor.getColumnIndex("idoportunidade"))
                val descricaoOportunidade = buscarDescricaoPorIdOportunidade(idoportunidade)
                val tipoativ = atividadeCursor.getString(atividadeCursor.getColumnIndex("tipoativ"))
                val estado = atividadeCursor.getInt(atividadeCursor.getColumnIndex("estado"))
                val estadoDesc = obterDescricaoEstadoId(estado)
                val assunto = atividadeCursor.getString(atividadeCursor.getColumnIndex("assunto"))
                val detalhe = atividadeCursor.getString(atividadeCursor.getColumnIndex("detalhe"))
                val dataHora = atividadeCursor.getString(atividadeCursor.getColumnIndex("data_hora"))
                val dataHoraFim = atividadeCursor.getString(atividadeCursor.getColumnIndex("data_hora_fim"))


                val formattedDataHora = LocalDateTime.parse(dataHora, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                val formattedDataHoraFim = LocalDateTime.parse(dataHoraFim, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))

                val appointment = "Tipo de Atividade: $tipoativ\nOportunidade: $descricaoOportunidade\nEstado: $estadoDesc\nData/Hora: $formattedDataHora\nData/Hora Fim: $formattedDataHoraFim\nAssunto: $assunto\nDetalhe: $detalhe"
                appointments.add(appointment)
            }
            atividadeCursor.close()
        }

        val entrevistaQuery = "SELECT identrevista, data_hora, data_hora_fim, estado, comentarios, classificacao FROM Entrevista WHERE strftime('%m', data_hora) = ? AND iduser = $idUser"
        val entrevistaSelectionArgs = arrayOf(formattedDate.substring(5, 7))

        val entrevistaCursor = sqliteDatabase.rawQuery(entrevistaQuery, entrevistaSelectionArgs)

        if (entrevistaCursor != null) {
            while (entrevistaCursor.moveToNext()) {
                val identrevista = entrevistaCursor.getInt(entrevistaCursor.getColumnIndex("identrevista"))
                val dataHora = entrevistaCursor.getString(entrevistaCursor.getColumnIndex("data_hora"))
                val dataHoraFim = entrevistaCursor.getString(entrevistaCursor.getColumnIndex("data_hora_fim"))
                val estado = entrevistaCursor.getInt(entrevistaCursor.getColumnIndex("estado"))
                val estadoDesc = obterDescricaoEstadoId(estado)
                val comentario = entrevistaCursor.getString(entrevistaCursor.getColumnIndex("comentarios"))
                val classificacao = entrevistaCursor.getInt(entrevistaCursor.getColumnIndex("classificacao"))

                val formattedDataHora = LocalDateTime.parse(dataHora, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                val formattedDataHoraFim = LocalDateTime.parse(dataHoraFim, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))

                if(Globals.cargo == "ADMINISTRADOR"){
                    val appointment = "Entrevista: $identrevista\nEstado: $estadoDesc\nData/Hora Inicio: $formattedDataHora\nData/Hora Fim: $formattedDataHoraFim\nComentários: $comentario\nClassificação: $classificacao"
                    appointments.add(appointment)
                }else{
                    val appointment = "Entrevista: $identrevista\nData/Hora Inicio: $formattedDataHora\nData/Hora Fim: $formattedDataHoraFim"
                    appointments.add(appointment)
                }
            }
            entrevistaCursor.close()
        }

        val formattedDateString = formatarData(date)

        return Pair(formattedDateString, appointments)
    }

}
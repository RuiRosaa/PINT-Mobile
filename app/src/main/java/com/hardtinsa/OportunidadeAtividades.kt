package com.hardtinsa

//import com.instamobile.kotlinlogin.R
import android.annotation.SuppressLint
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.example.hardtinsa.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class OportunidadeAtividades  : BaseActivity(){
    private lateinit var db: BaseDeDados
    private lateinit var sqliteDatabase: SQLiteDatabase
    private lateinit var listView: ListView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oportunidade_atividades)

        db = BaseDeDados(this)
        sqliteDatabase = db.writableDatabase
        listView = findViewById(R.id.list_view)

        val idoportunidade = intent.getIntExtra("oportunidade_id", 0)
        Log.d("IntentgetAtividade", "oportunidade_id_atividade: $idoportunidade")
        if (idoportunidade != null) {
            updateCalendarView(idoportunidade)
        }
        // estes intents é para efetuar comunicação entre botões de navagação
        val idcliente = intent.getIntExtra("idcliente", 0)
        val descricao = intent.getStringExtra("descricao")
        val detalhe = intent.getStringExtra("detalhe")
        val estado = intent.getIntExtra("estado", 0)
        val idtpprojeto = intent.getIntExtra("idtpprojeto", 0)
        val valorprev = intent.getIntExtra("valorprev", 0)
        val idtipo = intent.getIntExtra("idtipo", 0)

        val internetStatusImageView = findViewById<ImageView>(R.id.internet_status)
        updateInternetStatusIcon(internetStatusImageView)

        val buttonC = findViewById<Button>(R.id.btnDetalhes)
        buttonC.setOnClickListener {
            val intent = Intent(this, Oportunidades_detalhe::class.java)
            intent.putExtra("oportunidade_id", idoportunidade)
            intent.putExtra("idcliente", idcliente)
            intent.putExtra("descricao", descricao)
            intent.putExtra("detalhe", detalhe)
            intent.putExtra("estado", estado)
            intent.putExtra("idtpprojeto", idtpprojeto)
            intent.putExtra("valorprev", valorprev)
            intent.putExtra("idtipo", idtipo)
            startActivity(intent)
        }
        val buttoncon = findViewById<Button>(R.id.btnContactos)
        buttoncon.setOnClickListener {
            val intent = Intent(this, Oportunidade_Contactos::class.java)
            intent.putExtra("oportunidade_id", idoportunidade)
            intent.putExtra("idcliente", idcliente)
            intent.putExtra("descricao", descricao)
            intent.putExtra("detalhe", detalhe)
            intent.putExtra("estado", estado)
            intent.putExtra("idtpprojeto", idtpprojeto)
            intent.putExtra("valorprev", valorprev)
            intent.putExtra("idtipo", idtipo)
            startActivity(intent)
        }
        val back = findViewById<Button>(R.id.btn_back)
        back.setOnClickListener {
            val intent = Intent(this, OportunidadesActivity::class.java)
            startActivity(intent)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateCalendarView(idoportunidade: Int) {
        val appointments = getMarcacaoPorIdOportunidade(idoportunidade)

        if (appointments.isNotEmpty()) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, appointments)
            listView.adapter = adapter
        } else {
            val emptyMessage = "Não existe nenhuma atividade."
            val emptyAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayOf(emptyMessage))
            listView.adapter = emptyAdapter
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

    @SuppressLint("Range")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getMarcacaoPorIdOportunidade(idOportunidade: Int): List<String> {
        val appointments = mutableListOf<String>()

        val query = "SELECT tipoativ, estado, assunto, detalhe, data_hora, data_hora_fim FROM Atividade WHERE idoportunidade = ?"
        val selectionArgs = arrayOf(idOportunidade.toString())

        val cursor = sqliteDatabase.rawQuery(query, selectionArgs)

        if (cursor != null) {
            while (cursor.moveToNext()) {
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

                val appointment = "Tipo de Atividade: $tipoativ\nEstado: $estadoDesc\nData/Hora: $formattedDataHora\nData/Hora Fim: $formattedDataHoraFim\nAssunto: $assunto\nDetalhe: $detalhe"
                appointments.add(appointment)
            }
            cursor.close()
        }

        return appointments
    }

}
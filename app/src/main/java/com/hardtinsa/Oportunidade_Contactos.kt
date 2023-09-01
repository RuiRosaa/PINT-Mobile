package com.hardtinsa

import android.annotation.SuppressLint
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.instamobile.kotlinlogin.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Oportunidade_Contactos: BaseActivity() {
    private lateinit var db: BaseDeDados
    private lateinit var sqliteDatabase: SQLiteDatabase
    private lateinit var contactosRecyclerView: RecyclerView
    private lateinit var listView: ListView
    private lateinit var Oportuni_ContactosAdapter: Oportuni_ContactosAdapter
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oportunidade_contactos)

        db = BaseDeDados(this)
        sqliteDatabase = db.writableDatabase
        //contactosRecyclerView = findViewById(R.id.rv_contactos)
        listView = findViewById(R.id.list_view)
        /*Oportuni_ContactosAdapter = Oportuni_ContactosAdapter(mutableListOf())


        val contactosList = db.todasOportunidades(db.readableDatabase).toMutableList()

      //  Oportuni_ContactosAdapter = Oportuni_ContactosAdapter(contactosList)
        contactosRecyclerView.adapter = Oportuni_ContactosAdapter
        contactosRecyclerView.layoutManager = LinearLayoutManager(this)*/

        // estes intents é para efetuar comunicação entre botões de navagação
        val oportunidadeId = intent.getIntExtra("oportunidade_id", 0)
        val idcliente = intent.getIntExtra("idcliente", 0)
        if (idcliente != null) {
            updateListView(idcliente)
        }
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
            intent.putExtra("oportunidade_id", oportunidadeId)
            intent.putExtra("idcliente", idcliente)
            intent.putExtra("descricao", descricao)
            intent.putExtra("detalhe", detalhe)
            intent.putExtra("estado", estado)
            intent.putExtra("idtpprojeto", idtpprojeto)
            intent.putExtra("valorprev", valorprev)
            intent.putExtra("idtipo", idtipo)
            startActivity(intent)
        }
        val buttonA = findViewById<Button>(R.id.btn_ativ)
        buttonA.setOnClickListener {
            val intent = Intent(this, OportunidadeAtividades::class.java)
            intent.putExtra("oportunidade_id", oportunidadeId)
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
    private fun updateListView(idCliente: Int) {
        val contactos = getContactosPorIdCliente(idCliente)

        if (contactos.isNotEmpty()) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, contactos)
            listView.adapter = adapter
        } else {
            val emptyMessage = "Não existe nenhum contacto associado a este cliente."
            val emptyAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayOf(emptyMessage))
            listView.adapter = emptyAdapter
        }
    }

    //Pesquisa na tabela contato usando o id cliente, retorna uma lista com todos os contactos associados ao idcliente
    @SuppressLint("Range")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getContactosPorIdCliente(idCliente: Int): List<String> {
        val contactos = mutableListOf<String>()

        val query = "SELECT nome, funcao, email, telefone, telemovel FROM Contato WHERE idcliente = ?"
        val selectionArgs = arrayOf(idCliente.toString())
        val cursor = sqliteDatabase.rawQuery(query, selectionArgs)

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val nome = cursor.getString(cursor.getColumnIndex("nome"))
                val funcao = cursor.getString(cursor.getColumnIndex("funcao"))
                val email = cursor.getString(cursor.getColumnIndex("email"))
                val telefone = cursor.getString(cursor.getColumnIndex("telefone"))
                val telemovel = cursor.getString(cursor.getColumnIndex("telemovel"))

                val contacto = "Nome: $nome\nFunção: $funcao\nEmail: $email\nTelefone: $telefone\nTelemovel: $telemovel\n"
                contactos.add(contacto)
            }
            cursor.close()
        }
        return contactos
    }
}
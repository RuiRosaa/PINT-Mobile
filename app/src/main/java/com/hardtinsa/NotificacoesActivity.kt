package com.hardtinsa

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.sft4all.BeneficiosActivity
import com.sft4all.CalendarioActivity
import com.sft4all.ClientesActivity
import com.instamobile.kotlinlogin.R
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Locale


class NotificacoesActivity : BaseActivity() {

    private lateinit var db: BaseDeDados
    private lateinit var notiRecyclerView: RecyclerView
    private lateinit var notiAdapter: NotificacoesAdapter
    private lateinit var sqliteDatabase: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notificacoes)

        db = BaseDeDados(this)
        sqliteDatabase = db.writableDatabase
        notiRecyclerView = findViewById(R.id.rv_noti)
        notiAdapter = NotificacoesAdapter(mutableListOf())

        // Fetch the list of beneficts from the database
        val notificacoesList = db.todasNotificacoes(db.readableDatabase).toMutableList()


            if (isInternetConnected()) {
                Toast.makeText(this, "A recarregar..", Toast.LENGTH_SHORT)
                    .show() // Example toast message
                fetchNotificacoesFromServer()
            } else {
                Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_SHORT).show()
            }


        //update ao adapter
        notiAdapter = NotificacoesAdapter(notificacoesList)
        notiRecyclerView.adapter = notiAdapter
        notiRecyclerView.layoutManager = LinearLayoutManager(this)


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

        val internetStatusImageView = findViewById<ImageView>(R.id.internet_status)
        updateInternetStatusIcon(internetStatusImageView)

        bottomNavigationView.menu.findItem(R.id.alertas).isChecked = true
    }

    private fun fetchNotificacoesFromServer() {
        val IDuser = Globals.userID
        val apiurl = Globals.apiurl
        val url = apiurl + "aviso/listarminhas/$IDuser"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val dataArray = response.getJSONArray("data")

                val notificacoesList = mutableListOf<Notificacoes>()

                val db = BaseDeDados(this)
                val writableDb = db.writableDatabase

                // Dá delete á lista de noti
                writableDb.delete("Notificacoes", null, null)

                for (i in 0 until dataArray.length()) {
                    val jsonObject = dataArray.getJSONObject(i)

                    // Parse the notification data from the JSONObject
                    val idaviso = jsonObject.getInt("IDAVISO")
                    val iduser = jsonObject.getInt("IDUSER")
                    val descricao = jsonObject.getString("DESCRICAO")
                    val estado = jsonObject.getString("ESTADO")
                    val criadodata = jsonObject.getString("CRIADODATA")
                    val publicado = jsonObject.getBoolean("PUBLICADO")
                    val data_inicio = jsonObject.getString("DATA_INICIO")
                    val data_fim = jsonObject.getString("DATA_FIM")
                    val generico = jsonObject.getBoolean("GENERICO")

                    val dateTimeFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    val parsedDataInicio = dateTimeFormatter.parse(data_inicio)
                    val parsedDataDataFim = dateTimeFormatter.parse(data_fim)
                    val formattedDataInicio = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(parsedDataInicio)
                    val formattedDataFim = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(parsedDataDataFim)


                    val notificacoes = Notificacoes(idaviso, iduser, descricao, estado, criadodata, publicado, formattedDataInicio, formattedDataFim, generico)
                    notificacoesList.add(notificacoes)

                    val contentValues = ContentValues().apply {
                        put("idaviso", notificacoes.idaviso)
                        put("iduser", notificacoes.iduser)
                        put("descricao", notificacoes.descricao)
                        put("estado", notificacoes.estado)
                        put("criadodata", notificacoes.criadodata)
                        put("publicado", notificacoes.publicado)
                        put("data_inicio", notificacoes.data_inicio)
                        put("data_fim", notificacoes.data_fim)
                        put("generico", notificacoes.generico)
                    }
                    writableDb.insert("Notificacoes", null, contentValues)
                }

                runOnUiThread {
                    notiAdapter.clearNotificacoes()
                    notiAdapter.addNotificacoes(notificacoesList)
                    notiAdapter.notifyDataSetChanged()
                }
            },
            { error ->
                // Handle error
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("NotificacoesActivity", "Error fetching notifications: ${error.message}", error)
            }
        )
        // Add the request to the Volley request queue
        Volley.newRequestQueue(this).add(request)
    }


}
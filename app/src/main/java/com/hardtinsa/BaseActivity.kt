package com.hardtinsa

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
//import com.instamobile.kotlinlogin.R
import android.content.DialogInterface
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.hardtinsa.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hardtinsa.BaseDeDados
import kotlinx.android.synthetic.main.bottom_nav.bottomNavigationView
import java.util.Calendar


open class BaseActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle
    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView
    lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var db: BaseDeDados
    private lateinit var sqliteDatabase: SQLiteDatabase

    private val updateInterval = 2000L // Update interval in milliseconds
    private val updateRunnable = object : Runnable {
        override fun run() {
            val internetStatusImageView : ImageView = findViewById(R.id.internet_status)
            updateInternetStatusIcon(internetStatusImageView)
            handler.postDelayed(this, updateInterval)
        }
    }
    private val handler = Handler()
    private var selectedNavigationItem: MenuItem? = null
    private val handler2 = Handler()
    private val intervaloDeVerificacao = 60 * 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.base_activity)

        db = BaseDeDados(this)
        sqliteDatabase = db.writableDatabase

        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        verificarAtividadesPrestesAIniciar(this)
        verificarEntrevistasPrestesAIniciar(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener { menuItem ->
            handleNavigationItemSelected(menuItem) // Call the overridden function
            drawerLayout.closeDrawer(GravityCompat.START) // Close the navigation drawer after item selection
            true
        }

         bottomNavigationView= findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            handleBottomNavigationItemSelected(menuItem)
            true
        }

    }

    open fun clearLoginCredentials() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            remove("username")
            remove("password")
            apply()
        }
    }

    public open fun handleNavigationItemSelected(menuItem: MenuItem) {
        selectedNavigationItem = menuItem
        when (menuItem.itemId) {
            R.id.nav_Clientes -> {
                val intent = Intent(this, ClientesActivity::class.java)
                startActivity(intent)
                finish()
            }

            R.id.nav_Ideias -> {
                val intent = Intent(this, IdeiasActivity::class.java)
                startActivity(intent)
                finish()
            }

            R.id.nav_Oportunidades -> {
                val intent = Intent(this, OportunidadesActivity::class.java)
                startActivity(intent)
                finish()
            }

            R.id.nav_Contactos -> {
                val intent = Intent(this, ContactosActivity::class.java)
                startActivity(intent)
                finish()
            }

            R.id.nav_Vagas -> {
                val intent = Intent(this, VagasActivity::class.java)
                startActivity(intent)
                finish()
            }

            R.id.nav_Beneficios -> {
                val intent = Intent(this, BeneficiosActivity::class.java)
                startActivity(intent)
                finish()
            }

            R.id.nav_Calendario -> {
                val intent = Intent(this, CalendarioActivity::class.java)
                startActivity(intent)
                finish()
            }

            R.id.nav_Dashboard -> {
                val intent = Intent(this, MainPage::class.java)
                startActivity(intent)
                finish()
            }

            R.id.nav_LogOut -> {
                logout()

            }
        }

        // Close the navigation drawer after item selection
        drawerLayout.closeDrawer(GravityCompat.START)


    }


    override fun onResume() {
        super.onResume()
        startInternetStatusUpdateTimer()
        iniciarVerificacaoPeriodica()
    }

    override fun onPause() {
        super.onPause()
        stopInternetStatusUpdateTimer()
        pararVerificacaoPeriodica()
    }

    private fun startInternetStatusUpdateTimer() {
        handler.postDelayed(updateRunnable, updateInterval)
    }

    private fun stopInternetStatusUpdateTimer() {
        handler.removeCallbacks(updateRunnable)
    }

    protected fun updateInternetStatusIcon(internetStatusImageView: ImageView) {
        if (isInternetConnected()) {
            internetStatusImageView.setImageResource(R.drawable.baseline_wifi_24)
        } else {
            internetStatusImageView.setImageResource(R.drawable.baseline_wifi_off_24)
        }
        startInternetStatusUpdateTimer() //dOU RESTART AO TIMER PARA O PROX UPDATE
    }

    protected fun isInternetConnected(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(network) ?: return false

            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo?.isConnected ?: false
        }
    }

    private fun logout() {

        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Confirmar Terminar Sessão")
        alertDialogBuilder.setMessage("Tem a certeza que pretende terminar sessão?")
        alertDialogBuilder.setPositiveButton("Sim")
        { _, _ ->

            Globals.autenticado = false
            Globals.username = ""
            Globals.userID = 0.toString()
            Globals.token = ""
            Globals.tokenvalidade = ""
            Globals.password = ""


            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("autenticado", false)
            startActivity(intent)
            finish()
        }
        alertDialogBuilder.setNegativeButton("Cancelar")
        { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()

    }
    private fun saveAuthenticationStatus(authenticated: Boolean) {
        val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("authenticated", authenticated)
        editor.apply()
    }


    fun onImageViewClick(view: View) {
        if (view.id == R.id.profile_image && !Globals.alterapassprox) {
            val intent = Intent(this, PerfilPreview::class.java)
            startActivity(intent)
        }
    }

    fun onImageViewClick2(view: View) {
        if (view.id == R.id.my_image_view && !Globals.alterapassprox) {
            val intent = Intent(this, NotificacoesActivity::class.java)
            startActivity(intent)
        }
    }

    fun openMenu(view: View) {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        drawerLayout.openDrawer(GravityCompat.START)

        val navView: NavigationView = findViewById(R.id.nav_view)
        val headerView: View =
            navView.getHeaderView(0)         //Colocar o globaName NO Header quando se abre o menu
        val tvName: TextView = headerView.findViewById(R.id.tv_name)
        tvName.text = Globals.username
        val tvView: TextView = headerView.findViewById(R.id.textView)
        tvView.text = Globals.email
    }

    open fun handleBottomNavigationItemSelected(menuItem: MenuItem) {
        val previousSelectedItem = selectedNavigationItem

        if (menuItem.itemId == previousSelectedItem?.itemId) {
            // The selected item is already the current item, do nothing
            return
        }

        previousSelectedItem?.isChecked = false
        menuItem.isChecked = true
        selectedNavigationItem = menuItem

        when (menuItem.itemId) {
            R.id.home -> {
                val intent = Intent(this, MainPage::class.java)
                startActivity(intent)
            }

            R.id.calendario -> {
                val intent = Intent(this, CalendarioActivity::class.java)
                startActivity(intent)
            }

            R.id.alertas -> {
                val intent = Intent(this, NotificacoesActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private val verificacaoRunnable = object : Runnable {
        override fun run() {
            verificarAtividadesPrestesAIniciar(this@BaseActivity)
            verificarEntrevistasPrestesAIniciar(this@BaseActivity)
            handler2.postDelayed(this, intervaloDeVerificacao)
        }
    }

    fun iniciarVerificacaoPeriodica() {
        handler2.postDelayed(verificacaoRunnable, intervaloDeVerificacao)
    }

    fun pararVerificacaoPeriodica() {
        handler2.removeCallbacks(verificacaoRunnable)
    }

    @SuppressLint("Range")
    fun verificarAtividadesPrestesAIniciar(context: Context) {
        val query = "SELECT * FROM Atividade WHERE strftime('%Y-%m-%d %H:%M:%S', datetime(data_hora)) <= strftime('%Y-%m-%d %H:%M:%S', datetime('now', '+5 minutes')) AND " +
                "strftime('%Y-%m-%d %H:%M:%S', datetime(data_hora)) > strftime('%Y-%m-%d %H:%M:%S', datetime('now')) AND popVisto = 0"

        try {
            val cursor: Cursor? = sqliteDatabase.rawQuery(query, null)
            cursor?.let {
                if (it.moveToFirst()) {
                    val idAtividade = cursor.getInt(cursor.getColumnIndex("idatividade"))
                    val assunto = cursor.getString(cursor.getColumnIndex("assunto"))

                    // Criar o AlertDialog
                    val alertDialog = AlertDialog.Builder(context)
                        .setTitle("Atividade Prestes a Começar")
                        .setMessage("ID: $idAtividade, Assunto: $assunto")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss() // Fechar o pop-up quando o botão "OK" é pressionado

                            // Atualizar o valor de popVisto para 1 na base de dados
                            val contentValues = ContentValues()
                            contentValues.put("popVisto", 1)
                            sqliteDatabase.update("Atividade", contentValues, "idatividade = ?", arrayOf(idAtividade.toString()))
                        }
                        .create()

                    // Exibir o pop-up
                    alertDialog.show()
                }
                it.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("Range")
    fun verificarEntrevistasPrestesAIniciar(context: Context) {
        val query = "SELECT * FROM Entrevista WHERE strftime('%Y-%m-%d %H:%M:%S', datetime(data_hora)) <= strftime('%Y-%m-%d %H:%M:%S', datetime('now', '+5 minutes')) AND" +
                " strftime('%Y-%m-%d %H:%M:%S', datetime(data_hora)) > strftime('%Y-%m-%d %H:%M:%S', datetime('now')) AND popVisto = 0"

        try {
            val cursor: Cursor? = sqliteDatabase.rawQuery(query, null)
            cursor?.let {
                if (it.moveToFirst()) {
                    val idEntrevista = cursor.getInt(cursor.getColumnIndex("identrevista"))
                    val nomeCandidato = cursor.getString(cursor.getColumnIndex("nomecandidato"))

                    // Criar o AlertDialog
                    val alertDialog = AlertDialog.Builder(context)
                        .setTitle("Entrevista Prestes a Começar")
                        .setMessage("ID: $idEntrevista, Assunto: $nomeCandidato")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss() // Fechar o pop-up quando o botão "OK" é pressionado

                            // Atualizar o valor de popVisto para 1 na base de dados
                            val contentValues = ContentValues()
                            contentValues.put("popVisto", 1)
                            sqliteDatabase.update("Entrevista", contentValues, "identrevista = ?", arrayOf(idEntrevista.toString()))
                        }
                        .create()

                    // Exibir o pop-up
                    alertDialog.show()
                }
                it.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}

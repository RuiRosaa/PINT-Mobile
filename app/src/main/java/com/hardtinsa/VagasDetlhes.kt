package com.hardtinsa

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.documentfile.provider.DocumentFile
import androidx.drawerlayout.widget.DrawerLayout

import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.hardtinsa.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

import com.instamobile.kotlinlogin.R
import kotlinx.android.synthetic.main.activity_vagas_detalhes.tv_detalhe
import okhttp3.Call
import okhttp3.Callback
import org.json.JSONObject
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.FileOutputStream
import java.io.IOException


class VagasDetalhes : BaseActivity() {

    private lateinit var tvDescricao: TextView
    private lateinit var tvDetalhe: TextView
    private lateinit var tvCentro: TextView
    private lateinit var tvEstadoD: TextView
    private lateinit var tvDataInicio: TextView
    private lateinit var tvNome: TextView
    private lateinit var tvDataFim: TextView
    private lateinit var btnApply: Button
    private lateinit var btnAddFile: Button
    private lateinit var etFileName: TextView
    private val OPEN_DOCUMENT_REQUEST_CODE = 1
    private var selectedFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vagas_detalhes)

        tvDescricao = findViewById(R.id.tv_descricao)
        tvDataInicio = findViewById(R.id.tv_datainicio)

        tvDetalhe = findViewById(R.id.tv_detalhe)
        tvEstadoD = findViewById(R.id.tv_estadod)
        tvNome = findViewById(R.id.tv_nome22)
        tvDataFim = findViewById(R.id.tv_datafim)
        btnAddFile = findViewById(R.id.btn_add_file)
        etFileName = findViewById(R.id.et_file_name)

        val tvEnviarButton: Button = findViewById(R.id.tv_enviar)
        tvEnviarButton.setOnClickListener {
            if (selectedFile != null) {
                if (isInternetConnected()) {
                    CandidaturaInserirApi(selectedFile!!)
                    Toast.makeText(this, "A sua Candidatura foi enviada!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Selecione um arquivo", Toast.LENGTH_SHORT).show()
            }
        }


        val btnsugere = findViewById<Button>(R.id.tv_sugerir)
        btnsugere.setOnClickListener {
            val intent = Intent(this, ActivitySugerirVaga::class.java)
            startActivity(intent)
        }

        val backButton = findViewById<Button>(R.id.btn_back)
        backButton.setOnClickListener {
            val intent = Intent(this, VagasActivity::class.java)
            startActivity(intent)
        }

        val internetStatusImageView = findViewById<ImageView>(R.id.internet_status)
        updateInternetStatusIcon(internetStatusImageView)


        btnAddFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            startActivityForResult(intent, OPEN_DOCUMENT_REQUEST_CODE)
        }

        val idoferta = intent.getIntExtra("idoferta", 0)
        Globals.idoferta = idoferta
        Log.d("Variable", "idoferta: $idoferta")
        val descricao = intent.getStringExtra("descricao")
        val detalhe = intent.getStringExtra("detalhe")
        val estadod = intent.getStringExtra("estadod")
        val datainicio = intent.getStringExtra("datainicio") ?: "N/A"
        val datafim = intent.getStringExtra("datafim") ?: "N/A"
        val nome = intent.getStringExtra("nome") ?: "N/A"
        //  val morada = intent.getStringExtra("morada") ?: "N/A"

        tvDescricao.text = "Descrição: $descricao"
        tvDataInicio.text = "Começa a: $datainicio"
        tvEstadoD.text = "Estado: $estadod"
        tvDetalhe.text = "Detalhe: $detalhe"
        tvDataFim.text = "Termina a: $datafim"
        tvNome.text = "Empresa: $nome"


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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OPEN_DOCUMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val fileUri = data?.data
            if (fileUri != null) {
                selectedFile = getFileFromUri(fileUri)
                val fileName = getFileNameFromUri(fileUri)
                etFileName.text = fileName
            } else {
                Toast.makeText(this, "Nenhum ficheiro foi selecionado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("Range")
    private fun getFileFromUri(uri: Uri): File? {
        val contentResolver = applicationContext.contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            it.moveToFirst()
            val displayName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            val file = File(cacheDir, displayName)
            file.createNewFile()
            val outputStream = FileOutputStream(file)
            val inputStream = contentResolver.openInputStream(uri)
            inputStream?.copyTo(outputStream)
            outputStream.close()
            file
        }
    }
    private fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        val contentResolver = applicationContext.contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName
    }


    private fun CandidaturaInserirApi(file: File) {
        val iduser = Globals.userID
        val apiurl = Globals.apiurl
        val idoferta = Globals.idoferta.toString()
        val externo_email = "null"
        val externo_nome = "null"

        val url = apiurl + "candidatura/inserir"

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("IDUSER", iduser)
            .addFormDataPart("IDOFERTA", idoferta)
            .addFormDataPart("EXTERNO_EMAIL", externo_email)
            .addFormDataPart("EXTERNO_NOME", externo_nome)
            .addFormDataPart(
                "filename",
                file.name,
                file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            )
            .build()
        Log.d("Request", "IDUSER: $iduser")
        Log.d("Request", "IDOFERTA: $idoferta")
        Log.d("Request", "EXTERNO_EMAIL: $externo_email")
        Log.d("Request", "EXTERNO_NOME: $externo_nome")
        Log.d("Request", "Filename: ${file.name}")

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: okhttp3.Response) {
                // Handle the response
                val responseBody = response.body?.string()
                Log.d("Response", responseBody ?: "")
            }

            override fun onFailure(call: Call, e: IOException) {
                // Handle the failure
                Log.e("Error", "OkHttp request failed: ${e.message}")
            }
        })
    }

}
package com.hardtinsa

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.hardtinsa.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.instamobile.kotlinlogin.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

private val REQUEST_CODE = 1001
class ActivitySugerirVaga : BaseActivity() {

    private lateinit var btnAddFile: Button
    private lateinit var etFileName: TextView
    private lateinit var EditText1: EditText
    private lateinit var EditText2: EditText
    private val OPEN_DOCUMENT_REQUEST_CODE = 1
    private var selectedFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sugerir_vaga)

        btnAddFile = findViewById(R.id.btn_add_file)
        etFileName = findViewById(R.id.et_file_name)
        EditText1 = findViewById(R.id.editText1)
        EditText2 = findViewById(R.id.editText2)

        val btnSugerir = findViewById<Button>(R.id.btn_sugerir)
        btnSugerir.setOnClickListener {
            val email = EditText1.text.toString()
            val nome = EditText2.text.toString()

            if (email.isNotEmpty() && nome.isNotEmpty()) {
                if (selectedFile != null) {
                    if (isInternetConnected()) {
                        CandidaturaInserirApi(selectedFile!!, email, nome)
                        Toast.makeText(this, "A sua Candidatura foi enviada!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Selecione um arquivo", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }

        val internetStatusImageView = findViewById<ImageView>(R.id.internet_status)
        updateInternetStatusIcon(internetStatusImageView)

        val backButton = findViewById<Button>(R.id.btn_back)
        backButton.setOnClickListener {
            val intent = Intent(this, VagasActivity::class.java)
            startActivity(intent)
        }

        btnAddFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            startActivityForResult(intent, OPEN_DOCUMENT_REQUEST_CODE)
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
    }

    fun isUserLoggedIn(): Boolean {
        return Globals.autenticado
    }

    // Parte relativa ao Enviar CV --------------------------------------------
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

    private fun CandidaturaInserirApi(file: File, email: String, nome: String) {
        val iduser = Globals.userID
        val apiurl = Globals.apiurl
        val idoferta = Globals.idoferta.toString()
        val externo_email = email
        val externo_nome = nome

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
    object RealPathUtil {
        fun getRealPath(context: Context, uri: Uri): String? {
            var realPath: String? = null

            if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
                val projection = arrayOf(MediaStore.Images.Media.DATA)
                var cursor: Cursor? = null
                try {
                    cursor = context.contentResolver.query(uri, projection, null, null, null)
                    cursor?.let {
                        if (it.moveToFirst()) {
                            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                            realPath = it.getString(columnIndex)
                        }
                    }
                } catch (e: Exception) {
                    // Handle the exception
                } finally {
                    cursor?.close()
                }
            }

            return realPath
        }
    }
}
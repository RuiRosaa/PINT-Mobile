package com.hardtinsa

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.hardtinsa.Globals.token
import com.android.volley.toolbox.Volley
import com.example.hardtinsa.R
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
//import com.instamobile.kotlinlogin.R
import com.hardtinsa.Globals.apiurl
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


open class MainActivity : AppCompatActivity() {
    private val RC_SIGN_IN = 1
    lateinit var googleSignInClient: GoogleSignInClient
    lateinit var callbackManager: CallbackManager
    private lateinit var btnLogin: Button
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var rememberMeCheckbox: CheckBox
    private lateinit var loginError1: TextView
    private val handler = Handler()

    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Globals.autenticado = false
        FacebookSdk.sdkInitialize(applicationContext)


        callbackManager = CallbackManager.Factory.create()

        // Register a callback for Facebook login
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onCancel() {
                    Log.e("facebook", "onCancel")
                }

                override fun onError(error: FacebookException) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error logging in with Facebook: ${error.message}",
                        Toast.LENGTH_LONG).show()
                }

                override fun onSuccess(result: LoginResult) {
                    // Handle successful Facebook login here
                    handleFacebookLogin(result.accessToken)
                }
            })




        //google sign in
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleButton.setOnClickListener {
            loading.visibility = View.VISIBLE
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
        // facebook sign in
        btLoginFacebook.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this@MainActivity, listOf("public_profile"))
        }


        val btncreate = findViewById<Button>(R.id.createacc)
        btncreate.setOnClickListener {
            val intent = Intent(this, SignUP::class.java)
            startActivity(intent)
        }

        val btnforgot = findViewById<Button>(R.id.forgotps)
        btnforgot.setOnClickListener {
            val intent2 = Intent(this, VerifyEmail::class.java)
            startActivity(intent2)
        }

        btnLogin = findViewById(R.id.btnLogin)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox)
        loginError1 = findViewById(R.id.loginError)

        btnLogin.setOnClickListener {
            if (isInternetConnected()) {
            performLogin()
            } else {
                Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_SHORT).show()
            }
        }


        val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val tokenn = sharedPref.getString("TK", "")
        val IDUser = sharedPref.getString("IDUser", "")

        val autenticado = intent.getBooleanExtra("autenticado", true)
        Globals.autenticado = autenticado

        //estou a chamar o retrive do tokenvalidade
        if (Globals.autenticado) {   //vai ser sempre true
            retrieveTokenExpirationDate(tokenn, IDUser)
        }

        val savedCredentials = retrieveLoginCredentials()
        if (savedCredentials != null) {
            val (username, password) = savedCredentials
            etEmail.setText(username)
            etPassword.setText(password)
            rememberMeCheckbox.isChecked = true
        }
    }

    fun compareDates(date1: String, date2: Date): Boolean {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val parsedDate1 = dateFormat.parse(date1)

        val check = parsedDate1!! >= date2
        Log.d("Compare Dates", "ValidadeTk: $parsedDate1, DataAtual: $date2, Result: $check")

        return check
    }

    fun retrieveTokenExpirationDate(token: String?, userID: String?) {
            val apiurl = Globals.apiurl
            Log.d("Token Expiration", "Token: $token, UserID: $userID")

            if (token == null || userID == null) {
                Log.e("Token Expiration", "Token ou UserID é null")
                return
            }
            val url = apiurl + "api_token/details/$token/$userID"

            val requestQueue = Volley.newRequestQueue(this)
            val jsonArrayRequest = JsonArrayRequest(
                Request.Method.GET, url, null,
                { response ->
                    try {
                        if (response.length() > 0) {
                            val jsonObject = response.getJSONObject(0)
                            val validData = jsonObject.getString("VALIDODATA")

                            // Update the token validity in Globals
                            Globals.tokenvalidade = validData
                            Log.d("Token Validade", validData)

                            val currentDate = Date()

                            val isValid = compareDates(validData, currentDate)
                            Log.d("Token Validation", "IsValid: $isValid")

                            if (isValid) {
                                Log.d("Token Validation", "Redireciona para MainPage")
                                Globals.autenticado = true   //para ativar a entrada no MainPage
                                performLogin()
                            } else {
                                Log.d("Token Validation", "Redireciona para MainActivity")
                                val intent2 = Intent(this, MainActivity::class.java)
                                startActivity(intent2)
                            }
                        }

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                },
                { error ->
                    Log.e("Token Details Error", "Error retrieving token details: ${error.message}")
                }
            )

            requestQueue.add(jsonArrayRequest)
        }

    fun retrieveUserDetails(context: Context) {
        val apiurl = Globals.apiurl
        val iDuser = Globals.userID
        val urltoken = token
        val url = apiurl +"utilizador/details/$urltoken/$iDuser"

        val requestQueue = Volley.newRequestQueue(context)
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->

                try {
                    val userDetails = response.getJSONObject(0)
                    val idUser = userDetails.getInt("IDUSER")
                    val idPerfil = userDetails.getInt("IDPERFIL")
                    val nome = userDetails.getString("NOME")
                    val email = userDetails.getString("EMAIL")
                    val linkFoto = userDetails.getString("LINKFOTO")
                    val alteraPassProx = userDetails.getBoolean("ALTERAPASSPROX")
                    val estado = userDetails.getInt("ESTADO")
                    val perfil = userDetails.getString("PERFIL")
                    val nivel = userDetails.getInt("NIVEL")
                    val idFuncionalidade = userDetails.getInt("IDFUNCIONALIDADE")
                    val funcionalidade = userDetails.getString("FUNCIONALIDADE")
                    val nomeUnico = userDetails.getString("NOMEUNICO")

                    Globals.username = nome
                    Globals.email = email
                    Globals.cargo = perfil
                    Globals.perfilID = idPerfil
                    Globals.tokenvalidade = ""
                    Globals.funcionalidade = funcionalidade
                    Globals.linkfoto = linkFoto

                    Log.d("User Details", "IDUSER: $idUser, IDPERFIL: $idPerfil, NOME: $nome, EMAIL: $email, LINKFOTO: $linkFoto, ALTERAPASSPROX: $alteraPassProx, ESTADO: $estado, PERFIL: $perfil, NIVEL: $nivel, IDFUNCIONALIDADE: $idFuncionalidade, FUNCIONALIDADE: $funcionalidade, NOMEUNICO: $nomeUnico")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                // Handle error
                Log.e("User Details Error", "Error retrieving user details: ${error.message}")
            }
        )

        requestQueue.add(jsonArrayRequest)
    }

    fun performLogin() {
        val apiurl = Globals.apiurl
        val username = etEmail.text.toString()
        val password = etPassword.text.toString()
        Globals.password = password   //para controlar no perfil activity
        val nome = Globals.username
        val email = Globals.email
        val perfil = Globals.cargo
        val linkfoto = Globals.linkfoto
        val funcionalidade = Globals.funcionalidade

        val rememberMeChecked = rememberMeCheckbox.isChecked
        if (rememberMeChecked) {
            saveLoginCredentials(username, password)
        } else {
            clearLoginCredentials()
        }

        val url = apiurl + "utilizador/dologin2"
        val params = JSONObject()
        params.put("utilizador", username)
        params.put("senha", password)

        val requestQueue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, params,
            { response ->
                Log.d("Response", response.toString())

                try {
                    val dataArray = response.getJSONArray("data")
                    if (dataArray.length() > 0) {
                        val responseData = dataArray.getJSONObject(0)
                        val resCode = responseData.getInt("RES_CODE")
                        val alteraPassProx = responseData.optBoolean("ALTERAPASSPROX", false)
                        val resMsg = if (responseData.has("RES_MSG")) {
                            responseData.getString("RES_MSG")
                        } else {
                            "Unknown error occurred"
                        }
                        val token = if (responseData.has("TOKEN")) {
                            responseData.getString("TOKEN")
                        } else {
                            ""
                        }
                        val userId = if (responseData.has("IDUSER")) {
                            responseData.getString("IDUSER")
                        } else {
                            ""
                        }
                        if (resCode == 1) {
                            Log.d("Response Nome", nome)
                            Globals.alterapassprox = alteraPassProx
                            Globals.username = nome
                            Globals.autenticado = true
                            Globals.userID = userId
                            Globals.token = token
                            Globals.funcionalidade = funcionalidade
                            Globals.email = email
                            Globals.linkfoto = linkfoto
                            Globals.cargo = perfil

                            val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                            val editor = sharedPref.edit()
                            editor.putString("TK", token)
                            editor.putString("IDUser", userId)
                            editor.putBoolean("autenticado", true)
                            editor.apply()

                            retrieveUserDetails(this)
                            handler.postDelayed({
                                if (alteraPassProx) {
                                    val intent = Intent(this, PerfilActivity::class.java)
                                    Toast.makeText(
                                        this,
                                        "Tem de alterar a Password!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    startActivity(intent)
                                } else {
                                    val intent = Intent(this, MainPage::class.java)
                                    startActivity(intent)
                                }
                            },1000)
                        } else {
                            val redMsg = responseData.optString("REDMSG", resMsg)
                            loginError1.text = redMsg
                        }
                    }
                } catch (e: JSONException) {
                    Toast.makeText(this, "Error parsing response: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            },
            { error ->
                Toast.makeText(this, "Utilizador efetuou Logout", Toast.LENGTH_LONG)
                    .show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }







    private fun handleFacebookLogin(accessToken: AccessToken) {
        // Perform necessary actions with the Facebook access token
        // For example, you can use it to authenticate the user on your server

        val token = accessToken.token

        // Example: Login successful, navigate to MainPageActivity
        val intent = Intent(this, MainPage::class.java)
        startActivity(intent)
    }

    private fun saveLoginCredentials(username: String, password: String) {
        val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("username", username)
        editor.putString("password", password)
        editor.apply()
    }
    private fun retrieveLoginCredentials(): Pair<String, String>? {
        val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", null)
        val password = sharedPref.getString("password", null)
        return if (username != null && password != null) {
            Pair(username, password)
        } else {
            null
        }
    }
    fun clearLoginCredentials() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            remove("username")
            remove("password")
            apply()
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)

        // Handle Google sign-in result here
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                // Handle successful Google sign-in here
            } catch (e: ApiException) {
                e.printStackTrace()
                loginError.text = e.message
            }
        }
    }

}
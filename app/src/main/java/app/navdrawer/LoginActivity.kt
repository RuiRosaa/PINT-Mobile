package app.navdrawer



import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Date


class LoginActivity : AppCompatActivity() {



    var globalVariables = GlobalVariables()
    var username = ""
    var userId = ""
    val secretKey = "BenficaCampeaoBenficaBenficaLUISAO"
    var checkInitialLogin = false





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)
        checkInitialLogin = checkPreviousLogin()
        if(checkInitialLogin){
            val intent: Intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


        val forgotPasswordTextView = findViewById<TextView>(R.id.forgotpass)

        forgotPasswordTextView.setOnClickListener {
            val intent = Intent(this, RecuperarPass::class.java)
            startActivity(intent)
        }





        val botaonewuser = findViewById<TextView>(R.id.newuser)
        botaonewuser.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        var str_email = ""
        var str_password = ""
        val edtxt_email = findViewById<View>(R.id.username) as EditText
        val edtxt_password = findViewById<View>(R.id.password) as EditText
        val btn_login = findViewById<View>(R.id.loginbtn) as Button

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        btn_login.setOnClickListener {
            str_email = edtxt_email.text.toString()
            str_password = edtxt_password.text.toString()
            if (checkLoginCredentials(str_email, str_password)) {
                SuccessfulLogin()
                val intent: Intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Credenciais invalidas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun checkPreviousLogin(): Boolean{
        val sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "")
        val token = sharedPreferences.getString("token", "")
        println(token)
        print(System.currentTimeMillis())
        if (username != null && token != null) {
            if (username.isNotEmpty() && token.isNotEmpty()) {
                Toast.makeText(this, "Login já efetuado", Toast.LENGTH_SHORT).show()
                try {
                    val claims: Claims = Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(secretKey.toByteArray()))
                        .build()
                        .parseClaimsJws(token)
                        .body

                    val expiration: Date? = claims.expiration
                    val currentTimeMillis = System.currentTimeMillis()

                    if (expiration != null && expiration.time < currentTimeMillis) {
                        // Token expirado
                        println("Token expirado")
                        return false
                    }

                    // Outras verificações adicionais podem ser feitas aqui

                    // Token válido
                    return true
                } catch (e: Exception) {
                    // Token inválido
                    println("Token inválido: ${e.message}")
                    return false
                }
            } else {
                Toast.makeText(this, "Login ainda não efetuado", Toast.LENGTH_SHORT).show()
            }
        }
        return false
    }

    fun SuccessfulLogin() {
        val sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        var token = generateToken(userId, secretKey)
        println("Login bem efetuado. Token: ")
        println(token)
        editor.putString("username", username) // substitua "username" pelo nome da chave que desejar
        editor.putString("token", token) // substitua "token" pelo nome da chave que desejar
        editor.apply()
    }

    fun generateToken(userId: String, key: String): String {
        val secretKey = Keys.hmacShaKeyFor(key.toByteArray())

        val currentTimeMillis = System.currentTimeMillis()
        var expirationTimeMillis = currentTimeMillis
        for (i in 1..30){
            expirationTimeMillis =+ (24 * 60 * 60 * 1000)
        }
        val expirationDate = Date(expirationTimeMillis)

        val token = Jwts.builder()
            .setSubject(userId)
            .setExpiration(expirationDate)
            .signWith(secretKey)
            .compact()

        return token
    }

    private fun checkLoginCredentials(email: String, password: String): Boolean {
        val savedEmail = "rafa@gmail.com"
        val savedPassword = "123"
        username = "Rafa".toString()
        userId = "7"
        if(savedEmail == email && savedPassword == password) {
            return true
        }
        else{
            return false
        }
    }


    internal class getLogin(val context: Context, val email: String, val password: String) :
        AsyncTask<Void?, Void?, String?>() {
        override fun doInBackground(vararg p0: Void?): String? {
            var urlConnection: HttpURLConnection? = null
            var reader: BufferedReader? = null
            try {
                val url =
                    URL("https://test-api-rj.000webhostapp.com/api/utilizadores/login.php?email=$email&palavraChave=$password")
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "GET"
                urlConnection!!.connect()
                val inputStream = urlConnection.inputStream
                reader = BufferedReader(InputStreamReader(inputStream))
                var linha: String?
                val buffer = StringBuffer()
                while (reader.readLine().also { linha = it } != null) {
                    buffer.append(linha)
                    buffer.append("\n")
                }
                return buffer.toString()
            } catch (e: Exception) {
                e.printStackTrace()
                urlConnection?.disconnect()
                if (reader != null) {
                    try {
                        reader.close()
                    } catch (e1: IOException) {
                        e1.printStackTrace()
                    }
                }
            }
            return null
        }

        override fun onPostExecute(dados: String?) {
            println("Dados: $dados")
            try {
                val jsonObject = JSONObject(dados)
                val state: Int = jsonObject.getInt("state")
                println("State: $state")

                if (state == 1) { //Login correto
                    val jsonArray: JSONArray = jsonObject.getJSONArray("data")
                    for (i in 0 until jsonArray.length()) {
                        val get = jsonArray.getJSONObject(i)
                        val idUtilizador = get.getInt("idUtilizador")
                        val nome = get.getString("nome")
                        val ativo = get.getString("ativo")
                    }
                    println("Vou mudar de activity")


                    println("Mensagem: " + jsonObject.getString("message"))

                } else {
                    println("Mensagem: " + jsonObject.getString("message"))
                }

            } catch (err: JSONException) {
                println("JSONException : $err")
                Toast.makeText(context, "JSONException: $err", Toast.LENGTH_LONG).show()
            } catch (err:  NullPointerException) {
                println("NullPointerException : $err")
                Toast.makeText(context, "NullPointerException: $err", Toast.LENGTH_LONG).show()
            } catch (err: Exception) {
                println("Exception : $err")
                Toast.makeText(context, "Exception: $err", Toast.LENGTH_LONG).show()
            }
        }
    }
}
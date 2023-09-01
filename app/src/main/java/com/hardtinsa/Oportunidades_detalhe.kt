package com.hardtinsa

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.instamobile.kotlinlogin.R
import org.json.JSONException

class Oportunidades_detalhe : BaseActivity() {

    private lateinit var tvDescricao: TextView
    private lateinit var tvDetalhe: TextView
    private lateinit var tvTipoPJ: TextView
    private lateinit var tvEstado: TextView
    private lateinit var tvValorprev: TextView
    private lateinit var tvCriadodata: TextView
    private lateinit var tvTipo: TextView
    private lateinit var tv_Cliente: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oportunidades_detalhe)

        tvDescricao = findViewById(R.id.tv_descricao)
        tvDetalhe = findViewById(R.id.tv_detalhe)
        tvTipoPJ = findViewById(R.id.tv_tipoprojeto)
        tvValorprev = findViewById(R.id.tv_valorprev)
        tvEstado = findViewById(R.id.tv_estado)
        tv_Cliente = findViewById(R.id.tv_cliente)
        tvTipo = findViewById(R.id.tv_tipo)

        if (isInternetConnected()) {
            Toast.makeText(this, "A recarregar..", Toast.LENGTH_SHORT)
                .show() // Example toast message
            fetchMarcacoesFromServer()
        } else {
            Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_SHORT).show()
        }

        val internetStatusImageView = findViewById<ImageView>(R.id.internet_status)
        updateInternetStatusIcon(internetStatusImageView)

        val idoportunidade = intent.getIntExtra("oportunidade_id", 0)
        Log.d("IntentgetDetalhe", "oportunidade_id: $idoportunidade")
        val descricao = intent.getStringExtra("descricao")
        val detalhe = intent.getStringExtra("detalhe")
        val estado = intent.getIntExtra("estado", 0)
        val idtpprojeto = intent.getIntExtra("idtpprojeto", 0)
        val valorprev = intent.getIntExtra("valorprev", 0)
        val cliente = intent.getIntExtra("idcliente", 0)
        Log.d("A ser passado", "IDCLIENTE: $cliente")
        val idtipo = intent.getIntExtra("idtipo", 0)

        val estadoText = when (estado) {
            0 -> "Não Ativo"
            1 -> "Ativo/Criado"
            2 -> "Em Curso"
            3 -> "Em Standby"
            4 -> "Em Proposta"
            5 -> "Em Análise"
            6 -> "Em Negociacao"
            7 -> "Fechado/Perdido"
            8 -> "Concluído"
            10 -> "Arquivado"
            99 -> "Eliminado"
            else -> "N/A"
        }

        val idtpprojetoText = when (idtpprojeto) {
            1 -> "Desenvolvimento WEB"
            2 -> "Desenvolvimento Mobile"
            3 -> "Consultoria"
            else -> "N/A"
        }
        val idtipoText = when (idtipo) {
            1 -> "Generico"
            2 -> "Software"
            3 -> "Consultoria"
            4 -> "Redes"
            5 -> "Financeiro"
            else -> "N/A"
        }

        tvDescricao.text = "Descrição: $descricao"
        tvDetalhe.text = "Detalhe: $detalhe"
        tvEstado.text = "Estado: $estadoText"
        tvValorprev.text = "Valor Prev: $valorprev"
        tvTipoPJ.text = "Tipo de Projeto: $idtpprojetoText"
        tv_Cliente.text = "ID Cliente: $cliente"
        tvTipo.text = "Área de Negócio: $idtipoText"

        val buttonC = findViewById<Button>(R.id.btnContactos)
        buttonC.setOnClickListener {
            val intent = Intent(this, Oportunidade_Contactos::class.java)
            intent.putExtra("oportunidade_id", idoportunidade)
            intent.putExtra("idcliente", cliente)
            Log.d("A ser passado", "IDCLIENTE: $cliente")
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
            intent.putExtra("oportunidade_id", idoportunidade)
            intent.putExtra("idcliente", cliente)
            Log.d("A ser passado", "IDCLIENTE: $cliente")
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
}
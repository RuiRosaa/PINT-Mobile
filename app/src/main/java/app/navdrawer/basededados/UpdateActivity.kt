package app.navdrawer.basededados

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import app.navdrawer.R

class UpdateActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnUpdate: Button
    private lateinit var dbHelper: DatabaseHelper
    private var pessoaID: Int = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)

        dbHelper = DatabaseHelper(this)


        var pessoaId = intent.getIntExtra("pessoaId", -1)
        val pessoaNome = intent.getStringExtra("pessoaNome")
        val pessoaEmail = intent.getStringExtra("pessoaEmail")


        // Preciso de buscar o ID da pessoa da previous activity

        pessoaId = intent.getIntExtra("pessoaId", -1)

        etName = findViewById(R.id.editTextName)
        etEmail = findViewById(R.id.editTextEmail)
        btnUpdate = findViewById(R.id.buttonUpdate)

        etName.setText(pessoaNome)
        etEmail.setText(pessoaEmail)

        btnUpdate.setOnClickListener {
            val nome = etName.text.toString()
            val email = etEmail.text.toString()
            if (nome.isNotEmpty() && email.isNotEmpty()) {
                val result = dbHelper.atualizaPessoa(pessoaId.toString(), nome, email)
                if (result > 0) {
                    Toast.makeText(this, "Pessoa atualizada com sucesso", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Erro ao atualizar pessoa", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }

        }
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}

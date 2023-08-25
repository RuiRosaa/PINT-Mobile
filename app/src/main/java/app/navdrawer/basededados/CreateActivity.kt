package app.navdrawer.basededados

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import app.navdrawer.R


class CreateActivity : AppCompatActivity() {
    private lateinit var db: SQLiteDatabase
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnAdd: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        db = BaseDeDados(this).writableDatabase

        etName = findViewById(R.id.editTextName)
        etEmail = findViewById(R.id.editTextEmail)
        btnAdd = findViewById(R.id.buttonCreate)

        btnAdd.setOnClickListener {
            val name = etName.text.toString()
            val email = etEmail.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty()) {
                val values = ContentValues()
                values.put("nome", name)
                values.put("email", email)

                val result = db.insert("pessoas", null, values)

                if (result > 0) {
                    Toast.makeText(this, "Record added successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Failed to add record", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter name and email", Toast.LENGTH_SHORT).show()
            }
        }
    }

}


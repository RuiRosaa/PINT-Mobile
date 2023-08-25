package app.navdrawer.basededados

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import app.navdrawer.R

class DeleteActivity : AppCompatActivity() {

    lateinit var deleteBtn: Button
    lateinit var cancelBtn: Button
    lateinit var idField: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete)

        deleteBtn = findViewById(R.id.delete_btn)
        cancelBtn = findViewById(R.id.cancel_btn)
        idField = findViewById(R.id.id_field)

        deleteBtn.setOnClickListener {
            val id = idField.text.toString().toIntOrNull()

            if (id != null) {
                val confirmDialog = AlertDialog.Builder(this)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this record?")
                    .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                        // Perform delete operation
                        val db = DatabaseHelper(this).writableDatabase
                        val selection = "id = ?"
                        val selectionArgs = arrayOf(id.toString())
                        val deletedRows = db.delete("pessoas", selection, selectionArgs)
                        db.close()

                        if (deletedRows > 0) {
                            setResult(RESULT_OK, Intent())
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            setResult(RESULT_CANCELED, Intent())
                            finish()
                        }
                    })
                    .setNegativeButton("No", null)
                    .create()

                confirmDialog.show()
            }
        }

        cancelBtn.setOnClickListener {
            setResult(RESULT_CANCELED, Intent())
            finish()
        }
    }
}

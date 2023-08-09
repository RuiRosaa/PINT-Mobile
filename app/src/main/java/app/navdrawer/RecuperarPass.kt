package app.navdrawer



import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class RecuperarPass : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recuperar_pass)

        val emailEditText = findViewById<TextInputEditText>(R.id.email)
        val sendButton = findViewById<MaterialButton>(R.id.send_btn)

        sendButton.setOnClickListener {
            val email = emailEditText.text.toString()
            // Implement the logic to send the recovery email here
            // For example, you can display a Toast indicating the email was sent
            // and handle the email sending logic using Firebase or your own backend.
        }
    }
}

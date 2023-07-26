package com.example.pint_eh_desta

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pint_eh_desta.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class SignupActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_layout)

        val usernameEditText = findViewById(R.id.username) as EditText
        val emailEditText = findViewById(R.id.email) as EditText
        val passwordEditText = findViewById(R.id.password) as EditText
        val repasswordEditText = findViewById(R.id.repassword) as EditText

        val signupButton = findViewById<MaterialButton>(R.id.signupbtn)

        signupButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val repassword = repasswordEditText.text.toString()

            // Perform validation here if needed
            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && repassword.isNotEmpty()) {
                if (password == repassword) {
                    // Passwords match, proceed with registration logic
                    Toast.makeText(
                        this@SignupActivity,
                        "Registration Successful!",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Implement your registration logic here
                } else {
                    Toast.makeText(
                        this@SignupActivity,
                        "Passwords do not match.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this@SignupActivity,
                    "Please fill in all fields.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

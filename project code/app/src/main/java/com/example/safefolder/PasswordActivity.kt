package com.example.safefolder

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PasswordActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "SafeFolderPrefs"
    private val KEY_PASSWORD = "password"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedPassword = sharedPreferences.getString(KEY_PASSWORD, null)

        val passwordInput: EditText = findViewById(R.id.passwordInput)
        val confirmInput: EditText = findViewById(R.id.confirmInput)
        val submitButton: Button = findViewById(R.id.submitButton)

        // If password not set, ask user to create one
        if (savedPassword == null) {
            confirmInput.visibility = android.view.View.VISIBLE
            submitButton.setOnClickListener {
                val pass = passwordInput.text.toString()
                val confirm = confirmInput.text.toString()

                if (pass.isEmpty() || confirm.isEmpty()) {
                    Toast.makeText(this, "Enter both fields", Toast.LENGTH_SHORT).show()
                } else if (pass != confirm) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                } else {
                    sharedPreferences.edit().putString(KEY_PASSWORD, pass).apply()
                    openMainActivity()
                }
            }
        } else {
            // If password already set, ask for it
            confirmInput.visibility = android.view.View.GONE
            submitButton.setOnClickListener {
                val entered = passwordInput.text.toString()
                if (entered == savedPassword) {
                    openMainActivity()
                } else {
                    Toast.makeText(this, "Wrong password", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Prevent going back to password screen
    }
}

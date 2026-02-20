package com.nexushardware.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nexushardware.app.ui.main.MainActivity
import com.nexushardware.app.data.local.NexusBDHelper
import com.nexushardware.app.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var dbHelper: NexusBDHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Inicalizamos la bd
        dbHelper = NexusBDHelper(this)

        setupListeners()
    }

    private fun setupListeners() {
        // btn ingresar
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // validacion
            val esValido = dbHelper.validarLogin(email, pass)

            if (esValido) {
                // login exitoso
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Cerramos Login para que no pueda volver atrás
            } else {
                binding.tilPassword.error = "Credenciales incorrectas"
                Toast.makeText(this, "Error de acceso", Toast.LENGTH_SHORT).show()
            }
        }

        // btn registrarsee
        binding.tvRegister.setOnClickListener {
            Toast.makeText(this, "Próximamente: Módulo de Registro", Toast.LENGTH_SHORT).show()
            // Aquí iría: startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
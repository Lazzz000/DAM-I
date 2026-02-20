package com.nexushardware.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nexushardware.app.ui.main.MainActivity
import com.nexushardware.app.data.local.NexusBDHelper
import com.nexushardware.app.databinding.ActivityLoginBinding
import android.util.Patterns
import com.google.android.material.snackbar.Snackbar
import androidx.core.widget.doOnTextChanged
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
        //limpiar el error en caso haya,cuando el usuario escribe su correo
        binding.etEmail.doOnTextChanged { _, _, _, _ ->
            binding.tilEmail.error = null
        }
        // btn ingresar
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Snackbar.make(binding.root, "Por favor complete todos los campos", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //validar el formato del correoo
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.error = "Ingrese un correo electrónico válido"
                return@setOnClickListener
            }

            // validacion de login
            val esValido = dbHelper.validarLogin(email, pass)

            if (esValido) {
                // login exitoso
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Cerramos Login para que no pueda volver atrás
            } else {
                Snackbar.make(binding.root, "Correo o contraseña incorrectos", Snackbar.LENGTH_SHORT).show()
            }
        }

        // btn registrarsee
        binding.tvRegister.setOnClickListener {
            Toast.makeText(this, "Próximamente: Módulo de Registro", Toast.LENGTH_SHORT).show()
            // Aquí iría: startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
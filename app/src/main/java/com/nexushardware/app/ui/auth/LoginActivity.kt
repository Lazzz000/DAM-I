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
        //limpia los errores visuales cuando se escribee
        binding.etEmail.doOnTextChanged { _, _, _, _ -> binding.tilEmail.error = null }
        binding.etPassword.doOnTextChanged { _, _, _, _ -> binding.tilPassword.error = null }

        //btn ingresar
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            var esValidoParaEnviar = true

            //validar correo visualmente
            if (email.isEmpty()) {
                binding.tilEmail.error = "Ingrese su correo"
                esValidoParaEnviar = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.error = "Ingrese un correo válido"
                esValidoParaEnviar = false
            }

            //validar contraseña visualmente
            if (pass.isEmpty()) {
                binding.tilPassword.error = "Ingrese su contraseña"
                esValidoParaEnviar = false
            }

            //si hay errores en las cajas detenemos el proceso aqui
            if (!esValidoParaEnviar) return@setOnClickListener

            // validacion de login en base de datos
            val loginExitoso = dbHelper.validarLogin(email, pass)

            if (loginExitoso) {
                //exitoso
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() //cerramos Login para que no pueda volver atrás
            } else {
                //Snackbar error
                Snackbar.make(binding.root, "❌ Correo o contraseña incorrectos", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(android.graphics.Color.parseColor("#CF6679"))
                    .setTextColor(android.graphics.Color.BLACK)
                    .show()
            }
        }

        //btn registrarse
        binding.tvRegister.setOnClickListener {
            Snackbar.make(binding.root, "Aún falta agregar este módulo", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(android.graphics.Color.parseColor("#03DAC5"))
                .setTextColor(android.graphics.Color.BLACK)
                .show()
        }
    }
}
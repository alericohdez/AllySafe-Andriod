package com.ieselrincon.allysafe.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ieselrincon.allysafe.databinding.ActivityRegisterBinding
import com.ieselrincon.allysafe.model.Usuario
import com.ieselrincon.allysafe.network.RetrofitClient
import com.ieselrincon.allysafe.util.SessionManager
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager(this)

        binding.btnRegistrar.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val correo = binding.etCorreo.text.toString().trim()
            val contrasena = binding.etContrasena.text.toString().trim()

            if (nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registrarUsuario(nombre, correo, contrasena)
        }

        binding.tvVolverLogin.setOnClickListener { finish() }
    }

    private fun registrarUsuario(nombre: String, correo: String, contrasena: String) {
        binding.btnRegistrar.isEnabled = false
        lifecycleScope.launch {
            try {
                val nuevoUsuario = Usuario(nombre = nombre, correo = correo, contrasena = contrasena)
                val response = RetrofitClient.api.registro(nuevoUsuario)
                if (response.isSuccessful && response.body() != null) {
                    sessionManager.guardarUsuario(response.body()!!)
                    val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    Toast.makeText(this@RegisterActivity, "No se pudo registrar (correo ya en uso)", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Error de conexión con el servidor", Toast.LENGTH_LONG).show()
            } finally {
                binding.btnRegistrar.isEnabled = true
            }
        }
    }
}
package com.ieselrincon.allysafe

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ieselrincon.allysafe.databinding.ActivityMainBinding
import com.ieselrincon.allysafe.model.LoginRequest
import com.ieselrincon.allysafe.network.RetrofitClient
import com.ieselrincon.allysafe.ui.HomeActivity
import com.ieselrincon.allysafe.ui.RegisterActivity
import com.ieselrincon.allysafe.util.SessionManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)

        if (sessionManager.haySesionActiva()) {
            irAHome()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val correo = binding.etCorreo.text.toString().trim()
            val contrasena = binding.etContrasena.text.toString().trim()

            if (correo.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Rellena correo y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            iniciarSesion(correo, contrasena)
        }

        binding.tvIrRegistro.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun iniciarSesion(correo: String, contrasena: String) {
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Cargando..."

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.login(LoginRequest(correo, contrasena))
                if (response.isSuccessful && response.body() != null) {
                    sessionManager.guardarUsuario(response.body()!!)
                    irAHome()
                } else {
                    Toast.makeText(this@MainActivity, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "Iniciar sesión"
            }
        }
    }

    private fun irAHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
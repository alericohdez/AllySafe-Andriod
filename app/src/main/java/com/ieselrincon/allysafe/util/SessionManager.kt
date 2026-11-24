package com.ieselrincon.allysafe.util

import android.content.Context
import com.ieselrincon.allysafe.model.Usuario

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("usuarioAllySafe", Context.MODE_PRIVATE)

    fun guardarUsuario(usuario: Usuario) {
        prefs.edit()
            .putInt("idUsuario", usuario.idUsuario ?: -1)
            .putString("nombre", usuario.nombre ?: "")
            .putString("correo", usuario.correo ?: "")
            .putString("token", usuario.token ?: "")
            .apply()
    }

    fun obtenerIdUsuario(): Int = prefs.getInt("idUsuario", -1)
    fun obtenerNombre(): String = prefs.getString("nombre", "") ?: ""
    fun obtenerToken(): String = prefs.getString("token", "") ?: ""
    fun haySesionActiva(): Boolean = obtenerIdUsuario() != -1
    fun cerrarSesion() = prefs.edit().clear().apply()
}
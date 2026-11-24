package com.ieselrincon.allysafe.model

data class LoginRequest(
    val correo: String,
    val contrasena: String
)

data class Usuario(
    val idUsuario: Int? = null,
    val nombre: String? = null,
    val correo: String? = null,
    val contrasena: String? = null,
    val token: String? = null,
    val alergenos: List<Alergeno>? = null
)

data class Categoria(
    val idCategoria: Int? = null,
    val nombre: String? = null,
    val iconoUrl: String? = null,
    val descripcion: String? = null
) {
    override fun toString(): String = nombre ?: "Sin categoría"
}

data class Alergeno(
    val idAlergeno: Int? = null,
    val nombre: String? = null
) {
    override fun toString(): String = nombre ?: ""
}

data class Alimento(
    val idAlimento: Int? = null,
    val nombre: String? = null,
    val marca: String? = null,
    val calorias: Int? = null,
    val codigoBarras: String? = null,
    val ingredientes: String? = null,
    val usuario: Usuario? = null,
    val categoria: Categoria? = null
)
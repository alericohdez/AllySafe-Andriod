package com.ieselrincon.allysafe.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ieselrincon.allysafe.model.Alimento
import com.ieselrincon.allysafe.model.Categoria
import com.ieselrincon.allysafe.model.Usuario
import com.ieselrincon.allysafe.util.SessionManager
import com.ieselrincon.allysafe.viewmodel.AlimentoViewModel

class AlimentoFormActivity : ComponentActivity() {

    private val viewModel: AlimentoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val idAlimento = intent.getIntExtra("idAlimento", -1)
        val nombreInicial = intent.getStringExtra("nombre") ?: ""
        val marcaInicial = intent.getStringExtra("marca") ?: ""
        val caloriasInicial = intent.getIntExtra("calorias", 0).let { if (it > 0) it.toString() else "" }
        val codigoInicial = intent.getStringExtra("codigoBarras") ?: ""
        val ingredientesInicial = intent.getStringExtra("ingredientes") ?: ""
        val idCategoriaInicial = intent.getIntExtra("idCategoria", -1)

        val sessionManager = SessionManager(this)
        val idUsuario = sessionManager.obtenerIdUsuario()

        viewModel.cargarAlimentosYCategorias()

        viewModel.mensaje.observe(this) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                viewModel.limpiarMensaje()
            }
        }

        viewModel.operacionExitosa.observe(this) { ok ->
            if (ok) finish()
        }

        setContent {
            MaterialTheme {
                AlimentoFormScreen(
                    esEdicion = idAlimento != -1,
                    nombreInicial = nombreInicial,
                    marcaInicial = marcaInicial,
                    caloriasInicial = caloriasInicial,
                    codigoInicial = codigoInicial,
                    ingredientesInicial = ingredientesInicial,
                    idCategoriaInicial = idCategoriaInicial,
                    viewModel = viewModel,
                    onVolver = { finish() },
                    onGuardar = { nombre, marca, calorias, codigo, ingredientes, categoria ->
                        val alimento = Alimento(
                            idAlimento = if (idAlimento != -1) idAlimento else null,
                            nombre = nombre,
                            marca = marca.ifBlank { null },
                            calorias = calorias.toIntOrNull(),
                            codigoBarras = codigo.ifBlank { null },
                            ingredientes = ingredientes.ifBlank { null },
                            usuario = Usuario(idUsuario = idUsuario),
                            categoria = Categoria(idCategoria = categoria.idCategoria)
                        )
                        if (idAlimento != -1) {
                            viewModel.actualizarAlimento(idAlimento, alimento)
                        } else {
                            viewModel.guardarAlimento(alimento)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlimentoFormScreen(
    esEdicion: Boolean,
    nombreInicial: String,
    marcaInicial: String,
    caloriasInicial: String,
    codigoInicial: String,
    ingredientesInicial: String,
    idCategoriaInicial: Int,
    viewModel: AlimentoViewModel,
    onVolver: () -> Unit,
    onGuardar: (String, String, String, String, String, Categoria) -> Unit
) {
    val context = LocalContext.current
    val allyPrimary = Color(0xFF1D4ED8)
    val allyBg = Color(0xFFF3F7FC)

    var nombre by remember { mutableStateOf(nombreInicial) }
    var marca by remember { mutableStateOf(marcaInicial) }
    var calorias by remember { mutableStateOf(caloriasInicial) }
    var codigoBarras by remember { mutableStateOf(codigoInicial) }
    var ingredientes by remember { mutableStateOf(ingredientesInicial) }

    var categorias by remember { mutableStateOf<List<Categoria>>(emptyList()) }
    var categoriaSeleccionada by remember { mutableStateOf<Categoria?>(null) }
    var menuExpandido by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }

    DisposableEffect(viewModel) {
        val obsCat = androidx.lifecycle.Observer<List<Categoria>> { lista ->
            categorias = lista
            if (categoriaSeleccionada == null && lista.isNotEmpty()) {
                categoriaSeleccionada = lista.find { it.idCategoria == idCategoriaInicial } ?: lista.first()
            }
        }
        val obsLoad = androidx.lifecycle.Observer<Boolean> { cargando = it }

        viewModel.categorias.observeForever(obsCat)
        viewModel.cargando.observeForever(obsLoad)
        onDispose {
            viewModel.categorias.removeObserver(obsCat)
            viewModel.cargando.removeObserver(obsLoad)
        }
    }

    Scaffold(
        containerColor = allyBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (esEdicion) "Editar alimento" else "Añadir alimento",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text("← Volver", color = allyPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = allyBg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre del alimento *") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = marca,
                        onValueChange = { marca = it },
                        label = { Text("Marca") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    ExposedDropdownMenuBox(
                        expanded = menuExpandido,
                        onExpandedChange = { menuExpandido = !menuExpandido }
                    ) {
                        OutlinedTextField(
                            value = categoriaSeleccionada?.nombre ?: "Selecciona una categoría",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoría *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpandido) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = menuExpandido,
                            onDismissRequest = { menuExpandido = false }
                        ) {
                            categorias.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.nombre ?: "") },
                                    onClick = {
                                        categoriaSeleccionada = cat
                                        menuExpandido = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = calorias,
                        onValueChange = { calorias = it },
                        label = { Text("Calorías (kcal)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = codigoBarras,
                        onValueChange = { codigoBarras = it },
                        label = { Text("Código de barras") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = ingredientes,
                        onValueChange = { ingredientes = it },
                        label = { Text("Ingredientes y alérgenos") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        maxLines = 4
                    )

                    Button(
                        onClick = {
                            val cat = categoriaSeleccionada
                            if (nombre.isBlank() || cat == null) {
                                Toast.makeText(
                                    context,
                                    "Introduce el nombre y selecciona una categoría",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                onGuardar(
                                    nombre.trim(),
                                    marca.trim(),
                                    calorias.trim(),
                                    codigoBarras.trim(),
                                    ingredientes.trim(),
                                    cat
                                )
                            }
                        },
                        enabled = !cargando,
                        colors = ButtonDefaults.buttonColors(containerColor = allyPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = if (cargando) "Guardando..." else if (esEdicion) "Guardar cambios" else "Guardar alimento",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
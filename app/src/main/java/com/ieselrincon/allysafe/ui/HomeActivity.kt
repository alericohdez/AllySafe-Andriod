package com.ieselrincon.allysafe.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.ieselrincon.allysafe.MainActivity
import com.ieselrincon.allysafe.databinding.ActivityHomeBinding
import com.ieselrincon.allysafe.model.Alergeno
import com.ieselrincon.allysafe.model.Alimento
import com.ieselrincon.allysafe.model.Categoria
import com.ieselrincon.allysafe.util.SessionManager
import com.ieselrincon.allysafe.viewmodel.AlimentoViewModel

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: AlimentoAdapter
    private val viewModel: AlimentoViewModel by viewModels()

    private var listaCompleta: List<Alimento> = emptyList()
    private var categoriasDisponibles: List<Categoria> = emptyList()
    private var alergenosDisponibles: List<Alergeno> = emptyList()

    private var categoriaSeleccionadaId: Int? = null
    private var alergenoSeleccionadoNombre: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        val nombreUsuario = sessionManager.obtenerNombre().ifBlank { "Usuario" }
        binding.cbSoloMios.text = "Solo mis alimentos ($nombreUsuario)"

        configurarRecyclerView()
        configurarFiltrosYBotones()
        observarViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.cargarAlimentosYCategorias()
    }

    private fun configurarRecyclerView() {
        adapter = AlimentoAdapter(
            lista = emptyList(),
            onEditar = { alimento ->
                val intent = Intent(this, AlimentoFormActivity::class.java).apply {
                    putExtra("idAlimento", alimento.idAlimento ?: -1)
                    putExtra("nombre", alimento.nombre ?: "")
                    putExtra("marca", alimento.marca ?: "")
                    putExtra("calorias", alimento.calorias ?: 0)
                    putExtra("codigoBarras", alimento.codigoBarras ?: "")
                    putExtra("ingredientes", alimento.ingredientes ?: "")
                    putExtra("idCategoria", alimento.categoria?.idCategoria ?: -1)
                }
                startActivity(intent)
            },
            onEliminar = { alimento ->
                val id = alimento.idAlimento ?: return@AlimentoAdapter
                val titulo = alimento.nombre ?: alimento.marca ?: "este producto"
                AlertDialog.Builder(this)
                    .setTitle("Eliminar alimento")
                    .setMessage("¿Seguro que quieres eliminar '$titulo'?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        viewModel.eliminarAlimento(id)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )
        binding.rvAlimentos.layoutManager = LinearLayoutManager(this)
        binding.rvAlimentos.adapter = adapter
    }

    private fun configurarFiltrosYBotones() {
        binding.etBuscar.addTextChangedListener { aplicarFiltros() }
        binding.etMaxKcal.addTextChangedListener { aplicarFiltros() }
        binding.cbSoloMios.setOnCheckedChangeListener { _, _ -> aplicarFiltros() }

        binding.spinnerAlergenos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                alergenoSeleccionadoNombre = if (position == 0) {
                    null
                } else {
                    alergenosDisponibles.getOrNull(position - 1)?.nombre
                }
                aplicarFiltros()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerCategorias.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                categoriaSeleccionadaId = if (position == 0) {
                    null
                } else {
                    categoriasDisponibles.getOrNull(position - 1)?.idCategoria
                }
                aplicarFiltros()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.btnLimpiar.setOnClickListener {
            binding.etBuscar.setText("")
            binding.etMaxKcal.setText("")
            binding.spinnerAlergenos.setSelection(0)
            binding.spinnerCategorias.setSelection(0)
            binding.cbSoloMios.isChecked = false
            aplicarFiltros()
        }

        binding.btnFiltrar.setOnClickListener {
            aplicarFiltros()
            Toast.makeText(this, "Filtros aplicados", Toast.LENGTH_SHORT).show()
        }

        binding.btnNuevoAlimento.setOnClickListener {
            startActivity(Intent(this, AlimentoFormActivity::class.java))
        }

        binding.btnAyuda.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Acerca de AllySafe")
                .setMessage("Consulta alimentos, filtra por alérgenos, calorías o categoría y gestiona tu despensa de forma segura.\n\nComprueba siempre el etiquetado físico del envase.")
                .setPositiveButton("Entendido", null)
                .show()
        }

        binding.btnMenuUsuario.setOnClickListener {
            val nombre = sessionManager.obtenerNombre()
            AlertDialog.Builder(this)
                .setTitle("Sesión de $nombre")
                .setMessage("¿Deseas cerrar tu sesión actual?")
                .setPositiveButton("Cerrar sesión") { _, _ ->
                    sessionManager.cerrarSesion()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun observarViewModel() {
        viewModel.alimentos.observe(this) { lista ->
            listaCompleta = lista
            aplicarFiltros()
        }

        viewModel.alergenos.observe(this) { lista ->
            alergenosDisponibles = lista
            val opciones = listOf("Sin alérgeno...") + lista.map { "Sin ${it.nombre}" }
            val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerAlergenos.adapter = spinnerAdapter
        }

        viewModel.categorias.observe(this) { cats ->
            categoriasDisponibles = cats
            val nombres = listOf("Categoría") + cats.map { it.nombre ?: "" }
            val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombres)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategorias.adapter = spinnerAdapter
        }

        viewModel.cargando.observe(this) { cargando ->
            binding.progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
        }

        viewModel.mensaje.observe(this) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                viewModel.limpiarMensaje()
            }
        }
    }

    private fun aplicarFiltros() {
        val texto = binding.etBuscar.text.toString().trim().lowercase()
        val maxKcal = binding.etMaxKcal.text.toString().trim().toIntOrNull()
        val soloMios = binding.cbSoloMios.isChecked
        val idUsuarioActual = sessionManager.obtenerIdUsuario()

        val filtrada = listaCompleta.filter { item ->
            val coincideTexto = texto.isEmpty() ||
                    (item.nombre?.lowercase()?.contains(texto) == true) ||
                    (item.marca?.lowercase()?.contains(texto) == true) ||
                    (item.ingredientes?.lowercase()?.contains(texto) == true) ||
                    (item.codigoBarras?.lowercase()?.contains(texto) == true)

            val sinAlergeno = alergenoSeleccionadoNombre.isNullOrBlank() ||
                    (item.ingredientes?.lowercase()?.contains(alergenoSeleccionadoNombre!!.lowercase()) != true)

            val coincideCat = categoriaSeleccionadaId == null ||
                    item.categoria?.idCategoria == categoriaSeleccionadaId

            val coincideKcal = maxKcal == null ||
                    (item.calorias != null && item.calorias <= maxKcal)

            val coincideUsuario = !soloMios ||
                    (item.usuario?.idUsuario == idUsuarioActual)

            coincideTexto && sinAlergeno && coincideCat && coincideKcal && coincideUsuario
        }

        adapter.actualizarLista(filtrada)
        binding.tvVacio.visibility = if (filtrada.isEmpty()) View.VISIBLE else View.GONE
        binding.tvContadorResultados.text = if (filtrada.size == 1) {
            "1 alimento"
        } else {
            "${filtrada.size} alimentos"
        }
    }
}
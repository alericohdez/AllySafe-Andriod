package com.ieselrincon.allysafe.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ieselrincon.allysafe.databinding.ItemAlimentoBinding
import com.ieselrincon.allysafe.model.Alimento

class AlimentoAdapter(
    private var lista: List<Alimento>,
    private val onEditar: (Alimento) -> Unit,
    private val onEliminar: (Alimento) -> Unit
) : RecyclerView.Adapter<AlimentoAdapter.AlimentoViewHolder>() {

    private var idExpandido: Int? = null

    inner class AlimentoViewHolder(val binding: ItemAlimentoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlimentoViewHolder {
        val binding = ItemAlimentoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AlimentoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlimentoViewHolder, position: Int) {
        val item = lista[position]
        val estaExpandido = item.idAlimento != null && item.idAlimento == idExpandido

        with(holder.binding) {
            val titulo = item.nombre?.takeIf { it.isNotBlank() }
                ?: item.marca?.takeIf { it.isNotBlank() }
                ?: "Alimento sin nombre"
            tvNombreAlimento.text = titulo

            val partesSubtitulo = mutableListOf<String>()
            item.marca?.takeIf { it.isNotBlank() }?.let { partesSubtitulo.add(it) }
            item.categoria?.nombre?.takeIf { it.isNotBlank() }?.let { partesSubtitulo.add(it) }
            item.calorias?.let { partesSubtitulo.add("$it kcal") }
            tvSubtituloAlimento.text = if (partesSubtitulo.isNotEmpty()) {
                partesSubtitulo.joinToString(" · ")
            } else {
                "Sin detalles adicionales"
            }

            tvFlecha.text = if (estaExpandido) "▲" else "▼"
            layoutDetalle.visibility = if (estaExpandido) View.VISIBLE else View.GONE

            val ing = item.ingredientes?.takeIf { it.isNotBlank() } ?: "No especificados"
            tvIngredientes.text = "Ingredientes: $ing"

            val cod = item.codigoBarras?.takeIf { it.isNotBlank() } ?: "No disponible"
            tvCodigoBarras.text = "Código de barras: $cod"

            layoutCabeceraTarjeta.setOnClickListener {
                idExpandido = if (estaExpandido) null else item.idAlimento
                notifyDataSetChanged()
            }

            btnEditar.setOnClickListener { onEditar(item) }
            btnEliminar.setOnClickListener { onEliminar(item) }
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<Alimento>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}
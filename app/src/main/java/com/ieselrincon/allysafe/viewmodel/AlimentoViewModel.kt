package com.ieselrincon.allysafe.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ieselrincon.allysafe.model.Alergeno
import com.ieselrincon.allysafe.model.Alimento
import com.ieselrincon.allysafe.model.Categoria
import com.ieselrincon.allysafe.network.RetrofitClient
import kotlinx.coroutines.launch

class AlimentoViewModel(application: Application) : AndroidViewModel(application) {

    private val api = RetrofitClient.getApi(application)

    private val _alimentos = MutableLiveData<List<Alimento>>(emptyList())
    val alimentos: LiveData<List<Alimento>> = _alimentos

    private val _categorias = MutableLiveData<List<Categoria>>(emptyList())
    val categorias: LiveData<List<Categoria>> = _categorias

    private val _alergenos = MutableLiveData<List<Alergeno>>(emptyList())
    val alergenos: LiveData<List<Alergeno>> = _alergenos

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _mensaje = MutableLiveData<String?>()
    val mensaje: LiveData<String?> = _mensaje

    private val _operacionExitosa = MutableLiveData(false)
    val operacionExitosa: LiveData<Boolean> = _operacionExitosa

    fun cargarAlimentosYCategorias() {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val resCat = api.listarCategorias()
                if (resCat.isSuccessful) {
                    _categorias.value = resCat.body() ?: emptyList()
                }

                try {
                    val resAler = api.listarAlergenos()
                    if (resAler.isSuccessful && !resAler.body().isNullOrEmpty()) {
                        _alergenos.value = resAler.body()
                    } else {
                        cargarAlergenosPorDefecto()
                    }
                } catch (_: Exception) {
                    cargarAlergenosPorDefecto()
                }

                val resAli = api.listarAlimentos()
                if (resAli.isSuccessful) {
                    _alimentos.value = resAli.body() ?: emptyList()
                } else {
                    _mensaje.value = "Error al cargar alimentos (${resAli.code()})"
                }
            } catch (e: Exception) {
                _mensaje.value = "Error de red: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    private fun cargarAlergenosPorDefecto() {
        _alergenos.value = listOf(
            Alergeno(1, "Gluten"),
            Alergeno(2, "Leche"),
            Alergeno(3, "Frutos de cáscara"),
            Alergeno(4, "Soja"),
            Alergeno(5, "Huevos"),
            Alergeno(6, "Pescado")
        )
    }

    fun guardarAlimento(alimento: Alimento) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val response = api.guardarAlimento(alimento)
                if (response.isSuccessful) {
                    _mensaje.value = "Alimento guardado correctamente"
                    _operacionExitosa.value = true
                } else {
                    _mensaje.value = "No se pudo guardar (${response.code()})"
                }
            } catch (e: Exception) {
                _mensaje.value = "Error al guardar: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun actualizarAlimento(id: Int, alimento: Alimento) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val response = api.actualizarAlimento(id, alimento)
                if (response.isSuccessful) {
                    _mensaje.value = "Alimento actualizado"
                    _operacionExitosa.value = true
                } else {
                    _mensaje.value = "No se pudo actualizar (${response.code()})"
                }
            } catch (e: Exception) {
                _mensaje.value = "Error al actualizar: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun eliminarAlimento(id: Int) {
        viewModelScope.launch {
            try {
                val response = api.eliminarAlimento(id)
                if (response.isSuccessful) {
                    _mensaje.value = "Alimento eliminado"
                    cargarAlimentosYCategorias()
                } else {
                    _mensaje.value = "No se pudo eliminar (${response.code()})"
                }
            } catch (e: Exception) {
                _mensaje.value = "Error al eliminar: ${e.message}"
            }
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}
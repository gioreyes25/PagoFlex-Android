package cl.duocuc.miregistro.viewmodel
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.miregistro.data.remote.PagoFlexRepository
import cl.duocuc.miregistro.model.Compromiso
import cl.duocuc.miregistro.model.ValidadorCompromiso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
class PagoFlexViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PagoFlexRepository()
    private val mutex = Mutex()
    private val _compromisos = MutableStateFlow<List<Compromiso>>(emptyList())
    val compromisos: StateFlow<List<Compromiso>> = _compromisos
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje
    init { cargar() }
    private fun ejecutar(accion: suspend (Long) -> Unit) {
        viewModelScope.launch {
            mutex.withLock {
                try { accion(requireNotNull(repository.usuario().id)) }
                catch (e: CancellationException) { throw e }
                catch (e: Exception) { _mensaje.value = "No se pudo completar la operación. Revisa el Gateway y los servicios." }
            }
        }
    }
    fun cargar() = ejecutar { _compromisos.value = repository.listar(it) }
    fun agregar(nombre: String, empresa: String, monto: String, fecha: String): Boolean {
        val error = ValidadorCompromiso.validar(nombre, empresa, monto, fecha)
        if (error != null) { _mensaje.value = error; return false }
        ejecutar { id -> repository.agregar(id, nombre.trim(), empresa.trim(), monto.toInt(), fecha); _compromisos.value = repository.listar(id); _mensaje.value = "Compromiso agregado correctamente." }
        return true
    }
    fun pagar(id: Long) = ejecutar { usuario ->
        val c = _compromisos.value.firstOrNull { it.id == id } ?: return@ejecutar
        if (c.estado == "Pagado") return@ejecutar
        repository.pagar(usuario, c); _compromisos.value = repository.listar(usuario); _mensaje.value = "Pago simulado registrado."
    }
    fun limpiarMensaje() { _mensaje.value = null }
}

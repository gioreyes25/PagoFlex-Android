package cl.duocuc.miregistro.model
import org.junit.Test
import org.junit.Assert.*
import cl.duocuc.miregistro.data.remote.Fechas
class ValidadorCompromisoTest {
 private fun v(n:String,e:String,m:String,f:String)=ValidadorCompromiso.validar(n,e,m,f)
 @Test fun nombreVacio() { assertNotNull(v("", "Empresa", "100", "01/12/2026")) }
 @Test fun empresaVacia() { assertNotNull(v("Cuota", "", "100", "01/12/2026")) }
 @Test fun valido() { assertNull(v("Cuota", "Empresa", "100", "01/12/2026")) }
 @Test fun fechaImposible() { assertNotNull(v("Cuota", "Empresa", "100", "31/02/2026")) }
}

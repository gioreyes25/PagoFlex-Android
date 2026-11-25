package cl.duocuc.miregistro.model
import org.junit.Test
import org.junit.Assert.*
import cl.duocuc.miregistro.data.remote.Fechas
class MontoTest {
 private fun v(n:String,e:String,m:String,f:String)=ValidadorCompromiso.validar(n,e,m,f)
 @Test fun negativo() { assertNotNull(v("Cuota", "Empresa", "-1", "01/12/2026")) }
 @Test fun cero() { assertNotNull(v("Cuota", "Empresa", "0", "01/12/2026")) }
 @Test fun texto() { assertNotNull(v("Cuota", "Empresa", "abc", "01/12/2026")) }
}

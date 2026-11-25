package cl.duocuc.miregistro.model
import org.junit.Test
import org.junit.Assert.*
import cl.duocuc.miregistro.data.remote.Fechas
class FechasTest {
 private fun v(n:String,e:String,m:String,f:String)=ValidadorCompromiso.validar(n,e,m,f)
 @Test fun iso() { assertEquals("2026-12-01", Fechas.convertir("01/12/2026", "dd/MM/yyyy", "yyyy-MM-dd")) }
 @Test fun local() { assertEquals("01/12/2026", Fechas.convertir("2026-12-01", "yyyy-MM-dd", "dd/MM/yyyy")) }
 @Test fun formatoInvalido() { assertTrue(runCatching { Fechas.convertir("1/12/2026", "dd/MM/yyyy", "yyyy-MM-dd") }.isFailure) }
}

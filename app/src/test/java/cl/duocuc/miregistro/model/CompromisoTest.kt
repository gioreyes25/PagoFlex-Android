package cl.duocuc.miregistro.model
import org.junit.Test
import org.junit.Assert.*
import cl.duocuc.miregistro.data.remote.Fechas
class CompromisoTest {
 private fun v(n:String,e:String,m:String,f:String)=ValidadorCompromiso.validar(n,e,m,f)
 @Test fun pendiente() { assertEquals("Pendiente", Compromiso(nombre="Cuota", empresa="Empresa", monto=100, vencimiento="01/12/2026").estado) }
 @Test fun pagado() { assertEquals("Pagado", Compromiso(nombre="Cuota", empresa="Empresa", monto=100, vencimiento="01/12/2026").copy(estado="Pagado").estado) }
}

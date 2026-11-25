package cl.duocuc.miregistro.data.remote
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import retrofit2.HttpException
import cl.duocuc.miregistro.model.Compromiso
import java.text.SimpleDateFormat
import java.util.Locale

data class UsuarioDto(val id: Long? = null, val nombre: String, val email: String, val activo: Boolean = true)
data class CompromisoDto(val id: Long? = null, val usuarioId: Long, val descripcion: String, val empresa: String, val monto: Int, val fechaVencimiento: String, val estado: String = "PENDIENTE")
data class PagoDto(val usuarioId: Long, val compromisoId: Long, val monto: Int, val medioPago: String = "SIMULADO")
interface PagoFlexApi {
    @GET("api/usuarios/email") suspend fun usuario(@Query("email") email: String): UsuarioDto
    @POST("api/usuarios") suspend fun crearUsuario(@Body usuario: UsuarioDto): UsuarioDto
    @GET("api/compromisos/usuario/{id}") suspend fun compromisos(@Path("id") id: Long): List<CompromisoDto>
    @POST("api/compromisos") suspend fun crear(@Body compromiso: CompromisoDto): CompromisoDto
    @POST("api/pagos") suspend fun pagar(@Body pago: PagoDto): PagoDto
}
object Fechas {
    fun convertir(valor: String, entrada: String, salida: String): String {
        val parser = SimpleDateFormat(entrada, Locale.ROOT).apply { isLenient = false }
        val fecha = requireNotNull(parser.parse(valor))
        require(parser.format(fecha) == valor)
        return SimpleDateFormat(salida, Locale.ROOT).format(fecha)
    }
}
class PagoFlexRepository(private val api: PagoFlexApi = Retrofit.Builder().baseUrl("http://10.0.2.2:8080/").addConverterFactory(GsonConverterFactory.create()).build().create(PagoFlexApi::class.java)) {
    suspend fun usuario(): UsuarioDto {
        val email = "demo@pagoflex.example"
        return try { api.usuario(email) } catch (e: HttpException) {
            if (e.code() != 404) throw e
            try { api.crearUsuario(UsuarioDto(nombre = "Usuario demo", email = email)) } catch (conflict: HttpException) {
                if (conflict.code() != 409) throw conflict
                api.usuario(email)
            }
        }
    }
    suspend fun listar(id: Long) = api.compromisos(id).map { Compromiso(requireNotNull(it.id), it.descripcion, it.empresa, it.monto, Fechas.convertir(it.fechaVencimiento, "yyyy-MM-dd", "dd/MM/yyyy"), if (it.estado == "PAGADO") "Pagado" else "Pendiente") }
    suspend fun agregar(id: Long, nombre: String, empresa: String, monto: Int, fecha: String) = api.crear(CompromisoDto(usuarioId = id, descripcion = nombre, empresa = empresa, monto = monto, fechaVencimiento = Fechas.convertir(fecha, "dd/MM/yyyy", "yyyy-MM-dd")))
    suspend fun pagar(id: Long, c: Compromiso) = api.pagar(PagoDto(id, c.id, c.monto))
}

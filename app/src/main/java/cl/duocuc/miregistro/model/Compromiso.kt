package cl.duocuc.miregistro.model

data class Compromiso(
    val id: Long = 0,
    val nombre: String,
    val empresa: String,
    val monto: Int,
    val vencimiento: String,
    val estado: String = "Pendiente"
)

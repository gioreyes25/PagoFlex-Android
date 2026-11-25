package cl.duocuc.miregistro.model


enum class NivelExperiencia(val etiqueta: String) {
    PRINCIPIANTE("Principiante"),
    INTERMEDIO("Intermedio"),
    AVANZADO("Avanzado")
}

data class FormularioUiState(
    val nombre: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val nivel: NivelExperiencia = NivelExperiencia.PRINCIPIANTE,
    val recibirNoticias: Boolean = false,
    val aceptaTerminos: Boolean = false,
    val contrasenaVisible: Boolean = false,
    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorContrasena: String? = null,
    val errorTerminos: String? = null,
    val registroExitoso: Boolean = false
)

package cl.duocuc.miregistro.model
object ValidadorCompromiso {
    fun validar(nombre: String, empresa: String, monto: String, fecha: String): String? {
        if (nombre.isBlank()) return "Ingresa el nombre del compromiso."
        if (empresa.isBlank()) return "Ingresa la organización asociada."
        val valor = monto.toIntOrNull()
        if (valor == null || valor <= 0) return "El monto debe ser un número mayor que cero."
        if (!fecha.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) return "Usa el formato DD/MM/AAAA."
        if (runCatching { cl.duocuc.miregistro.data.remote.Fechas.convertir(fecha, "dd/MM/yyyy", "yyyy-MM-dd") }.isFailure) return "Fecha inválida."
        return null
    }
}

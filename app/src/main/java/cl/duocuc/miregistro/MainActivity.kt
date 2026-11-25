package cl.duocuc.miregistro

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.miregistro.model.Compromiso
import cl.duocuc.miregistro.model.ValidadorCompromiso
import cl.duocuc.miregistro.ui.theme.PagoFlexTheme
import cl.duocuc.miregistro.viewmodel.PagoFlexViewModel
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent { PagoFlexTheme { PagoFlexApp() } }
    }
}

private val azul = androidx.compose.ui.graphics.Color(0xFF1455A3)
private val azulOscuro = androidx.compose.ui.graphics.Color(0xFF0B2D52)
private val celeste = androidx.compose.ui.graphics.Color(0xFFEAF4FF)
private val gris = androidx.compose.ui.graphics.Color(0xFF64748B)

@Composable
fun PagoFlexApp(vm: PagoFlexViewModel = viewModel()) {
    val context = LocalContext.current
    val compromisos by vm.compromisos.collectAsState()
    val mensaje by vm.mensaje.collectAsState()
    var seccion by remember { mutableIntStateOf(0) }
    var mostrarFormulario by remember { mutableStateOf(false) }
    var seleccionado by remember { mutableStateOf<Compromiso?>(null) }
    var foto by remember { mutableStateOf<Bitmap?>(null) }

    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { foto = it }
    val permisoNotificacion = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) enviarNotificacion(context) else Unit
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color(0xFFF5F8FC),
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(azulOscuro).padding(horizontal = 20.dp, vertical = 17.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("PagoFlex", color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Tus pagos, más simples", color = androidx.compose.ui.graphics.Color(0xFFD5E8FF), style = MaterialTheme.typography.labelMedium)
                }
                Text("PF", color = azulOscuro, modifier = Modifier.background(androidx.compose.ui.graphics.Color.White, CircleShape).padding(11.dp), fontWeight = FontWeight.Bold)
            }
        },
        bottomBar = {
            NavigationBar(containerColor = androidx.compose.ui.graphics.Color.White) {
                listOf("Inicio" to "⌂", "Compromisos" to "▤", "Historial" to "◷", "Perfil" to "☺").forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = seccion == index,
                        onClick = { seccion = index },
                        icon = { Text(item.second, style = MaterialTheme.typography.titleLarge) },
                        label = { Text(item.first) }
                    )
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            AnimatedVisibility(visible = mensaje != null, enter = fadeIn() + slideInVertically(), exit = fadeOut()) {
                mensaje?.let {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).background(celeste, RoundedCornerShape(12.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(it, color = azulOscuro, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { vm.limpiarMensaje(); vm.cargar() }) { Text("OK") }
                    }
                }
            }
            when (seccion) {
                0 -> InicioScreen(compromisos, onVer = { seccion = 1 }, onAgregar = { mostrarFormulario = true })
                1 -> CompromisosScreen(compromisos, onAgregar = { mostrarFormulario = true }, onSeleccionar = { seleccionado = it })
                2 -> HistorialScreen(compromisos.filter { it.estado == "Pagado" })
                else -> PerfilScreen(foto, onCamara = { camara.launch(null) }, onAviso = {
                    if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        permisoNotificacion.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else enviarNotificacion(context)
                })
            }
        }
    }

    if (mostrarFormulario) {
        FormularioCompromisoDialog(
            onCerrar = { mostrarFormulario = false },
            onGuardar = { nombre, empresa, monto, fecha ->
                val guardado = vm.agregar(nombre, empresa, monto, fecha)
                if (guardado) mostrarFormulario = false
            }
        )
    }
    seleccionado?.let { item ->
        AlertDialog(
            onDismissRequest = { seleccionado = null },
            title = { Text(item.nombre, color = azulOscuro, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(item.empresa)
                    Text("Monto: ${pesos(item.monto)}", fontWeight = FontWeight.SemiBold)
                    Text("Vencimiento: ${item.vencimiento}")
                    Text("Estado: ${item.estado}")
                }
            },
            confirmButton = {
                if (item.estado != "Pagado") Button(onClick = { vm.pagar(item.id); seleccionado = null }) { Text("Simular pago") }
            },
            dismissButton = { TextButton(onClick = { seleccionado = null }) { Text("Cerrar") } }
        )
    }
}

@Composable
private fun InicioScreen(compromisos: List<Compromiso>, onVer: () -> Unit, onAgregar: () -> Unit) {
    val pendientes = compromisos.filter { it.estado == "Pendiente" }
    val total = pendientes.sumOf { it.monto }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        item {
            Text("Hola, Giovanny 👋", style = MaterialTheme.typography.headlineSmall, color = azulOscuro, fontWeight = FontWeight.Bold)
            Text("Este es el resumen de tus compromisos.", color = gris)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = azul), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(22.dp)) {
                    Text("Total pendiente", color = androidx.compose.ui.graphics.Color.White.copy(alpha = .85f))
                    Text(pesos(total), color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text("${pendientes.size} compromisos por gestionar", color = androidx.compose.ui.graphics.Color.White)
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ResumenCard("Pendientes", pendientes.size.toString(), Modifier.weight(1f))
                ResumenCard("Pagados", compromisos.count { it.estado == "Pagado" }.toString(), Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Próximos compromisos", style = MaterialTheme.typography.titleMedium, color = azulOscuro, fontWeight = FontWeight.Bold)
                TextButton(onClick = onVer) { Text("Ver todos") }
            }
        }
        items(pendientes.take(2)) { item -> CompromisoCard(item, onClick = onVer) }
        item { Button(onClick = onAgregar, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("＋ Agregar compromiso") } }
        item { Text("Los datos de esta demostración son ficticios.", color = gris, style = MaterialTheme.typography.labelSmall) }
    }
}

@Composable
private fun ResumenCard(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) { Text(valor, color = azul, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Text(titulo, color = gris) }
    }
}

@Composable
private fun CompromisosScreen(compromisos: List<Compromiso>, onAgregar: () -> Unit, onSeleccionar: (Compromiso) -> Unit) {
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column { Text("Mis compromisos", style = MaterialTheme.typography.headlineSmall, color = azulOscuro, fontWeight = FontWeight.Bold); Text("Consulta y gestiona tus pagos", color = gris) }
            Button(onClick = onAgregar, contentPadding = PaddingValues(horizontal = 13.dp, vertical = 9.dp)) { Text("＋ Nuevo") }
        }
        Spacer(Modifier.height(15.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(compromisos) { item -> CompromisoCard(item) { onSeleccionar(item) } }
        }
    }
}

@Composable
private fun CompromisoCard(item: Compromiso, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).background(celeste, CircleShape), contentAlignment = Alignment.Center) { Text("$", color = azul, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.nombre, color = azulOscuro, fontWeight = FontWeight.SemiBold)
                Text(item.empresa, color = gris, style = MaterialTheme.typography.bodySmall)
                Text("Vence: ${item.vencimiento}", color = gris, style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(pesos(item.monto), color = azulOscuro, fontWeight = FontWeight.Bold)
                Text(item.estado, color = if (item.estado == "Pagado") androidx.compose.ui.graphics.Color(0xFF16804A) else azul, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun HistorialScreen(compromisos: List<Compromiso>) {
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        Text("Historial de pagos", style = MaterialTheme.typography.headlineSmall, color = azulOscuro, fontWeight = FontWeight.Bold)
        Text("Tus compromisos registrados como pagados.", color = gris)
        Spacer(Modifier.height(16.dp))
        if (compromisos.isEmpty()) Text("Todavía no tienes pagos registrados.", color = gris)
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) { items(compromisos) { CompromisoCard(it) {} } }
    }
}

@Composable
private fun PerfilScreen(foto: Bitmap?, onCamara: () -> Unit, onAviso: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Mi perfil", style = MaterialTheme.typography.headlineSmall, color = azulOscuro, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(22.dp))
        if (foto != null) Image(foto.asImageBitmap(), contentDescription = "Foto de perfil", modifier = Modifier.size(112.dp).background(celeste, CircleShape))
        else Box(Modifier.size(112.dp).background(celeste, CircleShape), contentAlignment = Alignment.Center) { Text("GR", color = azul, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(12.dp))
        Text("Giovanny Reyes", style = MaterialTheme.typography.titleLarge, color = azulOscuro, fontWeight = FontWeight.Bold)
        Text("Usuario de demostración", color = gris)
        Spacer(Modifier.height(28.dp))
        OutlinedButton(onClick = onCamara, modifier = Modifier.fillMaxWidth()) { Text("▣  Tomar foto de perfil") }
        Button(onClick = onAviso, modifier = Modifier.fillMaxWidth()) { Text("♧  Probar recordatorio") }
        Spacer(Modifier.height(18.dp))
        Text("PagoFlex · Prototipo académico", color = gris, style = MaterialTheme.typography.labelSmall)
        Text("No se procesan pagos reales ni se almacenan datos bancarios.", color = gris, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun FormularioCompromisoDialog(onCerrar: () -> Unit, onGuardar: (String, String, String, String) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var empresa by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Nuevo compromiso", color = azulOscuro, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlinedTextField(nombre, { nombre = it; error = null }, label = { Text("Nombre del compromiso") }, leadingIcon = { Text("✎") }, singleLine = true, modifier = Modifier.fillMaxWidth(), isError = error?.contains("nombre", true) == true)
                OutlinedTextField(empresa, { empresa = it; error = null }, label = { Text("Organización") }, leadingIcon = { Text("⌂") }, singleLine = true, modifier = Modifier.fillMaxWidth(), isError = error?.contains("organización", true) == true)
                OutlinedTextField(monto, { monto = it.filter(Char::isDigit); error = null }, label = { Text("Monto en pesos") }, leadingIcon = { Text("$") }, singleLine = true, modifier = Modifier.fillMaxWidth(), isError = error?.contains("monto", true) == true)
                OutlinedTextField(fecha, { fecha = it; error = null }, label = { Text("Vencimiento (DD/MM/AAAA)") }, leadingIcon = { Text("◷") }, singleLine = true, modifier = Modifier.fillMaxWidth(), isError = error?.contains("formato", true) == true)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = { Button(onClick = { error = ValidadorCompromiso.validar(nombre, empresa, monto, fecha); if (error == null) onGuardar(nombre, empresa, monto, fecha) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } }
    )
}

private fun pesos(valor: Int): String = NumberFormat.getCurrencyInstance(Locale("es", "CL")).format(valor.toLong()).replace(",", "")

private fun enviarNotificacion(context: Context) {
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "pagoflex_recordatorios"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        manager.createNotificationChannel(NotificationChannel(channelId, "Recordatorios PagoFlex", NotificationManager.IMPORTANCE_DEFAULT))
    }
    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("PagoFlex: recordatorio")
        .setContentText("Revisa tus próximos compromisos de pago.")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()
    if (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
        manager.notify(1001, notification)
    }
}

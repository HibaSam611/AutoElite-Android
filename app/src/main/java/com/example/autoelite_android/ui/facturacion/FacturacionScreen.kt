package com.example.autoelite_android.ui.facturacion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.autoelite_android.navigation.AutoEliteBottomBar

data class FacturaUi(
    val id: Int,
    val numero: String,
    val fecha: String,
    val total: String,
    val pagada: Boolean
)

//datos para ver como queda la cosa:
private val facturasEjemplo = listOf(
    FacturaUi(1, "FAC-2026-001", "10 Mar 2026", "245,00 €", true),
    FacturaUi(2, "FAC-2026-002", "12 Abr 2026", "130,50 €", false),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacturacionScreen(navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Mis facturas") }) },
        bottomBar = { AutoEliteBottomBar(navController) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(facturasEjemplo) { factura ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Receipt, contentDescription = null,
                            tint = if (factura.pagada) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(factura.numero, fontWeight = FontWeight.Bold)
                            Text(factura.fecha, fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(factura.total, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            Text(
                                if (factura.pagada) "✓ Pagada" else "Pendiente",
                                fontSize = 11.sp,
                                color = if (factura.pagada) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    // Botón de pago si está pendiente
                    if (!factura.pagada) {
                        Button(
                            onClick = { /* TODO: Stripe */ },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.CreditCard, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Pagar online")
                        }
                    }
                }
            }
        }
    }
}

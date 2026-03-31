package com.seizurewatch.prototype.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seizurewatch.prototype.data.EventLog
import com.seizurewatch.prototype.data.RiskLevel
import com.seizurewatch.prototype.data.label
import com.seizurewatch.prototype.monitoring.MonitoringUiState
import com.seizurewatch.prototype.monitoring.MonitoringViewModel

@Composable
fun SeizureWatchApp(viewModel: MonitoringViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.pendingAlertMessage) {
        state.pendingAlertMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissMessage()
        }
    }

    MonitoringDashboard(
        state = state,
        onAcknowledge = viewModel::onAcknowledgeAlert,
        onEmergency = viewModel::onTriggerEmergency,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonitoringDashboard(
    state: MonitoringUiState,
    onAcknowledge: () -> Unit,
    onEmergency: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Seizure Watch Prototype")
                        Text(
                            text = "Android • monitorización adjunta",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { PatientHeader(state = state) }
            item { StatusSummary(state = state) }
            item { ActionPanel(onAcknowledge = onAcknowledge, onEmergency = onEmergency) }
            item { ReasoningCard(state = state) }
            item { EventFeed(events = state.events) }
            item { PrototypeDisclaimer() }
        }
    }
}

@Composable
private fun PatientHeader(state: MonitoringUiState) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(state.patient.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("${state.patient.ageYears} años • ${state.patient.epilepsyType}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text("Basal FC ${state.patient.baselineHeartRate} bpm") })
                AssistChip(onClick = {}, label = { Text("Permisos: ${if (state.sensorPermissionGranted) "OK" else "Pendiente"}") })
            }
            AssistChip(onClick = {}, label = { Text("Contacto: ${state.patient.emergencyContactName}") })
        }
    }
}

@Composable
private fun StatusSummary(state: MonitoringUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Frecuencia cardíaca",
                value = "${state.liveVitals.heartRateBpm} bpm",
                subtitle = "Fuente: ${if (state.liveVitals.source.name == "SIMULATED") "simulada" else "sensor"}",
                icon = { Icon(Icons.Default.Favorite, contentDescription = null) }
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Movimiento",
                value = String.format("%.2f g", state.liveVitals.motionG),
                subtitle = "Calidad ${state.liveVitals.signalQuality}%",
                icon = { Icon(Icons.Default.NotificationsActive, contentDescription = null) }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RiskCard(
                modifier = Modifier.weight(1f),
                title = "Riesgo convulsivo",
                level = state.assessment.seizureRisk
            )
            RiskCard(
                modifier = Modifier.weight(1f),
                title = "Riesgo cardiaco",
                level = state.assessment.cardiacRisk
            )
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: @Composable () -> Unit
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.height(24.dp), contentAlignment = Alignment.CenterStart) { icon() }
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RiskCard(modifier: Modifier = Modifier, title: String, level: RiskLevel) {
    val colors = when (level) {
        RiskLevel.NORMAL -> CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        RiskLevel.ELEVATED -> CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        RiskLevel.HIGH -> CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        RiskLevel.CRITICAL -> CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    }
    Card(modifier = modifier, colors = colors) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.Security, contentDescription = null)
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(level.label(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ActionPanel(onAcknowledge: () -> Unit, onEmergency: () -> Unit) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Acciones clínicas rápidas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Este prototipo deja lista la UX para confirmar síntomas, registrar episodios y escalar a emergencia.")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(onClick = onAcknowledge, modifier = Modifier.weight(1f)) {
                    Text("Reconocer alerta")
                }
                Button(onClick = onEmergency, modifier = Modifier.weight(1f)) {
                    Text("Protocolo rojo")
                }
            }
        }
    }
}

@Composable
private fun ReasoningCard(state: MonitoringUiState) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Motor de decisión", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            state.assessment.reasons.forEach {
                Text("• $it")
            }
            Text(
                text = state.assessment.recommendedAction,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EventFeed(events: List<EventLog>) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Registro de eventos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            events.forEach { event ->
                EventRow(event)
            }
        }
    }
}

@Composable
private fun EventRow(event: EventLog) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Default.Warning, contentDescription = null)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("${event.timestampLabel} • ${event.title}", fontWeight = FontWeight.Bold)
            Text(event.description)
            Text(event.severity.label(), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PrototypeDisclaimer() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Aviso importante", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Este APK/proyecto es un prototipo de investigación. No diagnostica convulsiones ni infartos y no reemplaza atención médica ni servicios de emergencia."
            )
        }
    }
}

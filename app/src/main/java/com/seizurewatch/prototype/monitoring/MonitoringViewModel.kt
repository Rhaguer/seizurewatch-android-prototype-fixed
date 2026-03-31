package com.seizurewatch.prototype.monitoring

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seizurewatch.prototype.data.EventLog
import com.seizurewatch.prototype.data.LiveVitals
import com.seizurewatch.prototype.data.PatientProfile
import com.seizurewatch.prototype.data.RiskAssessment
import com.seizurewatch.prototype.data.RiskLevel
import com.seizurewatch.prototype.data.SensorSource
import com.seizurewatch.prototype.domain.RiskEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.math.sqrt
import kotlin.random.Random

class MonitoringViewModel : ViewModel(), SensorEventListener {

    private val profile = PatientProfile(
        id = "pac-001",
        name = "Paciente Demo",
        ageYears = 13,
        epilepsyType = "Epilepsia mioclónica juvenil",
        baselineHeartRate = 78,
        emergencyContactName = "Padre",
        emergencyPhone = "+56 9 1234 5678"
    )

    private val _uiState = MutableStateFlow(
        MonitoringUiState(
            patient = profile,
            liveVitals = LiveVitals(
                heartRateBpm = 79,
                motionG = 0.18,
                variabilityScore = 82,
                signalQuality = 90,
                source = SensorSource.SIMULATED
            ),
            assessment = RiskEngine.assess(
                profile,
                LiveVitals(79, 0.18, 82, 90, SensorSource.SIMULATED)
            ),
            events = defaultEvents()
        )
    )
    val uiState: StateFlow<MonitoringUiState> = _uiState.asStateFlow()

    private var simulationStarted = false

    init {
        startSimulationIfNeeded()
    }

    fun refreshPermissionState(context: Context) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.BODY_SENSORS
        ) == PackageManager.PERMISSION_GRANTED
        _uiState.value = _uiState.value.copy(sensorPermissionGranted = granted)
    }

    fun onAcknowledgeAlert() {
        _uiState.value = _uiState.value.copy(
            pendingAlertMessage = "Alerta reconocida por el usuario."
        )
    }

    fun onTriggerEmergency() {
        appendEvent(
            title = "Escalamiento manual",
            description = "El usuario activó el protocolo de emergencia.",
            severity = RiskLevel.CRITICAL
        )
        _uiState.value = _uiState.value.copy(
            pendingAlertMessage = "Protocolo rojo listo: llamar al 131 y avisar a ${profile.emergencyContactName}."
        )
    }

    fun dismissMessage() {
        _uiState.value = _uiState.value.copy(pendingAlertMessage = null)
    }

    private fun startSimulationIfNeeded() {
        if (simulationStarted) return
        simulationStarted = true

        viewModelScope.launch {
            while (true) {
                delay(2_000)
                val baseline = profile.baselineHeartRate
                val simulated = LiveVitals(
                    heartRateBpm = (baseline + Random.nextInt(-8, 45)).coerceIn(42, 165),
                    motionG = Random.nextDouble(0.05, 2.6),
                    variabilityScore = Random.nextInt(18, 96),
                    signalQuality = Random.nextInt(72, 99),
                    source = SensorSource.SIMULATED
                )
                publishVitals(simulated)
            }
        }
    }

    private fun publishVitals(vitals: LiveVitals) {
        val assessment = RiskEngine.assess(profile, vitals)
        _uiState.value = _uiState.value.copy(
            liveVitals = vitals,
            assessment = assessment,
            monitoringActive = true
        )

        if (assessment.seizureRisk >= RiskLevel.HIGH || assessment.cardiacRisk >= RiskLevel.HIGH) {
            appendEvent(
                title = "Evento detectado",
                description = assessment.reasons.joinToString(" "),
                severity = maxOf(assessment.seizureRisk, assessment.cardiacRisk)
            )
            _uiState.value = _uiState.value.copy(
                pendingAlertMessage = assessment.recommendedAction
            )
        }
    }

    private fun appendEvent(title: String, description: String, severity: RiskLevel) {
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val item = EventLog(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            timestampLabel = LocalTime.now().format(formatter),
            severity = severity
        )
        _uiState.value = _uiState.value.copy(events = listOf(item) + _uiState.value.events.take(19))
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        val current = _uiState.value.liveVitals
        when (event.sensor.type) {
            Sensor.TYPE_HEART_RATE -> {
                val bpm = event.values.firstOrNull()?.toInt() ?: return
                publishVitals(current.copy(heartRateBpm = bpm, source = SensorSource.DEVICE_SENSORS))
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values.getOrNull(0) ?: return
                val y = event.values.getOrNull(1) ?: return
                val z = event.values.getOrNull(2) ?: return
                val magnitude = sqrt((x * x + y * y + z * z).toDouble()) / SensorManager.GRAVITY_EARTH
                publishVitals(current.copy(motionG = magnitude, source = SensorSource.DEVICE_SENSORS))
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun defaultEvents(): List<EventLog> = listOf(
        EventLog(
            id = "seed-1",
            title = "Inicio del sistema",
            description = "El motor de monitorización fue inicializado en modo prototipo.",
            timestampLabel = "09:00:00",
            severity = RiskLevel.NORMAL
        )
    )
}

data class MonitoringUiState(
    val patient: PatientProfile,
    val liveVitals: LiveVitals,
    val assessment: RiskAssessment,
    val events: List<EventLog>,
    val monitoringActive: Boolean = false,
    val sensorPermissionGranted: Boolean = false,
    val pendingAlertMessage: String? = null
)

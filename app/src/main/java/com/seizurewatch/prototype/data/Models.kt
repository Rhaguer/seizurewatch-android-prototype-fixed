package com.seizurewatch.prototype.data

import androidx.compose.runtime.Immutable
import kotlin.math.roundToInt

@Immutable
data class PatientProfile(
    val id: String,
    val name: String,
    val ageYears: Int,
    val epilepsyType: String,
    val baselineHeartRate: Int,
    val emergencyContactName: String,
    val emergencyPhone: String
)

@Immutable
data class LiveVitals(
    val heartRateBpm: Int,
    val motionG: Double,
    val variabilityScore: Int,
    val signalQuality: Int,
    val source: SensorSource
)

@Immutable
data class RiskAssessment(
    val seizureRisk: RiskLevel,
    val cardiacRisk: RiskLevel,
    val reasons: List<String>,
    val recommendedAction: String
)

@Immutable
data class EventLog(
    val id: String,
    val title: String,
    val description: String,
    val timestampLabel: String,
    val severity: RiskLevel
)

enum class RiskLevel { NORMAL, ELEVATED, HIGH, CRITICAL }

enum class SensorSource { DEVICE_SENSORS, SIMULATED }

fun RiskLevel.label(): String = when (this) {
    RiskLevel.NORMAL -> "Normal"
    RiskLevel.ELEVATED -> "Elevado"
    RiskLevel.HIGH -> "Alto"
    RiskLevel.CRITICAL -> "Crítico"
}

fun Double.asPercent(): String = "${(this * 100).roundToInt()}%"

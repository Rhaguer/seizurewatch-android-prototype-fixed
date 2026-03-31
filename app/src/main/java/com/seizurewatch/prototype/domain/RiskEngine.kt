package com.seizurewatch.prototype.domain

import com.seizurewatch.prototype.data.LiveVitals
import com.seizurewatch.prototype.data.PatientProfile
import com.seizurewatch.prototype.data.RiskAssessment
import com.seizurewatch.prototype.data.RiskLevel
import kotlin.math.abs

object RiskEngine {

    fun assess(profile: PatientProfile, vitals: LiveVitals): RiskAssessment {
        val reasons = mutableListOf<String>()

        val heartRateDelta = vitals.heartRateBpm - profile.baselineHeartRate
        val seizureRisk = when {
            vitals.motionG >= 2.2 && heartRateDelta >= 35 -> {
                reasons += "Movimiento muy brusco combinado con taquicardia marcada."
                RiskLevel.CRITICAL
            }
            vitals.motionG >= 1.7 && heartRateDelta >= 20 -> {
                reasons += "Patrón compatible con evento motor a confirmar."
                RiskLevel.HIGH
            }
            vitals.motionG >= 1.2 || heartRateDelta >= 15 -> {
                reasons += "Actividad motora o autonómica fuera de línea basal."
                RiskLevel.ELEVATED
            }
            else -> RiskLevel.NORMAL
        }

        val cardiacRisk = when {
            vitals.heartRateBpm >= 155 -> {
                reasons += "Frecuencia cardíaca extremadamente alta."
                RiskLevel.CRITICAL
            }
            vitals.heartRateBpm <= 42 -> {
                reasons += "Bradicardia marcada."
                RiskLevel.HIGH
            }
            abs(heartRateDelta) >= 30 && vitals.variabilityScore <= 25 -> {
                reasons += "Cambio brusco de FC con variabilidad reducida."
                RiskLevel.HIGH
            }
            abs(heartRateDelta) >= 18 -> {
                reasons += "Desviación sostenida respecto del basal."
                RiskLevel.ELEVATED
            }
            else -> RiskLevel.NORMAL
        }

        val recommendedAction = when {
            seizureRisk == RiskLevel.CRITICAL || cardiacRisk == RiskLevel.CRITICAL -> {
                "Activar protocolo rojo: confirmar paciente, llamar a emergencia y avisar cuidador."
            }
            seizureRisk == RiskLevel.HIGH || cardiacRisk == RiskLevel.HIGH -> {
                "Confirmar síntomas en menos de 15 segundos y preparar escalamiento."
            }
            seizureRisk == RiskLevel.ELEVATED || cardiacRisk == RiskLevel.ELEVATED -> {
                "Continuar observación, registrar episodio y reevaluar en 30 segundos."
            }
            else -> "Continuar monitorización pasiva."
        }

        return RiskAssessment(
            seizureRisk = seizureRisk,
            cardiacRisk = cardiacRisk,
            reasons = if (reasons.isEmpty()) listOf("Sin hallazgos relevantes en este ciclo.") else reasons,
            recommendedAction = recommendedAction
        )
    }
}

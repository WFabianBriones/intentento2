package com.example.ergonomic.model

data class AnalysisResult(
    val riskLevel: RiskLevel,
    val riskScore: Int,
    val confidence: Float,
    val criticalAreas: List<CriticalArea>,
    val likelySymptoms: List<Symptom>,
    val recommendations: List<Recommendation>
)

data class CriticalArea(
    val name: String,
    val severity: Float,
    val description: String
)

data class Symptom(
    val name: String,
    val probability: Float,
    val description: String
)

data class Recommendation(
    val priority: String, // "URGENTE", "ALTA", "MEDIA", "BAJA"
    val category: String,
    val title: String,
    val description: String,
    val actions: List<String>
)
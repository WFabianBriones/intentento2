package com.example.ergonomic.model

data class KnowledgeBase(
    val version: String,
    val last_updated: String,
    val risk_descriptions: Map<String, RiskDescription>,
    val recommendations: Map<String, DetailedRecommendation>,
    val exercises: Map<String, List<Exercise>>,
    val product_recommendations: ProductRecommendations,
    val educational_content: EducationalContent
)

data class RiskDescription(
    val title: String,
    val description: String,
    val color: String,
    val emoji: String
)

data class DetailedRecommendation(
    val priority: String,
    val category: String,
    val title: String,
    val description: String,
    val actions: List<String>,
    val budget_options: Map<String, String>? = null,
    val health_impact: String? = null,
    val roi: String? = null,
    val quick_fix: String? = null,
    val diy_solution: String? = null,
    val total_investment: String? = null,
    val apps_recommended: List<String>? = null,
    val exercise_routine: List<String>? = null,
    val micro_breaks: String? = null,
    val ideal_setup: String? = null,
    val laptop_solution: String? = null,
    val prevention: String? = null,
    val standing_desk: String? = null
)

data class Exercise(
    val name: String,
    val description: String,
    val repetitions: String? = null,
    val duration: String? = null,
    val frequency: String,
    val benefit: String? = null,
    val image: String? = null
)

data class ProductRecommendations(
    val budget_friendly: List<Product>,
    val medium_investment: List<Product>,
    val premium: List<Product>
)

data class Product(
    val item: String,
    val price_range: String,
    val priority: String,
    val impact: String,
    val recommended_brands: List<String>? = null,
    val specs: String? = null,
    val recommended: List<String>? = null,
    val benefit: String? = null
)

data class EducationalContent(
    val proper_posture: ProperPosture,
    val warning_signs: WarningSigns
)

data class ProperPosture(
    val sitting: List<String>,
    val monitor_position: List<String>,
    val keyboard_mouse: List<String>
)

data class WarningSigns(
    val seek_medical_attention: List<String>,
    val early_warnings: List<String>
)
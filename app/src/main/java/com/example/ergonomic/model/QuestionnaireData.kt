package com.example.ergonomic.model

data class QuestionnaireData(
    // Sección: Mobiliario
    var chairType: ChairType? = null,
    var lumbarSupport: LumbarSupport? = null,
    var deskHeight: String? = null,
    var deskSpace: String? = null,

    // Sección: Monitor
    var monitorType: String? = null,
    var monitorHeight: MonitorHeight? = null,
    var monitorDistance: String? = null,
    var multipleMonitors: String? = null,

    // Sección: Teclado y Mouse
    var keyboardPosition: String? = null,
    var mouseType: String? = null,
    var wristPad: String? = null,

    // Sección: Iluminación
    var lighting: String? = null,
    var screenGlare: String? = null,
    var deskLamp: String? = null,
    var temperature: String? = null,
    var noiseLevel: String? = null,
    var ventilation: String? = null,

    // Sección: Pausas
    var pauseFrequency: PauseFrequency? = null,
    var pauseDuration: String? = null,
    var doesStretches: String? = null,
    var continuousSittingTime: String? = null,

    // Datos adicionales
    var workHours: Int = 8,
    var age: Int = 30,
    var hasNeckPain: Boolean = false,
    var hasBackPain: Boolean = false,
    var hasWristPain: Boolean = false
)